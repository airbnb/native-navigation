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
    resetTo: unresolvedPromise,
    pop: noop,
    dismiss: noop,
    signalFirstRenderComplete: noop,
    setScreenProperties: noop,
    registerScreen: noop,
  },
});

export default NavigatorModule;
