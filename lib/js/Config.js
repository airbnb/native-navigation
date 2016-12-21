import React, { PropTypes } from 'react';
import {
  StyleSheet,
  DeviceEventEmitter,
  Platform,
} from 'react-native';
import navigatorEmitter from './navigatorEmitter';
import shallowEquals from './shallowEquals';
import AirbnbNavigator from './NavigatorModule';

class Config extends React.Component {
  constructor(props, context) {
    super(props, context);
    this.deeSubscriptions = {};
    this.neSubscriptions = {};
    this.handleProps(
      props,
      {},
      context.nativeNavigationScreenInstanceId,
      context.nativeNavigationBarProps
    );
  }

  componentWillReceiveProps(nextProps, nextContext) {
    this.handleProps(
      nextProps,
      this.props,
      nextContext.nativeNavigationScreenInstanceId,
      nextContext.nativeNavigationBarProps
    );
  }

  componentWillUnmount() {
    Object.keys(this.deeSubscriptions).forEach(key => {
      // NOTE(lmr): when we upgrade RN, this will be the new API
      DeviceEventEmitter.removeSubscription(this.deeSubscriptions[key]);
    });

    Object.keys(this.neSubscriptions).forEach(key => {
      // NOTE(lmr): when we upgrade RN, this will be the new API
      navigatorEmitter.unsubscribe(this.neSubscriptions[key]);
    });
  }

  setCallbackIfNeeded(event, next, prev, id) {
    if (next[event] !== prev[event]) {
      this.setCallback(event, id, next[event]);
      if (event === 'onAppear' && Platform.OS === 'android') {
        // on android, the first `onAppear` event gets emitted before the JS has time to subscribe
        // to it. As a result, we fire it on `sceneDidMount`, since it's effectively the same
        // thing.
        this.setNavigatorEmitterCallback('sceneDidMount', id, next[event]);
      }
    }
  }

  setNavigatorEmitterCallback(event, id, cb) {
    const key = `${event}.${id}`;
    if (this.neSubscriptions[key]) {
      navigatorEmitter.unsubscribe(this.neSubscriptions[key]);
    }
    this.neSubscriptions[key] = navigatorEmitter.on(key, cb);
  }

  setCallback(event, id, cb) {
    const key = `NativeNavigationScreen.${event}.${id}`;
    if (this.deeSubscriptions[key]) {
      DeviceEventEmitter.removeSubscription(this.deeSubscriptions[key]);
    }
    this.deeSubscriptions[key] = DeviceEventEmitter.addListener(key, cb);
  }

  handleProps(next, prev, id, barStyle) {
    /* eslint-disable no-param-reassign */
    if (!id || !barStyle) {
      return;
    }
    const currentAndroidTheme = barStyle.androidTheme;
    if (next.title !== prev.title) {
      barStyle.title = next.title;
      AirbnbNavigator.setTitle(next.title, id);
    }
    if (next.backgroundColor !== prev.backgroundColor) {
      barStyle.backgroundColor = next.backgroundColor;
      AirbnbNavigator.setBackgroundColor(next.backgroundColor, id);
    }
    if (next.link !== prev.link) {
      AirbnbNavigator.setLink(next.link, id);
    }
    if (
      next.buttons !== prev.buttons &&
      !shallowEquals(next.buttons, prev.buttons)
    ) {
      barStyle.buttons = next.buttons;
      AirbnbNavigator.setButtons(next.buttons, id);
    }
    if (next.leadingButtonVisible !== prev.leadingButtonVisible) {
      barStyle.leadingButtonVisible = next.leadingButtonVisible;
      AirbnbNavigator.setLeadingButtonVisible(next.leadingButtonVisible, id);
    }
    if (next.closeBehavior !== prev.closeBehavior) {
      barStyle.closeBehavior = next.closeBehavior;
      AirbnbNavigator.setCloseBehavior(next.closeBehavior, id);
      // Default `leftIcon` to `"close"` if `closeBehavior` is `DISMISS`.
      if (next.leftIcon == null && next.closeBehavior === CLOSE_BEHAVIOR.DISMISS) {
        AirbnbNavigator.setLeftIcon(LEFT_ICON.CLOSE, id);
      }
    }
    if (next.snapToFoldOffset !== prev.snapToFoldOffset) {
      barStyle.snapToFoldOffset = next.snapToFoldOffset;
      AirbnbNavigator.setSnapToFoldOffset(next.snapToFoldOffset, id);
    }
    switch (Platform.OS) {
      case 'ios':
        if (next.barType !== prev.barType) {
          barStyle.barType = next.barType;
          AirbnbNavigator.setBarType(next.barType, id);
        }
        if (next.hideStatusBarUntilFoldOffset !== prev.hideStatusBarUntilFoldOffset &&
          next.hideStatusBarUntilFoldOffset != null) {
          AirbnbNavigator.setHideStatusBarUntilFoldOffset(
            next.hideStatusBarUntilFoldOffset, this.context.nativeNavigationScreenInstanceId);
        }
        if (next.showTabBar !== prev.showTabBar) {
          barStyle.showTabBar = next.showTabBar;
          AirbnbNavigator.setShowTabBar(next.showTabBar, id);
        }
        break;
      case 'android':
        if (next.leftIcon !== prev.leftIcon) {
          AirbnbNavigator.setLeftIcon(next.leftIcon, id);
        }
        if (next.barType !== prev.barType) {
          barStyle.barType = next.barType;
          barStyle.androidTheme = themeFromBarStyle(barStyle);
          AirbnbNavigator.setTheme(barStyle.androidTheme, id);
          if (barStyle.barType === 'static') {
            AirbnbNavigator.setBackgroundColor('white', id);
          }
        } else {
          barStyle.androidTheme = themeFromBarStyle(barStyle);
          if (barStyle.androidTheme !== currentAndroidTheme) {
            AirbnbNavigator.setTheme(barStyle.androidTheme, id);
            if (barStyle.barType === 'static') {
              AirbnbNavigator.setBackgroundColor('white', id);
            }
          }
        }
        break;
      default: break;
    }
    this.setCallbackIfNeeded('onLeftPress', next, prev, id);
    this.setCallbackIfNeeded('onRightPress', next, prev, id);
    this.setCallbackIfNeeded('onAppear', next, prev, id);
    this.setCallbackIfNeeded('onDisappear', next, prev, id);
    this.setCallbackIfNeeded('onLinkPress', next, prev, id);
    this.setCallbackIfNeeded('onTitlePress', next, prev, id);
    this.setCallbackIfNeeded('onButtonPress', next, prev, id);
    this.setCallbackIfNeeded('onEnterTransitionComplete', next, prev, id);
  }

  render() {
    if (this.props.children) {
      return React.Children.only(this.props.children);
    }
    return null;
  }
}

Config.propTypes = {
  children: PropTypes.node,

  // TODO(lmr): figure out how people can add extra events here
  onAppear: PropTypes.func,
  onDisappear: PropTypes.func,
  onLeftPress: PropTypes.func,
  onLinkPress: PropTypes.func,
  onButtonPress: PropTypes.func,
  onEnterTransitionComplete: PropTypes.func,
};

Config.contextTypes = {
  nativeNavigationScreenInstanceId: PropTypes.string,
  // TODO(lmr):
  nativeNavigationBarProps: PropTypes.object,
};

const style = StyleSheet.create({
  scene: {
    position: 'absolute',
    top: 0,
    left: 0,
    bottom: 0,
    right: 0,
  },
});

module.exports = Config;
