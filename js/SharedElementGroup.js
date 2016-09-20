import React, { PropTypes } from 'react';
import { View, requireNativeComponent } from 'react-native';

const propTypes = {
  id: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
};

const contextTypes = {
  sceneInstanceId: PropTypes.string,
};

class SharedElement extends React.Component {
  render() {
    return (
      <NativeNavigatorSharedElementGroup
        id={this.props.id}
        sceneInstanceId={this.context.sceneInstanceId}
      >
        {this.props.children}
      </NativeNavigatorSharedElementGroup>
    );
  }
}

SharedElement.propTypes = propTypes;
SharedElement.contextTypes = contextTypes;

const NativeNavigatorSharedElementGroup = requireNativeComponent('NativeNavigatorSharedElementGroup', {
  name: 'NativeNavigatorSharedElementGroup',
  propTypes: {
    ...View.propTypes,
    id: PropTypes.string,
    sceneInstanceId: PropTypes.string,
  },
});

export default SharedElement;
