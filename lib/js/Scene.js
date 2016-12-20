import React, { PropTypes } from 'react';
import {
  StyleSheet,
  View,
  DeviceEventEmitter,
  Platform,
} from 'react-native';
import navigatorEmitter from './navigatorEmitter';
import shallowEquals from './shallowEquals';
import AirbnbNavigator from './NavigatorModule';
import {
  BAR_TYPE,
  CLOSE_BEHAVIOR,
  COLOR,
  LEFT_ICON,
  themeFromBarStyle,
} from './navBar';

/**
 * NavigationBar Configuration:
 *
 * Android:
 * - theme
 *    - opaque + color
 *    - transparent-dark
 *    - transparent-light
 * - navigationIcon
 * - buttonList
 * - backgroundColor
 * - foregroundColor
 * - title
 * - leadingButtonVisible
 *
 * iOS:
 * - barType:
 *    - basic (shadow on scroll, link or buttons)
 *    - specialty (special scrolling, buttons)
 *    - inverse-specialty (special scrolling, buttons)
 *    - sheet (colored with scroll shadow, title, trailing link)
 *    - static (white with const shadow, title, link or buttons)
 *    - overlay (always transparent, single button)
 * - (background)color
 * - title
 * - link
 * - buttons
 * - showTabBar
 * - leadingButtonVisible
 * - snapToFoldOffset
 *
 * iOS => Android Mapping:
 * - specialty + buttons => transparent-light + buttons
 * - sheet + color + title + link => transparent-dark + color + title + link???
 * - static + title + link/buttons => opaque + white + title + link???/buttons
 * - overlay + button => transparent-light + button
 */

class Scene extends React.Component {
  constructor(props, context) {
    super(props, context);
    this.deeSubscriptions = {};
    this.neSubscriptions = {};
    this.handleProps(
      props,
      {},
      context.nativeNavigationScreenInstanceId,
      context.airbnbNavigationBarStyle
    );
  }

  componentWillReceiveProps(nextProps, nextContext) {
    this.handleProps(
      nextProps,
      this.props,
      nextContext.nativeNavigationScreenInstanceId,
      nextContext.airbnbNavigationBarStyle
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
    const key = `AirbnbNavigatorScene.${event}.${id}`;
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
      return (
        <View style={style.scene}>
          {this.props.children}
        </View>
      );
    }
    return null;
  }
}

Scene.propTypes = {
  children: PropTypes.node,

  // Shared Attributes
  title: PropTypes.string,
  link: PropTypes.string,
  buttons: PropTypes.arrayOf(
    PropTypes.oneOf([
      'filters',
      'invite',
      'map',
      'share',
      'heart-alt',
      'heart',
      'more',
    ])
  ),
  barType: PropTypes.oneOf(Object.values(BAR_TYPE)),
  closeBehavior: PropTypes.oneOf(Object.values(CLOSE_BEHAVIOR)),
  hideStatusBarUntilFoldOffset: PropTypes.bool,
  leadingButtonVisible: PropTypes.bool,

  onAppear: PropTypes.func,
  onDisappear: PropTypes.func,
  onLeftPress: PropTypes.func,
  onLinkPress: PropTypes.func,
  onButtonPress: PropTypes.func,
  onEnterTransitionComplete: PropTypes.func,

  backgroundColor: PropTypes.oneOf(Object.values(COLOR)),

  // Android Attributes
  leftIcon: PropTypes.oneOf(Object.values(LEFT_ICON)),
};

Scene.BAR_TYPE = BAR_TYPE;
Scene.COLOR = COLOR;
Scene.CLOSE_BEHAVIOR = CLOSE_BEHAVIOR;
Scene.LEFT_ICON = LEFT_ICON;

Scene.contextTypes = {
  nativeNavigationScreenInstanceId: PropTypes.string,
  // TODO(lmr):
  airbnbNavigationBarStyle: PropTypes.object,
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

module.exports = Scene;
