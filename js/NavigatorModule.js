import SafeModule from 'react-native-safe-module';

const noop = () => {};
const unresolvedPromise = () => new Promise(() => {});

const NativeNavigatorModule = SafeModule.create({
  moduleName: 'NativeNavigatorModule',
  mock: {
    push: unresolvedPromise,
    pushNative: unresolvedPromise,
    present: unresolvedPromise,
    presentNative: unresolvedPromise,
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
  },
});

module.exports = NativeNavigatorModule;
