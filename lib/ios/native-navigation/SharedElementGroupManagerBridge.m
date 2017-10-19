//
//  SharedElementGroupManagerBridge.m
//  NativeNavigation
//
//  Created by Leland Richardson on 8/10/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

#import <React/RCTLog.h>
#import <React/RCTViewManager.h>

@interface RCT_EXTERN_REMAP_MODULE(NativeNavigationSharedElementGroup, SharedElementGroupManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(nativeNavigationInstanceId, NSString);
RCT_REMAP_VIEW_PROPERTY(id, identifier, NSString);

+(BOOL)requiresMainQueueSetup {
    return YES;
}

@end
