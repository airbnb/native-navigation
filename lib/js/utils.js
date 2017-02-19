import {
  processColor,
} from 'react-native';

function isEventKey(key) {
  if (key.substr(0, 2) !== 'on') return false; // doesn't start with 'on';
  const c = key[2];
  if (c === undefined || !isNaN(c * 1)) return false; // 3rd char is numeric
  return c === c.toUpperCase(); // only event if 3rd char is uppercase letter
}

const IS_COLOR_REGEX = /color$/i;

function isColorKey(key) {
  return IS_COLOR_REGEX.test(key);
}

function processConfig(config) {
  const obj = {};
  Object.keys(config).forEach(key => {
    obj[key] = isColorKey(key) ? processColor(config[key]) : config[key];
  });
  return obj;
}

module.exports = {
  isEventKey,
  isColorKey,
  processConfig,
};
