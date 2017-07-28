import React from 'react';
import PropTypes from 'prop-types';
import {
  Text,
  StyleSheet,
} from 'react-native';
import murmurHash from 'murmur2js';

import theme from '../util/theme';
import titleForId from '../util/titleForId';

const propTypes = {
  id: PropTypes.number,
  ...Text.propTypes,
};

const contextTypes = {
  nativeNavigationInstanceId: PropTypes.string,
};

export default class LoremHeader extends React.Component {
  render() {
    const { id, style } = this.props;
    const { nativeNavigationInstanceId } = this.context;
    return (
      <Text {...this.props} style={[styles.header, style]}>
        {titleForId(id || murmurHash(nativeNavigationInstanceId))}
      </Text>
    );
  }
}

LoremHeader.propTypes = propTypes;
LoremHeader.contextTypes = contextTypes;

const styles = StyleSheet.create({
  header: theme.font.title,
});
