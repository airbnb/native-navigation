//
//  AppDelegate.swift
//  native-navigation
//
//  Created by bachand on 02/11/2017.
//  Copyright (c) 2017 bachand. All rights reserved.
//

import UIKit
import NativeNavigation
import React

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, RCTBridgeDelegate, ReactNavigationCoordinatorDelegate {

  var window: UIWindow?
  var bridge: RCTBridge?
  var reactViewController: ReactViewController?

  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {

    bridge = RCTBridge(delegate: self, launchOptions: nil)
    ReactNavigationCoordinator.sharedInstance.bridge = bridge
    ReactNavigationCoordinator.sharedInstance.delegate = self

    let rootViewController = ViewController()
    let navigationController = UINavigationController(rootViewController: rootViewController)

    window = UIWindow(frame: UIScreen.main.bounds)
    window?.rootViewController = navigationController
    window?.makeKeyAndVisible()

    return true
  }

  func applicationWillResignActive(_ application: UIApplication) {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
  }

  func applicationDidEnterBackground(_ application: UIApplication) {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
  }

  func applicationWillEnterForeground(_ application: UIApplication) {
    // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
  }

  func applicationDidBecomeActive(_ application: UIApplication) {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
  }

  func applicationWillTerminate(_ application: UIApplication) {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
  }

  func sourceURL(for bridge: RCTBridge!) -> URL! {
    return URL(string: "http://localhost:8081/example/index.bundle?platform=ios&dev=true")
  }

  func rootViewController(forCoordinator coordinator: ReactNavigationCoordinator) -> UIViewController? {
    return window?.rootViewController
  }

  func flowCoordinatorForId(_ name: String) -> ReactFlowCoordinator? {
    return nil
  }

  func registerReactDeepLinkUrl(_ deepLinkUrl: String) {
    
  }
  
}
