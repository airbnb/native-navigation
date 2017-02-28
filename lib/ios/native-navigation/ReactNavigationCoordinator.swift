//
//  ReactNavigationCoordinator.swift
//  NativeNavigation
//
//  Created by Spike Brehm on 5/5/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import React
import UIKit

public protocol ReactNavigationCoordinatorDelegate {
  func rootViewController(forCoordinator coordinator: ReactNavigationCoordinator) -> UIViewController?
  func flowCoordinatorForId(_ name: String) -> ReactFlowCoordinator?
  func registerReactDeepLinkUrl(_ deepLinkUrl: String)
}

public protocol ReactFlowCoordinator: class {
  var reactFlowId: String? { get set }

  func start(_ props: [String:AnyObject]?)

  func finish(_ resultCode: ReactFlowResultCode, payload: [String:AnyObject]?)
}

extension ReactFlowCoordinator {
  public func finish(_ resultCode: ReactFlowResultCode, payload: [String:AnyObject]?) {
    ReactNavigationCoordinator.sharedInstance.onFlowFinish(self, resultCode: resultCode, payload: payload)
  }
}

private var _uuid: Int = 0

private func getUuid() -> String {
  _uuid = _uuid + 1
  return "\(_uuid)"
}

private struct ViewControllerHolder {
  weak var viewController: InternalReactViewControllerProtocol?
}

open class ReactNavigationCoordinator: NSObject {

  // MARK: Lifecycle

  override init() {
    viewControllers = [:]
    promises = [:]
    flows = [:]
    deepLinkMapping = [:]
  }

  // MARK: Public

  open static let sharedInstance = ReactNavigationCoordinator()

  open var delegate: ReactNavigationCoordinatorDelegate?
  open var bridge: RCTBridge?
  open var navigation: ReactNavigationImplementation = DefaultReactNavigationImplementation()

  open func topViewController() -> UIViewController? {
    guard let a = delegate?.rootViewController(forCoordinator: self) else {
      return nil
    }
    return a.topMostViewController()
  }

  open func topNavigationController() -> UINavigationController? {
    return topViewController()?.navigationController
  }

  open func topTabBarController() -> UITabBarController? {
    return topViewController()?.tabBarController
  }

  open func startFlow(fromName name: String, withProps props: [String: AnyObject], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let flow = delegate?.flowCoordinatorForId(name) else {
      return
    }
    register(flow, resolve: resolve, reject: reject)
    flow.start(props)
  }

  open func register(_ flow: ReactFlowCoordinator, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let newId = getUuid()
    flow.reactFlowId = newId
    promises[newId] = ReactPromise(resolve: resolve, reject: reject)
    // This is used to prevent ReactFlowCoordinator from being garbage collected
    flows[newId] = flow
  }

  func viewControllerForId(_ id: String) -> InternalReactViewControllerProtocol? {
    return viewControllers[id]?.viewController
  }

  open func moduleNameForDeepLinkUrl(_ deepLinkUrl: String) -> String? {
    return deepLinkMapping[deepLinkUrl]
  }

  open func registerDeepLinkUrl(_ sceneName: String, deepLinkUrl: String) {
    deepLinkMapping[deepLinkUrl] = sceneName
    delegate?.registerReactDeepLinkUrl(deepLinkUrl)
  }

  // MARK: Internal

  func registerFlow(_ viewController: ReactViewController, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let newId = getUuid()
    viewController.reactFlowId = newId
    promises[newId] = ReactPromise(resolve: resolve, reject: reject)
  }

  func registerViewController(_ viewController: InternalReactViewControllerProtocol) {
    let nativeNavigationInstanceId = viewController.nativeNavigationInstanceId
    viewControllers[nativeNavigationInstanceId] = ViewControllerHolder(viewController: viewController)
  }

  func unregisterViewController(_ nativeNavigationInstanceId: String) {
    viewControllers[nativeNavigationInstanceId] = nil
  }

  var sceneInitialPropertiesMap: [String: [String: AnyObject]] = [:]

  func registerScreenProperties(_ sceneName: String, properties: [String: AnyObject]) {
    sceneInitialPropertiesMap[sceneName] = properties
  }

  func getScreenProperties(_ sceneName: String) -> [String: AnyObject]? {
    return sceneInitialPropertiesMap[sceneName]
  }

  func dismissViewController(_ nativeNavigationInstanceId: String, payload: [String: AnyObject]) {
    guard let viewController = viewControllers[nativeNavigationInstanceId]?.viewController else {
      print("Could not find viewController \(nativeNavigationInstanceId)")
      return
    }

    // Dismiss the view controller.
    viewController.dismiss(payload)

    // And remove it from the dictionary.
    viewControllers[nativeNavigationInstanceId] = nil
  }

  // MARK: Private

  fileprivate func onFlowFinish(_ flow: ReactFlowCoordinator, resultCode: ReactFlowResultCode, payload: [String:AnyObject]?) {
    guard let id = flow.reactFlowId else {
      return
    }
    guard let promise = promises[id] else {
      return
    }
    // promises can only be resolved once
    promises[id] = nil
    // Don't need to prevent flow from being garbage collected
    flows[id] = nil

    var result: [String:AnyObject] = [
      "code": resultCode.rawValue as AnyObject,
    ]
    if let payload = payload {
      result["payload"] = payload as AnyObject?
    }

    promise.resolve(result)

    if let vc = flow as? ReactViewController {
      unregisterViewController(vc.nativeNavigationInstanceId)
    }
  }

  fileprivate var promises: [String: ReactPromise]
  fileprivate var viewControllers: [String: ViewControllerHolder]
  fileprivate var flows: [String: ReactFlowCoordinator]
  fileprivate var deepLinkMapping: [String: String]
}

class ReactPromise {
  let resolve: RCTPromiseResolveBlock
  let reject: RCTPromiseRejectBlock
  init(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    self.resolve = resolve
    self.reject = reject
  }
}
