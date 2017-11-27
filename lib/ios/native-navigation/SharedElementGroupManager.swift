//
//  SharedElementGroupManager.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 8/10/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import React

// MARK: - SharedElementGroup

final class SharedElementGroup: RCTView {

  // MARK: Internal

  func setIdentifier(_ identifier: String!) {
    self.identifier = identifier
    addToViewControllerIfPossible()
  }

  func setNativeNavigationInstanceId(_ nativeNavigationInstanceId: String!) {
    self.nativeNavigationInstanceId = nativeNavigationInstanceId
    addToViewControllerIfPossible()
  }

  func addToViewControllerIfPossible() {
    guard let seid = identifier, let id = nativeNavigationInstanceId else { return }
    let vc = ReactNavigationCoordinator.sharedInstance.viewControllerForId(id)
    vc?.sharedElementGroupsById[seid] = WeakViewHolder(view: self)
  }

  // MARK: Private

  fileprivate var identifier: String?
  fileprivate var nativeNavigationInstanceId: String?
}

// MARK: - SharedElementGroupManager

private let VERSION: Int = 1

@objc(SharedElementGroupManager)
final class SharedElementGroupManager: RCTViewManager {
  override func view() -> UIView! {
    return SharedElementGroup()
  }

  override func constantsToExport() -> [AnyHashable: Any] {
    return [
      "VERSION": VERSION
    ]
  }
}
