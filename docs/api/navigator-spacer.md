# `<Navigator.Spacer />`

The `Spacer` component is intended to be used in order to properly pad content to never be rendered
under the navigation bar.

This component automatically sizes itself and changes height when the navigation bar changes height.


## Props

#### `animated: boolean`

Whether or not you want this view's height to animate when the size of the navigation bar changes.

Default is `false`.


## Example Usage

```jsx
import React from 'react';
import { ScrollView, View, Text } from 'react-native';
import { Spacer } from 'native-navigation';

export default class Screen extends React.Component {
  render() {
    return (
      <ScrollView>
      
        {/* This view will be the exact height of the Navigation Bar */}
        <Spacer animated />
        
        {/* This content will be rendered below the navigation bar */}
        <View>
          <Text>Hello World</Text>
        </View>
      </ScrollView>
    )
  }
}
```
