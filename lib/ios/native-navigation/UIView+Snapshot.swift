//
//  UIView+Snapshot.swift
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

  public func getScaleRatioToView(_ view: UIView) -> CGSize {
    let width: CGFloat = (frame.width > 0) ? frame.width : 1
    let height: CGFloat = (frame.height > 0) ? frame.height : 1
    return CGSize(
      width: view.frame.width / width,
      height: view.frame.height / height)
  }

  @discardableResult func snapshotInContainerView(
    _ containerView: UIView,
    afterScreenUpdates: Bool = true,
    completion: (()->())? = nil) -> UIViewSnapshot
  {

    // TODO: handle snapshot failure
    let view = snapshotView(afterScreenUpdates: afterScreenUpdates)!

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

// MARK: Lazy Snapshot

public protocol StandardLazySnapshot {
  @discardableResult func lazySnapshotInContainerView(_ containerView: UIView) -> UIView

  func setHidden(_ hidden: Bool)

  func reset()

  func getFrameInView(_ view: UIView) -> CGRect

  func getBackgroundColor() -> UIColor?

  func getAlpha() -> CGFloat
}

public struct SnapshottableUIView: StandardLazySnapshot {
  public init(
    view: UIView,
    originallyHidden: Bool) {
    self.view = view
    self.originallyHidden = originallyHidden
  }

  public func lazySnapshotInContainerView(_ containerView: UIView) -> UIView {
    return view.lazySnapshotInContainerView(containerView)
  }

  public func setHidden(_ hidden: Bool) {
    view.isHidden = hidden
  }

  public func reset() {
    view.isHidden = originallyHidden
  }

  public func getFrameInView(_ view: UIView) -> CGRect {
    return self.view.convert(self.view.bounds, to: view)
  }

  public func getBackgroundColor() -> UIColor? {
    return view.backgroundColor
  }

  public func getAlpha() -> CGFloat {
    return view.alpha
  }

  fileprivate let view: UIView
  fileprivate let originallyHidden: Bool
}

public extension UIView {

  func snapshottableView() -> SnapshottableUIView {
    return SnapshottableUIView(
      view: self,
      originallyHidden: isHidden)
  }

  func lazySnapshotInContainerView(_ containerView: UIView) -> UIView {
    // TODO: handle snapshot failure
    let view = snapshotView(afterScreenUpdates: true)!
    view.frame = containerView.convert(bounds, from: self)
    containerView.addSubview(view)
    return view
  }
}
