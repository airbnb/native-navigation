/* eslint no-restricted-syntax:0 */
const hasOwnProperty = Object.prototype.hasOwnProperty;

function shallowArray(a, b, compare) {
  const l = a.length;
  if (l !== b.length) return false;

  if (compare) {
    for (let i = 0; i < l; i += 1) {
      if (!compare(a[i], b[i])) return false;
    }
  } else {
    for (let i = 0; i < l; i += 1) {
      if (a[i] !== b[i]) return false;
    }
  }

  return true;
}

function shallowObject(a, b, compare) {
  let ka = 0;
  let kb = 0;

  if (compare) {
    for (const key in a) {
      if (
        hasOwnProperty.call(a, key) &&
        !compare(a[key], b[key])
      ) return false;

      ka += 1;
    }
  } else {
    for (const key in a) {
      if (
        hasOwnProperty.call(a, key) &&
        a[key] !== b[key]
      ) return false;

      ka += 1;
    }
  }

  for (const key in b) {
    if (hasOwnProperty.call(b, key)) kb += 1;
  }

  return ka === kb;
}

function flat(type) {
  return (
    type !== 'function' &&
    type !== 'object'
  );
}

function shallow(a, b, compare) {
  const aIsArray = Array.isArray(a);
  const bIsArray = Array.isArray(b);

  if (aIsArray !== bIsArray) return false;

  const aTypeof = typeof a;
  const bTypeof = typeof b;

  if (aTypeof !== bTypeof) return false;
  if (flat(aTypeof)) {
    return compare
      ? compare(a, b)
      : a === b;
  }

  return aIsArray
    ? shallowArray(a, b, compare)
    : shallowObject(a, b, compare);
}

module.exports = shallow;
