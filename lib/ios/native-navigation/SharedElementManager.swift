//
//  SharedElementManager.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 8/10/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import UIKit
#if !NN_NO_COCOAPODS
  import React
#endif

// MARK: - SharedElement

final class SharedElement: RCTView {

  // MARK: Internal

  func setIdentifier(_ identifier: String!) {
    self.identifier = identifier
  }

  func setNativeNavigationInstanceId(_ nativeNavigationInstanceId: String!) {
    self.nativeNavigationInstanceId = nativeNavigationInstanceId
  }

  override func insertReactSubview(_ subview: UIView, at index: Int) {
    super.insertReactSubview(subview, at: index)
    guard let seid = identifier, let id = nativeNavigationInstanceId else { return }
    let vc = ReactNavigationCoordinator.sharedInstance.viewControllerForId(id)
    vc?.sharedElementsById[seid] = WeakViewHolder(view: subview)
  }

  override func removeReactSubview(_ subview: UIView) {
    super.removeReactSubview(subview)
    guard let seid = identifier, let id = nativeNavigationInstanceId else { return }
    let vc = ReactNavigationCoordinator.sharedInstance.viewControllerForId(id)
    vc?.sharedElementsById[seid] = nil
  }

  // MARK: Private

  fileprivate var identifier: String?
  fileprivate var nativeNavigationInstanceId: String?
}

// MARK: - SharedElementManager

private let VERSION: Int = 1

@objc(SharedElementManager)
final class SharedElementManager: RCTViewManager {
  override func view() -> UIView! {
    return SharedElement()
  }

}
