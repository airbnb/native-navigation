//
//  ReactSharedElementAnimation.swift
//  NativeNavigator
//
//  Created by Leland Richardson on 9/17/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//

import UIKit

func distance(a: UIView, b: UIView) -> CGPoint {
  return CGPoint(
    x: a.center.x - b.center.x,
    y: a.center.y - b.center.y
  )
}

func scale(ratio: CGSize, a: CGPoint) -> CGPoint {
  return CGPoint(
    x: ratio.width * a.x,
    y: ratio.height * a.y
  )
}

func add(a: CGPoint, b: CGPoint) -> CGPoint {
  return CGPoint(x: a.x + b.x, y: a.y + b.y)
}


// MARK: SharedElementPair

private struct SharedElementPair {
  let from: UIView
  let to: UIView
}

// MARK: ReactAnimationStyle

public struct ReactAnimationStyle {
  let duration: TimeInterval
  let springDampingRatio: CGFloat
  let initialSpringVelocity: CGFloat
  let zoomIntoId: String?
}

public final class ReactSharedElementAnimation: TransitionAnimation {

  public typealias Style = ReactAnimationStyle
  public typealias FromContent = ReactAnimationFromContent
  public typealias ToContent = ReactAnimationToContent

  // MARK: Lifecycle

  public init(style: Style) {
    self.style = style
    self.duration = style.duration
  }

  // MARK: Public

  public let duration: TimeInterval

  public func animateWithContainer(
    container: UIView,
    isPresenting: Bool,
    fromContent: FromContent,
    toContent: ToContent,
    completion: @escaping ()->())
  {
    // we want the "fromContent" to be below the "toContent"
    container.sendSubview(toBack: fromContent.screenWithoutElements)

    let fromScreen = fromContent.screenWithoutElements

    var ratio: CGSize? = nil
    var dScreen: CGPoint? = nil

    if let zoomIntoId = style.zoomIntoId {
      // if "zoomIntoId" is set, we want to do a "zoom in" transition
      toContent.screenWithoutElements.alpha = 0
      if let from = fromContent.sharedElements[zoomIntoId], let to = toContent.sharedElements[zoomIntoId] {

        ratio = from.getScaleRatioToView(view: to)
        dScreen = getScreenDistanceMoved(from: from, to: to, screen: fromContent.screenWithoutElements)

        if !isPresenting {
          if let ratio = ratio, let dScreen = dScreen {
            // if we aren't presenting, we want the "from" screen to start off in "zoomed in" state
            fromScreen.transform = CGAffineTransform(scaleX: ratio.width, y: ratio.height)
            fromScreen.center = add(a: fromScreen.center, b: dScreen)
          }
        }
      }
    } else {
      // in this case we are doing the "normal" cross-fade transition
      toContent.screenWithoutElements.alpha = isPresenting ? 0 : 1
    }

    let sharedElementPairs = getSharedElementPairs(fromContent: fromContent, toContent: toContent)
    let animationBlocks = sharedElementPairs.map({ pair in
      return animateSharedElementBlockWithContainer(
        container: container,
        isPresenting: isPresenting,
        from: pair.from,
        to: pair.to
      )
    })

    UIView.animate(
      withDuration: duration,
      delay: 0,
      usingSpringWithDamping: style.springDampingRatio,
      initialSpringVelocity: style.initialSpringVelocity,
      options: [],
      animations: {
        if let dScreen = dScreen, let ratio = ratio {
          // if dScreen/ratio are set, we are doing a "zoom" transition
          if isPresenting {
            fromScreen.transform = CGAffineTransform(scaleX: ratio.width, y: ratio.height)
            fromScreen.center = add(a: fromScreen.center, b: dScreen)
          } else {
            let minus1 = CGSize(width: -1, height: -1)
            fromScreen.transform = CGAffineTransform.identity
            fromScreen.center = add(a: fromScreen.center, b: scale(ratio: minus1, a: dScreen))
          }
        } else {
          toContent.screenWithoutElements.alpha = isPresenting ? 1 : 0
        }
        animationBlocks.forEach({ fn in fn() })
      },
      completion: { _ in
        completion()
      }
    )
  }

  // MARK: Private

  private let style: Style

  private func getScreenDistanceMoved(from: UIView, to: UIView, screen: UIView) -> CGPoint {
    let ratio = from.getScaleRatioToView(view: to)

    let dr1 = distance(a: from, b: screen)
    let dr2 = distance(a: to, b: screen)
    let minus1 = CGSize(width: -1, height: -1)

    return add(a: scale(ratio: minus1, a: scale(ratio: ratio, a: dr1)), b: dr2)
  }

  private func setToState(
    from: UIView,
    to: UIView,
    toCenter: CGPoint,
    ratio: CGSize,
    container: UIView)
  {
    to.center = toCenter
    to.transform = CGAffineTransform.identity

    from.transform = CGAffineTransform(scaleX: ratio.width, y: ratio.height)
    from.center = toCenter
  }

  private func setFromState(
    from: UIView,
    to: UIView,
    fromCenter: CGPoint,
    ratio: CGSize,
    container: UIView)
  {
    from.center = fromCenter
    from.transform = CGAffineTransform.identity

    to.transform = CGAffineTransform(scaleX: 1.0 / ratio.width, y: 1.0 / ratio.height)
    to.center = fromCenter
  }

  private func animateSharedElementBlockWithContainer(
    container: UIView,
    isPresenting: Bool,
    from: UIView,
    to: UIView) -> (() -> ())
  {
    let toCenter = to.center
    let fromCenter = from.center
    let ratio = from.getScaleRatioToView(view: to)
    if isPresenting {
      self.setFromState(from: from, to: to, fromCenter: fromCenter, ratio: ratio, container: container)

      return {
        self.setToState(from: from, to: to, toCenter: toCenter, ratio: ratio, container: container)
      }
    } else {
      self.setToState(from: from, to: to, toCenter: toCenter, ratio: ratio, container: container)

      return {
        self.setFromState(from: from, to: to, fromCenter: fromCenter, ratio: ratio, container: container)
      }
    }
  }

  private func getSharedElementPairs(fromContent: FromContent, toContent: ToContent) -> [SharedElementPair] {
    var pairs = [SharedElementPair]();

    for (id, fromView) in fromContent.sharedElements {
      if let toView = toContent.sharedElements[id] {
        pairs.append(SharedElementPair(from: fromView, to: toView))
      }
    }
    
    return pairs
  }
  
}

