//
//  UIView+NativeNavigationSnapshot.swift
//  NativeNavigation
//
//  Created by Laura Skelton on 2/17/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import UIKit

public protocol StandardSnapshot {
  func removeFromSuperview()
}

public protocol ViewSnapshot: StandardSnapshot {
  var view: UIView { get }
  var backgroundColor: UIColor? { get }
  var alpha: CGFloat { get }
  var hidden: Bool { get }
}

public extension ViewSnapshot {
  public func removeFromSuperview() {
    view.removeFromSuperview()
  }
}

public struct UIViewSnapshot: ViewSnapshot {

  public init(
    view: UIView,
    backgroundColor: UIColor?,
    alpha: CGFloat,
    hidden: Bool)
  {
    self.view = view
    self.backgroundColor = backgroundColor
    self.alpha = alpha
    self.hidden = hidden
  }

  public let view: UIView
  public let backgroundColor: UIColor?
  public let alpha: CGFloat
  public let hidden: Bool

}

public extension UIView {

  func snapshotInContainerView(containerView: UIView, completion: (()->())? = nil) -> UIViewSnapshot {
    layoutIfNeeded()
    // TODO: handle snapshot failure
    let view = snapshotView(afterScreenUpdates: true)!

    view.frame = containerView.convert(bounds, from: self)
    containerView.addSubview(view)

    let snapshot = UIViewSnapshot(
      view: view,
      backgroundColor: backgroundColor,
      alpha: alpha,
      hidden: isHidden)

    if let completion = completion {
      DispatchQueue.main.async(execute: completion)
    }

    return snapshot
  }
}
