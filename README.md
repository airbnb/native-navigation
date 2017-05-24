# Native Navigation

[![Join the chat at https://gitter.im/airbnb/native-navigation](https://badges.gitter.im/airbnb/native-navigation.svg)](https://gitter.im/airbnb/native-navigation?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![npm Version](https://img.shields.io/npm/v/native-navigation.svg)](https://www.npmjs.com/package/native-navigation) 
[![License](https://img.shields.io/npm/l/native-navigation.svg)](https://www.npmjs.com/package/native-navigation) 
[![Build Status](https://travis-ci.org/airbnb/native-navigation.svg)](https://travis-ci.org/airbnb/native-navigation) 

## DISCLAIMER

**This project is currently in beta**. 

Many of the core APIs are subject to change, and we do not consider this project "Production Ready" until it hits a 1.0 release. We encourage people to try this library out and provide us feedback as we get it to a stable state we are confident in, but not to rely on it for production use until then.

Read more about our [Roadmap to 1.0](/docs/roadmap.md)

## Contents

- [Installation](#installation)
- [Running the Example Project](#running-the-example-project)
- [Guides](#guides)
- [API Documentation](#api-documentation)
- [Related Projects and Alternatives](#related-projects-and-alternatives)
- [Contributing](#contributing)
- [FAQ](#faq)
- [License](#license)

## Installation

See the [Installation Guide](/docs/installation.md)

## Running the Example Project

To run the example project, first clone this repo:

```bash
git clone https://github.com/airbnb/native-navigation.git
cd native-navigation
```

Both [`npm`](https://nodejs.org/) and the ruby [`bundler`](http://bundler.io/) gem are needed to run the project.

```bash
npm install
```

```bash
npm start
```

Then, in another CLI window:

To run on iOS: `npm run run:ios`

To run on Android: `npm run run:android`


## [Guides](/docs/guides/README.md)

- [Basic Usage](/docs/guides/basic-usage.md)
- [Integrating with existing apps](/docs/guides/integrating-with-existing-apps.md)
- [Custom Navigation Implementations](/docs/guides/custom-navigation-implementations.md)
- [Tabs](/docs/guides/tabs.md)
- [Deep Linking](/docs/guides/deep-linking.md)
- [Platform Differences](/docs/guides/platform-differences.md)
- [Project Structure](/docs/guides/project-structure.md)
- [Shared Element Transitions](/docs/guides/shared-element-transitions.md)

## [API Documentation](/docs/api/README.md)

- [`Navigator.registerScreen(...)`](/docs/api/navigator/registerScreen.md)
- [`Navigator.push(...)`](/docs/api/navigator/push.md)
- [`Navigator.present(...)`](/docs/api/navigator/present.md)
- [`Navigator.pop(...)`](/docs/api/navigator/pop.md)
- [`Navigator.dismiss(...)`](/docs/api/navigator/dismiss.md)
- [`Config`](/docs/api/navigator-config.md)
- [`Spacer`](/docs/api/navigator-spacer.md)
- [`Tab`](/docs/api/navigator-tab.md)
- [`TabBar`](/docs/api/navigator-tab-bar.md)
- [`SharedElement`](/docs/api/navigator-shared-element.md)
- [`SharedElementGroup`](/docs/api/navigator-shared-element-group.md)


## Related Projects and Alternatives

Native Navigation is a navigation library for the React Native platform. There are many navigation libraries in the React Native ecosystem. Native Navigation is unique in that it is built on top of the iOS and Android platform navigational components, and is thus more "native" than most other options which implement navigation from scratch in JavaScript on top of base React Native components like `View` and `Animated`.

[React Native Navigation](https://github.com/wix/react-native-navigation) by Wix engineering is an alternative library that uses "Native" navigation components of each platform, and has been around longer than Native Navigation. If you need a stable / production-ready navigation library *today* that uses native platform based navigation components, we recommend you check this library out.

If you are investigating navigation solutions and you are okay with JavaScript-based solutions, we also encourage you to check out [React Navigation](https://reactnavigation.org/).

## Contributing

See the [Contributors Guide](/CONTRIBUTING.md)

## FAQ

See the [Frequently Asked Questions](/docs/FAQ.md) page

## License

This project is licensed under the [MIT License](/LICENSE.md).
