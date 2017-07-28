import Navigator from 'native-navigation';

Navigator.registerScreen(
  'SharedElementToScreen',
  () => require('./screens/SharedElementToScreen')
);
Navigator.registerScreen(
  'SharedElementFromScreen',
  () => require('./screens/SharedElementFromScreen')
);
Navigator.registerScreen('NavigationBar', () =>
  require('./screens/NavigationBar')
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
