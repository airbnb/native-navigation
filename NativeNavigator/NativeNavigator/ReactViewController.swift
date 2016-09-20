//
//  ReactViewController.swift
//  NativeNavigator
//
//  Created by Leland Richardson on 9/17/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//


import Foundation
import React

// MARK: Public

@objc public enum ReactFlowResultCode: Int {
  // These must match the values in React Native and Android codebases.
  case Cancelled = 0
  case Ok = -1

  public static func isOk(value: Int?) -> Bool {
    if let value = value {
      return ReactFlowResultCode(rawValue: value) == .Ok
    } else {
      return false
    }
  }
}

@objc public protocol ReactViewControllerDelegate: class {
  func didDismiss(viewController: ReactViewController, withPayload payload: [String: AnyObject])
}


// MARK: Private

private let kSceneInstanceId = "sceneInstanceId"
private var index = 0

private func generateId(moduleName: String) -> String {
  index += 1
  return "\(moduleName)_\(index)"
}

private func propsWithMetadata(props: [String: AnyObject], sceneInstanceId: String) -> [String: AnyObject] {
  var newProps = props
  newProps[kSceneInstanceId] = sceneInstanceId as AnyObject?
  return newProps
}

struct WeakViewHolder {
  weak var view: UIView?
}

// MARK: Class Definition

public class ReactViewController: UIViewController {

  // MARK: Lifecycle

  public init(moduleName: String, props: [String: AnyObject] = [:]) {
    self.sceneInstanceId = generateId(moduleName: moduleName)
    self.moduleName = moduleName
    self.props = propsWithMetadata(props: props, sceneInstanceId: sceneInstanceId)
    self.coordinator = ReactNavigationCoordinator.sharedInstance

    super.init(nibName: nil, bundle: nil)
  }

  // MARK: Public

  public var reactFlowId: String?
  public var showTabBar: Bool = false
  public weak var delegate: ReactViewControllerDelegate?

  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  deinit {
    coordinator.unregisterViewController(sceneInstanceId: sceneInstanceId)
  }

  override public func preferredStatusBarUpdateAnimation() -> UIStatusBarAnimation {
    return statusBarAnimation
  }

  override public func prefersStatusBarHidden() -> Bool {
    return statusBarHidden
  }

  override public func preferredStatusBarStyle() -> UIStatusBarStyle {
    return statusBarStyle
  }

  override public func loadView() {
    coordinator.registerViewController(viewController: self)

    self.view = RCTRootView(
      bridge: ReactNavigationCoordinator.sharedInstance.bridge,
      moduleName: moduleName,
      initialProperties: props)
  }

  override public func viewDidLoad() {
    super.viewDidLoad()
  }

  override public func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)

    handleLeadingButtonVisibleChange()
  }

  override public func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    emitEvent(eventName: "onAppear", body: nil)
  }

  override public func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    emitEvent(eventName: "onDisappear", body: nil)
  }

  override public func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    if (self.isMovingFromParentViewController) {
      // viewController is being popped
      finish(resultCode: .Ok, payload: dismissPayload)
    }

    if (self.isBeingDismissed) {
      // viewController is being dismissed
      finish(resultCode: .Ok, payload: dismissPayload)
    }

    if let navController = self.navigationController {
      if (navController.isBeingDismissed) {
        // viewController is being dismissed
        finish(resultCode: .Ok, payload: dismissPayload)
      }
      // TODO(lmr): we dont need to do this here, do we?
//      if let parent = (navController.parentViewController as? BBFullSheetModalManager) {
//        if (parent.topViewController == nil) {
//          // viewController from BBFullSheetModalManager is being dismissed
//          finish(resultCode: .Ok, payload: dismissPayload)
//        }
//      }
    }
  }

  /**
   * This will get called by the ReactStatusBarBridge module, so that React Native JS
   * code can toggle the status bar style dynamically
   */
  public func setStatusBarStyle(style: UIStatusBarStyle, animated: Bool) {
    statusBarStyle = style
    updateStatusBar(animate: animated)
  }

  /**
   * This will get called by the ReactStatusBarBridge module, so that React Native JS
   * code can toggle the hidden state of the status bar dynamically
   */
  public func setStatusBarHidden(hidden: Bool, animation: UIStatusBarAnimation) {
    statusBarHidden = hidden
    statusBarAnimation = animation
    updateStatusBar(animate: animation != UIStatusBarAnimation.none)
  }

  public func attachScrollView(scrollView: RCTScrollView) {
    scrollView.addScrollListener(self)
  }

  // MARK: Internal

  /**
   * This is meant to be called by the ReactNavigationCoordinator. The intention is to
   * tell the coordinator that presented us to dismiss us.
   */
  func dismiss(payload: [String: AnyObject]) {
    delegate?.didDismiss(viewController: self, withPayload: payload)
  }

  func wrapInNavigationController() -> UINavigationController? {
    return UINavigationController(rootViewController: self)
  }

  func setScrollFoldOffset(offset: CGFloat) {
    scrollFoldOffset = offset;
  }

  func setLeadingButtonVisible(leadingButtonVisible: Bool) {
    self.leadingButtonVisible = leadingButtonVisible
    handleLeadingButtonVisibleChange()
  }

  func setCloseBehavior(closeBehavior: String) {
    // TODO(spike)
  }

  func updateNavigation() {
    // TODO(lmr):
//    _navigationBarType = currentBarType()
    if (isPendingNavigationTransition) {
      onNavigationBarTypeUpdated?()
    } else if (isCurrentlyTransitioning) {
      onTransitionCompleted = {
        // actually update things?
      }
    } else {
      // actually update things?
    }
  }

  let sceneInstanceId: String
  var sharedElementsById: [String: WeakViewHolder] = [:]
  var sharedElementGroupsById: [String: WeakViewHolder] = [:]
  var dismissResultCode: ReactFlowResultCode?
  var dismissPayload: [String: AnyObject]?
  var barType: String = "overlay"
  var backgroundColor: String?
  var navigationTitle: String?
  var link: String?
  var buttons: [String] = []

  // MARK: Private

  fileprivate func handleLeadingButtonVisibleChange() {
    navigationItem.setHidesBackButton(!leadingButtonVisible, animated: false)
    navigationController?.interactivePopGestureRecognizer!.isEnabled = leadingButtonVisible
  }

  fileprivate func emitEvent(eventName: String, body: AnyObject?) {
    let name = String(format: "NativeNavigatorConfig.%@.%@", eventName, self.sceneInstanceId)
    let args: [AnyObject]
    if let payload = body {
      args = [name as AnyObject, payload]
    } else {
      args = [name as AnyObject]
    }
    ReactNavigationCoordinator.sharedInstance.bridge?.enqueueJSCall("RCTDeviceEventEmitter.emit",
                                                                    args: args)
  }

  // TODO(spike): This method isn't currently used anywhere. Find out where to use it to
  // DRY up the result code code.
  private func extractResultCode(payload: [String: AnyObject]?) -> ReactFlowResultCode {
    if let rawResultCode = dismissPayload?["resultCode"] as? Int {
      return ReactFlowResultCode(rawValue: rawResultCode) ?? .Ok
    } else {
      return .Ok
    }
  }

  fileprivate func updateStatusBar(animate: Bool) {
    let duration = animate ? 0.2 : 0
    UIView.animate(withDuration: duration, animations: {
      self.setNeedsStatusBarAppearanceUpdate()
    })
  }

  fileprivate let moduleName: String
  fileprivate let props: [String: AnyObject]
  fileprivate let coordinator: ReactNavigationCoordinator
  fileprivate var reactView: UIView!
  fileprivate var statusBarAnimation: UIStatusBarAnimation = .fade
  fileprivate var statusBarHidden: Bool = false
  fileprivate var statusBarStyle: UIStatusBarStyle = UIStatusBarStyle.default
  fileprivate var onTransitionCompleted: (() -> Void)?

  // TODO(lmr): Remove
  fileprivate var scrollContentOffset: CGFloat = 0
  fileprivate var scrollFoldOffset: CGFloat? = nil
  fileprivate var isPendingNavigationTransition: Bool = false
  fileprivate var isCurrentlyTransitioning: Bool = false

  fileprivate var onNavigationBarTypeUpdated: (() -> Void)?
  fileprivate var leadingButtonVisible: Bool = true
}


// MARK: UIScrollViewDelegate

extension ReactViewController : UIScrollViewDelegate {
  public func scrollViewDidScroll(_ scrollView: UIScrollView) {
    scrollContentOffset = scrollView.contentOffset.y
  }
}


// MARK: ReactFlowCoordinator

extension ReactViewController : ReactFlowCoordinator {
  public func start(props: [String:AnyObject]?) {
    let nc = ReactNavigationCoordinator.sharedInstance.topNavigationController()
    nc?.pushReactViewController(viewController: self, animated: true)
  }
}

// MARK: ReactAnimationFromContentVendor

extension ReactViewController : ReactAnimationFromContentVendor {

  func snapshotForAnimationContainer(animationContainer: UIView) -> ReactSharedElementSnapshot {
    var sharedElements = [String:UIViewSnapshot]()
    sharedElementsById.forEach { (id: String, el: WeakViewHolder) in
      guard let view = el.view else { return }
      sharedElements[id] = view.snapshotInContainerView(containerView: animationContainer) {
        view.superview?.alpha = 1
      }
      view.superview?.alpha = 0
    }
    let screen = self.view.snapshotInContainerView(containerView: animationContainer)
    return ReactSharedElementSnapshot(
      screenWithoutElements: screen,
      sharedElements: sharedElements
    )
  }

  func snapshotForAnimationContainer(animationContainer: UIView, transitionGroup: String) -> ReactSharedElementSnapshot {
    guard let group = sharedElementGroupsById[transitionGroup]?.view else {
      return ReactSharedElementSnapshot(
        screenWithoutElements: self.view.snapshotInContainerView(containerView: animationContainer),
        sharedElements: [:]
      )
    }
    var sharedElements = [String:UIViewSnapshot]()
    sharedElementsById.forEach { (id: String, el: WeakViewHolder) in
      guard let view = el.view else { return }
      if view.isDescendant(of: group) {
        sharedElements[id] = view.snapshotInContainerView(containerView: animationContainer) {
          view.superview?.alpha = 1
        }
        view.superview?.alpha = 0
      }
    }

    let screenWithoutElements = self.view.snapshotInContainerView(containerView: animationContainer)

    return ReactSharedElementSnapshot(
      screenWithoutElements: screenWithoutElements,
      sharedElements: sharedElements
    )
  }

  func reactAnimationFromContent(animationContainer: UIView, transitionGroup: String) -> ReactAnimationFromContent {
    let snapshot = self.snapshotForAnimationContainer(animationContainer: animationContainer, transitionGroup: transitionGroup)
    animationContainer.sendSubview(toBack: snapshot.screenWithoutElements.view)
    return ReactAnimationFromContent(
      screenWithoutElements: snapshot.screenWithoutElements.view,
      sharedElements: snapshot.sharedElements.mapValues { $0.view }
    )
  }

  func containerView() -> UIView {
    return view
  }
}

// MARK: ReactAnimationToContentVendor

extension ReactViewController : ReactAnimationToContentVendor {
  func reactAnimationToContent(animationContainer: UIView) -> ReactAnimationToContent {
    let snapshot = self.snapshotForAnimationContainer(animationContainer: animationContainer)
    animationContainer.sendSubview(toBack: snapshot.screenWithoutElements.view)
    return ReactAnimationToContent(
      screenWithoutElements: snapshot.screenWithoutElements.view,
      sharedElements: snapshot.sharedElements.mapValues { $0.view }
    )
  }
}

// MARK: AIRNavigationController extension

// we will wait a maximum of 200ms for the RN view to tell us what the navigation bar should look like.
// should normally happen much quicker than this... This is just to make sure it transitions in a reasonable
// time frame even if the react thread takes an extra long time.

extension UINavigationController {

  public func pushReactViewController(viewController: ReactViewController, animated: Bool) {
    pushReactViewController(viewController: viewController, animated: animated, makeTransition: nil)
  }

  public func pushReactViewController(viewController: ReactViewController, animated: Bool, makeTransition: (() ->ReactSharedElementTransition)?) {
    // this should never evaluate true, but is here just to trigger loadView()
    guard (viewController.view != nil) else {
      return
    }

    let realPush: () -> Void = { [weak self] in
      viewController.onNavigationBarTypeUpdated = nil
      viewController.isPendingNavigationTransition = false
      viewController.isCurrentlyTransitioning = true

      if let transition = makeTransition?() {
        // TODO(lmr): this was needed to retain the delegate. come up with cleaner way to do this?
        ReactNavigationCoordinator.sharedInstance.transitionDelegate = transition
        self?.transitioningDelegate = transition
        // TODO(lmr): can we remove?
//        self?.navigationDelegate = transition
      }

      self?.pushViewController(viewController, animated: animated)
      self?.transitionCoordinator?.animate(alongsideTransition: nil, completion: { context in
        viewController.isCurrentlyTransitioning = false
        // The completion handler of the AIRNavigationController will be called
        // synchronously from this context, but AFTER this block is called. To
        // get around this, we call it async.
        DispatchQueue.main.async {
          viewController.onTransitionCompleted?()
        }
      })
    }

    viewController.isPendingNavigationTransition = true
    viewController.onNavigationBarTypeUpdated = realPush

    // we delay pushing the view controller just a little bit (50ms) so that the react view can render
    DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
      if (viewController.isPendingNavigationTransition) {
        print("Push Fallback Timer Called!")
        realPush()
      }
    }
  }
}

extension UIViewController {
  public func presentReactViewController(viewControllerToPresent: ReactViewController, animated: Bool, completion: (() -> Void)?) {
    // this should never evaluate to true, but is here just to trigger loadView()
    if (viewControllerToPresent.view == nil) {
      return
    }

    let realPresent = { [weak self, weak viewControllerToPresent] in
      guard let viewControllerToPresent = viewControllerToPresent else { return }
      guard let nav = viewControllerToPresent.wrapInNavigationController() else { return }

      viewControllerToPresent.onNavigationBarTypeUpdated = nil
      viewControllerToPresent.isPendingNavigationTransition = false
      viewControllerToPresent.isCurrentlyTransitioning = true

      self?.present(
        nav,
        animated: animated,
        completion: {
          viewControllerToPresent.isCurrentlyTransitioning = false
          completion?()
          // The completion handler of the NavigationController will be called
          // synchronously from this context, but AFTER this block is called. To
          // get around this, we call it async.
          // TODO(lmr): Do we still need to do this?
          DispatchQueue.main.async {
            viewControllerToPresent.onTransitionCompleted?()
          }
        }
      )
    }

    viewControllerToPresent.isPendingNavigationTransition = true
    viewControllerToPresent.onNavigationBarTypeUpdated = realPresent

    // we delay pushing the view controller just a little bit (50ms) so that the react view can render
    DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
      if (viewControllerToPresent.isPendingNavigationTransition) {
        print("Present Fallback Timer Called!")
        viewControllerToPresent.onNavigationBarTypeUpdated?()
      } else {
        viewControllerToPresent.onNavigationBarTypeUpdated = nil
      }
    }
  }
}
