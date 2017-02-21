//
//  ReactViewController.swift
//  NativeNavigation
//
//  Created by Spike Brehm on 5/2/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import React
import UIKit

// MARK: Public

@objc public enum ReactFlowResultCode: Int {
  // These must match the values in React Native and Android codebases.
  case cancelled = 0
  case ok = -1

  public static func isOk(_ value: Int?) -> Bool {
    if let value = value {
      return ReactFlowResultCode(rawValue: value) == .ok
    } else {
      return false
    }
  }
}

@objc public protocol ReactViewControllerDelegate: class {
  func didDismiss(_ viewController: ReactViewController, withPayload payload: [String: AnyObject])
}


// MARK: Private

private let kNativeNavigationInstanceId = "nativeNavigationInstanceId"
private let kNativeNavigationBarHeight = "nativeNavigationInitialBarHeight"
private let kViewControllerId = "viewControllerId"
private var index = 0
private let EMPTY_MAP = [String: AnyObject]()

private func generateId(_ moduleName: String) -> String {
  index += 1
  return "\(moduleName)_\(index)"
}

private func propsWithMetadata(
  _ props: [String: AnyObject],
  _ nativeNavigationInstanceId: String,
  _ barHeight: CGFloat
) -> [String: AnyObject] {
  // TODO(lmr): make this non mutative?
  var newProps = props
  newProps[kNativeNavigationInstanceId] = nativeNavigationInstanceId as AnyObject?
  newProps[kNativeNavigationBarHeight] = barHeight as AnyObject?
  return newProps
}

struct WeakViewHolder {
  weak var view: UIView?
}

// MARK: Class Definition

open class ReactViewController: UIViewController {

  // MARK: Lifecycle

  public init(moduleName: String, props: [String: AnyObject] = [:]) {
    self.nativeNavigationInstanceId = generateId(moduleName)
    self.moduleName = moduleName
    self.coordinator = ReactNavigationCoordinator.sharedInstance

    self.barHeight = -1;
    self.props = EMPTY_MAP

    self.initialConfig = EMPTY_MAP
    self.renderedConfig = EMPTY_MAP

    super.init(nibName: nil, bundle: nil)

    if let initialConfig = coordinator.getScreenProperties(moduleName) {
      self.initialConfig = initialConfig
      self.renderedConfig = initialConfig
    }

    self.barHeight = coordinator.navigation.getBarHeight(
      viewController: self,
      navigationController: navigationController,
      config: renderedConfig
    )
    self.props = propsWithMetadata(props, nativeNavigationInstanceId, barHeight)

    // I'm all ears for a better way to solve this, but just setting this to false seems to be the best way to attain
    // consistency across screens and platforms. If we don't do this, then scrollview insets will be set differently
    // after popping to a screen. Also, android and ios logic will have to be different. Just opting out of the
    // behavior seems like a better approach at the moment.
    self.automaticallyAdjustsScrollViewInsets = false
  }

  // MARK: Public

  open var reactFlowId: String?

  // TODO(lmr): showTabBar?
  open var showTabBar: Bool = false
  open weak var delegate: ReactViewControllerDelegate?

  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  deinit {
    coordinator.unregisterViewController(nativeNavigationInstanceId)
  }

  override open var preferredStatusBarUpdateAnimation : UIStatusBarAnimation {
    return statusBarAnimation
  }

  override open var prefersStatusBarHidden : Bool {
    return statusBarHidden
  }

  override open var preferredStatusBarStyle : UIStatusBarStyle {
    return statusBarStyle
  }

  override open func loadView() {
    coordinator.registerViewController(self)

    self.view = RCTRootView(
      bridge: coordinator.bridge,
      moduleName: moduleName,
      initialProperties: props)

    if let screenColor = colorForKey("screenColor", initialConfig) {
      self.view.backgroundColor = screenColor
    }
  }

  override open func viewDidLoad() {
    super.viewDidLoad()
  }

  override open func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)

    if (!self.isMovingToParentViewController) {
      reconcileScreenConfig()
    }

    handleLeadingButtonVisibleChange()
  }

  override open func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    updateBarHeightIfNeeded()
    emitEvent("onAppear", body: nil)
  }

  override open func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    emitEvent("onDisappear", body: nil)
  }

  override open func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    if (self.isMovingFromParentViewController) {
      // viewController is being popped
      finish(.ok, payload: dismissPayload)
      transition = nil
    }

    if (self.isBeingDismissed) {
      // viewController is being dismissed
      finish(.ok, payload: dismissPayload)
      transition = nil
    }

    if let navController = self.navigationController {
      if (navController.isBeingDismissed) {
        // viewController is being dismissed
        finish(.ok, payload: dismissPayload)
        transition = nil
      }
    }
  }

  open func setStatusBarStyle(_ style: UIStatusBarStyle) {
    if (statusBarStyle != style) {
      statusBarStyle = style
      statusBarIsDirty = true
    }
  }

  open func setStatusBarHidden(_ hidden: Bool) {
    if (statusBarHidden != hidden) {
      statusBarHidden = hidden
      statusBarIsDirty = true
    }
  }

  open func setStatusBarAnimation(_ animation: UIStatusBarAnimation) {
    statusBarAnimation = animation
  }

  // MARK: Internal

  /**
   * This is meant to be called by the ReactNavigationCoordinator. The intention is to
   * tell the coordinator that presented us to dismiss us.
   */
  func dismiss(_ payload: [String: AnyObject]) {
    delegate?.didDismiss(self, withPayload: payload)
  }

  func wrapInNavigationController() -> UINavigationController? {
    return coordinator.navigation.makeNavigationController(rootViewController: self)
  }

  func setLeadingButtonVisible(_ leadingButtonVisible: Bool) {
    // TODO(lmr): does this belong in navigation implementation
    self.leadingButtonVisible = leadingButtonVisible
    handleLeadingButtonVisibleChange()
  }

  func setCloseBehavior(_ closeBehavior: String) {
    // TODO(lmr): does this belong in navigation implementation?
    // TODO(spike)
  }

  let nativeNavigationInstanceId: String
  var sharedElementsById: [String: WeakViewHolder] = [:]
  var sharedElementGroupsById: [String: WeakViewHolder] = [:]
  var dismissResultCode: ReactFlowResultCode?
  var dismissPayload: [String: AnyObject]?

  // MARK: Private

  fileprivate func handleLeadingButtonVisibleChange() {
    // TODO(lmr): does this belong in navigation implementation?
    navigationItem.setHidesBackButton(!leadingButtonVisible, animated: false)
    navigationController?.interactivePopGestureRecognizer!.isEnabled = leadingButtonVisible
  }

  public func emitEvent(_ eventName: String, body: AnyObject?) {
    let name = String(format: "NativeNavigationScreen.%@.%@", eventName, self.nativeNavigationInstanceId)
    let args: [AnyObject]
    if let payload = body {
      args = [name as AnyObject, payload]
    } else {
      args = [name as AnyObject]
    }
    // TODO(lmr): there's a more appropriate way to do this now???
    coordinator.bridge?.enqueueJSCall("RCTDeviceEventEmitter.emit", args: args)
  }

  // TODO(spike): This method isn't currently used anywhere. Find out where to use it to
  // DRY up the result code code.
  fileprivate func extractResultCode(_ payload: [String: AnyObject]?) -> ReactFlowResultCode {
    if let rawResultCode = dismissPayload?["resultCode"] as? Int {
      return ReactFlowResultCode(rawValue: rawResultCode) ?? .ok
    } else {
      return .ok
    }
  }

  func updateStatusBarIfNeeded() {
    if (statusBarIsDirty) {
      statusBarIsDirty = false
      let duration = statusBarAnimation != .none ? 0.2 : 0
      UIView.animate(withDuration: duration, animations: {
        self.setNeedsStatusBarAppearanceUpdate()
      })
    }
  }

  fileprivate let moduleName: String
  fileprivate var props: [String: AnyObject]
  fileprivate let coordinator: ReactNavigationCoordinator
  fileprivate var initialConfig: [String: AnyObject]
  fileprivate var renderedConfig: [String: AnyObject]
  fileprivate var reactView: UIView!
  fileprivate var statusBarAnimation: UIStatusBarAnimation = .fade
  fileprivate var statusBarHidden: Bool = false
  fileprivate var statusBarStyle: UIStatusBarStyle = UIStatusBarStyle.default
  fileprivate var statusBarIsDirty: Bool = false
  fileprivate var isPendingNavigationTransition: Bool = false
  fileprivate var isCurrentlyTransitioning: Bool = false
  fileprivate var onTransitionCompleted: (() -> Void)?
  fileprivate var onNavigationBarTypeUpdated: (() -> Void)?
  fileprivate var leadingButtonVisible: Bool = true
  fileprivate var transition: ReactSharedElementTransition?
  fileprivate var eagerNavigationController: UINavigationController?

  fileprivate func reconcileScreenConfig() {
    let nav = navigationController ?? eagerNavigationController
    let props = initialConfig.combineWith(values: renderedConfig)
    coordinator.navigation.reconcileScreenConfig(viewController: self, navigationController: nav, props: props)
  }

  private func updateNavigationImpl(props: [String: AnyObject]) {
    renderedConfig = props
    reconcileScreenConfig()
    updateBarHeightIfNeeded()
  }

  func setNavigationBarProperties(props: [String: AnyObject]) {
    if (isCurrentlyTransitioning) {
      onTransitionCompleted = {
        self.updateNavigationImpl(props: props)
        self.onTransitionCompleted = nil // prevent retain cycle
      }
    } else {
      updateNavigationImpl(props: props)
    }
  }

  func signalFirstRenderComplete() {
    if (isPendingNavigationTransition) {
      onNavigationBarTypeUpdated?()
    }
  }

  func back(sender: UIBarButtonItem) {
    print("onBackPress")
    emitEvent("onBackPress", body: nil)
  }

  // this gets fired right after the first real navigation action gets called (push or present)
  fileprivate func realNavigationDidHappen() {

  }

  // this gets fired after things are set up and we are now waiting for the first navigation config from JS
  // to get passed back
  fileprivate func startedWaitingForRealNavigation() {
    reconcileScreenConfig()
    if let waitForRender = boolForKey("waitForRender", initialConfig) {
      if (!waitForRender && isPendingNavigationTransition) {
        onNavigationBarTypeUpdated?()
      }
    }
  }


  private var barHeight: CGFloat

  func updateBarHeightIfNeeded() {

    let newHeight = coordinator.navigation.getBarHeight(
      viewController: self,
      navigationController: navigationController ?? eagerNavigationController,
      config: renderedConfig
    )
    if newHeight != barHeight {
      barHeight = newHeight
      emitEvent("onBarHeightChanged", body: barHeight as AnyObject)
    }
  }

}

// MARK: ReactFlowCoordinator

extension ReactViewController : ReactFlowCoordinator {
  public func start(_ props: [String:AnyObject]?) {
    let nc = ReactNavigationCoordinator.sharedInstance.topNavigationController()
    nc?.pushReactViewController(self, animated: true)
  }
}

// MARK: ReactAnimationFromContentVendor

extension ReactViewController : ReactAnimationFromContentVendor {

  func snapshotForAnimationContainer(_ animationContainer: UIView) -> ReactSharedElementSnapshot {
    var sharedElements = [String:UIViewSnapshot]()
    sharedElementsById.forEach { (id: String, el: WeakViewHolder) in
      guard let view = el.view else { return }
      sharedElements[id] = view.snapshotInContainerView(animationContainer) {
        view.superview?.alpha = 1
      }
      view.superview?.alpha = 0
    }
    let screen = self.view.snapshotInContainerView(animationContainer)
    return ReactSharedElementSnapshot(
      screenWithoutElements: screen,
      sharedElements: sharedElements
    )
  }

  func snapshotForAnimationContainer(_ animationContainer: UIView, transitionGroup: String) -> ReactSharedElementSnapshot {
    guard let group = sharedElementGroupsById[transitionGroup]?.view else {
      return ReactSharedElementSnapshot(
        screenWithoutElements: self.view.snapshotInContainerView(animationContainer),
        sharedElements: [:]
      )
    }
    var sharedElements = [String:UIViewSnapshot]()
    sharedElementsById.forEach { (id: String, el: WeakViewHolder) in
      guard let view = el.view else { return }
      if view.isDescendant(of: group) {
        sharedElements[id] = view.snapshotInContainerView(animationContainer) {
          view.superview?.alpha = 1
        }
        view.superview?.alpha = 0
      }
    }

    let screenWithoutElements = self.view.snapshotInContainerView(animationContainer)

    return ReactSharedElementSnapshot(
      screenWithoutElements: screenWithoutElements,
      sharedElements: sharedElements
    )
  }

  public func reactAnimationFromContent(_ animationContainer: UIView, transitionGroup: String, options: [String: Any]) -> ReactAnimationFromContent {
    let snapshot = self.snapshotForAnimationContainer(animationContainer, transitionGroup: transitionGroup)
    animationContainer.sendSubview(toBack: snapshot.screenWithoutElements.view)
    return ReactAnimationFromContent(
      screenWithoutElements: snapshot.screenWithoutElements.view,
      sharedElements: snapshot.sharedElements.mapValues { $0.view }
    )
  }

  public func containerView() -> UIView {
    return view
  }
}

// MARK: ReactAnimationToContentVendor

extension ReactViewController : ReactAnimationToContentVendor {
  public func reactAnimationToContent(_ animationContainer: UIView) -> ReactAnimationToContent {
    let snapshot = self.snapshotForAnimationContainer(animationContainer)
    animationContainer.sendSubview(toBack: snapshot.screenWithoutElements.view)
    return ReactAnimationToContent(
      screenWithoutElements: snapshot.screenWithoutElements.view,
      sharedElements: snapshot.sharedElements.mapValues { $0.view }
    )
  }
}

// we will wait a maximum of 200ms for the RN view to tell us what the navigation bar should look like.
// should normally happen much quicker than this... This is just to make sure it transitions in a reasonable
// time frame even if the react thread takes an extra long time.
private let DELAY: Int64 = Int64(0.2 * Double(NSEC_PER_SEC))

extension UINavigationController {

  public func pushReactViewController(_ viewController: ReactViewController, animated: Bool) {
    pushReactViewController(viewController, animated: animated, makeTransition: nil)
  }

  public func pushReactViewController(
    _ viewController: ReactViewController,
    animated: Bool,
    delay: Int64 = DELAY,
    makeTransition: (() -> ReactSharedElementTransition)?) {

    // TODO(lmr): we need to debounce this

    viewController.eagerNavigationController = self

    // this should never evaluate true, but is here just to trigger loadView()
    guard (viewController.view != nil) else {
      return
    }

    let realPush: () -> Void = { [weak self] in
      viewController.onNavigationBarTypeUpdated = nil
      viewController.isPendingNavigationTransition = false
      viewController.isCurrentlyTransitioning = true

      if let transition = makeTransition?() {
        viewController.transition = transition
        self?.transitioningDelegate = transition
      }

      self?.pushViewController(viewController, animated: animated)
      viewController.eagerNavigationController = nil
      viewController.realNavigationDidHappen()
      self?.transitionCoordinator?.animate(alongsideTransition: nil, completion: { context in
        viewController.isCurrentlyTransitioning = false
        // The completion handler of the AIRNavigationController will be called
        // synchronously from this context, but AFTER this block is called. To
        // get around this, we call it async.
        DispatchQueue.main.async(execute: {
          viewController.onTransitionCompleted?()
          viewController.emitEvent("onEnterTransitionComplete", body: nil)
        })
      })
    }

    viewController.isPendingNavigationTransition = true
    viewController.onNavigationBarTypeUpdated = realPush
    viewController.startedWaitingForRealNavigation()

    // we delay pushing the view controller just a little bit (50ms) so that the react view can render
    DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + Double(delay) / Double(NSEC_PER_SEC)) {
      if (viewController.isPendingNavigationTransition) {
        print("Push Fallback Timer Called!")
        realPush()
      }
    }
  }
}

extension UIViewController {
  public func presentReactViewController(_ viewControllerToPresent: ReactViewController, animated: Bool, completion: (() -> Void)?) {

    // TODO(lmr): we need to debounce this

    // we wrap the vc in a navigation controller early on so that when reconcileScreenConfig happens, it has a navigation
    // controller
    guard let nav = viewControllerToPresent.wrapInNavigationController() else { return }

    // set this so we know which nav it should operate on before getting presented
    viewControllerToPresent.eagerNavigationController = nav

    // this should never evaluate to true, but is here just to trigger loadView()
    if (viewControllerToPresent.view == nil) {
      return
    }

    let realPresent = { [weak self, weak viewControllerToPresent, weak nav] in
      guard let viewControllerToPresent = viewControllerToPresent else { return }
      guard let nav = nav else { return }

      let identifier = viewControllerToPresent.nativeNavigationInstanceId
      viewControllerToPresent.onNavigationBarTypeUpdated = nil
      viewControllerToPresent.isPendingNavigationTransition = false
      viewControllerToPresent.isCurrentlyTransitioning = true

      self?.present(nav, animated: animated, completion: {
        viewControllerToPresent.isCurrentlyTransitioning = false
        completion?()
        // The completion handler of the AIRNavigationController will be called
        // synchronously from this context, but AFTER this block is called. To
        // get around this, we call it async.
        DispatchQueue.main.async {
          viewControllerToPresent.onTransitionCompleted?()
          viewControllerToPresent.emitEvent("onEnterTransitionComplete", body: nil)
        }
      })
      // viewController should have a navigationController now. nil out to prevent retain cycles
      viewControllerToPresent.eagerNavigationController = nil
      viewControllerToPresent.realNavigationDidHappen()
    }

    viewControllerToPresent.isPendingNavigationTransition = true
    viewControllerToPresent.onNavigationBarTypeUpdated = realPresent
    viewControllerToPresent.startedWaitingForRealNavigation()

    // we delay pushing the view controller just a little bit (50ms) so that the react view can render
    DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + Double(DELAY) / Double(NSEC_PER_SEC)) {
      if (viewControllerToPresent.isPendingNavigationTransition) {
        print("Present Fallback Timer Called!")
        viewControllerToPresent.onNavigationBarTypeUpdated?()
      } else {
        viewControllerToPresent.onNavigationBarTypeUpdated = nil
      }
    }
  }
}
