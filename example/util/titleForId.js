const HEADERS = [
  'Lorem ipsum dolor sit amet, magna ante nec.',
  'Risus nibh.',
  'Ultricies non, amet penatibus fermentum.',
  'Nunc wisi donec.',
  'Molestiae integer.',
  'Tincidunt aliquet justo.',
  'Pellentesque ullamcorper.',
  'Mattis sit, posuere ut quam.',
  'Sodales a erat, felis a.',
  'Ac interdum, suspendisse lacus dignissim.',
  'Urna augue et, magna ipsum dictum.',
  'Varius vestibulum vulputate.',
  'Ut eros, in praesent nunc, dolor torquent.',
  'Et in, nisl a, eleifend risus nulla.',
  'Urna sit lacus, faucibus tortor arcu.',
  'Vehicula porta et, varius duis turpis.',
  'Justo in, duis massa.',
  'Sagittis ac.',
  'Tempus velit donec.',
  'Aptent auctor, mauris suspendisse eu.',
];

module.exports = function titleForId(id) {
  return HEADERS[id % HEADERS.length];
};
