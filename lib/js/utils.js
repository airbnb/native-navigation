import {
  processColor,
  Image,
} from 'react-native';

function isEventKey(key) {
  if (key.substr(0, 2) !== 'on') return false; // doesn't start with 'on';
  const c = key[2];
  if (c === undefined || !isNaN(c * 1)) return false; // 3rd char is numeric
  return c === c.toUpperCase(); // only event if 3rd char is uppercase letter
}
const IS_IMAGE_REGEX = /image/i;

function isImageKey(key) {
  return IS_IMAGE_REGEX.test(key);
}

const IS_COLOR_REGEX = /color$/i;


function isColorKey(key) {
  return IS_COLOR_REGEX.test(key);
}

function processConfig(config) {
  if (typeof config !== 'object') {
    return config;
  }
  const obj = {};
  Object.keys(config).forEach(key => {
    if (isColorKey(key)) {
      obj[key] = processColor(config[key]);
    } else if (isImageKey(key)) {
      obj[key] = Image.resolveAssetSource(config[key]);
    } else if (isEventKey(key)) {
      // do nothing
    } else if (Array.isArray(config[key])) {
      obj[key] = config[key].map(processConfig);
    } else {
      obj[key] = config[key];
    }
  });
  return obj;
}

module.exports = {
  isEventKey,
  isColorKey,
  processConfig,
};
