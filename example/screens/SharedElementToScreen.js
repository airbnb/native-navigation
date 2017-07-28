import React, {
  Component,
} from 'react';
import PropTypes from 'prop-types';
import {
  View,
  Dimensions,
} from 'react-native';

import LoremImage from '../components/LoremImage';
import LoremHeader from '../components/LoremHeader';
import LoremParagraph from '../components/LoremParagraph';
import Screen from '../components/Screen';
import theme from '../util/theme';

const propTypes = {
  id: PropTypes.number.isRequired,
};
const defaultProps = {};

const { width } = Dimensions.get('window');

export default class SharedElementToScreen extends Component {
  render() {
    const { id } = this.props;
    return (
      <Screen>
        <LoremImage
          id={id}
          width={width}
          height={width * theme.aspectRatio}
        />
        <View
          style={{
            overflow: 'hidden',
            paddingHorizontal: theme.size.horizontalPadding,
            marginTop: theme.size.verticalPadding,
            marginBottom: theme.size.verticalPadding,
          }}
        >
          <LoremHeader id={id} />
        </View>
        {Array.from({ length: 8 }).map((_, i) => (
          <View
            key={id + i}
            style={{
              overflow: 'hidden',
              paddingHorizontal: theme.size.horizontalPadding,
              marginBottom: theme.size.verticalPadding,
            }}
          >
            <LoremParagraph
              id={id + i}
            />
          </View>
        ))}
      </Screen>
    );
  }
}

SharedElementToScreen.defaultProps = defaultProps;
SharedElementToScreen.propTypes = propTypes;
