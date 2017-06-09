import React from 'react';
import PropTypes from 'prop-types';
import {
  Image,
  StyleSheet,
} from 'react-native';
import murmurHash from 'murmur2js';
import { SharedElement } from 'native-navigation';

import theme from '../util/theme';

const propTypes = {
  width: PropTypes.number.isRequired,
  height: PropTypes.number.isRequired,
  urlWidth: PropTypes.number,
  urlHeight: PropTypes.number,
  id: PropTypes.number,
};

const defaultProps = {
  urlWidth: 0,
  urlheight: 0,
  id: null,
};

const contextTypes = {
  nativeNavigationInstanceId: PropTypes.string,
};

const CARDINALITY = 30;

// const getImage = (w, h, id) => `https://unsplash.it/${w}/${h}?image=${id}`;
// const getImage = (w, h, id) => `http://lorempixel.com/${w}/${h}/abstract/${id}/`;
const getImage = (w, h, id) => `https://placem.at/places?w=${w}&h=${h}&random=${id}&txt=0`;

export default class LoremImage extends React.PureComponent {
  render() {
    const { id, width, height, urlWidth, urlHeight } = this.props;
    const w = Math.round(urlWidth || width);
    const h = Math.round(urlHeight || height);
    const { nativeNavigationInstanceId } = this.context;
    const image = id == null ? murmurHash(nativeNavigationInstanceId) % CARDINALITY : id;
    const uri = getImage(w, h, image);
    const sizeStyle = { width: Math.round(width), height: Math.round(height) };
    return (
      <SharedElement
        type="lorem-image"
        typeId={image}
        style={sizeStyle}
      >
        <Image
          style={[styles.image, sizeStyle]}
          source={{
            uri,
          }}
        />
      </SharedElement>
    );
  }
}

LoremImage.defaultProps = defaultProps;
LoremImage.propTypes = propTypes;
LoremImage.contextTypes = contextTypes;

const styles = StyleSheet.create({
  image: {
    backgroundColor: theme.color.image,
  },
});
