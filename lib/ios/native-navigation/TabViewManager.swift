//
//  TabViewManager.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 8/10/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import UIKit;
#if !NN_NO_COCOAPODS
    import React
#endif

// MARK: - TabView

final class TabView: UIView {

//  init(implementation: ReactNavigationImplementation) {
//    super.init()
//    self.implementation = implementation
//  }

//  required init?(coder aDecoder: NSCoder) {
//    fatalError("init(coder:) has not been implemented")
//  }

  // MARK: Internal

  func setRoute(_ route: String!) {
    self.route = route
  }

  func setConfig(_ config: [String: AnyObject]) {
    self.prevConfig = self.renderedConfig
    self.renderedConfig = config
    implementation.reconcileTabConfig(
      tabBarItem: tabBarItem,
      prev: prevConfig,
      next: renderedConfig
    );
  }

  func setProps(_ props: [String: AnyObject]) {
    self.props = props
  }

  func getViewController() -> UIViewController? {

    if let viewController = viewController {
      return viewController
    }

    // TODO(lmr): handle non-RN tabs
    if let route = route {
      let vc = ReactViewController(moduleName: route, props: props).prepareViewControllerForPresenting()
      vc.tabBarItem = tabBarItem
      viewController = vc
    }

    return viewController

  }

  // MARK: Private

  private var implementation: ReactNavigationImplementation = ReactNavigationCoordinator.sharedInstance.navigation
  private var viewController: UIViewController?
  private var tabBarItem: UITabBarItem = UITabBarItem()
  private var route: String?
  private var props: [String: AnyObject] = [:]
  private var prevConfig: [String: AnyObject] = [:]
  private var renderedConfig: [String: AnyObject] = [:]

}

// MARK: - TabViewManager

private let VERSION: Int = 1

@objc(TabViewManager)
final class TabViewManager: RCTViewManager {
  override func view() -> UIView! {
    return TabView()
//    return TabView(implementation: ReactNavigationCoordinator.sharedInstance.navigation)
  }
}
