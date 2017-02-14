import React, { PropTypes } from 'react';
import {
  DeviceEventEmitter,
  Platform,
} from 'react-native';
import navigatorEmitter from './navigatorEmitter';
import AirbnbNavigator from './NavigatorModule';

function isEventKey(key) {
  if (key.substr(0, 2) !== 'on') return false; // doesn't start with 'on';
  const c = key[2];
  if (c === undefined || !isNaN(c * 1)) return false; // 3rd char is numeric
  return c === c.toUpperCase(); // only event if 3rd char is uppercase letter
}

class Config extends React.Component {
  constructor(props, context) {
    super(props, context);
    this.deeSubscriptions = {};
    this.neSubscriptions = {};
    this.handleProps(
      props,
      {},
      context.nativeNavigationInstanceId,
      context.nativeNavigationBarProps
    );
  }

  componentWillReceiveProps(nextProps, nextContext) {
    this.handleProps(
      nextProps,
      this.props,
      nextContext.nativeNavigationInstanceId,
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

  handleProps(next, prev, id, barProps) {
    /* eslint-disable no-param-reassign */
    if (!id || !barProps) {
      return;
    }
    let hasMutated = false;
    Object.keys(next).forEach(key => {
      if (isEventKey(key)) {
        this.setCallbackIfNeeded(key, next, prev, id);
      } else {
        switch (key) {
          case 'children':
            // these properties are not part of the arbitrary style attributes
            break;
          default:
            // add to barProps
            if (prev[key] !== next[key]) {
              barProps[key] = next[key];
              hasMutated = true;
            }
            break;
        }
      }
    });

    if (hasMutated) {
      AirbnbNavigator.setNavigationBarProperties(barProps, id);
    }
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
  nativeNavigationInstanceId: PropTypes.string,
  nativeNavigationBarProps: PropTypes.object,
};

module.exports = Config;
