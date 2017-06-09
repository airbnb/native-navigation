import React from 'react';
import PropTypes from 'prop-types';
import {
  TouchableNativeFeedback,
  View,
  Platform,
} from 'react-native';

export const touchablePropTypes = {
  children: PropTypes.element.isRequired,
  disabled: PropTypes.bool,
  onPress: PropTypes.func,
};

export const touchableDefaultProps = {
  disabled: false,
};

const supportsRipple = Platform.select({
  android: () => Platform.Version >= 21,
  ios: () => false,
});

export function createPlatformTouchableComponent(options) {
  class PlatformTouchable extends React.Component {
    renderNativeFeedback(color) {
      let content = this.props.children;
      if (this.props.children.type !== View) {
        content = <View>{this.props.children}</View>;
      }
      return (
        <TouchableNativeFeedback
          // eslint-disable-next-line new-cap
          background={TouchableNativeFeedback.Ripple(color, false)}
          disabled={this.props.disabled}
          onPress={this.props.onPress}
        >
          {content}
        </TouchableNativeFeedback>
      );
    }

    render() {
      const { color } = this.props;
      if (supportsRipple()) {
        return this.renderNativeFeedback(color);
      }
      return options.renderDefaultTouchable(this.props, color);
    }
  }

  PlatformTouchable.displayName = options.displayName;
  PlatformTouchable.propTypes = options.propTypes;
  PlatformTouchable.defaultProps = options.defaultProps || touchableDefaultProps;

  return PlatformTouchable;
}
