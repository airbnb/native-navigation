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

let kNativeNavigationInstanceId = "nativeNavigationInstanceId"
let kNativeNavigationBarHeight = "nativeNavigationInitialBarHeight"
let kViewControllerId = "viewControllerId"
private var index = 0
let EMPTY_MAP = [String: AnyObject]()

func generateId(_ moduleName: String) -> String {
  index += 1
  return "\(moduleName)_\(index)"
}

func propsWithMetadata(
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

public struct WeakViewHolder {
  weak var view: UIView?
}

// MARK: Class Definition

open class ReactViewController: UIViewController {

  let nativeNavigationInstanceId: String
  var sharedElementsById: [String: WeakViewHolder] = [:]
  var sharedElementGroupsById: [String: WeakViewHolder] = [:]
  var isPendingNavigationTransition: Bool = false
  var isCurrentlyTransitioning: Bool = false
  var onTransitionCompleted: (() -> Void)?
  var onNavigationBarTypeUpdated: (() -> Void)?
  var reactViewHasBeenRendered: Bool = false
  var transition: ReactSharedElementTransition?
  var eagerNavigationController: UINavigationController?
  open var reactFlowId: String?
  open var showTabBar: Bool = false // TODO(lmr): showTabBar? is this needed?
  open weak var delegate: ReactViewControllerDelegate?
  var dismissResultCode: ReactFlowResultCode?
  var dismissPayload: [String: AnyObject]?
  fileprivate let moduleName: String
  fileprivate var props: [String: AnyObject]
  fileprivate let coordinator: ReactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance
  fileprivate var initialConfig: [String: AnyObject]
  fileprivate var prevConfig: [String: AnyObject]
  fileprivate var renderedConfig: [String: AnyObject]
  fileprivate var reactView: UIView!
  fileprivate var statusBarAnimation: UIStatusBarAnimation = .fade
  fileprivate var statusBarHidden: Bool = false
  fileprivate var statusBarStyle: UIStatusBarStyle = UIStatusBarStyle.default
  fileprivate var statusBarIsDirty: Bool = false
  fileprivate var leadingButtonVisible: Bool = true
  private var barHeight: CGFloat

  // MARK: Lifecycle

  public convenience init(moduleName: String) {
    self.init(moduleName: moduleName, props: [:])
  }

  public init(moduleName: String, props: [String: AnyObject]) {
    self.nativeNavigationInstanceId = generateId(moduleName)
    self.moduleName = moduleName

    self.barHeight = -1;
    self.props = EMPTY_MAP

    self.initialConfig = EMPTY_MAP
    self.prevConfig = EMPTY_MAP
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

  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  deinit {
    coordinator.unregisterViewController(nativeNavigationInstanceId)
  }


  // MARK: UIViewController Overrides

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




  // MARK: Public Setters

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

  func setLeadingButtonVisible(_ leadingButtonVisible: Bool) {
    // TODO(lmr): does this belong in navigation implementation
    self.leadingButtonVisible = leadingButtonVisible
    handleLeadingButtonVisibleChange()
  }

  public func setCloseBehavior(_ closeBehavior: String) {
    // TODO(lmr): does this belong in navigation implementation?
    // TODO(spike)
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


  // MARK: Public Events

  func signalFirstRenderComplete() {
    reactViewHasBeenRendered = true
    if (isPendingNavigationTransition) {
      onNavigationBarTypeUpdated?()
    }
  }

  // this gets fired right after the first real navigation action gets called (push or present)
  func realNavigationDidHappen() {

  }

  // this gets fired after things are set up and we are now waiting for the first navigation config from JS
  // to get passed back
  func startedWaitingForRealNavigation() {
    reconcileScreenConfig()
    // TODO(lmr): this is no longer an option in initialConfig
    if let waitForRender = boolForKey("waitForRender", initialConfig) {
      if (!waitForRender && isPendingNavigationTransition) {
        onNavigationBarTypeUpdated?()
      }
    }
  }

  // MARK: Internal

  /**
   * This is meant to be called by the ReactNavigationCoordinator. The intention is to
   * tell the coordinator that presented us to dismiss us.
   */
  public func dismiss(_ payload: [String: AnyObject]) {
    dismissPayload = payload
    delegate?.didDismiss(self, withPayload: payload)
  }

  func prepareViewControllerForPresenting() -> UIViewController {
    let navigationController = coordinator.navigation.makeNavigationController(rootViewController: self)
    if let screenColor = colorForKey("screenColor", initialConfig) {
      if (screenColor.cgColor.alpha < 1.0) {
        navigationController.modalPresentationStyle = .overCurrentContext
      }
    }
    return navigationController
  }

  fileprivate func reconcileScreenConfig() {
    let nav = navigationController ?? eagerNavigationController
    coordinator.navigation.reconcileScreenConfig(
      viewController: self,
      navigationController: nav,
      prev: prevConfig,
      next: renderedConfig
    )
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



  // MARK: Private

  fileprivate func handleLeadingButtonVisibleChange() {
    // TODO(lmr): does this belong in navigation implementation?
    navigationItem.setHidesBackButton(!leadingButtonVisible, animated: false)
    navigationController?.interactivePopGestureRecognizer!.isEnabled = leadingButtonVisible
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

  private func updateNavigationImpl(props: [String: AnyObject]) {
    prevConfig = renderedConfig
    renderedConfig = initialConfig.combineWith(values: props)
    reconcileScreenConfig()
    updateBarHeightIfNeeded()
  }

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
  @objc public func finish(_ resultCode: ReactFlowResultCode, payload: [String:AnyObject]?) {
    ReactNavigationCoordinator.sharedInstance.onFlowFinish(self, resultCode: resultCode, payload: payload)
  }
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

