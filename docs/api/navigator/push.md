# `push(screenName[, props, options])`

## Arguments

1. `screenName` (`string`): The screen identifier of the screen to be pushed.
2. `props` (`Object`): Props to be passed into the pushed screen.
3. `options` (`Object`): Options for the navigation transition:
  - `options.transitionGroup` (`string`): The shared element group ID to use for the shared element
  transition

## Returns

(`Promise<NavigationResult>`): A 

## Example Usage

```js
import Navigator from 'native-navigation';

Navigator.push('ScreenOne', { foo: 'bar' });
```


## Related Guides

- [Shared Element Transitions](/docs/guides/shared-element-transitions.md)


## Types

- [`NavigationResult`](/docs/types/NavigationResult.md)
