import Navigator from 'native-navigation';
import {AppRegistry} from 'react-native';

AppRegistry.registerComponent(
  'ACustomTitleView',
  () => require('./components/ACustomTitleView').default
);

Navigator.registerScreen(
  'SharedElementToScreen',
  () => require('./screens/SharedElementToScreen')
);
Navigator.registerScreen(
  'SharedElementFromScreen',
  () => require('./screens/SharedElementFromScreen')
);
Navigator.registerScreen(
  'ScreenOne',
  () => require('./screens/NavigationExampleScreen'),
  {
    initialConfig: {
      // title: 'FooBar',
    },
  }
);
Navigator.registerScreen(
  'TabScreen',
  () => require('./screens/TabScreen'),
  {
    mode: 'tabs',
  }
);
