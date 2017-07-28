import React from 'react';
import PropTypes from 'prop-types';
import SafeModule from 'react-native-safe-module';

const NativeSharedElement = SafeModule.component({
  viewName: 'NativeNavigationSharedElementGroup',
  mockComponent: ({ children }) => children,
  propTypes: {
    id: PropTypes.string,
    nativeNavigationInstanceId: PropTypes.string,
  },
});

const propTypes = {
  id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  children: PropTypes.node.isRequired,
};

const contextTypes = {
  nativeNavigationInstanceId: PropTypes.string,
};

class SharedElement extends React.Component {
  render() {
    return (
      <NativeSharedElement
        id={`${this.props.id}`}
        nativeNavigationInstanceId={this.context.nativeNavigationInstanceId}
      >
        {this.props.children}
      </NativeSharedElement>
    );
  }
}

SharedElement.propTypes = propTypes;
SharedElement.contextTypes = contextTypes;

module.exports = SharedElement;
