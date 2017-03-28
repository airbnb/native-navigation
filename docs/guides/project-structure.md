# Project Structure


## Directory Structure

Native Navigation might make you end up structuring your app a little bit differently than 
how the React Native project encourages you to do.

With Native Navigation, "Screens" are the top level react components of your app, and we have to
register each of them individually. We have found that this leads to a natural way of splitting up
your code base, and also has some performance advantages by preventing all of your code from getting
executed at app start.

The typical project structure we recommend is something like this:

```
root/
├── components/
├── screens/
│   ├── FooScreen.js
│   └── BarScreen.js
├── routes.js
└── index.js
```

In this case, "Screens" are separated from "Components", and there is a top level `index.js` and `routes.js`
file.

Our `routes.js` file might look something like this:

```js
export const FOO = 'MyApp/Foo';
export const BAR = 'MyApp/Bar';
```

`routes.js` is just a set of constants that correspond to screens. You can think of them like URLs
for different pages of a website.

Our `index.js` file might look something like this:

```js
import Navigator from 'native-navigation';
import { FOO, BAR } from './routes';

Navigator.registerScreen(FOO, () => require('./screens/FooScreen'));
Navigator.registerScreen(BAR, () => require('./screens/BarScreen'));

```

The `index.js` file is the index file that you should have as the entry point to your JavaScript 
bundle that the React Native bridge uses.


## Integrating with Redux

There is nothing special that one needs to do to integrate redux into an app using Native Navigation,
however, since NativeNavigation results in splitting up every screen of your app into separate root
views and registering each separately, there are ways to reduce the boilerplate of redux integration.
 
One way we recommend doing this is by registering all of your screens with a custom `registerScreen`
function, rather than calling `Navigator.registerScreen` directly. This allows you to add custom 
options to the call that are specific to your app or internal infrastructure.

Redux is a good simple example of this, where we may want to add a `getStore` option to the
`registerScreen` method that automatically registers the screen with the redux store provided in
context.

One could accomplish this like:

```js
import React from 'react';
import { Provider } from 'react-redux';
import Navigator from 'native-navigation';

// This ensures that we can use `() => require('./foo')` as a `getScreen` or `getStore` option,
// even if `foo.js` is an ES6 module.  It's not really important to this example.
function unwrapDefaultExport(module) {
  if (module != null && module.__esModule === true) {
    return module.default;
  }
  return module;
}

// provided a `getScreen` function and a `getStore` function, we want to return a new function
// that returns a new react component that renders the old one, but with the redux store provided
// through context.
function wrapScreenGetter(route, getScreen, { getStore }) {

  class ConnectedScreen extends React.Component {
    render() {
      const Screen = unwrapDefaultExport(getScreen());
      const store = unwrapDefaultExport(getStore());
      return (
        <Provider store={store}>
          <Screen {...this.props} />
        </Provider>
      );
    }
  }
  
  // customize the display name for better React dev tools interop.
  ConnectedScreen.displayName = `ConnectedScreen(${route})`;

  return () => {
    // invoking this here ensures that our original `getScreen` function gets called, which we want
    // to happen to make sure that `Navigator.preload` still does meaningful work.
    getScreen();
    return ConnectedScreen;
  };
}

export default function registerConnectedScreen(route, getScreen, options = {}) {
  const getWrapper = wrapScreenGetter(route, getScreen, options);
  return Navigator.registerScreen(route, getWrapper, options);
}
```

Then you would register screens like in the following example:

```js
// index.js
import registerConnectedScreen from './utils/registerConnectedScreen';

registerConnectedScreen(
  'SomeScreen',
  () => require('./screens/SomeScreen'),
  {
    getStore: () => require('./path/to/redux/store'),
  },
);
```


## Related Guides

- [Integrating with Existing Apps](/docs/guides/integrating-with-existing-apps.md)
- [Basic Usage](/docs/guides/basic-usage.md)
