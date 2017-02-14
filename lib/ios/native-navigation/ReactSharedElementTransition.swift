//
//  ReactSharedElementTransition.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 8/11/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import UIKit

// MARK: - ReactAnimationFromContent

public final class ReactAnimationFromContent {

  public let screenWithoutElements: UIView
  public let sharedElements: [String: UIView]

  public init(screenWithoutElements: UIView, sharedElements: [String: UIView]) {
    self.screenWithoutElements = screenWithoutElements
    self.sharedElements = sharedElements
  }
}

// MARK: - ReactAnimationToContent

public final class ReactAnimationToContent {
  public let screenWithoutElements: UIView
  public let sharedElements: [String: UIView]

  public init(screenWithoutElements: UIView, sharedElements: [String: UIView]) {
    self.screenWithoutElements = screenWithoutElements
    self.sharedElements = sharedElements
  }
}

// MARK: - ReactSharedElementSnapshot

public final class ReactSharedElementSnapshot {
  public let screenWithoutElements: UIViewSnapshot
  public let sharedElements: [String: UIViewSnapshot]

  public init(screenWithoutElements: UIViewSnapshot, sharedElements: [String: UIViewSnapshot]) {
    self.screenWithoutElements = screenWithoutElements
    self.sharedElements = sharedElements
  }
}

// MARK: - ReactAnimationFromContentVendor

public protocol ReactAnimationFromContentVendor: class {
  func reactAnimationFromContent(_ animationContainer: UIView, transitionGroup: String, options: [String: Any]) -> ReactAnimationFromContent
  func containerView() -> UIView
}

// MARK: - ReactAnimationToContentVendor

public protocol ReactAnimationToContentVendor: class {
  func reactAnimationToContent(_ animationContainer: UIView) -> ReactAnimationToContent
  func containerView() -> UIView
}

// MARK: - ReactSharedElementTransition

final public class ReactSharedElementTransition: NSObject,
  UIViewControllerTransitioningDelegate,
  UINavigationControllerDelegate
{

  // MARK: Lifecycle

  public init(
    transitionGroup: String,
    fromViewController: ReactAnimationFromContentVendor,
    toViewController: ReactAnimationToContentVendor,
    style: ReactAnimationStyle,
    options: [String: Any])
  {
    self.transitionGroup = transitionGroup
    self.fromViewController = fromViewController
    self.toViewController = toViewController
    self.style = style
    self.options = options
    super.init()
  }

  // MARK: Public

  public static let DefaultStyle: ReactAnimationStyle = ReactAnimationStyle(
    duration: 0.5,
    springDampingRatio: 1,
    initialSpringVelocity: 0,
    zoomIntoId: nil,
    forceAspectRatio: .none
  )

  public static func makeDefaultStyle(_ options: [String: AnyObject]) -> ReactAnimationStyle {
    return ReactAnimationStyle(
      duration: 0.5,
      springDampingRatio: 1,
      initialSpringVelocity: 0,
      zoomIntoId: options["zoomIntoId"] as? String,
      forceAspectRatio: ReactAnimationForceAspectRatio(rawValue: options["forceAspectRatio"] as? Int ?? 0) ?? .none)
  }

  public func animationController(forPresented presented: UIViewController, presenting: UIViewController, source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
    if !fromViewController.containerView().isDescendant(of: presenting.view) {
      return nil
    }
    return makeAnimationController(isPresenting: true)
  }

  public func animationController(forDismissed dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
    if (noPoppingViewControllerAnimation()) {
      return nil
    }
    guard let presentingViewController = dismissed.presentingViewController else { return nil }
    if !fromViewController.containerView().isDescendant(of: presentingViewController.view) {
      return nil
    }
    return makeAnimationController(isPresenting: false)
  }

  public func navigationController(
    _ navigationController: UINavigationController,
    animationControllerFor
    operation: UINavigationControllerOperation, from
    fromVC: UIViewController, to
    toVC: UIViewController) -> UIViewControllerAnimatedTransitioning?
  {
    if operation == .push {
      if fromVC === fromViewController && toVC === toViewController {
        return makeAnimationController(isPresenting: true)
      }
    } else if operation == .pop {
      if (noPoppingViewControllerAnimation()) {
        return nil
      }
      if fromVC === toViewController && toVC === fromViewController {
        return makeAnimationController(isPresenting: false)
      }
    }
    return nil
  }

  // MARK: Private

  fileprivate let transitionGroup: String
  fileprivate let fromViewController: ReactAnimationFromContentVendor
  fileprivate let toViewController: ReactAnimationToContentVendor
  fileprivate let style: ReactAnimationStyle
  fileprivate let options: [String: Any]

  fileprivate func noPoppingViewControllerAnimation() -> Bool {
    guard let noPoppingTransition = options["no_popping_animated_transition"] as? Bool else { return false }
    return noPoppingTransition
  }

  fileprivate func makeAnimationController(isPresenting: Bool) -> UIViewControllerAnimatedTransitioning {

    let animationContentGenerator = { [weak self] animationContainer in
      return self?.generateAnimationContentFromViewControllers(animationContainer)
    }

    return SharedElementTransitionController(
      isPresenting: isPresenting,
      animation: ReactSharedElementAnimation(style: style, options: options),
      animationContentGenerator: animationContentGenerator)
  }

  fileprivate func generateAnimationContentFromViewControllers(_ animationContainer: UIView) -> (ReactAnimationFromContent, ReactAnimationToContent) {
    let fromContent = fromViewController.reactAnimationFromContent(animationContainer, transitionGroup: transitionGroup, options: options)
    let toContent = toViewController.reactAnimationToContent(animationContainer)

    return (fromContent, toContent)
  }
}
