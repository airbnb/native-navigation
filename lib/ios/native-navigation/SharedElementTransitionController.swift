//
//  SharedElementTransitionController.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 9/8/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

open class SharedElementTransitionController<AnimationType>: NSObject,
  UIViewControllerAnimatedTransitioning where
  AnimationType: TransitionAnimation
{
  // MARK: Lifecycle

  public init(
    isPresenting: Bool,
    animation: AnimationType,
    animationContentGenerator: @escaping (_ animationContainer: UIView) -> (fromContent: AnimationType.FromContent, toContent: AnimationType.ToContent)?)
  {
    self.isPresenting = isPresenting
    self.animation = animation
    self.animationContentGenerator = animationContentGenerator
    super.init()
  }

  // MARK: Public

  open func transitionDuration(using transitionContext: UIViewControllerContextTransitioning?) -> TimeInterval {
    return animation.duration
  }

  open func animateTransition(using transitionContext: UIViewControllerContextTransitioning) {
    animateTransitionWithContext(transitionContext, animationContentGenerator: animationContentGenerator)
  }

  // MARK: Private

  fileprivate let isPresenting: Bool
  fileprivate let animation: AnimationType
  fileprivate let animationContentGenerator: (_ animationContainer: UIView) -> (fromContent: AnimationType.FromContent, toContent: AnimationType.ToContent)?

  fileprivate func animateTransitionWithContext(
    _ transitionContext: UIViewControllerContextTransitioning,
    animationContentGenerator: @escaping (_ animationContainer: UIView) -> (fromContent: AnimationType.FromContent, toContent: AnimationType.ToContent)?)
  {
    let containerView = transitionContext.containerView

    guard let fromViewController = transitionContext.viewController(forKey: UITransitionContextViewControllerKey.from),
      let toViewController = transitionContext.viewController(forKey: UITransitionContextViewControllerKey.to) else {
        return
    }

    // Note that the To View must be already visible and added to the container view
    // at the time the snapshot is created. It can be hidden afterwards.
    toViewController.view.frame = transitionContext.finalFrame(for: toViewController)
    containerView.insertSubview(toViewController.view, belowSubview: fromViewController.view)

    // We are immediately laying out the toViewController so that it will be
    // accurately laid out when we snapshot it. We can assume the fromViewController is already laid out since it's visible
    toViewController.view.layoutIfNeeded()

    let animationContainer = UIView()
    containerView.insertSubview(animationContainer, belowSubview: fromViewController.view)
    animationContainer.frame = containerView.bounds

    // while we are getting fromContent + toContent, we will snapshot the entire "state of the world" and cover
    // the entire screen so that we can hide/manipulate our view hierarchy to take snapshots without causing any
    // sort of flicker. This is particularly useful if we want to hide subviews and take snapshots with the
    // background of the view exposed.
    let fullSnapshot = fromViewController.view.snapshotInContainerView(animationContainer)

    containerView.addSubview(fullSnapshot.view)

    // NOTE(lmr);
    // It's important that we `dispatch_async` TWICE here, because React Native requires two "tick"s in order
    // to render text. If we don't wait both ticks, snapshots of React Native views will not include the
    // text.
    DispatchQueue.main.async {
      DispatchQueue.main.async {

        if let (fromContent, toContent) = animationContentGenerator(animationContainer) {

          DispatchQueue.main.async {
            // Only in this block is it guaranteed that our snapshots are fully rendered,
            // so we can start hiding their source views and showing the animation container

            // We can remove the full snapshot now
            fullSnapshot.removeFromSuperview()

            if self.isPresenting {
              toViewController.view.isHidden = true
              fromViewController.view.isUserInteractionEnabled = false
              containerView.bringSubview(toFront: toViewController.view)
              containerView.bringSubview(toFront: animationContainer)
            } else {
              fromViewController.view.removeFromSuperview()
            }

            self.animateWithContainer(animationContainer,
                                      fromContent: fromContent,
                                      toContent: toContent,
                                      completion: {
                                        // On completion, remove all temporary animation snapshot views from view hierarchy
                                        animationContainer.removeFromSuperview()

                                        if self.isPresenting {
                                          toViewController.view.isHidden = false
                                          fromViewController.view.isUserInteractionEnabled = true
                                        }

                                        transitionContext.completeTransition(true)
            })
          }
        }
      }
    }
  }

  fileprivate func animateWithContainer(
    _ animationContainer: UIView,
    fromContent: AnimationType.FromContent,
    toContent: AnimationType.ToContent,
    completion: @escaping () -> Void)
  {
    animation.animateWithContainer(animationContainer,
                                   isPresenting: isPresenting,
                                   fromContent: fromContent,
                                   toContent: toContent,
                                   completion: completion)
  }
}
