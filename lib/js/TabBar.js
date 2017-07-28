import React from 'react';
import PropTypes from 'prop-types';
import {
  View,
} from 'react-native';
import SafeModule from 'react-native-safe-module';
import { processConfig } from './utils';

const NativeTabBar = SafeModule.component({
  viewName: 'NativeNavigationTabBarView',
  mockComponent: () => <View />,
});

class TabBar extends React.Component {
  render() {
    const { children, ...config } = this.props;
    // TODO(lmr): handle event registration with config as well...
    return (
      <NativeTabBar
        config={processConfig(config)}
      >
        {children}
      </NativeTabBar>
    );
  }
}

TabBar.propTypes = {
  children: PropTypes.node,
};

module.exports = TabBar;
