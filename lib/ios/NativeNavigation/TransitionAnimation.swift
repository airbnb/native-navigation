//
//  TransitionAnimation.swift
//  NativeNavigation
//
//  Created by Laura Skelton on 2/17/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

public protocol TransitionAnimation {

  associatedtype Style
  associatedtype FromContent
  associatedtype ToContent

  init(style: Style)

  var duration: TimeInterval { get }

  func animateWithContainer(
    _ container: UIView,
    isPresenting: Bool,
    fromContent: FromContent,
    toContent: ToContent,
    completion: @escaping ()->())
}
