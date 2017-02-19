//
//  ReactNavigationImplementation.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 2/14/17.
//
//

import Foundation
import UIKit
import React


public protocol ReactNavigationImplementation {

  func makeNavigationController(rootViewController: UIViewController) -> UINavigationController

  func reconcileScreenConfig(
    viewController: ReactViewController,
    navigationController: UINavigationController?,
    props: [String: AnyObject]
  )
}

// this is a convenience class to allow us to easily assign lambdas as press handlers
class BlockBarButtonItem: UIBarButtonItem {
  var actionHandler: ((Void) -> Void)?

  convenience init(title: String?, style: UIBarButtonItemStyle) {
    self.init(title: title, style: style, target: nil, action: #selector(barButtonItemPressed))
    self.target = self
  }

  convenience init(image: UIImage?, style: UIBarButtonItemStyle) {
    self.init(image: image, style: style, target: nil, action: #selector(barButtonItemPressed))
    self.target = self
  }

  convenience init(
    title: String?,
    image: UIImage?,
    style: UIBarButtonItemStyle,
    enabled: Bool?,
    tintColor: UIColor?,
    titleTextAttributes: [String: Any]?
  ) {
    if let title = title {
      self.init(title: title, style: style)
    } else {
      self.init(image: image, style: style)
    }
    if let enabled = enabled {
      isEnabled = enabled
    }
    if let tintColor = tintColor {
      self.tintColor = tintColor
    }
    if let titleTextAttributes = titleTextAttributes {
      // TODO(lmr): what about other control states? do we care?
      setTitleTextAttributes(titleTextAttributes, for: .normal)
    }
  }

  func barButtonItemPressed(sender: UIBarButtonItem) {
    actionHandler?()
  }
}

func colorForKey(_ key: String, _ props: [String: AnyObject]) -> UIColor? {
  guard let val = props[key] as? NSNumber, (val != nil) else { return nil }
  let argb: UInt = val.uintValue;
  let a = CGFloat((argb >> 24) & 0xFF) / 255.0;
  let r = CGFloat((argb >> 16) & 0xFF) / 255.0;
  let g = CGFloat((argb >> 8) & 0xFF) / 255.0;
  let b = CGFloat(argb & 0xFF) / 255.0;
  return UIColor(red: r, green: g, blue: b, alpha: a)
}

func stringForKey(_ key: String, _ props: [String: AnyObject]) -> String? {
  if let val = props[key] as? String?, (val != nil) {
    return val
  }
  return nil
}

func intForKey(_ key: String, _ props: [String: AnyObject]) -> Int? {
  if let val = props[key] as? Int {
    return val
  }
  return nil
}

func floatForKey(_ key: String, _ props: [String: AnyObject]) -> CGFloat? {
  if let val = props[key] as? CGFloat {
    return val
  }
  return nil
}

func doubleForKey(_ key: String, _ props: [String: AnyObject]) -> Double? {
  if let val = props[key] as? Double {
    return val
  }
  return nil
}

func boolForKey(_ key: String, _ props: [String: AnyObject]) -> Bool? {
  if let val = props[key] as? Bool {
    return val
  }
  return nil
}

func imageForKey(_ key: String, _ props: [String: AnyObject]) -> UIImage? {
  if let json = props[key] as? NSDictionary, (json != nil) {
    return RCTConvert.uiImage(json)
  }
  return nil
}

func barButtonStyleFromString(_ string: String?) -> UIBarButtonItemStyle {
  switch(string) {
  case .some("done"): return .done
  default: return .plain
  }
}

func statusBarStyleFromString(_ string: String?) -> UIStatusBarStyle {
  switch(string) {
  case .some("light"): return .lightContent
  default: return .default
  }
}

func statusBarAnimationFromString(_ string: String?) -> UIStatusBarAnimation {
  switch(string) {
  case .some("slide"): return .slide
  case .some("none"): return .none
  default: return UIStatusBarAnimation.fade
  }
}

func textAttributesFromPrefix(_ prefix: String, _ props: [String: AnyObject]) -> [String: Any]? {
  var attributes: [String: Any] = [:]
  if let color = colorForKey("\(prefix)Color", props) {
    attributes[NSForegroundColorAttributeName] = color
  }
  let fontName = stringForKey("\(prefix)FontName", props)
  let fontSize = floatForKey("\(prefix)FontSize", props)
  if let name = fontName, let size = fontSize {
    if let font = UIFont(name: name, size: size) {
      attributes[NSFontAttributeName] = font
    }
  }
  return attributes.count == 0 ? nil : attributes
}

func lower(_ key: String) -> String {
  let i = key.index(key.startIndex, offsetBy: 1)
  return key.substring(to: i).lowercased() + key.substring(from: i)
}

func configurebarButtonItemFromPrefix(
  _ prefix: String,
  _ props: [String: AnyObject],
  _ passedItem: UIBarButtonItem?
) -> BlockBarButtonItem? {

  //    ?.customView = nil
  //    ?.setBackButtonBackgroundImage(nil, for: .focused, barMetrics: .default)
  //    ?.style = .done
  //    ?.tintColor = UIColor.black
  //    ?.width = 20
  //    ?.accessibilityLabel = ""
  //    ?.image = nil
  //    ?.isEnabled = false
  let title = stringForKey(lower("\(prefix)Title"), props)
  let image = imageForKey(lower("\(prefix)Image"), props)
  let enabled = boolForKey(lower("\(prefix)Enabled"), props)
  let tintColor = colorForKey(lower("\(prefix)TintColor"), props)
  let style = stringForKey(lower("\(prefix)Style"), props)
  let titleTextAttributes = textAttributesFromPrefix(lower("\(prefix)Title"), props)

  if let prev = passedItem {
    if (
      title != prev.title ||
      enabled != prev.isEnabled ||
      tintColor != prev.tintColor
    ) {
      return BlockBarButtonItem(
        title: title ?? prev.title,
        image: image ?? prev.image,
        style: barButtonStyleFromString(style),
        enabled: enabled,
        tintColor: tintColor,
        titleTextAttributes: titleTextAttributes
      )
    } else {
      return nil
    }
  } else {
    if (title != nil || image != nil) {
      return BlockBarButtonItem(
        title: title,
        image: image,
        style: barButtonStyleFromString(style),
        enabled: enabled,
        tintColor: tintColor,
        titleTextAttributes: titleTextAttributes
      )
    } else {
      return nil
    }
  }
}

func configureBarButtonArrayForKey(_ key: String, _ props: [String: AnyObject]) -> [BlockBarButtonItem]? {
  if let buttons = props[key] as? [AnyObject] {
    var result = [BlockBarButtonItem]()
    for item in buttons {
      if let buttonProps = item as? [String: AnyObject] {
        if let button = configurebarButtonItemFromPrefix("", buttonProps, nil) {
          result.append(button)
        }
      }
    }
    return result
  }
  return nil
}

open class DefaultReactNavigationImplementation: ReactNavigationImplementation {
  public func makeNavigationController(rootViewController: UIViewController) -> UINavigationController {
    // TODO(lmr): pass initialConfig
    // TODO(lmr): do we want to provide a way to customize the NavigationBar class?
    return UINavigationController(rootViewController: rootViewController)
  }

  // TODO(lmr): should we pass in previous props?
  public func reconcileScreenConfig(
    viewController: ReactViewController,
    navigationController: UINavigationController?,
    props: [String: AnyObject]
  ) {

    // status bar
    if let statusBarHidden = boolForKey("statusBarHidden", props) {
      viewController.setStatusBarHidden(statusBarHidden)
    } else {
      viewController.setStatusBarHidden(false)
    }

    if let statusBarStyle = stringForKey("statusBarStyle", props) {
      viewController.setStatusBarStyle(statusBarStyleFromString(statusBarStyle))
    } else {
      viewController.setStatusBarStyle(.default)
    }

    if let statusBarAnimation = stringForKey("statusBarAnimation", props) {
      viewController.setStatusBarAnimation(statusBarAnimationFromString(statusBarAnimation))
    } else {
      viewController.setStatusBarAnimation(.fade)
    }

    viewController.updateStatusBarIfNeeded()


    let navItem = viewController.navigationItem

    if let title = stringForKey("title", props) {
      viewController.title = title
    }

    if let subtitle = stringForKey("subtitle", props) {
      // TODO(lmr): we should implement a customView for this...
    }

    if let backgroundColor = colorForKey("backgroundColor", props) {
      viewController.view.backgroundColor = backgroundColor
    }

    if let prompt = stringForKey("prompt", props) {
      navItem.prompt = prompt
    } else if navItem.prompt != nil {
      navItem.prompt = nil
    }

    if let rightBarButtonItems = configureBarButtonArrayForKey("rightButtons", props) {
      for (i, item) in rightBarButtonItems.enumerated() {
        item.actionHandler = {
          viewController.emitEvent("onRightPress", body: i as AnyObject?)
        }
      }
      navItem.setRightBarButtonItems(rightBarButtonItems, animated: true)
    } else if let rightBarButtonItem = configurebarButtonItemFromPrefix("right", props, navItem.rightBarButtonItem) {
      rightBarButtonItem.actionHandler = {
        viewController.emitEvent("onRightPress", body: nil)
      }
      navItem.setRightBarButton(rightBarButtonItem, animated: true)
    }

    // TODO(lmr): we have to figure out how to reset this back to the default "back" behavior...
    if let leftBarButtonItems = configureBarButtonArrayForKey("leftButtons", props) {
      for (i, item) in leftBarButtonItems.enumerated() {
        item.actionHandler = {
          viewController.emitEvent("onLeftPress", body: i as AnyObject?)
        }
      }
      navItem.setLeftBarButtonItems(leftBarButtonItems, animated: true)
    } else if let leftBarButtonItem = configurebarButtonItemFromPrefix("left", props, navItem.leftBarButtonItem) {
      leftBarButtonItem.actionHandler = {
        // TODO(lmr): we want to dismiss here...
        viewController.emitEvent("onLeftPress", body: nil)
      }
      navItem.setLeftBarButton(leftBarButtonItem, animated: true)
    }

    if let hidesBackButton = boolForKey("hidesBackButton", props) {
      navItem.setHidesBackButton(hidesBackButton, animated: true)
    }

    if let navController = navigationController {

      if let hidesBarsOnTap = boolForKey("hidesBarsOnTap", props) {
        navController.hidesBarsOnTap = hidesBarsOnTap
      }

      if let hidesBarsOnSwipe = boolForKey("hidesBarsOnSwipe", props) {
        navController.hidesBarsOnSwipe = hidesBarsOnSwipe
      }

      if let hidesBarsWhenKeyboardAppears = boolForKey("hidesBarsWhenKeyboardAppears", props) {
        navController.hidesBarsWhenKeyboardAppears = hidesBarsWhenKeyboardAppears
      }

      if let isNavigationBarHidden = boolForKey("isNavigationBarHidden", props) {
        navController.setNavigationBarHidden(isNavigationBarHidden, animated: true)
      }

      if let isToolbarHidden = boolForKey("isToolbarHidden", props) {
        navController.setToolbarHidden(isToolbarHidden, animated: true)
      }

      let navBar = navController.navigationBar

      if let titleAttributes = textAttributesFromPrefix("title", props) {
        navBar.titleTextAttributes = titleAttributes
      }

      if let backIndicatorImage = imageForKey("backIndicatorImage", props) {
        navBar.backIndicatorImage = backIndicatorImage
      }

      if let backIndicatorTransitionMaskImage = imageForKey("backIndicatorTransitionMaskImage", props) {
        navBar.backIndicatorTransitionMaskImage = backIndicatorTransitionMaskImage
      }

      if let barTintColor = colorForKey("barTintColor", props) {
        navBar.barTintColor = barTintColor
      }

      if let tintColor = colorForKey("tintColor", props) {
        navBar.tintColor = tintColor
      }

      if let alpha = floatForKey("alpha", props) {
        navBar.alpha = alpha
      }

      if let translucent = boolForKey("translucent", props) {
        navBar.isTranslucent = translucent
      }

      //    navigationController?.navigationBar.barStyle = .blackTranslucent
      //    navigationController?.navigationBar.shadowImage = nil
    }

//    viewController.navigationItem.titleView = nil
//    viewController.navigationItem.accessibilityHint = ""
//    viewController.navigationItem.accessibilityLabel = ""
//    viewController.navigationItem.accessibilityValue = ""
//    viewController.navigationItem.accessibilityTraits = 1

    // TODO: 
    // right button(s)
    // statusbar stuff
  }
}
