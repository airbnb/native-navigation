//
//  SharedElementManger.swift
//  NativeNavigator
//
//  Created by Leland Richardson on 9/17/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//

import React

// MARK: - SharedElement

final class SharedElement: RCTView {

  // MARK: Internal

  func setIdentifier(identifier: String!) {
    self.identifier = identifier
  }

  func setSceneInstanceId(sceneInstanceId: String!) {
    self.sceneInstanceId = sceneInstanceId
  }

  override func insertReactSubview(subview: UIView, atIndex index: Int) {
    super.insertReactSubview(subview, atIndex: index)
    guard let seid = identifier, let id = sceneInstanceId else { return }
    let vc = ReactNavigationCoordinator.sharedInstance.viewControllerForId(id: id)
    vc?.sharedElementsById[seid] = WeakViewHolder(view: subview)
  }

  override func removeReactSubview(subview: UIView) {
    super.removeReactSubview(subview)
    guard let seid = identifier, let id = sceneInstanceId else { return }
    let vc = ReactNavigationCoordinator.sharedInstance.viewControllerForId(id: id)
    vc?.sharedElementsById[seid] = nil
  }

  // MARK: Private

  private var identifier: String?
  private var sceneInstanceId: String?
}

// MARK: - SharedElementManager

@objc(SharedElementManager)
final class SharedElementManager: RCTViewManager {
  override func view() -> UIView! {
    return SharedElement()
  }
}
