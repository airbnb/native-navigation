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
const contextTypes = {
  nativeNavigationInstanceId: PropTypes.string,
};

export default class NavigationExampleScreen extends Component {
  constructor(props) {
    super(props);
    this.state = {
      buttons: [
        {
          title: 'A',
        },
      ],
    };
  }
  render() {
    const screen = (
      <ScrollView>
        <View style={{ paddingTop: 64 }}>
          <Text>Navigation</Text>
        </View>
        <View>
          <Text>
            {this.context.nativeNavigationInstanceId}
          </Text>
          <View
            style={{ marginBottom: 10 }}
          >
            <Button
              title="Add button"
              onPress={() => this.setState({
                buttons: this.state.buttons.concat([
                  {
                    title: 'B',
                  },
                ]),
              })}
            />
          </View>
          <View
            style={{ marginBottom: 10 }}
          >
            <Button
              title="Present new modal"
              onPress={() => Navigator.present('ScreenOne')}
            />
          </View>
          <View
            style={{ marginBottom: 10 }}
          >
            <Button
              title="Push new screen"
              onPress={() => Navigator.push('ScreenOne')}
            />
          </View>
          <View
            style={{ marginBottom: 10 }}
          >
            <Button
              title="Pop"
              onPress={() => Navigator.pop()}
            />
          </View>
          <View
            style={{ marginBottom: 10 }}
          >
            <Button
              title="Dismiss"
              onPress={() => Navigator.dismiss()}
            />
          </View>
        </View>
      </ScrollView>
    );

    // return screen;

    return (
      <Navigator.Config
        title={this.context.nativeNavigationInstanceId}
        //prompt="subtitle"
        subtitle={this.context.nativeNavigationInstanceId}
        //rightTitle="Foo"
        //statusBarHidden={true}
        //statusBarStyle="light"

        //hidesBackButton={false}
        //isNavigationBarHidden={false}

        //homeButtonEnabled={false}
        //displayHomeAsUp={false}

        //backgroundColor="blue"
        //foregroundColor="white"
        //backgroundColor="red"
        //barTintColor="#ccc"
        rightButtons={this.state.buttons}
        //titleColor="black"
        rightImage={{
          uri: Platform.select({
            ios: 'NavBarButtonPlus',
            android: 'ic_menu_black_24dp',
          }),
          width: 51,
          height: 51,
        }}

        navIcon={{
          uri: Platform.select({
            ios: 'NavBarButtonPlus',
            android: 'ic_menu_black_24dp',
          }),
          rrwidth: 51,
          height: 51,
        }}
        //elevation={30}
        onBackPress={() => console.log('onBackPress')}
        onLeftPress={() => console.log('onLeftPress')}
        onRightPress={(x) => console.log('onRightPress', x)}
        onAppear={() => console.log('onAppear', this.context.nativeNavigationInstanceId)}
        onDisappear={() => console.log('onDisappear', this.context.nativeNavigationInstanceId)}
      >
        {screen}
      </Navigator.Config>
    );
  }
}

NavigationExampleScreen.defaultProps = defaultProps;
NavigationExampleScreen.propTypes = propTypes;
NavigationExampleScreen.contextTypes = contextTypes;
