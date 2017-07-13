import React from 'react';
import PropTypes from 'prop-types';
import SafeModule from 'react-native-safe-module';

const NativeSharedElement = SafeModule.component({
  viewName: 'NativeNavigationSharedElement',
  mockComponent: ({ children }) => children,
  propTypes: {
    id: PropTypes.string,
    nativeNavigationInstanceId: PropTypes.string,
  },
});

const numberOrString = PropTypes.oneOfType([
  PropTypes.number,
  PropTypes.string,
]);

const IdPropTypes = {
  type: PropTypes.string.isRequired,
  typeId: numberOrString,
  subType: PropTypes.string,
  subTypeId: numberOrString,
};

const propTypes = {
  ...IdPropTypes,
  children: PropTypes.node.isRequired,
};

const defaultProps = {
  typeId: '',
  subType: '',
  subTypeId: '',
};

const contextTypes = {
  nativeNavigationInstanceId: PropTypes.string,
};

class SharedElement extends React.Component {
  getId() {
    const { type, typeId, subType, subTypeId } = this.props;
    return `${type}|${typeId}|${subType}|${subTypeId}`;
  }
  render() {
    return (
      <NativeSharedElement
        id={this.getId()}
        nativeNavigationInstanceId={this.context.nativeNavigationInstanceId}
      >
        {React.Children.only(this.props.children)}
      </NativeSharedElement>
    );
  }
}

SharedElement.propTypes = propTypes;
SharedElement.defaultProps = defaultProps;
SharedElement.contextTypes = contextTypes;
SharedElement.IdPropTypes = IdPropTypes;

module.exports = SharedElement;
