import React, { PropTypes } from 'react';
import {
  DeviceEventEmitter,
  Platform,
  processColor,
} from 'react-native';
import navigatorEmitter from './navigatorEmitter';
import AirbnbNavigator from './NavigatorModule';
import {
  isEventKey,
  isColorKey,
} from './utils';
import shallowEquals from './shallowEquals';

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
            if (Array.isArray(next[key])) {
              // if the prop is an array, perform a quick shallow compare, which might
              // prevent us from thinking the config has mutated on every render
              if (!shallowEquals(next[key], prev[key], shallowEquals)) {
                barProps[key] = next[key];
                hasMutated = true;
              }
            } else if (prev[key] !== next[key]) {
              barProps[key] = isColorKey(key) ? processColor(next[key]) : next[key];
              hasMutated = true;
            }
            break;
        }
      }
    });

    // we need to also cycle through keys that were there previously but might not be
    // anymore and treat that as a removal
    Object.keys(prev).forEach(key => {
      if (key in prev && !(key in next)) {
        if (isEventKey(key)) {
          this.setCallbackIfNeeded(key, next, prev, id);
        } else {
          switch (key) {
            case 'children':
              // these properties are not part of the arbitrary style attributes
              break;
            default:
              // remove from barProps
              barProps[key] = undefined;
              hasMutated = true;
              break;
          }
        }
      }
    });

    if (hasMutated) {
      // it is important that we clone `barProps` here so the instance of
      // this `barProps` object we have is not sent across the bridge. We
      // should not mutate objects being sent across the bridge.
      AirbnbNavigator.setNavigationBarProperties({ ...barProps }, id);
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
