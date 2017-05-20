import React from 'react'
import {SegmentedControlIOS, View} from 'react-native'

export default class ACustomTitleView extends React.Component {
  render() {
    return (
      <View style={{width: '100%', alignItems: 'center', justifyContent: 'center', height: '100%', }}>
        <View style={{width: 100, height: 30}}>
          <SegmentedControlIOS
            values={['One', 'Two']}
          />
        </View>
      </View>
    );
  }
}
