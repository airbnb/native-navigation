import React, { Component, PropTypes } from 'react';
import { Dimensions } from 'react-native';

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
  state = {};

  render() {
    return (
      <Screen>
        <Navigator.Config {...this.state} />
        <LoremImage width={width} height={width / 1.6} />
        <Row
          title="Title"
          onPress={() => this.setState({
            title: 'A title',
            subtitle: undefined,
            rightButtons: undefined,
          })}
        />
        <Row
          title="Title and subtitle"
          onPress={() => this.setState({
            title: 'A title',
            subtitle: 'A subtitle',
            rightButtons: undefined,
          })}
        />
        <Row
          title="Right button with title"
          onPress={() => this.setState({
            title: 'A title',
            subtitle: undefined,
            rightButtons: [{ title: 'Hello' }],
          })}
        />
        <Row
          title="Right button with system item"
          onPress={() => this.setState({
            title: 'A title',
            subtitle: undefined,
            rightButtons: [{ systemItem: 'add' }],
          })}
        />
        <Row
          title="Several right buttons"
          onPress={() => this.setState({
            title: 'A title',
            subtitle: undefined,
            rightButtons: [{ systemItem: 'add' }, { systemItem: 'edit' }],
          })}
        />
      </Screen>
    );
  }
}

NavigationExampleScreen.defaultProps = defaultProps;
NavigationExampleScreen.propTypes = propTypes;
NavigationExampleScreen.contextTypes = contextTypes;
