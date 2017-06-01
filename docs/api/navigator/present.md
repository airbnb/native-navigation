# `present(screenName[, props, options])`

## Arguments

1. `screenName` (`string`): The screen identifier of the screen to be pushed.
2. `props` (`Object`): Props to be passed into the presented screen.
3. `options` (`Object`): Options for the navigation transition:
  - `options.transitionGroup` (`string`): The shared element group ID to use for the shared element 
  transition
  - `options.modalPresentationStyle` (`string`, iOS only): The presentation style to use when presenting
  the view modally. Either `fullScreen` (default), `pageSheet`, `formSheet`, `currentContext`, `custom`,
  `overFullScreen`, `overCurrentContext`, `popover` or `none`.

## Returns

(`Promise<NavigationResult>`): A promise that resolves when the presented screen gets dismissed.

## Example Usage


```js
import React from 'react';

import Navigator from 'native-navigation';

class Foo extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      registered: false,
    };
    this.onPress = this.onPress.bind(this);
  }
  onPress() {
    return Navigator
      .present('Register', { source: 'Foo' })
      .then(({ code }) => this.setState({ registered: code === Navigator.RESULT_OK })); 
  }
  render() {
    return (
      <View>
        {!registered &&  (
          <Button
            title="Register Now"
            onPress={this.onPress} 
          />
        )}
        {!!registered &&  (
          <Button
            title="Continue"
            onPress={this.onPress} 
          />
        )}
      </View>
    );
  }
}


```


## Related Guides

- [Shared Element Transitions](/docs/guides/shared-element-transitions.md)


## Types

- [`NavigationResult`](/docs/types/NavigationResult.md)
