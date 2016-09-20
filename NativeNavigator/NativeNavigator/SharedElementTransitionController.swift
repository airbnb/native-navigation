//
//  SharedElementTransitionController.swift
//  NativeNavigator
//
//  Created by Leland Richardson on 9/18/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//


public class SharedElementTransitionController<AnimationType>: NSObject,
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

  public func transitionDuration(using transitionContext: UIViewControllerContextTransitioning?) -> TimeInterval {
    return animation.duration
  }

  public func animateTransition(using transitionContext: UIViewControllerContextTransitioning) {
    animateTransitionWithContext(transitionContext: transitionContext, animationContentGenerator: animationContentGenerator)
  }

  // MARK: Private

  private let isPresenting: Bool
  private let animation: AnimationType
  private let animationContentGenerator: (_ animationContainer: UIView) -> (fromContent: AnimationType.FromContent, toContent: AnimationType.ToContent)?

  private func animateTransitionWithContext(
    transitionContext: UIViewControllerContextTransitioning,
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

    let animationContainer = UIView()
    containerView.insertSubview(animationContainer, belowSubview: fromViewController.view)
    animationContainer.frame = containerView.bounds

    // while we are getting fromContent + toContent, we will snapshot the entire "state of the world" and cover
    // the entire screen so that we can hide/manipulate our view hierarchy to take snapshots without causing any
    // sort of flicker. This is particularly useful if we want to hide subviews and take snapshots with the
    // background of the view exposed.
    let fullSnapshot = fromViewController.view.snapshotInContainerView(containerView: animationContainer)

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

            self.animateWithContainer(animationContainer: animationContainer,
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

  private func animateWithContainer(
    animationContainer: UIView,
    fromContent: AnimationType.FromContent,
    toContent: AnimationType.ToContent,
    completion: () -> Void)
  {
    animation.animateWithContainer(container: animationContainer,
                                   isPresenting: isPresenting,
                                   fromContent: fromContent,
                                   toContent: toContent,
                                   completion: completion)
  }
}
