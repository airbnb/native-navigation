//
//  UIView+NativeNavigationAnimation.swift
//  ReactNativeNavigation
//
//  Created by Laura Skelton on 2/17/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import UIKit

public enum CGRectHorizontalEdge {
  case Top
  case CenterY
  case Bottom
}

public enum CGRectVerticalEdge {
  case Left
  case CenterX
  case Right
}

public extension UIView {

  public func getFrameWithEdge(
    edge: CGRectHorizontalEdge,
    equalToRect rect: CGRect) -> CGRect
  {
    return getFrameWithEdge(edge: edge, equalToRect: rect, otherEdge: edge)
  }

  public func getFrameWithEdge(
    edge: CGRectHorizontalEdge,
    equalToRect rect: CGRect,
    otherEdge: CGRectHorizontalEdge) -> CGRect
  {
    let startOffset: CGFloat
    let endOffset: CGFloat

    switch edge {
    case .Top:
      startOffset = frame.minY
    case .CenterY:
      startOffset = frame.minY + frame.height/2
    case .Bottom:
      startOffset = frame.maxY
    }

    switch otherEdge {
    case .Top:
      endOffset = rect.minY
    case .CenterY:
      endOffset = rect.minY + rect.height/2
    case .Bottom:
      endOffset = rect.maxY
    }

    let offset = endOffset - startOffset

    let newFrame = frame.offsetBy(dx: 0, dy: offset)

    return newFrame
  }

  public func getFrameWithEdge(
    edge: CGRectVerticalEdge,
    equalToRect rect: CGRect) -> CGRect
  {
    return getFrameWithEdge(edge: edge, equalToRect: rect, otherEdge: edge)
  }

  public func getFrameWithEdge(
    edge: CGRectVerticalEdge,
    equalToRect rect: CGRect,
    otherEdge: CGRectVerticalEdge) -> CGRect
  {
    let startOffset: CGFloat
    let endOffset: CGFloat

    switch edge {
    case .Left:
      startOffset = frame.minX
    case .CenterX:
      startOffset = frame.minX + frame.width/2
    case .Right:
      startOffset = frame.maxX
    }

    switch otherEdge {
    case .Left:
      endOffset = rect.minX
    case .CenterX:
      endOffset = rect.minX + rect.width/2
    case .Right:
      endOffset = rect.maxX
    }

    let offset = endOffset - startOffset

    let newFrame = frame.offsetBy(dx: offset, dy: 0)

    return newFrame
  }

  public func getFrameWithCenterEqualToRect(
    rect: CGRect) -> CGRect
  {
    let startOffsetX: CGFloat = frame.minX + (frame.maxX - frame.minX)/2
    let startOffsetY: CGFloat = frame.minY + (frame.maxY - frame.minY)/2

    let endOffsetX: CGFloat = rect.minX + (rect.maxX - rect.minX)/2
    let endOffsetY: CGFloat = rect.minY + (rect.maxY - rect.minY)/2

    let offsetX = endOffsetX - startOffsetX
    let offsetY = endOffsetY - startOffsetY

    let newFrame = frame.offsetBy(dx: offsetX, dy: offsetY)

    return newFrame
  }

  public func getFrameWithCenterXEqualToRect(
    rect: CGRect) -> CGRect
  {
    let startOffset: CGFloat
    let endOffset: CGFloat

    startOffset = frame.minX + (frame.maxX - frame.minX)/2

    endOffset = rect.minX + (rect.maxX - rect.minX)/2

    let offset = endOffset - startOffset

    let newFrame = frame.offsetBy(dx: offset, dy: 0)

    return newFrame
  }

  public func getFrameWithCenterYEqualToRect(
    rect: CGRect) -> CGRect
  {
    let startOffset: CGFloat
    let endOffset: CGFloat

    startOffset = frame.minY + (frame.maxY - frame.minY)/2

    endOffset = rect.minY + (rect.maxY - rect.minY)/2

    let offset = endOffset - startOffset

    let newFrame = frame.offsetBy(dx: 0, dy: offset)

    return newFrame
  }

  public func getFrameWithWidthEqualToRect(
    rect: CGRect) -> CGRect
  {
    let newFrame = CGRect(
      x: frame.minX,
      y: frame.minY,
      width: rect.width,
      height: frame.height)

    return newFrame
  }

  public func getFrameWithHeightEqualToRect(
    rect: CGRect) -> CGRect
  {
    let newFrame = CGRect(
      x: frame.minX,
      y: frame.minY,
      width: frame.width,
      height: rect.height)

    return newFrame
  }

  public func getFrameWithDimensionsEqualToRect(
    rect: CGRect) -> CGRect
  {
    let newFrame = CGRect(
      x: frame.minX,
      y: frame.minY,
      width: rect.width,
      height: rect.height)

    return newFrame
  }

  public func setEdge(
    edge: CGRectHorizontalEdge,
    equalToRect rect: CGRect)
  {
    frame = getFrameWithEdge(edge: edge, equalToRect: rect)
  }

  public func setEdge(
    edge: CGRectHorizontalEdge,
    equalToRect rect: CGRect,
    otherEdge: CGRectHorizontalEdge)
  {
    frame = getFrameWithEdge(edge: edge, equalToRect: rect, otherEdge: otherEdge)
  }

  public func setEdge(
    edge: CGRectVerticalEdge,
    equalToRect rect: CGRect)
  {
    frame = getFrameWithEdge(edge: edge, equalToRect: rect)
  }

  public func setEdge(
    edge: CGRectVerticalEdge,
    equalToRect rect: CGRect,
    otherEdge: CGRectVerticalEdge)
  {
    frame = getFrameWithEdge(edge: edge, equalToRect: rect, otherEdge: otherEdge)
  }

  public func setCenterEqualToRect(
    rect: CGRect)
  {
    setCenterXEqualToRect(rect: rect)
    setCenterYEqualToRect(rect: rect)
  }

  public func setCenterXEqualToRect(
    rect: CGRect)
  {
    frame = getFrameWithCenterXEqualToRect(rect: rect)
  }

  public func setCenterYEqualToRect(
    rect: CGRect)
  {
    frame = getFrameWithCenterYEqualToRect(rect: rect)
  }

  public func setWidthEqualToRect(
    rect: CGRect)
  {
    frame = getFrameWithWidthEqualToRect(rect: rect)
  }

  public func setHeightEqualToRect(
    rect: CGRect)
  {
    frame = getFrameWithHeightEqualToRect(rect: rect)
  }

  public func setDimensionsEqualToRect(
    rect: CGRect)
  {
    frame = getFrameWithDimensionsEqualToRect(rect: rect)
  }

  public func getScaleRatioToView(view: UIView) -> CGSize {
    let width: CGFloat = (frame.width > 0) ? frame.width : 1
    let height: CGFloat = (frame.height > 0) ? frame.height : 1
    return CGSize(
      width: view.frame.width / width,
      height: view.frame.height / height)
  }

  public func moveToView(view: UIView) {
    let newFrame = convert(bounds, to: view)
    view.addSubview(self)
    frame = newFrame
  }
}
