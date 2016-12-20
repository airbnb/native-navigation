import React, { PropTypes } from 'react';
import {
  AppRegistry,
  processColor,
  Platform,
} from 'react-native';
import AirbnbNavigator from './NavigatorModule';
import navigatorEmitter from './navigatorEmitter';
import {
  BAR_TYPE,
  CLOSE_BEHAVIOR,
  COLOR,
  LEFT_ICON,
  themeFromBarStyle,
} from './navBar';

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
  const Scene = thunk();
  /* eslint no-underscore-dangle: 0 */
  if (Scene.__esModule === true) {
    return Scene.default;
  }
  return Scene;
}

function showMissingBarTypeWarning(sceneName) {
  /* eslint quotes: 0 */
  console.warn(
    `The screen "${sceneName}" rendered without specifying a navigation bar type. ` +
    `This should never happen. This means that a <Navigator.Scene /> component was ` +
    `never rendered. This can happen if you're not rendering a <Marquee /> ` +
    `component, which often happens by rendering a full screen <Loader /> component ` +
    `while data is loading for the screen. In this case, you should make sure to ` +
    `render a <Marquee /> component during your loading state or just make sure to ` +
    `include a <Navigator.Scene /> component somewhere in that screen.`
  );
}

function wrapScene(sceneName, sceneThunk) {
  class WrappedScene extends React.Component {
    constructor(props) {
      super(props);
      this.airbnbNavigationBarStyle = {
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
        airbnbNavigationBarStyle: this.airbnbNavigationBarStyle,
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
        if (this.airbnbNavigationBarStyle.barType === null) {
          // We have to put this in a `setTimeout`, because the screen likely isn't pushed onto
          // the viewController stack yet, since it waits for a render to complete before doing
          // that.
          setTimeout(() => showMissingBarTypeWarning(sceneName), 0);
        }
      }
    }
    render() {
      const SceneComponent = unwrap(sceneThunk);
      return <SceneComponent {...this.props} />;
    }
  }

  WrappedScene.propTypes = {
    nativeNavigationScreenInstanceId: PropTypes.string,
  };

  WrappedScene.childContextTypes = {
    nativeNavigationScreenInstanceId: PropTypes.string,
    airbnbNavigationBarStyle: PropTypes.object,
  };

  return WrappedScene;
}

const NavigatorModule = {
  RESULT_CANCELED,
  RESULT_OK,
  BAR_TYPE,
  CLOSE_BEHAVIOR,
  COLOR,
  LEFT_ICON,
  registerScene(sceneName, sceneThunk, options = {}) {
    if (options.backgroundColor) {
      AirbnbNavigator.registerSceneBackgroundColor(
        sceneName,
        processColor(options.backgroundColor)
      );
    }
    if (options.barType) {
      if (Platform.OS === 'android') {
        const theme = themeFromBarStyle({
          barType: options.barType,
          backgroundColor: options.backgroundColor,
        });
        AirbnbNavigator.registerSceneNavigationBarTheme(sceneName, theme);
      }
      if (Platform.OS === 'ios') {
        AirbnbNavigator.registerSceneNavigationBarType(sceneName, options.barType);
      }
    }
    if (options.barColor) {
      AirbnbNavigator.registerSceneNavigationBarColor(
        sceneName,
        options.barColor
      );
    }

    const WrappedScene = wrapScene(sceneName, sceneThunk);
    AppRegistry.registerComponent(sceneName, () => WrappedScene);
    return WrappedScene;
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
