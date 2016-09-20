//
//  ReactSharedElementTransition.swift
//  NativeNavigator
//
//  Created by Leland Richardson on 9/17/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//

// MARK: - ReactAnimationFromContent

public struct ReactAnimationFromContent {
  public let screenWithoutElements: UIView
  public let sharedElements: [String: UIView]
}

// MARK: - ReactAnimationToContent

public struct ReactAnimationToContent {
  public let screenWithoutElements: UIView
  public let sharedElements: [String: UIView]
}

// MARK: - ReactSharedElementSnapshot

public struct ReactSharedElementSnapshot {
  public let screenWithoutElements: UIViewSnapshot
  public let sharedElements: [String: UIViewSnapshot]
}

// MARK: - ReactAnimationFromContentVendor

protocol ReactAnimationFromContentVendor: class {
  func reactAnimationFromContent(animationContainer: UIView, transitionGroup: String) -> ReactAnimationFromContent
  func containerView() -> UIView
}

// MARK: - ReactAnimationToContentVendor

protocol ReactAnimationToContentVendor: class {
  func reactAnimationToContent(animationContainer: UIView) -> ReactAnimationToContent
  func containerView() -> UIView
}

// MARK: - ReactSharedElementTransition

final public class ReactSharedElementTransition: NSObject,
  UIViewControllerTransitioningDelegate,
  UINavigationControllerDelegate
{

  // MARK: Lifecycle

  init(
    transitionGroup: String,
    fromViewController: ReactAnimationFromContentVendor,
    toViewController: ReactAnimationToContentVendor,
    style: ReactAnimationStyle)
  {
    self.transitionGroup = transitionGroup
    self.fromViewController = fromViewController
    self.toViewController = toViewController
    self.style = style
    super.init()
  }

  // MARK: Public

  public static let DefaultStyle: ReactAnimationStyle = ReactAnimationStyle(
    duration: 0.5,
    springDampingRatio: 1,
    initialSpringVelocity: 0,
    zoomIntoId: nil
  )

  public static func makeDefaultStyle(options: [String: AnyObject]) -> ReactAnimationStyle {
    return ReactAnimationStyle(
      duration: 0.5,
      springDampingRatio: 1,
      initialSpringVelocity: 0,
      zoomIntoId: options["zoomIntoId"] as? String
    )
  }

  public func animationControllerForPresentedController(presented: UIViewController, presentingController presenting: UIViewController, sourceController source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
    if !fromViewController.containerView().isDescendant(of: presenting.view) {
      return nil
    }
    return makeAnimationController(isPresenting: true)
  }

  public func animationControllerForDismissedController(dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
    guard let presentingViewController = dismissed.presentingViewController else { return nil }
    if !fromViewController.containerView().isDescendant(of: presentingViewController.view) {
      return nil
    }
    return makeAnimationController(isPresenting: false)
  }

// TODO(lmr): This was from AIRNavigationControllerDelegate. Do we need it?

//  public func navigationController(
//    navigationController: UINavigationController,
//    animationControllerForOperation
//    operation: UINavigationControllerOperation, fromViewController
//    fromVC: UIViewController, toViewController
//    toVC: UIViewController) -> UIViewControllerAnimatedTransitioning?
//  {
//    if operation == .Push {
//      if fromVC === fromViewController && toVC === toViewController {
//        return makeAnimationController(isPresenting: true)
//      }
//    } else if operation == .Pop {
//      if fromVC === toViewController && toVC === fromViewController {
//        return makeAnimationController(isPresenting: false)
//      }
//    }
//    return nil
//  }

  // MARK: Private

  private let transitionGroup: String
  private let fromViewController: ReactAnimationFromContentVendor
  private let toViewController: ReactAnimationToContentVendor
  private let style: ReactAnimationStyle

  private func makeAnimationController(isPresenting: Bool) -> UIViewControllerAnimatedTransitioning {

    let animationContentGenerator = { [weak self] animationContainer in
      return self?.generateAnimationContentFromViewControllers(animationContainer: animationContainer)
    }

    return SharedElementTransitionController(
      isPresenting: isPresenting,
      animation: ReactSharedElementAnimation(style: style),
      animationContentGenerator: animationContentGenerator)
  }

  private func generateAnimationContentFromViewControllers(animationContainer: UIView) -> (ReactAnimationFromContent, ReactAnimationToContent) {
    let fromContent = fromViewController.reactAnimationFromContent(animationContainer: animationContainer, transitionGroup: transitionGroup)
    let toContent = toViewController.reactAnimationToContent(animationContainer: animationContainer)

    return (fromContent, toContent)
  }
}

