import React, {
  Component,
  PropTypes,
} from 'react';
import {
  View,
  Text,
  Button,
  ScrollView,
} from 'react-native';

import Navigator from 'native-navigation';

const propTypes = {};
const defaultProps = {};
const contextTypes = {
  nativeNavigationInstanceId: PropTypes.string,
};

export default class NavigationExampleScreen extends Component {
  render() {
    return (
      <Navigator.Config
        title="Title"
        rightTitle="Foo"
        onRightPress={() => console.log('onRightPress')}
        onAppear={() => console.log('onAppear', this.context.nativeNavigationInstanceId)}
        onDisappear={() => console.log('onDisappear', this.context.nativeNavigationInstanceId)}
      >
        <ScrollView>
          <View style={{ paddingTop: 64 }}>
            <Text>Navigation</Text>
          </View>
          <View>
            <Text>
              {this.context.nativeNavigationInstanceId}
            </Text>
            <Button
              title="Present new modal"
              onPress={() => Navigator.present('ScreenOne')}
            />
            <Button
              title="Push new screen"
              onPress={() => Navigator.push('ScreenOne')}
            />
            <Button
              title="Pop"
              onPress={() => Navigator.pop()}
            />
            <Button
              title="Dismiss"
              onPress={() => Navigator.dismiss()}
            />
          </View>
        </ScrollView>
      </Navigator.Config>
    );
  }
}

NavigationExampleScreen.defaultProps = defaultProps;
NavigationExampleScreen.propTypes = propTypes;
NavigationExampleScreen.contextTypes = contextTypes;
