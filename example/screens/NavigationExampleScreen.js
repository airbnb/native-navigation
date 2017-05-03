import React, {
  Component,
} from 'react';
import PropTypes from 'prop-types';
import {
  Dimensions,
} from 'react-native';

import Navigator from 'native-navigation';

import LoremImage from '../components/LoremImage';
import Screen from '../components/Screen';
import Row from '../components/Row';

const propTypes = {};
const defaultProps = {};
const contextTypes = {
  nativeNavigationInstanceId: PropTypes.string,
};

const { width } = Dimensions.get('window');

export default class NavigationExampleScreen extends Component {
  render() {
    return (
      <Screen>
        <LoremImage
          width={width}
          height={width / 1.6}
        />
        <Row
          title="Screen Id"
          subtitle={this.context.nativeNavigationInstanceId}
        />
        <Row
          title="Present new modal"
          onPress={() => Navigator.present('ScreenOne')}
        />
        <Row
          title="Push new screen"
          onPress={() => Navigator.push('ScreenOne')}
        />
        <Row
          title="Pop"
          onPress={() => Navigator.pop()}
        />
        <Row
          title="Dismiss"
          onPress={() => Navigator.dismiss()}
        />
        <Row
          title="Shared elements"
          onPress={() => Navigator.push('SharedElementFromScreen')}
        />
        <Row
          title="Navigation bar customisation"
          onPress={() => Navigator.push('NavigationBar')}
        />
      </Screen>
    );
  }
}

NavigationExampleScreen.defaultProps = defaultProps;
NavigationExampleScreen.propTypes = propTypes;
NavigationExampleScreen.contextTypes = contextTypes;
