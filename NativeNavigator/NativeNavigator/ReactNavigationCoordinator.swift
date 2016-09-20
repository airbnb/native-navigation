//
//  ReactNavigationCoordinator.swift
//  NativeNavigator
//
//  Created by Leland Richardson on 9/17/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//


import Foundation
import React

public protocol ReactNavigationCoordinatorDelegate {
  func rootViewController(forCoordinator coordinator: ReactNavigationCoordinator) -> UIViewController?
  func flowCoordinatorForId(name: String) -> ReactFlowCoordinator?
}

public protocol ReactFlowCoordinator: class {
  var reactFlowId: String? { get set }

  func start(props: [String:AnyObject]?)

  func finish(resultCode: ReactFlowResultCode, payload: [String:AnyObject]?)
}

extension ReactFlowCoordinator {
  public func finish(resultCode: ReactFlowResultCode, payload: [String:AnyObject]?) {
    ReactNavigationCoordinator.sharedInstance.onFlowFinish(flow: self, resultCode: resultCode, payload: payload)
  }
}

private var _uuid: Int = 0

private func getUuid() -> String {
  _uuid = _uuid + 1
  return "\(_uuid)"
}

private struct ViewControllerHolder {
  weak var viewController: ReactViewController?
}

public class ReactNavigationCoordinator: NSObject {

  // MARK: Lifecycle

  override init() {
    viewControllers = [:]
    promises = [:]
    flows = [:]
  }

  // MARK: Public

  public static let sharedInstance = ReactNavigationCoordinator()

  public var delegate: ReactNavigationCoordinatorDelegate?
  public var bridge: RCTBridge?

  public func topViewController() -> UIViewController? {
    guard let a = delegate?.rootViewController(forCoordinator: self) else {
      return nil
    }
    return a.topMostViewController()
  }

  public func topNavigationController() -> UINavigationController? {
    return topViewController()?.navigationController
  }

  public func topTabBarController() -> UITabBarController? {
    return topViewController()?.tabBarController
  }

  public func startFlow(fromName name: String, withProps props: [String:AnyObject], resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    guard let flow = delegate?.flowCoordinatorForId(name: name) else {
      return
    }
    register(flow, resolve: resolve, reject: reject)
    flow.start(props: props)
  }

  public func register(flow: ReactFlowCoordinator, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let newId = getUuid()
    flow.reactFlowId = newId
    promises[newId] = ReactPromise(resolve: resolve, reject: reject)
    // This is used to prevent ReactFlowCoordinator from being garbage collected
    flows[newId] = flow
  }

  public func viewControllerForId(id: String) -> ReactViewController? {
    return viewControllers[id]?.viewController
  }

  // MARK: Internal

  var transitionDelegate: ReactSharedElementTransition?

  func registerFlow(viewController: ReactViewController, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let newId = getUuid()
    viewController.reactFlowId = newId
    promises[newId] = ReactPromise(resolve: resolve, reject: reject)
  }

  func registerViewController(viewController: ReactViewController) {
    let sceneInstanceId = viewController.sceneInstanceId
    viewControllers[sceneInstanceId] = ViewControllerHolder(viewController: viewController)
  }

  func unregisterViewController(sceneInstanceId: String) {
    viewControllers[sceneInstanceId] = nil
  }

  func dismissViewController(sceneInstanceId: String, payload: [String: AnyObject]) {
    guard let viewController = viewControllers[sceneInstanceId]?.viewController else {
      // Assert(false, desc: "Could not find viewController \(sceneInstanceId)")
      return
    }

    // Dismiss the view controller.
    viewController.dismiss(payload: payload)

    // And remove it from the dictionary.
    viewControllers[sceneInstanceId] = nil
  }

  // MARK: Private

  fileprivate func onFlowFinish(flow: ReactFlowCoordinator, resultCode: ReactFlowResultCode, payload: [String:AnyObject]?) {
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
      unregisterViewController(sceneInstanceId: vc.sceneInstanceId)
    }
  }

  private var promises: [String: ReactPromise]
  private var viewControllers: [String: ViewControllerHolder]
  private var flows: [String: ReactFlowCoordinator]
}

class ReactPromise {
  let resolve: RCTPromiseResolveBlock
  let reject: RCTPromiseRejectBlock
  init(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    self.resolve = resolve
    self.reject = reject
  }
}
