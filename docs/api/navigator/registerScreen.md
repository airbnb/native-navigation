# `registerScreen(screenName, getScreen[, options])`



## Arguments

1. `screenName` (`string`): The screen identifier to be registered with this screen.

2. `getScreen` (`Thunk<Component>`): A function returning the component to use for the screen.

3. `options` (`ScreenOptions` _optional_):

  - `options.waitForRender` (`boolean` _optional_): If `true`, the screen will not get pushed or
   presented until it renders once, allowing for react-rendered views to render and navigation configuration
   to happen if it needs to. Defaults to `true`.
  - `options.initialConfig` (`ScreenConfig` _optional_): Any `ScreenConfig` props to apply initially to the
  screen. Values passed here will get merged with values passed into any `Navigator.Config` in the actual
  rendered screen, with the rendered config values taking precedence. Defaults to `{}`.
  - `options.mode` (`'screen' | 'tabs'`): Defaults to `'screen'`.


## Example Usage

This function is used to expose React components to the native context. Under the hood, it utilizes
[`Application.registerComponent`](http://facebook.github.io/react-native/docs/appregistry.html#registercomponent).

We recommend that you separate screen _registration_ from screen _definition_. As your app grows in
number of screens, it is advantageous to import as few files as possible at app startup time, and
lazily import some files using `require` as they are needed.

For example, here we have our screen component defined and exported in one file:

```js
// ScreenOne.js
import React from 'react';

export default class ScreenOne extends React.Component {
  render() {
    // ...
  }
}
```

And then we have a separate `register.js` file where we register the screen, with the `getScreen`
parameter being an anonymous function returning the result of `require('./ScreenOne')`, which
is referred to a "Thunk", or a "lazy value".

```js
// register.js
import Navigator from 'native-navigation';

Navigator.registerScreen('ScreenOne', () => require('./ScreenOne'));
```

You can also pass some static configuration to the screen, if it is convenient:

```js
// register.js
import Navigator from 'native-navigation';

Navigator.registerScreen('ScreenOne', () => require('./ScreenOne'), {
  waitForRender: false,
  initialConfig: {
    title: 'ScreenOne',
  },
});
```


## Related Guides

- [Project Structure](/docs/guides/project-structure.md)
