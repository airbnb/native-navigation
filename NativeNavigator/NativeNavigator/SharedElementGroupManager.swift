//
//  SharedElementGroupManager.swift
//  NativeNavigator
//
//  Created by Leland Richardson on 9/17/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//

import React

// MARK: - SharedElementGroup

final class SharedElementGroup: RCTView {

  // MARK: Internal

  func setIdentifier(identifier: String!) {
    self.identifier = identifier
    addToViewControllerIfPossible()
  }

  func setSceneInstanceId(sceneInstanceId: String!) {
    self.sceneInstanceId = sceneInstanceId
    addToViewControllerIfPossible()
  }

  func addToViewControllerIfPossible() {
    guard let seid = identifier, let id = sceneInstanceId else { return }
    let vc = ReactNavigationCoordinator.sharedInstance.viewControllerForId(id: id)
    vc?.sharedElementGroupsById[seid] = WeakViewHolder(view: self)
  }

  // MARK: Private

  private var identifier: String?
  private var sceneInstanceId: String?
}

// MARK: - SharedElementGroupManager

@objc(SharedElementGroupManager)
final class SharedElementGroupManager: RCTViewManager {
  override func view() -> UIView! {
    return SharedElementGroup()
  }
}
