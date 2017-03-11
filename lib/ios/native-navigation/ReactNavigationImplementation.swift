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


@objc public protocol ReactNavigationImplementation: class {

  func makeNavigationController(rootViewController: UIViewController) -> UINavigationController

  func reconcileScreenConfig(
    viewController: ReactViewController,
    navigationController: UINavigationController?,
    prev: [String: AnyObject],
    next: [String: AnyObject]
  )

  func reconcileTabConfig(
    tabBarItem: UITabBarItem,
    prev: [String: AnyObject],
    next: [String: AnyObject]
  )

  func reconcileTabBarConfig(
    tabBar: UITabBar,
    prev: [String: AnyObject],
    next: [String: AnyObject]
  )

  func getBarHeight(viewController: ReactViewController, navigationController: UINavigationController?, config: [String: AnyObject]) -> CGFloat;
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

func stringHasChanged(_ key: String, _ prev: [String: AnyObject], _ next: [String: AnyObject]) -> Bool {
  let a = stringForKey(key, prev)
  let b = stringForKey(key, next)
  if let a = a, let b = b {
      return a != b
  } else if let a = a {
    return true
  } else if let b = b {
    return true
  }
  return false
}

func boolHasChanged(_ key: String, _ prev: [String: AnyObject], _ next: [String: AnyObject]) -> Bool {
  let a = boolForKey(key, prev)
  let b = boolForKey(key, next)
  if let a = a, let b = b {
    return a != b
  } else if let a = a {
    return true
  } else if let b = b {
    return true
  }
  return false
}

func numberHasChanged(_ key: String, _ prev: [String: AnyObject], _ next: [String: AnyObject]) -> Bool {
  if let before = prev[key] as? NSNumber, (before != nil) {
    if let after = next[key] as? NSNumber, (after != nil) {
      return before != after
    } else {
      return true
    }
  } else if let after = next[key] as? NSNumber, (after != nil) {
    return true
  }
  return false
}

func mapHasChanged(_ key: String, _ prev: [String: AnyObject], _ next: [String: AnyObject]) -> Bool {
  if let before = prev[key] {
    if let after = next[key] {
      return true // TODO: could do more here...
    } else {
      return true
    }
  } else if let after = next[key] {
    return true
  }
  return false
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

func textAttributesFromPrefix(
  _ prefix: String,
  _ props: [String: AnyObject]
) -> [String: Any]? {
  var attributes: [String: Any] = [:]
  if let color = colorForKey("\(prefix)Color", props) {
    attributes[NSForegroundColorAttributeName] = color
  } else if let color = colorForKey("foregroundColor", props) {
    attributes[NSForegroundColorAttributeName] = color
  }
  let fontName = stringForKey("\(prefix)FontName", props)
  let fontSize = floatForKey("\(prefix)FontSize", props)
  // TODO(lmr): use system font if no fontname is given
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

  public func getBarHeight(
    viewController: ReactViewController,
    navigationController: UINavigationController?,
    config: [String: AnyObject]
  ) -> CGFloat {
    var statusBarHidden = false
    var navBarHidden = false
    var hasPrompt = false;
    if let hidden = boolForKey("statusBarHidden", config) {
      statusBarHidden = hidden
    }
    if let hidden = boolForKey("hidden", config) {
      navBarHidden = hidden
    }
    if let prompt = stringForKey("prompt", config) {
      hasPrompt = true
    }
    if let navController = navigationController {
      return navController.navigationBar.frame.height + (statusBarHidden ? 0 : 20)
    }
    // make a best guess based on config
    return (statusBarHidden ? 0 : 20) + (navBarHidden ? 0 : 44) + (hasPrompt ? 30 : 0)
  }

  public func makeNavigationController(rootViewController: UIViewController) -> UINavigationController {
    // TODO(lmr): pass initialConfig
    // TODO(lmr): do we want to provide a way to customize the NavigationBar class?
    return UINavigationController(rootViewController: rootViewController)
  }

  public func reconcileTabConfig(
    tabBarItem: UITabBarItem,
    prev: [String: AnyObject],
    next: [String: AnyObject]
  ){
    if mapHasChanged("image", prev, next) {
      tabBarItem.image = imageForKey("image", next)
    }
    if stringHasChanged("title", prev, next) {
      tabBarItem.title = stringForKey("title", next)
    }
    // badgeValue
    // selectedImage
    // titlePositionAdjustment
    //    tabBarItem.title = title
    //    tabBarItem.badgeColor
    //    tabBarItem.badgeTextAttributes(for: .normal)
    //    tabBarItem.badgeValue
    //    tabBarItem.selectedImage
    //    tabBarItem.imageInsets
    //    tabBarItem.titleTextAttributes(for: .normal)
    //    tabBarItem.titlePositionAdjustment

  }

  public func reconcileTabBarConfig(
    tabBar: UITabBar,
    prev: [String: AnyObject],
    next: [String: AnyObject]
  ) {
    if boolHasChanged("translucent", prev, next) {
      if let translucent = boolForKey("translucent", next) {
        tabBar.isTranslucent = translucent
      } else {
        tabBar.isTranslucent = false
      }
    }
    if numberHasChanged("tintColor", prev, next) {
      if let tintColor = colorForKey("tintColor", next) {
        tabBar.tintColor = tintColor
      } else {

      }
    }

    if numberHasChanged("barTintColor", prev, next) {
      if let barTintColor = colorForKey("barTintColor", next) {
        tabBar.barTintColor = barTintColor
      } else {

      }
    }

//    tabBar.alpha
//    tabBar.backgroundColor

    // itemPositioning
    // barStyle
    // itemSpacing float
    // itemWidth: float
    // backgroundImage: image
    // shadowImage: image
    // selectionIndicatorImage: image
    // unselectedItemTintColor: color

  }

  public func reconcileScreenConfig(
    viewController: ReactViewController,
    navigationController: UINavigationController?,
    prev: [String: AnyObject],
    next: [String: AnyObject]
  ) {
    // status bar
    if let statusBarHidden = boolForKey("statusBarHidden", next) {
      viewController.setStatusBarHidden(statusBarHidden)
    } else {
      viewController.setStatusBarHidden(false)
    }

    if stringHasChanged("statusBarStyle", prev, next) {
      if let statusBarStyle = stringForKey("statusBarStyle", next) {
        viewController.setStatusBarStyle(statusBarStyleFromString(statusBarStyle))
      } else {
        viewController.setStatusBarStyle(.default)
      }
    }

    if let statusBarAnimation = stringForKey("statusBarAnimation", next) {
      viewController.setStatusBarAnimation(statusBarAnimationFromString(statusBarAnimation))
    } else {
      viewController.setStatusBarAnimation(.fade)
    }

    viewController.updateStatusBarIfNeeded()

    let navItem = viewController.navigationItem


    if let titleView = titleAndSubtitleViewFromProps(next) {
      if let title = stringForKey("title", next) {
        // set the title anyway, for accessibility
        viewController.title = title
      }
      navItem.titleView = titleView
    } else if let title = stringForKey("title", next) {
      navItem.titleView = nil
      viewController.title = title
    }

    if let screenColor = colorForKey("screenColor", next) {
      viewController.view.backgroundColor = screenColor
    }

    if let prompt = stringForKey("prompt", next) {
      navItem.prompt = prompt
    } else if navItem.prompt != nil {
      navItem.prompt = nil
    }

    if let rightBarButtonItems = configureBarButtonArrayForKey("rightButtons", next) {
      for (i, item) in rightBarButtonItems.enumerated() {
        item.actionHandler = { [weak viewController] in
          viewController?.emitEvent("onRightPress", body: i as AnyObject?)
        }
      }
      navItem.setRightBarButtonItems(rightBarButtonItems, animated: true)
    } else if let rightBarButtonItem = configurebarButtonItemFromPrefix("right", next, navItem.rightBarButtonItem) {
      rightBarButtonItem.actionHandler = { [weak viewController] in
        viewController?.emitEvent("onRightPress", body: nil)
      }
      navItem.setRightBarButton(rightBarButtonItem, animated: true)
    }

    // TODO(lmr): we have to figure out how to reset this back to the default "back" behavior...
    if let leftBarButtonItems = configureBarButtonArrayForKey("leftButtons", next) {
      for (i, item) in leftBarButtonItems.enumerated() {
        item.actionHandler = { [weak viewController] in
          viewController?.emitEvent("onLeftPress", body: i as AnyObject?)
        }
      }
      navItem.setLeftBarButtonItems(leftBarButtonItems, animated: true)
    } else if let leftBarButtonItem = configurebarButtonItemFromPrefix("left", next, navItem.leftBarButtonItem) {
      leftBarButtonItem.actionHandler = { [weak viewController] in
        // TODO(lmr): we want to dismiss here...
        viewController?.emitEvent("onLeftPress", body: nil)
      }
      navItem.setLeftBarButton(leftBarButtonItem, animated: true)
    }

    if let hidesBackButton = boolForKey("hidesBackButton", next) {
      navItem.setHidesBackButton(hidesBackButton, animated: true)
    }

    if let navController = navigationController {

      if let hidesBarsOnTap = boolForKey("hidesBarsOnTap", next) {
        navController.hidesBarsOnTap = hidesBarsOnTap
      }

      if let hidesBarsOnSwipe = boolForKey("hidesBarsOnSwipe", next) {
        navController.hidesBarsOnSwipe = hidesBarsOnSwipe
      }

      if let hidesBarsWhenKeyboardAppears = boolForKey("hidesBarsWhenKeyboardAppears", next) {
        navController.hidesBarsWhenKeyboardAppears = hidesBarsWhenKeyboardAppears
      }

      if let hidden = boolForKey("hidden", next) {
        navController.setNavigationBarHidden(hidden, animated: true)
      }

      if let isToolbarHidden = boolForKey("isToolbarHidden", next) {
        navController.setToolbarHidden(isToolbarHidden, animated: true)
      }

      let navBar = navController.navigationBar

      if let titleAttributes = textAttributesFromPrefix("title", next) {
        navBar.titleTextAttributes = titleAttributes
      }

      if let backIndicatorImage = imageForKey("backIndicatorImage", next) {
        navBar.backIndicatorImage = backIndicatorImage
      }

      if let backIndicatorTransitionMaskImage = imageForKey("backIndicatorTransitionMaskImage", next) {
        navBar.backIndicatorTransitionMaskImage = backIndicatorTransitionMaskImage
      }

      if let backgroundColor = colorForKey("backgroundColor", next) {
        navBar.barTintColor = backgroundColor
      }

      if let foregroundColor = colorForKey("foregroundColor", next) {
        navBar.tintColor = foregroundColor
      }

      if let alpha = floatForKey("alpha", next) {
        navBar.alpha = alpha
      }

      if let translucent = boolForKey("translucent", next) {
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


func getFont(_ name: String, _ size: CGFloat) -> UIFont {
  guard let font = UIFont(name: name, size: size) else {
    return UIFont.systemFont(ofSize: size)
  }
  return font

}

func buildFontFromProps(nameKey: String, sizeKey: String, defaultSize: CGFloat, props: [String: AnyObject]) -> UIFont {
  let name = stringForKey(nameKey, props)
  let size = floatForKey(sizeKey, props)
  if let name = name, let size = size {
    return getFont(name, size)
  } else if let name = name {
    return getFont(name, defaultSize)
  } else if let size = size {
    return UIFont.systemFont(ofSize: size)
  } else {
    return UIFont.systemFont(ofSize: defaultSize)
  }
}

func titleAndSubtitleViewFromProps(_ props: [String: AnyObject]) -> UIView? {
  guard let title = stringForKey("title", props) else { return nil }
  guard let subtitle = stringForKey("subtitle", props) else { return nil }

  let titleHeight = 18
  let subtitleHeight = 12

  let foregroundColor = colorForKey("foregroundColor", props)

  let titleLabel = UILabel(frame: CGRect(x:0, y:-5, width:0, height:0))

  titleLabel.backgroundColor = UIColor.clear

  if let titleColor = colorForKey("titleColor", props) {
    titleLabel.textColor = titleColor
  } else if let titleColor = foregroundColor {
    titleLabel.textColor = titleColor
  } else {
    titleLabel.textColor = UIColor.gray
  }

  titleLabel.font = buildFontFromProps(nameKey: "titleFontName", sizeKey: "titleFontSize", defaultSize: 17, props: props)
  titleLabel.text = title
  titleLabel.sizeToFit()

  let subtitleLabel = UILabel(frame: CGRect(x:0, y:titleHeight, width:0, height:0))
  subtitleLabel.backgroundColor = UIColor.clear


  if let subtitleColor = colorForKey("subtitleColor", props) {
    subtitleLabel.textColor = subtitleColor
  } else if let titleColor = foregroundColor {
    subtitleLabel.textColor = titleColor
  } else {
    subtitleLabel.textColor = UIColor.black
  }

  subtitleLabel.font = buildFontFromProps(nameKey: "subtitleFontName", sizeKey: "subtitleFontSize", defaultSize: 12, props: props)
  subtitleLabel.text = subtitle
  subtitleLabel.sizeToFit()

  let titleView = UIView(
    frame: CGRect(
      x: 0,
      y:0,
      width: max(Int(titleLabel.frame.size.width), Int(subtitleLabel.frame.size.width)),
      height: titleHeight + subtitleHeight
    )
  )
  titleView.addSubview(titleLabel)
  titleView.addSubview(subtitleLabel)

  let widthDiff = subtitleLabel.frame.size.width - titleLabel.frame.size.width

  if widthDiff > 0 {
    var frame = titleLabel.frame
    frame.origin.x = widthDiff / 2
    titleLabel.frame = frame
  } else {
    var frame = subtitleLabel.frame
    frame.origin.x = abs(widthDiff) / 2
    subtitleLabel.frame = frame
  }

  return titleView
}
