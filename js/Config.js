import React, { PropTypes } from 'react';
import {
  View,
  DeviceEventEmitter,
  Platform,
} from 'react-native';
import shallowEquals from '../utils/shallowEquals';
import AirbnbNavigator from '../utils/AirbnbNavigator';

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
 *    - sheet (colored with scroll shadow, title, trailing link)
 *    - static (white with const shadow, title, link or buttons)
 *    - overlay (always transparent, single button)
 * - (background)color
 * - title
 * - link
 * - buttons
 * - showTabBar
 * - leadingButtonVisible
 *
 * iOS => Android Mapping:
 * - specialty + buttons => transparent-light + buttons
 * - sheet + color + title + link => transparent-dark + color + title + link???
 * - static + title + link/buttons => opaque + white + title + link???/buttons
 * - overlay + button => transparent-light + button
 */

const BAR_TYPE = {
  SPECIALTY: 'specialty',
  SHEET: 'sheet',
  STATIC: 'static',
  OVERLAY: 'overlay',
  BASIC: 'basic',
};

const COLOR = {
  CELEBRATORY: 'celebratory',
  VALID: 'valid',
  INVALID: 'invalid',
  UNVALIDATED: 'unvalidated',
};

const CLOSE_BEHAVIOR = {
  POP: 'pop',
  DISMISS: 'dismiss',
};

// Android-only.
const LEFT_ICON = {
  CLOSE: 'close',
  MENU: 'menu',
  NONE: 'none',
  NAV_LEFT: 'nav-left',
};

function themeFromBarStyle({ barType }) {
  // NOTE(lmr):
  // This function could be replaced with a simple map, but I believe that we
  // may actually need a more nuanced method that looks at other properties, so'
  // I am proactively making it a function + switch statement.
  switch (barType) {
    case BAR_TYPE.SPECIALTY: return 'transparent-light';
    case BAR_TYPE.SHEET: return 'transparent-light';
    case BAR_TYPE.STATIC: return 'opaque';
    case BAR_TYPE.OVERLAY: return 'transparent-light';
    case BAR_TYPE.BASIC: return 'transparent-dark';
    default: return 'transparent-light';
  }
}

class Config extends React.Component {
  constructor(props, context) {
    super(props, context);
    this.subscriptions = {};
    this.handleProps(
      props,
      {},
      context.sceneInstanceId,
      context.nativeNavigationBarStyle
    );
  }

  componentWillReceiveProps(nextProps, nextContext) {
    this.handleProps(
      nextProps,
      this.props,
      nextContext.sceneInstanceId,
      nextContext.nativeNavigationBarStyle
    );
  }

  componentWillUnmount() {
    Object.keys(this.subscriptions).forEach(key => {
      // NOTE(lmr): when we upgrade RN, this will be the new API
      DeviceEventEmitter.removeSubscription(this.subscriptions[key]);
    });
  }

  setCallbackIfNeeded(event, next, prev, id) {
    if (next[event] !== prev[event]) {
      this.setCallback(event, id, next[event]);
    }
  }

  setCallback(event, id, cb) {
    const key = `AirbnbNavigatorScene.${event}.${id}`;
    if (this.subscriptions[key]) {
      DeviceEventEmitter.removeSubscription(this.subscriptions[key]);
    }
    this.subscriptions[key] = DeviceEventEmitter.addListener(key, cb);
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
    switch (Platform.OS) {
      case 'ios':
        if (next.barType !== prev.barType) {
          barStyle.barType = next.barType;
          AirbnbNavigator.setBarType(next.barType, id);
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
    this.setCallbackIfNeeded('onTitlePress', next, prev, id);
    this.setCallbackIfNeeded('onButtonPress', next, prev, id);
  }

  render() {
    if (this.props.children) {
      return (
        <View style={StyleSheet.absoluteFill}>
          {this.props.children}
        </View>
      );
    }
    return null;
  }
}

Config.propTypes = {
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
  leadingButtonVisible: PropTypes.bool,

  onAppear: PropTypes.func,
  onDisappear: PropTypes.func,
  onLeftPress: PropTypes.func,
  onLinkPress: PropTypes.func,
  onButtonPress: PropTypes.func,

  backgroundColor: PropTypes.oneOf(Object.values(COLOR)),

  // Android Attributes
  leftIcon: PropTypes.oneOf(Object.values(LEFT_ICON)),
};

Config.BAR_TYPE = BAR_TYPE;
Config.COLOR = COLOR;
Config.CLOSE_BEHAVIOR = CLOSE_BEHAVIOR;
Config.LEFT_ICON = LEFT_ICON;

Config.contextTypes = {
  sceneInstanceId: PropTypes.string,
  nativeNavigationBarStyle: PropTypes.object,
};

module.exports = Config;
