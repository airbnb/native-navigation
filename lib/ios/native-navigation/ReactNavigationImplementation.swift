//
//  ReactNavigationImplementation.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 2/14/17.
//
//

import Foundation
import UIKit


public protocol ReactNavigationImplementation {

  func makeNavigationController(rootViewController: UIViewController) -> UINavigationController

  func reconcileScreenConfig(
    viewController: ReactViewController,
    navigationController: UINavigationController?,
    props: [String: AnyObject]
  )
}

class BlockBarButtonItem: UIBarButtonItem {
  private var actionHandler: ((Void) -> Void)?

  convenience init(title: String?, style: UIBarButtonItemStyle, actionHandler: ((Void) -> Void)?) {
    self.init(title: title, style: style, target: nil, action: #selector(barButtonItemPressed))
    self.target = self
    self.actionHandler = actionHandler
  }

  convenience init(image: UIImage?, style: UIBarButtonItemStyle, actionHandler: ((Void) -> Void)?) {
    self.init(image: image, style: style, target: nil, action: #selector(barButtonItemPressed))
    self.target = self
    self.actionHandler = actionHandler
  }

  func barButtonItemPressed(sender: UIBarButtonItem) {
    actionHandler?()
  }
}

open class DefaultReactNavigationImplementation: ReactNavigationImplementation {
  public func makeNavigationController(rootViewController: UIViewController) -> UINavigationController {
    // TODO(lmr): do we want to provide a way to customize the NavigationBar class?
    return UINavigationController(rootViewController: rootViewController)
  }

  // TODO(lmr): should we pass in previous props?
  public func reconcileScreenConfig(
    viewController: ReactViewController,
    navigationController: UINavigationController?,
    props: [String: AnyObject]
  ) {
    if let title = props["title"] as? String?, (title != nil) {
      viewController.title = title
    }
    if let rightTitle = props["rightTitle"] as? String?, (rightTitle != nil) {
      viewController.navigationItem.rightBarButtonItem = BlockBarButtonItem(
        title: rightTitle,
        style: .plain,
        actionHandler: {
          print("onRightPress");
          // TODO(lmr): is this a retain cycle?
          viewController.emitEvent("onRightPress", body: nil)
        })
    }

    // TODO: 
    // back indicator image
    // right button(s)
    // button images
    // tintColor
    // barTintColor
    // text color (titleTextAttributes = NSForegroundColorAttributeName: color
    // statusbar stuff
    // barStyle: UIBarStyle

  }
}
