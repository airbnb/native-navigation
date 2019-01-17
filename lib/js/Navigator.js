import React from 'react';
import PropTypes from 'prop-types';
import {
  AppRegistry,
  Platform,
} from 'react-native';
import AirbnbNavigator from './NavigatorModule';
import navigatorEmitter from './navigatorEmitter';
import { processConfig } from './utils';

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


function wrapScreen(sceneName, sceneThunk) {
  class WrappedScreen extends React.Component {
    constructor(props) {
      super(props);
      // this object will get mutated over the lifetime of this screen
      // and collect all of the screen config
      this.nativeNavigationConfig = {};
    }
    getChildContext() {
      return {
        nativeNavigationSceneName: sceneName,
        nativeNavigationInstanceId: this.props.nativeNavigationInstanceId,
        nativeNavigationInitialBarHeight: this.props.nativeNavigationInitialBarHeight,
        nativeNavigationGetConfig: () => this.nativeNavigationConfig,
      };
    }
    componentDidMount() {
      navigatorEmitter.emit(`sceneDidMount.${this.props.nativeNavigationInstanceId}`);
      setTimeout(
        () => AirbnbNavigator.signalFirstRenderComplete(this.props.nativeNavigationInstanceId),
        0
      );
    }
    render() {
      const ScreenComponent = unwrap(sceneThunk);
      return <ScreenComponent {...this.props} />;
    }
  }

  WrappedScreen.displayName = `Scene(${sceneName})`;

  WrappedScreen.propTypes = {
    nativeNavigationInstanceId: PropTypes.string,
    nativeNavigationInitialBarHeight: PropTypes.number,
  };

  WrappedScreen.childContextTypes = {
    nativeNavigationSceneName: PropTypes.string,
    nativeNavigationInstanceId: PropTypes.string,
    nativeNavigationInitialBarHeight: PropTypes.number,
    nativeNavigationGetConfig: PropTypes.func,
  };

  return WrappedScreen;
}

const registeredScreens = {};

const NavigatorModule = {
  RESULT_CANCELED,
  RESULT_OK,
  isScreenRegistered(screenName) { return !!registeredScreens[screenName] },
  registerScreen(screenName, sceneThunk, options = {}) {
    const waitForRender = !!options.waitForRender;
    const mode = options.mode || 'screen';
    const initialConfig = options.initialConfig || null;

    AirbnbNavigator.registerScreen(
      screenName,
      initialConfig ? processConfig(initialConfig) : null,
      waitForRender,
      mode
    );
    registeredScreens[screenName] = true;

    const WrappedScreen = wrapScreen(screenName, sceneThunk);
    AppRegistry.registerComponent(screenName, () => {
      // execute sceneThunk here immediately. This is important so we can invoke the
      // registered "thunk" (which is the function this comment is contained in), and
      // ensure that the underlying sceneThunk is also invoked, potentially requiring
      // some JS files that haven't been required yet. This allows us to "warm" the
      // module cache before a screen is rendered.
      sceneThunk();
      return WrappedScreen;
    });
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
  resetTo(screenName, props = null, options = {}) {
    if (AppRegistry.getAppKeys().indexOf(screenName) !== -1) {
      return wrapResult(AirbnbNavigator.resetTo(screenName, props, options));
    }
  },
  showModal(screenName, props = null, options = {}) {
    if (AppRegistry.getAppKeys().indexOf(screenName) !== -1) {
      const showModal = Platform.OS === 'android' ? AirbnbNavigator.showModal : AirbnbNavigator.present;
      return wrapResult(showModal(screenName, props, options));
    }
  },
  pop(payload = null, animated = true) {
    AirbnbNavigator.pop(payload, animated);
  },
  dismiss(payload = null, animated = true) {
    AirbnbNavigator.dismiss(payload, animated);
  },
};

module.exports = NavigatorModule;
