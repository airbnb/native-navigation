import React, {
  Component,
} from 'react';
import Navigator from 'native-navigation';

import ImageRow from '../components/ImageRow';
import Screen from '../components/Screen';

export default class SharedElementFromScreen extends Component {
  render() {
    return (
      <Screen>
        {Array.from({ length: 8 }).map((_, id) => (
          <Navigator.SharedElementGroup
            key={id}
            id={id}
            style={{ overflow: 'hidden' }}
          >
            <ImageRow
              id={id}
              onPress={() => Navigator.push('SharedElementToScreen', { id }, {
                transitionGroup: `${id}`,
              })}
            />
          </Navigator.SharedElementGroup>
        ))}
      </Screen>
    );
  }
}
