import React from 'react';
import PropTypes from 'prop-types';
import {
  DeviceEventEmitter,
  View,
  LayoutAnimation,
} from 'react-native';

class Spacer extends React.Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      height: context.nativeNavigationInitialBarHeight || 0,
    };

    this.subscription = null;
    this.onHeightChanged = this.onHeightChanged.bind(this);
  }

  componentDidMount() {
    const id = this.context.nativeNavigationInstanceId;
    const key = `NativeNavigationScreen.onBarHeightChanged.${id}`;
    this.subscription = DeviceEventEmitter.addListener(key, this.onHeightChanged);
  }

  componentWillUnmount() {
    DeviceEventEmitter.removeSubscription(this.subscription);
  }

  onHeightChanged(height) {
    if (this.props.animated) {
      LayoutAnimation.easeInEaseOut();
    }
    this.setState({ height });
  }

  render() {
    return <View style={{ height: this.state.height }} />;
  }
}

Spacer.propTypes = {
  animated: PropTypes.bool,
};

Spacer.defaultProps = {
  animated: false,
};

Spacer.contextTypes = {
  nativeNavigationInstanceId: PropTypes.string,
  nativeNavigationInitialBarHeight: PropTypes.number,
};

module.exports = Spacer;
