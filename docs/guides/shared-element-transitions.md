# Shared Element Transitions


## Top Level

The core idea behind the shared element transition API is that any screen can define some number of views that are 
`SharedElementGroup`s and each `SharedElementGroup` can have some number of views that are `SharedElement`s inside of it.

Each `SharedElementGroup` has an identifier (a string) which uniquely identifies it on the screen.

Each `SharedElement` has an identifier (a string) which uniquely identifies it inside of the `SharedElementGroup`

Also, `SharedElement`s need not live inside of a `SharedElementGroup`, but then they can only be used in the `to` portion 
of the transition, and not the `from` part.

If a `transitionGroup` is specified as an option in the navigation call (ie, `Navigator.push`), then a shared element
transition will be attempted with the provided `id` of the `SharedElementGroup` on the screen that you are navigating away 
from.



## Example

```jsx
import React from 'react';
import { View, Image } from 'react-native';
import Navigator, { SharedElement, SharedElementGroup } from 'native-navigation';

const onPress = id => () => 
 Navigator.push('ToScreen', { id }, {
  transitionGroup: id,
 });

class FromScreen extends React.Component {
 render() {
  const { posters } = this.props;
  return (
   <View>
    {posters.map(poster => (
     <SharedElementGroup id={poster.id}>
      <Touchable onPress={onPress(poster.id)}>
       <SharedElement
        type="poster"
        typeId={poster.id}
       >
        <Image source={{ uri: poster.url }} />
       </SharedElement>
      </Touchable>
     </SharedElementGroup>
    ))}
   </View>
  );
 }
}
```

```jsx
import React from 'react';
import { View, Image } from 'react-native';
import Navigator, { SharedElement, SharedElementGroup } from 'native-navigation';

class ToScreen extends React.Component {
 render() {
  return (
   <View>
    {/* ... */}
    <SharedElement
     type="poster"
     typeId={poster.id}
    >
     <Image source={{ uri: poster.url }} />
    </SharedElement>
    {/* ... */}
   </View>
  );
 }
}
```


## Shared Element Identifiers

_This section has not yet been filled out_


## How it works (iOS)

The animation is executed by creating a number of snapshots (using [snapshotViewAfterScreenUpdates](https://developer.apple.com/reference/uikit/uiview/1622531-snapshotviewafterscreenupdates?language=objc) ) and animating them inside of an animation container
using a [UIViewControllerTransitioningDelegate](https://developer.apple.com/library/ios/documentation/UIKit/Reference/UIViewControllerTransitioningDelegate_protocol/index.html#//apple_ref/occ/intf/UIViewControllerTransitioningDelegate)

The snapshot itself is several steps, as outlined below. Provided you have the following three inputs:

1. `fromViewController`: The view controller you are navigating away from
2. `toViewController`: The view controller you are navigating to
3. `transitionGroup`: the id of the transition group in the `fromViewController` that is going to be involved in the transition.

With those parameters, we do the following:

1. Snapshot `fromViewController` and put the snapshot on top of the view hierarchy to cover the screen
2. In `fromViewController` collect snapshots of every `SharedElement` inside of the `SharedElementGroup` with id matching `transitionGroup`.
3. Snapshot the entire `fromViewController` view hierarchy with each `SharedElement` from #2 having `alpha` set to 0.
4. In `toViewController` collect snapshots of every `SharedElement`.
5. Snapshot the entire `toViewController` view hierarchy with each `SharedElement` from #4 having `alpha` set to 0.
6. Find all of the matching `SharedElements` in `toViewController` and `fromViewController`, matching by `typeId` etc.
7. Remove snapshot from #1. Insert snapshots into the animation container in the following order:
  - full screen snapshot of `fromViewController` minus shared elements 
  - all shared elements from `fromViewController` without a match
  - full screen snapshot of `toViewController` minus shared elements (set alpha to 0)
  - all shared elements from `toViewController` without a match (set alpha to 0)
  - all shared elements from `fromViewController` with matches in `toViewController`
8. Execute a `UIView.animateWithDuration(...)` and set the following things in the animation:
  - full screen snapshot of `toViewController` minus shared elements (set alpha to 1)
  - all shared elements from `toViewController` without a match (set alpha to 1)
  - all shared elements from `fromViewController` with matches in `toViewController`:
    - set `.center` to the `.center` of the corresponding matched element
    - set `.transform` to be an XY scale transform that matches the ratio of the widths/heights of the element relative to its matched pair.
9. On completion of animation, remove the animation container completely.



## How it works (Android)

_This section has not yet been filled out_


## Common Issues

**White screen on iPhone 7 Simulators**:

Shared Element Transitions use custom transition coordinators and view snapshotting on iOS. There is 
a [known issue](https://forums.developer.apple.com/thread/63438) with these on iPhone 7 Simulators. 
In these simulators, the whole screen will appear white and not work. This affects simulators only 
and not real devices.


## Related Pages

- [`SharedElement` API Reference](/docs/api/navigator-shared-element.md)
- [`SharedElementGroup` API Reference](/docs/api/navigator-shared-element-group.md)
