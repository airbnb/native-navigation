//
//  ReactNavigationCoordinator.swift
//  NativeNavigation
//
//  Created by Spike Brehm on 5/5/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

#if !NN_NO_COCOAPODS
  import React
#endif
import UIKit

@objc public protocol ReactNavigationCoordinatorDelegate {
  func rootViewController(forCoordinator coordinator: ReactNavigationCoordinator) -> UIViewController?
  @objc optional func flowCoordinatorForId(_ name: String) -> ReactFlowCoordinator?
  @objc optional func registerReactDeepLinkUrl(_ deepLinkUrl: String)
}

@objc public protocol ReactFlowCoordinator: class {
  @objc var reactFlowId: String? { get set }

  @objc func start(_ props: [String:AnyObject]?)

  @objc func finish(_ resultCode: ReactFlowResultCode, payload: [String:AnyObject]?)
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

  @objc open static let sharedInstance = ReactNavigationCoordinator()

  @objc open var delegate: ReactNavigationCoordinatorDelegate?
  @objc open var bridge: RCTBridge?
  @objc open var navigation: ReactNavigationImplementation = DefaultReactNavigationImplementation()

  @objc open func topViewController() -> UIViewController? {
    guard let a = delegate?.rootViewController(forCoordinator: self) else {
      return nil
    }
    return a.topMostViewController()
  }

  @objc open func topNavigationController() -> UINavigationController? {
    return topViewController()?.navigationController
  }

  @objc open func topTabBarController() -> UITabBarController? {
    return topViewController()?.tabBarController
  }

  @objc open func startFlow(fromName name: String, withProps props: [String: AnyObject], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard let flow = delegate?.flowCoordinatorForId?(name) else {
      return
    }
    register(flow, resolve: resolve, reject: reject)
    flow.start(props)
  }

  @objc open func register(_ flow: ReactFlowCoordinator, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let newId = getUuid()
    flow.reactFlowId = newId
    promises[newId] = ReactPromise(resolve: resolve, reject: reject)
    // This is used to prevent ReactFlowCoordinator from being garbage collected
    flows[newId] = flow
  }

  func viewControllerForId(_ id: String) -> InternalReactViewControllerProtocol? {
    return viewControllers[id]?.viewController
  }

  @objc open func moduleNameForDeepLinkUrl(_ deepLinkUrl: String) -> String? {
    return deepLinkMapping[deepLinkUrl]
  }

  @objc open func registerDeepLinkUrl(_ sceneName: String, deepLinkUrl: String) {
    deepLinkMapping[deepLinkUrl] = sceneName
    delegate?.registerReactDeepLinkUrl?(deepLinkUrl)
  }

  // MARK: Internal

  @objc func registerFlow(_ viewController: ReactViewController, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let newId = getUuid()
    viewController.reactFlowId = newId
    promises[newId] = ReactPromise(resolve: resolve, reject: reject)
  }

  func registerViewController(_ viewController: InternalReactViewControllerProtocol) {
    let nativeNavigationInstanceId = viewController.nativeNavigationInstanceId
    viewControllers[nativeNavigationInstanceId] = ViewControllerHolder(viewController: viewController)
  }

  @objc func unregisterViewController(_ nativeNavigationInstanceId: String) {
    viewControllers[nativeNavigationInstanceId] = nil
  }

  @objc var sceneInitialPropertiesMap: [String: [String: AnyObject]] = [:]

  @objc open func registerScreenProperties(_ sceneName: String, properties: [String: AnyObject]) {
    sceneInitialPropertiesMap[sceneName] = properties
  }

  @objc func getScreenProperties(_ sceneName: String) -> [String: AnyObject]? {
    return sceneInitialPropertiesMap[sceneName]
  }

  @objc func dismissViewController(_ nativeNavigationInstanceId: String, payload: [String: AnyObject]) {
    guard let viewController = viewControllers[nativeNavigationInstanceId]?.viewController else {
      print("Could not find viewController \(nativeNavigationInstanceId)")
      return
    }

    // Dismiss the view controller.
    viewController.dismiss(payload)

    // And remove it from the dictionary.
    viewControllers[nativeNavigationInstanceId] = nil
  }

  @objc func onFlowFinish(_ flow: ReactFlowCoordinator, resultCode: ReactFlowResultCode, payload: [String:AnyObject]?) {
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

  // MARK: Private

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
