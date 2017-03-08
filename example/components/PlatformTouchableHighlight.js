/* eslint react/prop-types: 0 */
import React from 'react';
import {
  ColorPropType,
  TouchableHighlight,
} from 'react-native';
import {
  createPlatformTouchableComponent,
  touchablePropTypes,
} from '../util/platformTouchable';

const propTypes = {
  ...touchablePropTypes,
  color: ColorPropType,
};

export default createPlatformTouchableComponent({
  displayName: 'PlatformTouchableHighlight',
  renderDefaultTouchable(props, color) {
    return (
      <TouchableHighlight
        disabled={props.disabled}
        onPress={props.onPress}
        style={{ overflow: 'hidden' }}
        underlayColor={color}
      >
        {props.children}
      </TouchableHighlight>
    );
  },
  propTypes,
});
