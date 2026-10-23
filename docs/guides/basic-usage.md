# Basic Usage

Native Navigation hosts each React component as a native navigation screen. Complete the [installation guide](/docs/installation.md) first; the native project must be configured as well as the JavaScript bundle. In particular, use the same screen name for the initial native screen and its JavaScript registration.

## Register screens

Register screens from the JavaScript entry point. `registerScreen` takes a stable name and a function that returns the screen module:

```js
// index.js
import Navigator from 'native-navigation';

Navigator.registerScreen('Home', () => require('./screens/HomeScreen'));
Navigator.registerScreen('Details', () => require('./screens/DetailsScreen'));
```

Using a function around `require` lets the library load screen modules lazily. See the [`registerScreen` reference](/docs/api/navigator/registerScreen.md) and [project structure guide](/docs/guides/project-structure.md) for more detail.

## Push and pop screens

A screen can navigate to another registered screen with `Navigator.push`. Props in the second argument are passed to the destination component:

```jsx
// screens/HomeScreen.js
import React, { Component } from 'react';
import { Button, View } from 'react-native';
import Navigator from 'native-navigation';

export default class HomeScreen extends Component {
  render() {
    return (
      <View>
        <Navigator.Config title="Home" />
        <Button
          title="Open details"
          onPress={() => Navigator.push('Details', { itemId: 'example' })}
        />
      </View>
    );
  }
}
```

The destination receives `itemId` as a prop. Use `Navigator.pop()` to return to the previous screen:

```js
Navigator.pop();
```

`Navigator.Config` can set navigation-bar and screen properties from a screen component; see the [`Config` reference](/docs/api/navigator-config.md).

## Present a screen modally

Use `Navigator.present` to present a registered screen, and call `Navigator.dismiss` from that screen to close it:

```js
Navigator.present('Details', { itemId: 'example' });
```

```js
Navigator.dismiss();
```

See the [`push`](/docs/api/navigator/push.md), [`pop`](/docs/api/navigator/pop.md), [`present`](/docs/api/navigator/present.md), and [`dismiss`](/docs/api/navigator/dismiss.md) API references for options and return values. Platform setup differs; consult the [platform differences guide](/docs/guides/platform-differences.md) when integrating with iOS and Android.
