import React, { PropTypes } from 'react';
import {
  View,
  StyleSheet,
} from 'react-native';

import PlatformTouchableHighlight from './PlatformTouchableHighlight';
import theme from '../util/theme';

const propTypes = {
  children: PropTypes.node,
  onPress: PropTypes.func,
};

const defaultProps = {
};

export default class BaseRow extends React.Component {
  render() {
    const {
      children,
      onPress,
    } = this.props;

    const content = (
      <View style={styles.container}>
        <View style={styles.row}>
          {children}
        </View>
      </View>
    );

    if (onPress) {
      return (
        <PlatformTouchableHighlight
          onPress={this.props.onPress}
          color="#dedede"
        >
          {content}
        </PlatformTouchableHighlight>
      );
    }
    return content;
  }
}

BaseRow.defaultProps = defaultProps;
BaseRow.propTypes = propTypes;

const styles = StyleSheet.create({
  container: {
    overflow: 'hidden',
    paddingHorizontal: theme.size.horizontalPadding,
  },
  row: {
    paddingVertical: theme.size.verticalPadding,
    borderBottomColor: '#dedede',
    borderBottomWidth: 1,
  },
});
