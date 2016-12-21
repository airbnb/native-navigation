/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "AppDelegate.h"

#import "RCTBundleURLProvider.h"
#import "RCTRootView.h"
#import <NativeNavigation/NativeNavigation.h>


@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{


  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  self.rootViewController = [[EXRootViewController alloc] initWithNibName:nil bundle:nil];
  self.navigationController = [[UINavigationController alloc] initWithRootViewController:self.rootViewController];

  self.window.rootViewController = self.rootViewController;
  [self.window addSubview:self.navigationController.view];

  self.bridge = [[RCTBridge alloc] initWithDelegate:self.rootViewController launchOptions:nil];

//  [[RNNCoordinator sharedInstance] setDelegate:self.rootViewController];
//  [[RNNCoordinator sharedInstance] setBridge:self.bridge];

  [self.window makeKeyAndVisible];



//  NSURL *jsCodeLocation;
//
//  jsCodeLocation = [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"example/index" fallbackResource:nil];
//
//  RCTRootView *rootView = [[RCTRootView alloc] initWithBundleURL:jsCodeLocation
//                                                      moduleName:@"example"
//                                               initialProperties:nil
//                                                   launchOptions:launchOptions];
//  rootView.backgroundColor = [[UIColor alloc] initWithRed:1.0f green:1.0f blue:1.0f alpha:1];
//
//  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
//  UIViewController *rootViewController = [UIViewController new];
//  rootViewController.view = rootView;
//  self.window.rootViewController = rootViewController;
//  [self.window makeKeyAndVisible];
  return YES;
}

@end
