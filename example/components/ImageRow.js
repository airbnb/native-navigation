import React from 'react';
import PropTypes from 'prop-types';
import {
  View,
  Text,
  StyleSheet,
  Dimensions,
} from 'react-native';

import BaseRow from './BaseRow';
import LoremImage from './LoremImage';
import theme from '../util/theme';
import titleForId from '../util/titleForId';

const { width } = Dimensions.get('window');

const propTypes = {
  id: PropTypes.number.isRequired,
  onPress: PropTypes.func,
};

const defaultProps = {
};

export default class ImageRow extends React.Component {
  render() {
    const {
      id,
      onPress,
    } = this.props;

    const w = width - (2 * theme.size.horizontalPadding);

    return (
      <BaseRow onPress={onPress}>
        <LoremImage
          id={id}
          width={w}
          height={w * theme.aspectRatio}
          urlWidth={width}
          urlHeight={width * theme.aspectRatio}
        />
        <View style={styles.titleContainer}>
          <Text style={styles.title}>{titleForId(id)}</Text>
        </View>
      </BaseRow>
    );
  }
}

ImageRow.defaultProps = defaultProps;
ImageRow.propTypes = propTypes;

const styles = StyleSheet.create({
  titleContainer: {
    marginTop: theme.size.verticalPadding,
  },
  title: theme.font.large,
});
