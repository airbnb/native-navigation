import {
  processColor,
  Image,
} from 'react-native';
import shallowEquals from './shallowEquals';

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
      //do nothing
    } else if (Array.isArray(config[key])) {
      obj[key] = config[key].map(processConfig);
    } else {
      obj[key] = config[key];
    }
  });
  return obj;
}

function processConfigWatchingForMutations(target, prev, next, mutationFlag, onEvent) {
  /* eslint no-param-reassign: 0, no-unused-expressions: 0 */
  if (typeof next !== 'object') {
    return;
  }
  Object.keys(next).forEach(key => {
    if (key === 'children') return;
    if (isEventKey(key)) {
      onEvent && onEvent(key);
    } else if (prev[key] !== next[key]) {
      if (isColorKey(key)) {
        target[key] = processColor(next[key]);
        mutationFlag.hasMutated = true;
      } else if (isImageKey(key)) {
        target[key] = Image.resolveAssetSource(next[key]);
        mutationFlag.hasMutated = true;
      } else if (Array.isArray(next[key])) {
        // TODO: shallow
        if (!shallowEquals(next[key], prev[key], shallowEquals)) {
          processConfigWatchingForMutations(target[key]);
          target[key] = next[key].map(el => {
            const result = {};
            processConfigWatchingForMutations(result, {}, el, mutationFlag, null);
            return result;
          });
          mutationFlag.hasMutated = true;
        }
      } else {
        target[key] = next[key];
        mutationFlag.hasMutated = true;
      }
    }
  });

  // we need to also cycle through keys that were there previously but might not be
  // anymore and treat that as a removal
  Object.keys(prev).forEach(key => {
    if (key in prev && !(key in next)) {
      if (isEventKey(key)) {
        onEvent && onEvent(key);
      } else if (key === 'children') {
        // do nothing
      } else {
        // remove from barProps
        target[key] = undefined;
        mutationFlag.hasMutated = true;
      }
    }
  });
}

module.exports = {
  isEventKey,
  isColorKey,
  processConfig,
  processConfigWatchingForMutations,
};
