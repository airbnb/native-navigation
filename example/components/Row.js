import React from 'react';
import PropTypes from 'prop-types';
import {
  View,
  Text,
  Image,
  StyleSheet,
} from 'react-native';

import BaseRow from './BaseRow';
import theme from '../util/theme';

const propTypes = {
  title: PropTypes.string.isRequired,
  subtitle: PropTypes.string,
  onPress: PropTypes.func,
};

const defaultProps = {
};

export default class Row extends React.Component {
  render() {
    const {
      title,
      subtitle,
      onPress,
    } = this.props;

    return (
      <BaseRow onPress={onPress}>
        <View style={styles.content}>
          <View style={styles.titleContainer}>
            <Text style={styles.title}>
              {title}
            </Text>
            {!!subtitle && (
              <Text style={styles.subtitle}>
                {subtitle}
              </Text>
            )}
          </View>
          {onPress && (
            <Image
              source={require('../icons/chevron_right.png')}
              style={{
                width: 24,
                height: 24,
                opacity: 0.5,
              }}
            />
          )}
        </View>
      </BaseRow>
    );
  }
}

Row.defaultProps = defaultProps;
Row.propTypes = propTypes;

const styles = StyleSheet.create({
  content: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginVertical: 8,
  },
  contentTween: {
    marginVertical: 24,
  },
  loadingContainer: {
    flex: 0,
  },
  titleContainer: {
    flex: 1,
  },
  title: theme.font.large,
  subtitle: {
    ...theme.font.small,
    marginTop: 4,
  },
  icon: {
    marginTop: 0.4 * 8,
  },
});
