//
//  UIViewController+TopMostViewController.swift
//  NativeNavigator
//
//  Created by Leland Richardson on 9/18/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//

import Foundation

public protocol CustomTopMostViewController {
  func customTopMostViewController() -> UIViewController?
}

public extension UIViewController {
  func topMostViewController() -> UIViewController? {
    if let custom = (self as? CustomTopMostViewController)?.customTopMostViewController() {
      return custom.topMostViewController()
    } else if let presented = self.presentedViewController {
      return presented.topMostViewController()
    } else if let tabBarSelected = (self as? UITabBarController)?.selectedViewController {
      return tabBarSelected.topMostViewController()
    } else if let navVisible = (self as? UINavigationController)?.visibleViewController {
      return navVisible.topMostViewController()
    } else if let lastChild = self.childViewControllers.last {
      return lastChild.topMostViewController()
    } else {
      return self
    }
  }
}
