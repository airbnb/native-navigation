const BAR_TYPE = {
  SPECIALTY: 'specialty',
  INVERSE_SPECIALTY: 'inverse-specialty',
  SHEET: 'sheet',
  STATIC: 'static',
  OVERLAY: 'overlay',
  BASIC: 'basic',
};

const COLOR = {
  CELEBRATORY: 'celebratory',
  VALID: 'valid',
  INVALID: 'invalid',
  UNVALIDATED: 'unvalidated',
};

const CLOSE_BEHAVIOR = {
  POP: 'pop',
  DISMISS: 'dismiss',
};

// Android-only.
const LEFT_ICON = {
  CLOSE: 'close',
  MENU: 'menu',
  NONE: 'none',
  NAV_LEFT: 'nav-left',
};

function themeFromBarStyle({ barType }) {
  // NOTE(lmr):
  // This function could be replaced with a simple map, but I believe that we
  // may actually need a more nuanced method that looks at other properties, so'
  // I am proactively making it a function + switch statement.
  switch (barType) {
    case BAR_TYPE.SPECIALTY: return 'transparent-light';
    case BAR_TYPE.INVERSE_SPECIALTY: return 'transparent-light';
    case BAR_TYPE.SHEET: return 'transparent-light';
    case BAR_TYPE.STATIC: return 'opaque';
    case BAR_TYPE.OVERLAY: return 'transparent-light';
    case BAR_TYPE.BASIC: return 'transparent-dark';
    default: return 'transparent-light';
  }
}

module.exports = {
  BAR_TYPE,
  COLOR,
  CLOSE_BEHAVIOR,
  LEFT_ICON,
  themeFromBarStyle,
};
