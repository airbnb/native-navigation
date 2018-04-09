//
//  ReactNavigationBridge.m
//  NativeNavigation
//
//  Created by Spike Brehm on 5/5/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_REMAP_MODULE(NativeNavigationModule, ReactNavigation, NSObject)

RCT_EXTERN_METHOD(registerScreen:(NSString *)sceneName
                  properties:(NSDictionary *)properties
                  waitForRender:(BOOL)waitForRender
                  mode:(NSString *)mode)
RCT_EXTERN_METHOD(signalFirstRenderComplete:(NSString *)instanceId)
RCT_EXTERN_METHOD(setScreenProperties:(NSDictionary *)props withInstanceId:(NSString *)instanceId)
RCT_EXTERN_METHOD(push:(NSString *)screenName
                  withProps:(NSDictionary *)props
                  options:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(pushNative:(NSString *)name
                  withProps:(NSDictionary *)props
                  options:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(present:(NSString *)screenName
                  withProps:(NSDictionary *)props
                  options:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(resetTo:(NSString *)screenName
                  withProps:(NSDictionary *)props
                  options:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(presentNative:(NSString *)name
                  withProps:(NSDictionary *)props
                  options:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(dismiss:(NSDictionary *)payload animated:(BOOL)animated)
RCT_EXTERN_METHOD(pop:(NSDictionary *)payload animated:(BOOL)animated)
RCT_EXTERN_METHOD(replace:(NSString *)screenName withProps:(NSDictionary *)props animated:(BOOL)animated)

+(BOOL)requiresMainQueueSetup {
    return YES;
}

@end
