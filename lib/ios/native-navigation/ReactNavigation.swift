//
//  ReactNavigation.swift
//  NativeNavigation
//
//  Created by Andy Bartholomew on 8/2/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

#if !NN_NO_COCOAPODS
  import React
#endif
import UIKit

private let VERSION: Int = 2

@objc(ReactNavigation)
class ReactNavigation: NSObject {
  fileprivate let coordinator: ReactNavigationCoordinator

  override init() {
    coordinator = ReactNavigationCoordinator.sharedInstance
  }

  func constantsToExport() -> [String: Any] {
    return [
      "VERSION": VERSION
    ]
  }

  func registerScreen(
    _ screenName: String,
    properties: [String: AnyObject],
    waitForRender: Bool,
    mode: String
  ) {
    // TODO: register mode and stuff
    coordinator.registerScreenProperties(screenName, properties: properties)
  }

  func setScreenProperties(_ props: [String: AnyObject], withInstanceId instanceId: String) {
    if let vc = coordinator.viewControllerForId(instanceId) {
      DispatchQueue.main.async {
        vc.setNavigationBarProperties(props: props)
      }
    }
  }

  func setCloseBehavior(_ closeBehavior: String, withInstanceId instanceId: String) {
    print("setting closeBehavior: \(closeBehavior)")
    if let vc = coordinator.viewControllerForId(instanceId) {
      vc.setCloseBehavior(closeBehavior)
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
        let screen = ReactViewController(moduleName: screenName, props: props)
        nav.pushReactViewController(screen, animated: true)
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
    
  func resetTo(_ screenName: String, withProps props: [String: AnyObject], options: [String: AnyObject], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    print("resetTo \(screenName)")
    
    DispatchQueue.main.async {
        guard let nav = self.coordinator.topNavigationController() else { return }
        
        let pushed = ReactViewController(moduleName: screenName, props: props)
        let current = self.coordinator.topViewController()
        
        if let currentAsReact = current as? ReactViewController {
            pushed.delegate = currentAsReact.delegate
        }
        
        self.coordinator.registerFlow(pushed, resolve: resolve, reject: reject)
        nav.internalResetToReactViewControllers(pushed, animated: false)
    }
  }

  func pushNative(_ name: String, withProps props: [String: AnyObject], options: [String: AnyObject], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    print("pushNative: \(name)")
    DispatchQueue.main.async {
      self.coordinator.startFlow(fromName: name, withProps: props, resolve: resolve, reject: reject)
    }
  }
  
func present(_ screenName: String,
                 withProps props: [String: AnyObject],
                 options: [String: AnyObject],
                 resolve: @escaping RCTPromiseResolveBlock,
                 reject: @escaping RCTPromiseRejectBlock) {
    debugPrint("pushNative: \(screenName)")
    DispatchQueue.main.async {
        guard let nav = self.coordinator.topNavigationController() else { return }
        
        let animated = (options["animated"] as? Bool) ?? true
        let presented = ReactViewController(moduleName: screenName, props: props)
        
        var makeTransition: (() -> ReactSharedElementTransition)? = nil
        
        if let current = self.coordinator.topViewController() as? ReactViewController,
            let transitionGroup = options["transitionGroup"] as? String {
            makeTransition = {
                return ReactSharedElementTransition(
                    transitionGroup: transitionGroup,
                    fromViewController: current,
                    toViewController: presented as ReactAnimationToContentVendor,
                    style: ReactSharedElementTransition.makeDefaultStyle(options),
                    options: [:]
                )
            }
        }
        
        self.coordinator.registerFlow(presented, resolve: resolve, reject: reject)
        nav.presentReactViewController(presented,
                                       animated: animated,
                                       completion: nil,
                                       presentationStyle: self.modalPresentationStyle(from: options),
                                       makeTransition: makeTransition)
    }
  }
  
  private func modalPresentationStyle(from options: [String: Any]) -> UIModalPresentationStyle {
    guard let modalPresentationStyle = options["modalPresentationStyle"] as? String else {
      return .fullScreen // this is the system default
    }
    
    switch modalPresentationStyle {
    case "fullScreen":          return .fullScreen
    case "pageSheet":           return .pageSheet
    case "formSheet":           return .formSheet
    case "currentContext":      return .currentContext
    case "custom":              return .custom
    case "overFullScreen":      return .overFullScreen
    case "overCurrentContext":  return .overCurrentContext
    case "popover":             return .popover
    case "none":                return .none
    default:                    return .fullScreen // This is the system default
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
      if let nav = self.coordinator.topNavigationController() {
        if nav.viewControllers.count == 1 {
          nav.dismiss(animated: animated, completion: nil)
        } else {
          nav.popViewController(animated: animated)
        }
      }
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
