//
//  ReactTabViewController.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 2/22/17.
//
//

import Foundation
import UIKit
import React

open class ReactTabBarController: UITabBarController {

  open weak var reactViewControllerDelegate: ReactViewControllerDelegate?

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
  var dismissResultCode: ReactFlowResultCode?
  var dismissPayload: [String: AnyObject]?
  fileprivate let moduleName: String
  fileprivate var props: [String: AnyObject]
  fileprivate let coordinator: ReactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance
  fileprivate var initialConfig: [String: AnyObject]
  fileprivate var renderedConfig: [String: AnyObject]
  fileprivate var reactView: UIView?
  fileprivate var statusBarAnimation: UIStatusBarAnimation = .fade
  fileprivate var statusBarHidden: Bool = false
  fileprivate var statusBarStyle: UIStatusBarStyle = UIStatusBarStyle.default
  fileprivate var statusBarIsDirty: Bool = false
  fileprivate var leadingButtonVisible: Bool = true
  fileprivate var tabViews: [TabView] = []
  private var barHeight: CGFloat

  public init(moduleName: String, props: [String: AnyObject] = [:]) {
    self.nativeNavigationInstanceId = generateId(moduleName)
    self.moduleName = moduleName

    self.barHeight = -1;
    self.props = EMPTY_MAP

    self.initialConfig = EMPTY_MAP
    self.renderedConfig = EMPTY_MAP

    super.init(nibName: nil, bundle: nil)

    // tab bar controller delegate
    delegate = self

    // I'm all ears for a better way to solve this, but just setting this to false seems to be the best way to attain
    // consistency across screens and platforms. If we don't do this, then scrollview insets will be set differently
    // after popping to a screen. Also, android and ios logic will have to be different. Just opting out of the
    // behavior seems like a better approach at the moment.
    self.automaticallyAdjustsScrollViewInsets = false
  }

  deinit {
    coordinator.unregisterViewController(nativeNavigationInstanceId)
  }

  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override open func viewDidLoad() {
    super.viewDidLoad()
    self.view.backgroundColor = .white
    coordinator.registerViewController(self)
    if let reactView = RCTRootView(
      bridge: coordinator.bridge,
      moduleName: moduleName,
      initialProperties: propsWithMetadata(props, nativeNavigationInstanceId, barHeight)
    ) {

      // If we do end up attaching the view, we might want to use this to prevent it from
      // being visible to the user
      // reactView.isHidden = true

      // It seems like tabs only work some of the time when we don't actually attach it to a view hierarchy.
      // for this reason, we are going to add the rootview as frame-less subview.
      reactView.isHidden = true
      self.view.addSubview(reactView)
      self.reactView = reactView
    }
  }

  func wrapInNavigationController() -> UINavigationController? {
    return coordinator.navigation.makeNavigationController(rootViewController: self)
  }

  func realNavigationDidHappen() {

  }

  func startedWaitingForRealNavigation() {

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

  func setCloseBehavior(_ closeBehavior: String) {

  }

  func signalFirstRenderComplete() {
    reactViewHasBeenRendered = true
    refreshTabViews();
    if (isPendingNavigationTransition) {
      onNavigationBarTypeUpdated?()
    }
  }

  func setNavigationBarProperties(props: [String : AnyObject]) {
    // TODO(lmr): not sure what we are supposed to do here for tabbar case...
  }

  public func dismiss(_ payload: [String : AnyObject]) {
    
  }

  private func refreshTabViews() {

    var stack: [UIView] = [reactView!]

    var next = [TabView]()

    while (!stack.isEmpty) {
      if let node = stack.popLast() {
        for child in node.subviews {
          if let child = child as? TabBar {
            // TODO(lmr): if we end up doing this more than once, perhaps we should throw?
            child.tabBar = self.tabBar
            child.refresh()
            stack.append(child)
          } else if let child = child as? TabView {
            next.append(child)
          } else {
            stack.append(child)
          }
        }
      }
    }

    tabViews = next

    let nullableViewControllers = tabViews.map { $0.getViewController() }
    let viewControllers = nullableViewControllers.flatMap { $0 }
    self.setViewControllers(viewControllers, animated: true)
  }
}

private let DELAY: Int64 = Int64(1.2 * Double(NSEC_PER_SEC))
private var IN_PROGRESS: Bool = false

extension ReactTabBarController: UITabBarControllerDelegate {

  public func tabBarController(_ tabBarController: UITabBarController, shouldSelect viewController: UIViewController) -> Bool {

    // initially, we return false in order to let the view controller content render in RN, and then we will programmatically
    // transition once it's done. This prevents white flashes from occurring


    if (viewController.view == nil) {
      // this should never evaluate to true, we are just doing this here to make sure we load the RN view hierarchy
      return false
    }


    guard let index = self.viewControllers?.index(where: { $0 === viewController }) else {
      return true
    }
    guard let viewController = viewController as? InternalReactViewControllerProtocol else {
      return true
    }

    if (viewController.reactViewHasBeenRendered) {
      return true
    }

    if (IN_PROGRESS) {
      return false
    }
    IN_PROGRESS = true

    let realSelect: () -> Void = { [weak self] in
      IN_PROGRESS = false
      viewController.onNavigationBarTypeUpdated = nil
      viewController.isPendingNavigationTransition = false
      viewController.isCurrentlyTransitioning = true

      self?.selectedIndex = index

      viewController.realNavigationDidHappen()
      viewController.onTransitionCompleted?()
      viewController.emitEvent("onEnterTransitionComplete", body: nil)
    }

    viewController.isPendingNavigationTransition = true
    viewController.onNavigationBarTypeUpdated = realSelect
    viewController.startedWaitingForRealNavigation()

    // we delay pushing the view controller just a little bit (50ms) so that the react view can render
    DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + Double(DELAY) / Double(NSEC_PER_SEC)) {
      if (viewController.isPendingNavigationTransition) {
        print("Push Fallback Timer Called!")
        realSelect()
      } else {
        viewController.eagerNavigationController = nil
        viewController.onNavigationBarTypeUpdated = nil
      }
    }

    return false;
  }

  public func tabBarController(_ tabBarController: UITabBarController, didSelect viewController: UIViewController) {

  }
}
