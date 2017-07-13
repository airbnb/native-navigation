import React from 'react';
import PropTypes from 'prop-types';
import {
  DeviceEventEmitter,
  Platform,
} from 'react-native';
import navigatorEmitter from './navigatorEmitter';
import AirbnbNavigator from './NavigatorModule';
import {
  processConfigWatchingForMutations,
} from './utils';

class Config extends React.Component {
  constructor(props, context) {
    super(props, context);
    this.deeSubscriptions = {};
    this.neSubscriptions = {};
    this.handleProps(
      props,
      {},
      context.nativeNavigationInstanceId,
      context.nativeNavigationGetConfig()
    );
  }

  componentWillReceiveProps(nextProps, nextContext) {
    this.handleProps(
      nextProps,
      this.props,
      nextContext.nativeNavigationInstanceId,
      nextContext.nativeNavigationGetConfig()
    );
  }

  componentWillUnmount() {
    Object.keys(this.deeSubscriptions).forEach(key => {
      DeviceEventEmitter.removeSubscription(this.deeSubscriptions[key]);
    });

    Object.keys(this.neSubscriptions).forEach(key => {
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

  handleProps(next, prev, id, screenProps) {
    /* eslint-disable no-param-reassign */
    if (!id || !screenProps) {
      return;
    }
    const flag = { hasMutated: false };
    processConfigWatchingForMutations(
      screenProps,
      prev,
      next,
      flag,
      key => this.setCallbackIfNeeded(key, next, prev, id)
    );

    if (flag.hasMutated) {
      // it is important that we clone `barProps` here so the instance of
      // this `barProps` object we have is not sent across the bridge. We
      // should not mutate objects being sent across the bridge.
      AirbnbNavigator.setScreenProperties({ ...screenProps }, id);
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

  // These are the standard events, but there can be more
  onAppear: PropTypes.func,
  onDisappear: PropTypes.func,
  onEnterTransitionComplete: PropTypes.func,
};

Config.contextTypes = {
  nativeNavigationInstanceId: PropTypes.string,
  nativeNavigationGetConfig: PropTypes.func,
};

module.exports = Config;
