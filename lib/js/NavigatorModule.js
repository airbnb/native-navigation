import SafeModule from 'react-native-safe-module';

const noop = () => {};
const unresolvedPromise = () => new Promise(() => {});

const NavigatorModule = SafeModule.module({
  moduleName: 'NativeNavigationModule',
  mock: {
    push: unresolvedPromise,
    pushNative: unresolvedPromise,
    present: unresolvedPromise,
    presentNative: unresolvedPromise,
    replace: unresolvedPromise,
    pop: noop,
    dismiss: noop,
    setTitle: noop,
    setBackgroundColor: noop,
    setLink: noop,
    setButtons: noop,
    setLeadingButtonVisible: noop,
    setCloseBehavior: noop,
    setLeftIcon: noop,
    setBarType: noop,
    setTheme: noop,
    setFoldOffset: noop,
    setSnapToFoldOffset: noop,
    setShowTabBar: noop,
    setHideStatusBarUntilFoldOffset: noop,
    signalFirstRenderComplete: noop,
    registerSceneBackgroundColor: noop,
    registerSceneNavigationBarTheme: noop,
    registerSceneNavigationBarColor: noop,
    registerSceneNavigationBarType: noop,
  },
});

export default NavigatorModule;
