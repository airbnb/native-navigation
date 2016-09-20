import React, { PropTypes } from 'react';
import { View, requireNativeComponent } from 'react-native';

const propTypes = {
  type: PropTypes.string.isRequired,
  typeId: PropTypes.number,
  subType: PropTypes.string,
  subTypeId: PropTypes.number,
  children: PropTypes.node.isRequired,
};

const contextTypes = {
  sceneInstanceId: PropTypes.string,
};

class SharedElement extends React.Component {
  getId() {
    const { type, typeId, subType, subTypeId } = this.props;
    return `${type}|${typeId}|${subType}|${subTypeId}`;
  }
  render() {
    return (
      <NativeNavigatorSharedElement
        id={this.getId()}
        sceneInstanceId={this.context.sceneInstanceId}
      >
        {React.Children.only(this.props.children)}
      </NativeNavigatorSharedElement>
    );
  }
}

SharedElement.propTypes = propTypes;
SharedElement.contextTypes = contextTypes;

const NativeNavigatorSharedElement = requireNativeComponent('NativeNavigatorSharedElement', {
  name: 'NativeNavigatorSharedElement',
  propTypes: {
    ...View.propTypes,
    id: PropTypes.string,
    sceneInstanceId: PropTypes.string,
  },
});

export default SharedElement;
