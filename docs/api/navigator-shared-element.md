# `<Navigator.SharedElement />`


## Props

#### `type: string`

#### `typeId: number | string`

#### `subType: string`

#### `subTypeId: number | string`


## Example Usage

```jsx
import React from 'react';
import { Image } from 'react-native';
import { SharedElement } from 'native-navigation';

export default class UserImage extends React.Component {
  render() {
    const { user } = this.props;
    return (
      <SharedElement
        type="user-image"
        typeId={user.id}
        style={sizeStyle}
      >
        <Image
          style={[styles.image, sizeStyle]}
          source={{ uri: user.profile_image }}
        />
      </SharedElement>
    );
  }
}
```


## Related Guides

- [Shared Element Transitions](/docs/guides/shared-element-transitions.md)


## Related pages

- [`SharedElementGroup` API Reference](/docs/api/navigator-shared-element-group.md)
