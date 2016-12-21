//
//  ReactNavigation.swift
//  NativeNavigation
//
//  Created by Andy Bartholomew on 8/2/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import UIKit
import React

private let VERSION: Int = 2

@objc(ReactNavigation)
class ReactNavigation: NSObject {
  fileprivate var dirtySet: Set<ReactViewController>
  fileprivate let coordinator: ReactNavigationCoordinator

  override init() {
    dirtySet = Set<ReactViewController>()
    coordinator = ReactNavigationCoordinator.sharedInstance
  }

  func batchDidComplete() {
    if dirtySet.isEmpty {
      return
    }
    let setCopy = dirtySet
    dirtySet.removeAll()
    DispatchQueue.main.async {
      for vc in setCopy {
        vc.updateNavigation()
      }
    }
  }

  func constantsToExport() -> [String: Any] {
    return [
      "VERSION": VERSION
    ]
  }

  func makeDirty(_ controller: ReactViewController) {
    dirtySet.insert(controller)
  }

  func registerSceneBackgroundColor(_ sceneName: String, withColor color: UIColor) {
    coordinator.setSceneBackgroundColor(sceneName, color: color)
  }

  func setCloseBehavior(_ closeBehavior: String, withAirbnbInstanceId instanceId: String) {
    print("setting closeBehavior: \(closeBehavior)")
    if let vc = coordinator.viewControllerForId(instanceId) {
      vc.setCloseBehavior(closeBehavior)
    }
  }

  func setSnapToFoldOffset(_ snapToFoldOffset: Bool, withAirbnbInstanceId instanceId: String) {
    print("setting snapToFoldOffset: \(snapToFoldOffset)")
    if let vc = coordinator.viewControllerForId(instanceId) {
      vc.setSnapToFoldOffset(snapToFoldOffset)
    }
  }

  func setShowTabBar(_ showTabBar: Bool, withAirbnbInstanceId instanceId: String) {
    print("setting setShowTabBar: \(showTabBar)")
    if let vc = coordinator.viewControllerForId(instanceId) {
      vc.showTabBar = showTabBar
    }
  }

  func signalFirstRenderComplete(_ instanceId: String) {
    if let vc = coordinator.viewControllerForId(instanceId) {
      DispatchQueue.main.async {
        vc.signalFirstRenderComplete()
      }
    }
  }

  // MARK Transitions
  
  func push(_ screenName: String, withProps props: [String: AnyObject], options: [String: AnyObject], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    print("push \(screenName)")
    DispatchQueue.main.async {
      guard let nav = self.coordinator.topNavigationController() else { return }
      guard let current = self.coordinator.topViewController() as? ReactViewController else {
        print("Called push() when topViewController() isn't a ReactViewController")
        return
      }

      let pushed = ReactViewController(moduleName: screenName, props: props)
      pushed.delegate = current.delegate

      let animated = (options["animated"] as? Bool) ?? true
      var makeTransition: (() -> ReactSharedElementTransition)? = nil

      if let transitionGroup = options["transitionGroup"] as? String {
        makeTransition = {
          return ReactSharedElementTransition(
            transitionGroup: transitionGroup,
            fromViewController: current,
            toViewController: pushed as ReactAnimationToContentVendor,
            style: ReactSharedElementTransition.makeDefaultStyle(options),
            options: [:]
          )
        }
      }

      self.coordinator.registerFlow(pushed, resolve: resolve, reject: reject)
      nav.pushReactViewController(pushed, animated: animated, makeTransition: makeTransition)
    }
  }

  func pushNative(_ name: String, withProps props: [String: AnyObject], options: [String: AnyObject], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    print("pushNative: \(name)")
    DispatchQueue.main.async {
      self.coordinator.startFlow(fromName: name, withProps: props, resolve: resolve, reject: reject)
    }
  }
  
  func present(_ screenName: String, withProps props: [String: AnyObject], options: [String: AnyObject], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    print("present \(screenName)")
    DispatchQueue.main.async {
      guard let nav = self.coordinator.topNavigationController() else { return }
      let animated = (options["animated"] as? Bool) ?? true
      let presented = ReactViewController(moduleName: screenName, props: props)
      self.coordinator.registerFlow(presented, resolve: resolve, reject: reject)
      nav.presentReactViewController(presented, animated: animated, completion: nil)
    }
  }
  
  func presentNative(_ name: String, withProps props: [String: AnyObject], options: [String: AnyObject], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    print("presentNative: \(name)")
    DispatchQueue.main.async {
      self.coordinator.startFlow(fromName: name, withProps: props, resolve: resolve, reject: reject)
    }
  }

  func dismiss(_ payload: [String: AnyObject], animated: Bool) {
    print("dismiss")
    DispatchQueue.main.async {
      guard let vc = self.coordinator.topViewController() else { return }
      (vc as? ReactViewController)?.dismiss(payload)
      // TODO(lmr): we need to figure out how to do this with our internal modal manager
      vc.dismiss(animated: animated, completion: nil)
    }
  }
  
  func pop(_ payload: [String: AnyObject], animated: Bool) {
    print("pop")
    // if top VC is being presented in a TabBarController, pop will pop all of the
    // Tabs, in which case we should make sure to dereference each of them.
    
    // TODO(lmr):
    // what if the JS environment wants to pop a parent navigationController of the
    // top navigationController? Perhaps we could pass an optional "level" param or something.
    DispatchQueue.main.async {
      guard let vc = self.coordinator.topViewController() else { return }
      (vc as? ReactViewController)?.dismiss(payload)
      self.coordinator.topNavigationController()?.popViewController(animated: animated)
    }
  }

  func replace(_ screenName: String, withProps props: [String: AnyObject], animated: Bool) {
    print("replace \(screenName)")
    DispatchQueue.main.async {
      let pushed = ReactViewController(moduleName: screenName, props: props)
      self.coordinator.topNavigationController()?.pushViewController(pushed, animated: animated)
    }
  }
}
