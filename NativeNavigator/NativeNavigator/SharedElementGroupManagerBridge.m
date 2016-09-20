//
//  SharedElementGroupManagerBridge.m
//  NativeNavigator
//
//  Created by Leland Richardson on 9/17/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//


#import <React/RCTViewManager.h>

@interface RCT_EXTERN_REMAP_MODULE(NativeNavigatorSharedElementGroup, SharedElementGroupManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(sceneInstanceId, NSString);
RCT_REMAP_VIEW_PROPERTY(id, identifier, NSString);

@end
