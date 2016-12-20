//
//  LottieContainerView.m
//  LottieReactNative
//
//  Created by Leland Richardson on 12/12/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

#import "LottieContainerView.h"

@implementation LottieContainerView {
  LAAnimationView *_animationView;
}

- (void)reactSetFrame:(CGRect)frame
{
  [super reactSetFrame:frame];
  if (_animationView != nil) {
    [_animationView reactSetFrame:frame];
  }
}

- (void)setProgress:(CGFloat)progress {
  _progress = progress;
  if (_animation != nil) {
    _animationView.progress = _progress;
  }
}

- (void)setSpeed:(CGFloat)speed {
  _speed = speed;
  if (_animation != nil) {
    _animationView.speed = _speed;
  }
}

- (void)setLoop:(BOOL)loop {
  _loop = loop;
  if (_animation != nil) {
    _animationView.loop = _loop;
  }
}

- (void)setSourceJson:(NSDictionary *)json {
  [self replaceAnimationView:[LAAnimationView animationFromJSON:json]];
}

- (void)setSourceName:(NSString *)name {
  [self replaceAnimationView:[LAAnimationView animationNamed:name]];
}

- (void)play {
  if (_animationView != nil) {
    _animationView.play();
  }
}

- (void)reset {
  if (_animationView != nil) {
    _animationView.pause();
  }
}

# pragma mark Private

- (void)replaceAnimationView:(LAAnimationView *)next {
  if (_animationView != nil) {
    [_animationView removeFromSuperview];
  }
  _animationView = next;
  [self addSubview: next];
  [self applyProperties];
}

- (void)applyProperties {
  _animationView.animationProgress = _progress
  _animationView.animationSpeed = _speed
  _animationView.loopAnimation = _loop
}

@end
