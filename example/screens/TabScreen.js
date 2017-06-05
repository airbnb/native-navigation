import React from 'react';
import { View, StyleSheet } from 'react-native';
import { TabBar, Tab } from 'native-navigation';

const propTypes = {};
const defaultProps = {};

export default class TabScreen extends React.Component {
  render() {
    return (
      <View style={styles.container}>
        <TabBar elevation={20}>
          <Tab
            route={'ScreenOne'}
            title="Home"
            image={require('../icons/home.png')}
          />
          <Tab
            route={'ScreenOne'}
            title="Chat"
            image={require('../icons/chat.png')}
          />
          <Tab
            route={'ScreenOne'}
            title="Data"
            image={require('../icons/backup.png')}
          />
          <Tab
            route={'ScreenOne'}
            title="Settings"
            image={require('../icons/settings.png')}
          />
        </TabBar>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

TabScreen.defaultProps = defaultProps;
TabScreen.propTypes = propTypes;
