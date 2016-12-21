/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import <UIKit/UIKit.h>

#import "EXRootViewController.h"
#import "RCTBridge.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (nonatomic, strong) UIWindow *window;

@property (strong, nonatomic) EXRootViewController *rootViewController;
@property (strong, nonatomic) UINavigationController *navigationController;
@property (strong, nonatomic) RCTBridge *bridge;

@end
