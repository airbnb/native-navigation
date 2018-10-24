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
  func prepareViewControllerForPresenting() -> UIViewController
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
  public func presentReactViewController(
    _ viewControllerToPresent: ReactViewControllerProtocol,
    animated: Bool,
    completion: (() -> Void)?
  ) {
    presentReactViewController(viewControllerToPresent, animated: animated, completion: completion, presentationStyle: .fullScreen, makeTransition: nil)
  }
  public func presentReactViewController(
    _ viewControllerToPresent: ReactViewControllerProtocol,
    animated: Bool,
    completion: (() -> Void)?,
    presentationStyle: UIModalPresentationStyle = .fullScreen,
    makeTransition: (() -> ReactSharedElementTransition)?
  ) {
    guard let irvc = viewControllerToPresent as? InternalReactViewControllerProtocol else {
      assertionFailure("Unrecognized ReactViewController type.")
      if let vc = viewControllerToPresent as? UIViewController {
        present(vc, animated: animated, completion: completion)
      }
      return
    }
    internalPresentReactViewController(irvc, animated: animated, completion: completion, presentationStyle: presentationStyle, makeTransition: makeTransition)
  }

  func internalPresentReactViewController(
    _ rvc: InternalReactViewControllerProtocol,
    animated: Bool,
    completion: (() -> Void)?,
    presentationStyle: UIModalPresentationStyle,
    makeTransition: (() -> ReactSharedElementTransition)?
  ) {

    // we wrap the vc in a navigation controller early on so that when reconcileScreenConfig happens, it has a navigation
    // controller
    // in the common case, this means wrapping the view controller in a navigation controller, but that's not always
    // what we want.
    let viewControllerToPresent = rvc.prepareViewControllerForPresenting()

    if (IN_PROGRESS) {
      return
    }
    IN_PROGRESS = true

    // set this so we know which nav it should operate on before getting presented
    if let nav = viewControllerToPresent as? UINavigationController {
      rvc.eagerNavigationController = nav
    } else if let nav = viewControllerToPresent.navigationController {
      rvc.eagerNavigationController = nav
    }

    // this should never evaluate to true, but is here just to trigger loadView()
    if (rvc.viewController().view == nil) {
      IN_PROGRESS = false
      return
    }

    let realPresent = { [weak self, weak rvc, weak viewControllerToPresent] in
      IN_PROGRESS = false
      guard let rvc = rvc else { return }
      guard let viewControllerToPresent = viewControllerToPresent else { return }

      let identifier = rvc.nativeNavigationInstanceId
      rvc.onNavigationBarTypeUpdated = nil
      rvc.isPendingNavigationTransition = false
      rvc.isCurrentlyTransitioning = true

      if let transition = makeTransition?() {
        rvc.transition = transition
        viewControllerToPresent.transitioningDelegate = transition
      }
      
      viewControllerToPresent.modalPresentationStyle = presentationStyle
      // TODO: preferredContentSize - for popover types

      self?.present(viewControllerToPresent, animated: animated, completion: {
        rvc.isCurrentlyTransitioning = false
        completion?()
        // The completion handler of the AIRNavigationController will be called
        // synchronously from this context, but AFTER this block is called. To
        // get around this, we call it async.
        DispatchQueue.main.async {
          rvc.onTransitionCompleted?()
          rvc.emitEvent("onEnterTransitionComplete", body: nil)
        }
      })
      // viewController should have a navigationController now. nil out to prevent retain cycles
      rvc.eagerNavigationController = nil
      rvc.realNavigationDidHappen()
    }

    rvc.isPendingNavigationTransition = true
    rvc.onNavigationBarTypeUpdated = realPresent
    rvc.startedWaitingForRealNavigation()

    // we delay pushing the view controller just a little bit (50ms) so that the react view can render
    DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + Double(DELAY) / Double(NSEC_PER_SEC)) {
      if (rvc.isPendingNavigationTransition) {
        print("Present Fallback Timer Called!")
        rvc.onNavigationBarTypeUpdated?()
      } else {
        rvc.onNavigationBarTypeUpdated = nil
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
        self?.delegate = transition
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

  func completeViewControllerTransition(_ viewController: InternalReactViewControllerProtocol) {
    viewController.isCurrentlyTransitioning = false
    // The completion handler of the AIRNavigationController will be called
    // synchronously from this context, but AFTER this block is called. To
    // get around this, we call it async.
    DispatchQueue.main.async(execute: {
      viewController.onTransitionCompleted?()
      viewController.emitEvent("onEnterTransitionComplete", body: nil)
    })
  }
    
  func internalResetToReactViewControllers(
      _ viewController: InternalReactViewControllerProtocol,
      animated: Bool,
      delay: Int64 = DELAY) {

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

          if let navController = ReactNavigationCoordinator.sharedInstance.topNavigationController(), let transition = self?.getResetTransition() {
              navController.view.layer.add(transition, forKey: kCATransition)
              navController.setViewControllers([viewController.viewController()], animated: false)

              viewController.eagerNavigationController = nil
              viewController.realNavigationDidHappen()

            if let coordinator = self?.transitionCoordinator {
              coordinator.animate(alongsideTransition: nil, completion: { context in
                self?.completeViewControllerTransition(viewController)
              })
            } else {
              self?.completeViewControllerTransition(viewController)
            }
        }
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
  
  func getResetTransition() -> CATransition {
    let transition = CATransition.init()
    transition.duration = 0.3
    transition.type = kCATransitionFade
    transition.subtype = kCATransitionFromTop
    
    return transition
  }
}
