//
//  ReactSharedElementAnimation.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 8/11/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import UIKit

func distance(_ a: UIView, b: UIView) -> CGPoint {
  return CGPoint(
    x: a.center.x - b.center.x,
    y: a.center.y - b.center.y
  )
}

func scale(_ ratio: CGSize, a: CGPoint) -> CGPoint {
  return CGPoint(
    x: ratio.width * a.x,
    y: ratio.height * a.y
  )
}

func add(_ a: CGPoint, b: CGPoint) -> CGPoint {
  return CGPoint(x: a.x + b.x, y: a.y + b.y)
}


// MARK: SharedElementPair

private struct SharedElementPair {
  let from: UIView
  let to: UIView
}

// MARK: ReactAnimationStyle

public enum ReactAnimationForceAspectRatio: Int {
  case none
  case cover
}

public final class ReactAnimationStyle {

  // MARK: Lifecycle

  init(
    duration: TimeInterval,
    springDampingRatio: CGFloat,
    initialSpringVelocity: CGFloat,
    zoomIntoId: String?,
    forceAspectRatio: ReactAnimationForceAspectRatio)
  {
    self.duration = duration
    self.springDampingRatio = springDampingRatio
    self.initialSpringVelocity = initialSpringVelocity
    self.zoomIntoId = zoomIntoId
    self.forceAspectRatio = forceAspectRatio
  }

  // MARK: Internal

  let duration: TimeInterval
  let springDampingRatio: CGFloat
  let initialSpringVelocity: CGFloat
  let zoomIntoId: String?
  let forceAspectRatio: ReactAnimationForceAspectRatio
}

public final class ReactSharedElementAnimation: TransitionAnimation {

  public typealias Style = ReactAnimationStyle
  public typealias FromContent = ReactAnimationFromContent
  public typealias ToContent = ReactAnimationToContent

  // MARK: Lifecycle

  public convenience init(style: Style) {
    self.init(style: style, options: [:])
  }

  public init(style: Style, options: [String: Any]) {
    self.style = style
    self.duration = style.duration
    self.options = options
  }
  // MARK: Public

  public let duration: TimeInterval

  public func animateWithContainer(
    _ container: UIView,
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
        ratio = getScaleRatio(from, to: to)
        dScreen = getScreenDistanceMoved(from, to: to, screen: fromContent.screenWithoutElements)

        if !isPresenting {
          if let ratio = ratio, let dScreen = dScreen {
            // if we aren't presenting, we want the "from" screen to start off in "zoomed in" state
            fromScreen.transform = CGAffineTransform(scaleX: ratio.width, y: ratio.height)
            fromScreen.center = add(fromScreen.center, b: dScreen)
          }
        }
      }
    } else {
      // in this case we are doing the "normal" cross-fade transition
      toContent.screenWithoutElements.alpha = isPresenting ? 0 : 1
    }

    let sharedElementPairs = getSharedElementPairs(fromContent, toContent: toContent)
    let animationBlocks = sharedElementPairs.map({ pair in
      return animateSharedElementBlockWithContainer(
        container,
        isPresenting: isPresenting,
        from: pair.from,
        to: pair.to
      )
    })

    let forceFadeInElements = getForceFadeInElements(toContent)
    let fadeInAnimationBlocks = forceFadeInElements.map({
      animateFadeInElementBlockWithContainer(container, element: $0, isPresenting: isPresenting)
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
            fromScreen.center = add(fromScreen.center, b: dScreen)
          } else {
            let minus1 = CGSize(width: -1, height: -1)
            fromScreen.transform = CGAffineTransform.identity
            fromScreen.center = add(fromScreen.center, b: scale(minus1, a: dScreen))
          }
        } else {
          toContent.screenWithoutElements.alpha = isPresenting ? 1 : 0
        }
        animationBlocks.forEach({ fn in fn() })
        fadeInAnimationBlocks.forEach({ fn in fn() })
      },
      completion: { _ in
        completion()
      }
    )
  }

  // MARK: Private

  fileprivate let style: Style
  fileprivate let options: [String: Any]

  fileprivate func getScreenDistanceMoved(_ from: UIView, to: UIView, screen: UIView) -> CGPoint {
    let ratio = getScaleRatio(from, to: to)

    let dr1 = distance(from, b: screen)
    let dr2 = distance(to, b: screen)
    let minus1 = CGSize(width: -1, height: -1)

    return add(scale(minus1, a: scale(ratio, a: dr1)), b: dr2)
  }

  fileprivate func getScaleRatio(_ from: UIView, to: UIView) -> CGSize {
    var ratio = from.getScaleRatioToView(to)
    switch style.forceAspectRatio {
    case .none: break
    case .cover:
      ratio = CGSize(
        width: max(ratio.width, ratio.height),
        height: max(ratio.width, ratio.height))
    }
    return ratio
  }

  fileprivate func setToState(
    _ from: UIView,
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

  fileprivate func setFromState(
    _ from: UIView,
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

  fileprivate func animateSharedElementBlockWithContainer(
    _ container: UIView,
    isPresenting: Bool,
    from: UIView,
    to: UIView) -> (() -> ())
  {
    // alert: bug fix
    // for some reason the `to` snapshots are not showing up with images visible, even though they are cached.
    // this hack makes it so that the `from` view is used for the whole transition.  Ideally, we figure out a
    // better solution for this.
    to.alpha = 0;

    let toCenter = to.center
    let fromCenter = from.center
    let ratio = getScaleRatio(from, to: to)
    if isPresenting {
      self.setFromState(from, to: to, fromCenter: fromCenter, ratio: ratio, container: container)

      return {
        self.setToState(from, to: to, toCenter: toCenter, ratio: ratio, container: container)
      }
    } else {
      self.setToState(from, to: to, toCenter: toCenter, ratio: ratio, container: container)

      return {
        self.setFromState(from, to: to, fromCenter: fromCenter, ratio: ratio, container: container)
      }
    }
  }

  fileprivate func animateFadeInElementBlockWithContainer(
    _ container: UIView,
    element: UIView,
    isPresenting: Bool) -> (() -> ())
  {
    container.bringSubview(toFront: element)

    if isPresenting {
      element.alpha = 0
      return {
        element.alpha = 1
      }
    } else {
      element.alpha = 1
      return {
        element.alpha = 0
      }
    }
  }

  fileprivate func getForceFadeInElements(_ toContent: ToContent) -> [UIView] {
    guard let fadeInElementIds = options["forceFadeInElementIds"] as? [String] else { return [] }
    let elementIds = Set(fadeInElementIds).intersection(toContent.sharedElements.keys)
    return elementIds.flatMap { toContent.sharedElements[$0] }
  }

  fileprivate func getSharedElementPairs(_ fromContent: FromContent, toContent: ToContent) -> [SharedElementPair] {
    var pairs = [SharedElementPair]();

    for (id, fromView) in fromContent.sharedElements {
      if let toView = toContent.sharedElements[id] {
        pairs.append(SharedElementPair(from: fromView, to: toView))
      }
    }
    
    return pairs
  }

}
