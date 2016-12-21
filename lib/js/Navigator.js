import React, { PropTypes } from 'react';
import {
  AppRegistry,
  processColor,
  Platform,
} from 'react-native';
import deline from 'deline';
import AirbnbNavigator from './NavigatorModule';
import navigatorEmitter from './navigatorEmitter';

const RESULT_CANCELED = 0;
const RESULT_OK = -1;

function wrapResult(promise) {
  // React Native doesn't allow you to pass complex data through
  // promise rejections, so we pass it back through resolve, and
  // do the right thing here instead.
  return promise /* .then(data => {
    if (data && data.code === RESULT_CANCELED) {
      // rejection
      throw data;
    } else {
      // resolved
      return data;
    }
  })*/;
}

function unwrap(thunk) {
  const Screen = thunk();
  /* eslint no-underscore-dangle: 0 */
  if (Screen.__esModule === true) {
    return Screen.default;
  }
  return Screen;
}

function showMissingBarTypeWarning(sceneName) {
  console.warn(deline`
    The screen "${sceneName}" rendered without specifying a navigation bar type.
    This should never happen. This means that a <Navigator.Config /> component was
    never rendered. This often happens by rendering a full screen <Loader /> component
    while data is loading for the screen. In this case, you should make sure to
    include a <Navigator.Config /> component somewhere in that scre
  `);
}

function wrapScreen(sceneName, sceneThunk) {
  class WrappedScreen extends React.Component {
    constructor(props) {
      super(props);
      this.nativeNavigationBarProps = {
        title: null,
        backgroundColor: null,
        buttons: null,
        link: null,
        barType: null,
        androidTheme: null,
      };
    }
    getChildContext() {
      return {
        nativeNavigationScreenInstanceId: this.props.nativeNavigationScreenInstanceId,
        nativeNavigationBarProps: this.nativeNavigationBarProps,
      };
    }
    componentDidMount() {
      navigatorEmitter.emit(`sceneDidMount.${this.props.nativeNavigationScreenInstanceId}`);
      AirbnbNavigator.signalFirstRenderComplete(this.props.nativeNavigationScreenInstanceId);

      if (__DEV__) {
        // We never want to have a scene fully render without specifying a `barType`. If we have
        // reached `componentDidMount` and `barType` still isn't set, then we have done just that.
        // This often happens because people render an empty View or a Loader while loading data
        // async, but we STILL want to specify a `barType` in that case. Even better, we want to
        // encourage people to actually use a full-on Marquee, even in loading states.
        if (this.nativeNavigationBarProps.barType === null) {
          // We have to put this in a `setTimeout`, because the screen likely isn't pushed onto
          // the viewController stack yet, since it waits for a render to complete before doing
          // that.
          setTimeout(() => showMissingBarTypeWarning(sceneName), 0);
        }
      }
    }
    render() {
      const ScreenComponent = unwrap(sceneThunk);
      return <ScreenComponent {...this.props} />;
    }
  }

  WrappedScreen.propTypes = {
    nativeNavigationScreenInstanceId: PropTypes.string,
  };

  WrappedScreen.childContextTypes = {
    nativeNavigationScreenInstanceId: PropTypes.string,
    nativeNavigationBarProps: PropTypes.object,
  };

  return WrappedScreen;
}

const NavigatorModule = {
  RESULT_CANCELED,
  RESULT_OK,
  registerScreen(sceneName, sceneThunk, options = {}) {
    if (options.backgroundColor) {
      AirbnbNavigator.registerScreenBackgroundColor(
        sceneName,
        processColor(options.backgroundColor)
      );
    }
    // TODO(lmr):
    // if (options.barType) {
    //   if (Platform.OS === 'android') {
    //     const theme = themeFromBarStyle({
    //       barType: options.barType,
    //       backgroundColor: options.backgroundColor,
    //     });
    //     AirbnbNavigator.registerScreenNavigationBarTheme(sceneName, theme);
    //   }
    //   if (Platform.OS === 'ios') {
    //     AirbnbNavigator.registerScreenNavigationBarType(sceneName, options.barType);
    //   }
    // }
    // if (options.barColor) {
    //   AirbnbNavigator.registerScreenNavigationBarColor(
    //     sceneName,
    //     options.barColor
    //   );
    // }

    const WrappedScreen = wrapScreen(sceneName, sceneThunk);
    AppRegistry.registerComponent(sceneName, () => WrappedScreen);
    return WrappedScreen;
  },

  push(screenName, props = null, options = {}) {
    if (typeof screenName === 'function') {
      // TODO(lmr): handle the ability for users to pass in a component instead of a string name.
      // we could put the identifier statically on the constructor, and look for it here and
      // throw if it's not present...
    }
    if (AppRegistry.getAppKeys().indexOf(screenName) !== -1) {
      return wrapResult(AirbnbNavigator.push(screenName, props, options));
    }
    return wrapResult(AirbnbNavigator.pushNative(screenName, props, options));
  },
  present(screenName, props = null, options = {}) {
    if (AppRegistry.getAppKeys().indexOf(screenName) !== -1) {
      return wrapResult(AirbnbNavigator.present(screenName, props, options));
    }
    return wrapResult(AirbnbNavigator.presentNative(screenName, props, options));
  },
  pop(payload = null, animated = true) {
    AirbnbNavigator.pop(payload, animated);
  },
  dismiss(payload = null, animated = true) {
    AirbnbNavigator.dismiss(payload, animated);
  },
};

module.exports = NavigatorModule;
