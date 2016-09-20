//
//  ReactNavigationBridge.m
//  NativeNavigator
//
//  Created by Leland Richardson on 9/17/16.
//  Copyright © 2016 Airbnb, Inc. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_REMAP_MODULE(NativeNavigatorModule, ReactNavigation, NSObject)

RCT_EXTERN_METHOD(setTitle:(NSString *)title withSceneInstanceId:(NSString *)instanceId)
RCT_EXTERN_METHOD(setLeftIcon:(NSString *)icon withSceneInstanceId:(NSString *)instanceId)
RCT_EXTERN_METHOD(setButtons:(NSArray<NSString *> *)buttons withSceneInstanceId:(NSString *)instanceId)
RCT_EXTERN_METHOD(setTheme:(NSString *)theme withSceneInstanceId:(NSString *)instanceId)
RCT_EXTERN_METHOD(setBarType:(NSString *)barType withSceneInstanceId:(NSString *)instanceId)
RCT_EXTERN_METHOD(setLink:(NSString *)link withSceneInstanceId:(NSString *)instanceId)
RCT_EXTERN_METHOD(setBackgroundColor:(NSString *)color withSceneInstanceId:(NSString *)instanceId)
RCT_EXTERN_METHOD(setFoldOffset:(CGFloat)offset withSceneInstanceId:(NSString *)instanceId)
RCT_EXTERN_METHOD(setLeadingButtonVisible:(BOOL *)withSceneInstanceId withSceneInstanceId:(NSString *)instanceId)
RCT_EXTERN_METHOD(setCloseBehavior:(NSString *)closeBehavior withSceneInstanceId:(NSString *)instanceId)
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
RCT_EXTERN_METHOD(presentNative:(NSString *)name
                  withProps:(NSDictionary *)props
                  options:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(dismiss:(NSDictionary *)payload animated:(BOOL)animated)
RCT_EXTERN_METHOD(pop:(NSDictionary *)payload animated:(BOOL)animated)
RCT_EXTERN_METHOD(replace:(NSString *)screenName withProps:(NSDictionary *)props animated:(BOOL)animated)

@end
