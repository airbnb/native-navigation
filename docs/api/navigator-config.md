# `<Navigator.Config />`

The `Config` component is a a component that is used to configure the current screen and navigation
bar. The component itself does not result in any UI being rendered in the current react view hierarchy,
but instead is used as a declarative interface for the screen.

The `Config` component can be rendered at any point in the react view hierarchy, and multiple instances
of the component can be rendered in the same hierarchy, with the props of each getting merged together
with the preference being given to the instances in the order that they mount.

## Usage

```jsx
import React from 'react';
import Navigator from 'native-navigation';

export default class Screen extends React.Component {
  render() {
    const {
      children,
      title,
    } = this.props;

    return (
      <Navigator.Config
        title={title}
        backgroundColor="#F7F7F7"
        elevation={4}
        onBackPress={() => console.log('onBackPress')}
        onLeftPress={() => console.log('onLeftPress')}
        onRightPress={(index) => console.log('onRightPress', index)}
        onAppear={() => console.log('onAppear')}
      >
        {children}
      </Navigator.Config>
    );
  }
}
```


## Props

#### `children: node`

If `children` are passed into the component, the `Config`'s `render` method will return the result of
`React.Children.only(this.props.children)`. Otherwise, `render` will just return `null`.

#### `title: string`

The title of the screen. This shows up in the center of the navigation bar by default.


#### `titleColor: Color`

The text color of the title.


#### `subtitle: string`

The subtitle of the screen. This shows up as a string below the title of the screen.


#### `subtitleColor: Color`

The text color of the subtitle.


#### `alpha: number`

The opacity of the navigation bar. Should be between `0` and `1`.


#### `rightTitle: string`

The 


#### `rightImage: Image`

#### `rightButtons: Array<Button>`

- 


#### `screenColor: Color`

The background color of the entire screen.
If the color is translucent, the old screen will be kept below this one (to use it like a modal, for instance).

Defaults to `#FFFFFF`.



#### `hidden: boolean`

If true, the navigation bar will not be visible.

Defaults to `false`.


#### `backgroundColor: Color` 

#### `foregroundColor: Color`

#### `statusBarHidden: boolean`

If true, the status bar will not be visible.

Defaults to `false`.


#### `statusBarAnimation: 'slide' | 'none' | 'fade'`

Configures how the status bar should animate between configurations.

Defaults to `'fade'`.

#### `statusBarStyle: 'light' | 'default'`





## Events

#### `onAppear: () => void` 

#### `onDisappear: () => void`

#### `onEnterTransitionCompleted: () => void`

#### `onBarHeightChanged: (height: number) => void`

#### `onLeftPress: () => void`

#### `onRightPress: (index: number) => void`





## Example Usage


## Related Guides

- [Shared Element Transitions](/docs/guides/shared-element-transitions.md)


## Types

- [`ScreenConfig`](/docs/types/NavigationResult.md)



# Navigator.Config


## Props

```js

type Image = {
  uri: string;
  width: number;
  height: number;
}

type Button = {
  // shared
  title: string;
  image: Image;
  
  // ios-only-but-should-share
  enabled: boolean;
  tintColor: boolean;
  fontName: string;
  fontSize: number;
  
  // ios-only
  style: 'plain' | 'default';
  systemItem: 'done' | 'cancel' | 'edit' | 'save' | 'add' | 'flexibleSpace' | 'compose' | 'reply' | 'action' | 'organize' | 'bookmarks' | 'search' | 'refresh' | 'stop' | 'camera' | 'trash' | 'play' | 'pause' | 'rewind' | 'fastForward' | 'undo' | 'redo' | 'pageCurl';

  // android-only
};

type NavigatorConfigProps = {

  // shared
  title: string;
  titleColor: Color;
  subtitle: string;
  subtitleColor: Color;
  alpha: number;
  rightTitle: string;
  rightImage: Image;
  rightButtons: Array<Button>;
  screenColor: Color;
  hidden: boolean;
  backgroundColor: Color; 
  foregroundColor: Color;
  statusBarHidden: boolean;
  statusBarAnimation: 'slide' | 'none' | 'fade'; // 'fade' is default
  statusBarStyle: 'light' | 'default';
  
  // ios-only-but-should-share
  backIndicatorImage: Image;
  titleFontName: string;
  titleFontSize: number;
  subtitleFontName: string;
  subtitleFontSize: number;
  
  // android-only-but-should-share
  navIcon: Image;
  logo: Image;
  textAlign: 'left' | 'center' | 'right';
  leftButtons: Array<Button>;
  
  // ios-only
  prompt: string;
  hidesBackButton: boolean;
  hidesBarsOnTap: boolean;
  hidesBarsOnSwipe: boolean;
  hidesBarsWhenKeyboardAppears: boolean;
  isToolbarHidden: boolean;
  backIndicatorTransitionMaskImage: Image;
  translucent: boolean;
  backButtonTitle: string;
  interactivePopGestureEnabled: boolean;

  // android-only
  statusBarColor: Color;
  statusBarTranslucent: boolean;
  windowTitle: string;
  elevation: number;
  overflowIcon: Image;
  displayHomeAsUp: boolean;
  homeButtonEnabled: boolean;
  showHome: boolean;
  showTitle: boolean;
  showCustom: boolean;
  useLogo: boolean;
  useShowHideAnimation: boolean;
  hideOnScroll: boolean;
  hideOffset: number;
}






```




