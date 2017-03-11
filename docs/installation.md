# Installation


## Creating a new project with Native Navigation

You can start your project using React Native's CLI. If you do not have the CLI installed yet,
run the following command:

```bash
npm i -g react-native-cli
```

With the CLI installed, you can create a new project. In this example we will call it `MyNewProject`:

```bash
react-native init MyNewProject
cd MyNewProject
```

Install the latest version of native-navigation:

```bash
npm i --save native-navigation
```

*IMPORTANT*: Do NOT use `react-native link` to link native navigation. It is currently not supported.
 


### JavaScript Project Setup

We recommend structuring your project in a slightly different way than the React Native template starts
you off with. For a more detailed guide, please check out our [Project Structure Guide](/docs/guides/project-structure.md).

For a quicker setup, we are going to just change the minimal amount in this guide.

You should start with two entry files, `index.ios.js` and `index.android.js`.

In both files, you should have a demo app component set up, with a call to `AppRegistry.registerComponent`
that looks something like this:

```js
AppRegistry.registerComponent('MyNewProject', () => MyNewProject);
```

We want to change this to use `NativeNavigator.registerScreen` instead in both files:

```js
Navigator.registerScreen('Home', () => MyNewProject);
```

And you're done!  We have now made this component a "Screen", and we have called it "Home".


Note: It is also completely fine to just delete one of these files, and rename it to be `index.js`, and
have the entry point of your application be cross-platform. This is entirely up to you, though.



### iOS Project Setup

Native Navigation is written in Swift, and thus it is currently required to be imported into
your iOS App as a dynamic framework. We are actively working on an approach to importing this project
such that it can be statically linked, but for now the easiest way to use Native Navigation is with CocoaPods.

It is important to understand that if you use CocoaPods for Native Navigation, all of the other 
native libraries that depend on React, as well as React itself, need to also be imported using CocoaPods.

To get started with CocoaPods, we first want to make sure we have it installed:

```bash
sudo gem install cocoapods
```

React Native requires CocoaPods version 1.2.0 or greater, so verify that you have at least that version:

```bash
pod --version
```

If you do, we now need to create a `Podfile` for your ios project.

```bash
cd ios
pod init
```

There should now be a `Podfile` in this directory. Open it. You should edit your Podfile to look like this:

```ruby
target 'MyNewProject' do
  # Native Navigation uses Swift, so this line is required!
  use_frameworks!

  # Native Navigation!
  pod 'native-navigation', :path => '../node_modules/native-navigation'

  # To use CocoaPods with React Native, you need to add this specific Yoga spec as well
  pod 'Yoga', :path => '../node_modules/react-native/ReactCommon/yoga/Yoga.podspec'

  # You don't necessarily need all of these subspecs, but this would be a typical setup.
  pod 'React', :path => '../node_modules/react-native', :subspecs => [
    'Core',
    'RCTText',
    'RCTNetwork',
    'RCTWebSocket', # needed for debugging
    'RCTAnimation',
    'RCTImage',
    'RCTNetwork'
    # Add any other subspecs you want to use in your project
  ]
  
  # Add any other dependencies here, including any 3rd party native libraries that you depend on for
  # React Native.
end
```

Now that your `Podfile` is ready, you can run `pod install`:

```bash
pod install
```

There should now be an `MyNewProject.xcworkspace` available in your ios directory, which was created
by CocoaPods. You should use this file to open your app up in XCode, NOT the .xcodeproj file.

We now need to make some modifications to our project in XCode. Open up the project:

```bash
open MyNewProject.xcworkspace
```

React Native's starter template will have the React Project initially statically linked to your project,
but now we are using CocoaPods, so we want to get rid of them.


_TODO: show some screenshots and better instructions of doing this_


Now we need to set up our navigation in our project's actual code. We are going to do most of the
work in the `AppDelegate` class that React Native's starter template provides you.

The starter template has a single `RCTRootView` being created with a bundle URL, which then internally creates
an `RCTBridge` instance. Native Navigation needs a single `RCTBridge` instance to handle the creation
of `RCTRootView`s itself. To do this, we recommend implementing a shared "singleton" object in your app
that implements both the `RCTBridgeDelegate` and `ReactNavigationCoordinatorDelegate` protocols. In the starter 
template, the main `AppDelegate`  object is a good candidate.

Start by modifying your `AppDelegate.h` header file to make your `AppDelegate` class implement the
`RCTBridgeDelegate` and `ReactNavigationCoordinatorDelegate` protocols:

```objective-c
@interface AppDelegate : UIResponder <UIApplicationDelegate, RCTBridgeDelegate, ReactNavigationCoordinatorDelegate>
```

You will also need to include the following headers:

```objective-c
#import <React/RCTBridge.h>
@import NativeNavigation;
```

In the `AppDelegate.m` implementation file, we need to implement those delegate methods and set up
Native Navigation's `ReactNavigationCoordinator`.

The `RCTBridgeDelegate` has a `sourceURLForBridge:` method, which is expected to return an `NSURL` for
the app's JavaScript entry point.  For this, we recommend using the `RCTBundleURLProvider`, and you can
actually just copy the code that the starter template uses in `application:didFinishLaunchingWithOptions:`.

```objective-c
- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge {
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index.ios" fallbackResource:nil];
}
```

The `ReactNavigationCoordinatorDelegate` has a `rootViewControllerForCoordinator:` delegate method that
is meant to return the root view controller of the entire app that NativeNavigation will use to understand
the current navigation stack.  Typically, this is just the `rootViewController` of the window:

```objective-c
- (UIViewController *)rootViewControllerForCoordinator: (ReactNavigationCoordinator *)coordinator {
  return self.window.rootViewController;
}
```

Now we want to adjust the `AppDelegate`'s `application:didFinishLaunchingWithOptions:` method.

The first thing we want to do is create an `RCTBridge` instance with the delegate:

```objective-c
RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self
                                          launchOptions:launchOptions];
```

Native Navigation has a shared `ReactNavigationCoordinator` instance that does a lot of the coordination
of the library. It needs an `RCTBridge` and an `ReactNavigationCoordinatorDelegate`:

```objective-c
ReactNavigationCoordinator *coordinator = [ReactNavigationCoordinator sharedInstance];

[coordinator setBridge:bridge];
[coordinator setDelegate:self];
```

Now the last thing you need to do is create the App's root ViewController.  If your app launches
with a React Native screen, you would want to do this:

```objective-c
ReactViewController *mainViewController = [[ReactViewController alloc] initWithModuleName:@"Home"];
self.window.rootViewController = [[coordinator navigation] makeNavigationControllerWithRootViewController:mainViewController];
```

In this example we have used the string `@"Home"` as the registered screen name, which we will later have
to set up in our JavaScript code.

At this point your `AppDelegate.h` file should look like:

```objective-c
#import <UIKit/UIKit.h>
#import <React/RCTBridge.h>
@import NativeNavigation;

@interface AppDelegate : UIResponder <UIApplicationDelegate, RCTBridgeDelegate, ReactNavigationCoordinatorDelegate>

@property (nonatomic, strong) UIWindow *window;

@end
```

And your `AppDelegate.m` file like this:

```objective-c
#import "AppDelegate.h"

#import <React/RCTBundleURLProvider.h>
@import NativeNavigation;

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self
                                      launchOptions:launchOptions];
  ReactNavigationCoordinator *coordinator = [ReactNavigationCoordinator sharedInstance];

  [coordinator setBridge:bridge];
  [coordinator setDelegate:self];

  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  ReactViewController *mainViewController = [[ReactViewController alloc] initWithModuleName:@"Home"];
  self.window.rootViewController = [[coordinator navigation] makeNavigationControllerWithRootViewController:mainViewController];
  [self.window makeKeyAndVisible];
  return YES;
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge {
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index.ios" fallbackResource:nil];
}

- (UIViewController *)rootViewControllerForCoordinator: (ReactNavigationCoordinator *)coordinator {
  return self.window.rootViewController;
}

@end
```

At this point, you should be able to build and run your iOS project!


## Android Project Setup

_This section still needs to be filled out_
