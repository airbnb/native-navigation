//
//  ReactNavigation.swift
//  NativeNavigator
//
//  Created by Leland Richardson on 9/17/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//


import UIKit
import React

private let VERSION: Int = 1

@objc(ReactNavigation)
class ReactNavigation: NSObject {
  private var dirtySet: Set<ReactViewController>
  private let coordinator: ReactNavigationCoordinator

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

  func constantsToExport() -> [String: AnyObject] {
    return [
      "VERSION": VERSION as AnyObject
    ]
  }

  func makeDirty(controller: ReactViewController) {
    dirtySet.insert(controller)
  }

  func setTitle(title: String, withSceneInstanceId instanceId: String) {
    if let vc = coordinator.viewControllerForId(id: instanceId) {
      vc.navigationTitle = title
      makeDirty(controller: vc)
    }
  }

  func setLeftIcon(leftIcon: String, withSceneInstanceId instanceId: String) {
    // NOTE(lmr): iOS handles the left icon automatically... so this just does nothing.
  }

  func setButtons(buttons: [String], withSceneInstanceId instanceId: String) {
    if let vc = coordinator.viewControllerForId(id: instanceId) {
      vc.buttons = buttons
      makeDirty(controller: vc)
    }
  }

  func setTheme(theme: String, withSceneInstanceId instanceId: String) {
    // NOTE(lmr): android only property... should probably remove
  }

  func setBarType(barType: String, withSceneInstanceId instanceId: String) {
    if let vc = coordinator.viewControllerForId(id: instanceId) {
      vc.barType = barType
      makeDirty(controller: vc)
    }
  }

  func setLink(link: String, withSceneInstanceId instanceId: String) {
    if let vc = coordinator.viewControllerForId(id: instanceId) {
      vc.link = link
      makeDirty(controller: vc)
    }
  }

  func setBackgroundColor(backgroundColor: String, withSceneInstanceId instanceId: String) {
    if let vc = coordinator.viewControllerForId(id: instanceId) {
      vc.backgroundColor = backgroundColor
      makeDirty(controller: vc)
    }
  }

  func setFoldOffset(foldOffset: CGFloat, withSceneInstanceId instanceId: String) {
    if let vc = coordinator.viewControllerForId(id: instanceId) {
      vc.setScrollFoldOffset(offset: foldOffset)
    }
  }

  func setLeadingButtonVisible(leadingButtonVisible: Bool, withSceneInstanceId instanceId: String) {
    if let vc = coordinator.viewControllerForId(id: instanceId) {
      vc.setLeadingButtonVisible(leadingButtonVisible: leadingButtonVisible)
    }
  }

  func setCloseBehavior(closeBehavior: String, withSceneInstanceId instanceId: String) {
    if let vc = coordinator.viewControllerForId(id: instanceId) {
      vc.setCloseBehavior(closeBehavior: closeBehavior)
    }
  }

  // MARK Transitions

  func push(screenName: String, withProps props: [String: AnyObject], options: [String: AnyObject], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    DispatchQueue.main.async {
      guard let nav = self.coordinator.topNavigationController() else { return }
      guard let current = self.coordinator.topViewController() as? ReactViewController else {
        // Assert(false, desc: "Called push() when topViewController() isn't a ReactViewController")
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
            style: ReactSharedElementTransition.makeDefaultStyle(options: options)
          )
        }
      }

      self.coordinator.registerFlow(pushed, resolve: resolve, reject: reject)
      nav.pushReactViewController(viewController: pushed, animated: animated, makeTransition: makeTransition)
    }
  }

  func pushNative(name: String, withProps props: [String: AnyObject], options: [String: AnyObject], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    DispatchQueue.main.async {
      self.coordinator.startFlow(fromName: name, withProps: props, resolve: resolve, reject: reject)
    }
  }

  func present(screenName: String, withProps props: [String: AnyObject], options: [String: AnyObject], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    DispatchQueue.main.async {
      guard let nav = self.coordinator.topNavigationController() else { return }
      let animated = (options["animated"] as? Bool) ?? true
      let presented = ReactViewController(moduleName: screenName, props: props)
      self.coordinator.registerFlow(presented, resolve: resolve, reject: reject)
      nav.presentReactViewController(viewControllerToPresent: presented, animated: animated, completion: nil)
    }
  }

  func presentNative(name: String, withProps props: [String: AnyObject], options: [String: AnyObject], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    DispatchQueue.main.async {
      self.coordinator.startFlow(fromName: name, withProps: props, resolve: resolve, reject: reject)
    }
  }

  func dismiss(payload: [String: AnyObject], animated: Bool) {
    DispatchQueue.main.async {
      guard let vc = self.coordinator.topViewController() else { return }
      (vc as? ReactViewController)?.dismiss(payload: payload)
      vc.dismiss(animated: animated, completion: nil)
    }
  }

  func pop(payload: [String: AnyObject], animated: Bool) {
    // if top VC is being presented in a TabBarController, pop will pop all of the
    // Tabs, in which case we should make sure to dereference each of them.

    // TODO(lmr):
    // what if the JS environment wants to pop a parent navigationController of the
    // top navigationController? Perhaps we could pass an optional "level" param or something.
    DispatchQueue.main.async {
      guard let vc = self.coordinator.topViewController() else { return }
      (vc as? ReactViewController)?.dismiss(payload: payload)
      self.coordinator.topNavigationController()?.popViewController(animated: animated)
    }
  }

  func replace(screenName: String, withProps props: [String: AnyObject], animated: Bool) {
    DispatchQueue.main.async {
      let pushed = ReactViewController(moduleName: screenName, props: props)
      self.coordinator.topNavigationController()?.pushViewController(pushed, animated: animated)
    }
  }
}
