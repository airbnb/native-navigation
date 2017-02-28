import React, {
  Component,
  PropTypes,
} from 'react';
import {
  View,
  Text,
  Button,
  ScrollView,
  Platform,
} from 'react-native';

import Navigator from 'native-navigation';

const propTypes = {};
const defaultProps = {};

export default class TabScreen extends React.Component {
  render() {
    console.log('rendering MainTabScene');
    return (
      <Navigator.TabBar
        barTintColor="red"
        backgroundColor="red"
        elevation={100}
      >
        <Navigator.Tab
          title="Foo"
          image={{
            uri: Platform.select({
              ios: 'NavBarButtonPlus',
              android: 'ic_menu_black_24dp',
            }),
            width: 51,
            height: 51,
          }}

          route={'ScreenOne'}
          props={{ foo: 'bar' }}
        />
        <Navigator.Tab title="Bar" route={'ScreenOne'} props={{ foo: 'bar' }} />
        <Navigator.Tab title="Bam" route={'ScreenOne'} props={{ foo: 'bar' }} />
        <Navigator.Tab title="Baz" route={'ScreenOne'} props={{ foo: 'bar' }} />
      </Navigator.TabBar>
    );
  }
}

TabScreen.defaultProps = defaultProps;
TabScreen.propTypes = propTypes;
