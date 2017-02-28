//
//  ReactViewControllerProtocol.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 2/22/17.
//
//

protocol InternalReactViewControllerProtocol: class {
  var nativeNavigationInstanceId: String { get }
  var eagerNavigationController: UINavigationController? { get set }
  var isCurrentlyTransitioning: Bool { get set }
  var isPendingNavigationTransition: Bool { get set }
  var reactViewHasBeenRendered: Bool { get set }
  var onNavigationBarTypeUpdated: (() -> Void)? { get set }
  var onTransitionCompleted: (() -> Void)? { get }
  var transition: ReactSharedElementTransition? { get set }
  var sharedElementsById: [String: WeakViewHolder] { get set }
  var sharedElementGroupsById: [String: WeakViewHolder] { get set }
  func viewController() -> UIViewController
  func wrapInNavigationController() -> UINavigationController?
  func realNavigationDidHappen()
  func startedWaitingForRealNavigation()
  func emitEvent(_ eventName: String, body: AnyObject?)
  func dismiss(_ payload: [String: AnyObject])
  func setNavigationBarProperties(props: [String: AnyObject])
  func signalFirstRenderComplete()
  func setCloseBehavior(_ closeBehavior: String)
}

public protocol ReactViewControllerProtocol: class {
  func emitEvent(_ eventName: String, body: AnyObject?)
  func dismiss(_ payload: [String: AnyObject])
}

extension ReactViewController: ReactViewControllerProtocol {}
extension ReactViewController: InternalReactViewControllerProtocol {
  public func viewController() -> UIViewController {
    return self
  }
}

extension ReactTabBarController: ReactViewControllerProtocol {}
extension ReactTabBarController: InternalReactViewControllerProtocol {
  public func viewController() -> UIViewController {
    return self
  }
}


// we will wait a maximum of 200ms for the RN view to tell us what the navigation bar should look like.
// should normally happen much quicker than this... This is just to make sure it transitions in a reasonable
// time frame even if the react thread takes an extra long time.
private let DELAY: Int64 = Int64(1.2 * Double(NSEC_PER_SEC))
private var IN_PROGRESS: Bool = false

extension UIViewController {
  public func presentReactViewController(_ viewControllerToPresent: ReactViewControllerProtocol, animated: Bool, completion: (() -> Void)?) {
    guard let irvc = viewControllerToPresent as? InternalReactViewControllerProtocol else {
      assertionFailure("Unrecognized ReactViewController type.")
      if let vc = viewControllerToPresent as? UIViewController {
        present(vc, animated: animated, completion: completion)
      }
      return
    }
    internalPresentReactViewController(irvc, animated: animated, completion: completion)
  }

  func internalPresentReactViewController(_ viewControllerToPresent: InternalReactViewControllerProtocol, animated: Bool, completion: (() -> Void)?) {

    // we wrap the vc in a navigation controller early on so that when reconcileScreenConfig happens, it has a navigation
    // controller
    guard let nav = viewControllerToPresent.wrapInNavigationController() else { return }

    if (IN_PROGRESS) {
      return
    }
    IN_PROGRESS = true

    // set this so we know which nav it should operate on before getting presented
    viewControllerToPresent.eagerNavigationController = nav

    // this should never evaluate to true, but is here just to trigger loadView()
    if (viewControllerToPresent.viewController().view == nil) {
      IN_PROGRESS = false
      return
    }

    let realPresent = { [weak self, weak viewControllerToPresent, weak nav] in
      IN_PROGRESS = false
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

extension UINavigationController {

  public func pushReactViewController(_ viewController: ReactViewControllerProtocol, animated: Bool) {
    pushReactViewController(viewController, animated: animated, delay: DELAY, makeTransition: nil)
  }

  public func pushReactViewController(
    _ viewController: ReactViewControllerProtocol,
    animated: Bool,
    delay: Int64 = DELAY,
    makeTransition: (() -> ReactSharedElementTransition)?
  ) {
    guard let irvc = viewController as? InternalReactViewControllerProtocol else {
      assertionFailure("Unrecognized ReactViewController type.")
      if let vc = viewController as? UIViewController {
        pushViewController(vc, animated: animated)
      }
      return
    }
    internalPushReactViewController(irvc, animated: animated, delay: delay, makeTransition: makeTransition)
  }

  func internalPushReactViewController(
    _ viewController: InternalReactViewControllerProtocol,
    animated: Bool,
    delay: Int64 = DELAY,
    makeTransition: (() -> ReactSharedElementTransition)?) {

    // we debounce this call globally
    if (IN_PROGRESS) {
      return
    }
    IN_PROGRESS = true

    viewController.eagerNavigationController = self

    // this should never evaluate true, but is here just to trigger loadView()
    guard (viewController.viewController().view != nil) else {
      IN_PROGRESS = false
      return
    }


    let realPush: () -> Void = { [weak self] in
      IN_PROGRESS = false
      viewController.onNavigationBarTypeUpdated = nil
      viewController.isPendingNavigationTransition = false
      viewController.isCurrentlyTransitioning = true

      if let transition = makeTransition?() {
        viewController.transition = transition
        self?.transitioningDelegate = transition
      }

      self?.pushViewController(viewController.viewController(), animated: animated)
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
      } else {
        viewController.eagerNavigationController = nil
        viewController.onNavigationBarTypeUpdated = nil
      }
    }
  }
}
