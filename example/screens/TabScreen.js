import React from 'react';
import { TabBar, Tab } from 'native-navigation';

const propTypes = {};
const defaultProps = {};

export default class TabScreen extends React.Component {
  render() {
    return (
      <TabBar
        elevation={20}
      >
        <Tab
          route={'ScreenOne'}
          title="Home"
          props={{ title: 'Home' }}
          image={require('../icons/home.png')}
        />
        <Tab
          route={'ScreenOne'}
          title="Chat"
          props={{ title: 'Chat' }}
          image={require('../icons/chat.png')}
        />
        <Tab
          route={'ScreenOne'}
          title="Data"
          props={{ title: 'Data' }}
          image={require('../icons/backup.png')}
        />
        <Tab
          route={'ScreenOne'}
          title="Settings"
          props={{ title: 'Settings' }}
          image={require('../icons/settings.png')}
        />
      </TabBar>
    );
  }
}

TabScreen.defaultProps = defaultProps;
TabScreen.propTypes = propTypes;
