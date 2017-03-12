# Custom Navigation Implementations

Native Navigation is built in such a way that a core interface, called a "Navigation Implementation" 
can be provided to the library to handle a lot of the UI aspects of Navigation. This allows users of 
the library to actually use their own custom navigation solutions without hacking the internals of it 
or rolling their own.

Native Navigation ships with [`DefaultImplementation`](/docs/implementations/DefaultImplementation.md) 
which is an implementation that uses the iOS and Android idiomatic platform navigation components, and
attempts to make them as customizable as they were meant to be, but this may not be customizable 
enough for your use case.

Below is some documentation around the specific interface methods

## iOS Navigation Implementations


```swift
protocol ReactNavigationImplementation {

  func makeNavigationController(rootViewController: UIViewController) -> UINavigationController

  func reconcileScreenConfig(
    viewController: ReactViewController,
    navigationController: UINavigationController?,
    prev: [String: AnyObject],
    next: [String: AnyObject]
  )

  func reconcileTabConfig(
    tabBarItem: UITabBarItem,
    prev: [String: AnyObject],
    next: [String: AnyObject]
  )

  func reconcileTabBarConfig(
    tabBar: UITabBar,
    prev: [String: AnyObject],
    next: [String: AnyObject]
  )

  func getBarHeight(
    viewController: ReactViewController, 
    navigationController: UINavigationController?, 
    config: [String: AnyObject]
  ) -> CGFloat;
}
```

If you have a navigation implementation that you would like to use, you have to inject it into the 
`ReactNavigationCoordinator`:

```swift
let implementation: ReactNavigationImplementation = MyCustomNavigationImplementation();
ReactNavigationCoordinator.sharedInstance.navigation = implementation;
```


### Android Navigation Implementations


```java
interface NavigationImplementation {
  void reconcileNavigationProperties(
      ReactInterface component,
      ReactToolbar toolbar,
      ActionBar bar,
      ReadableMap previous,
      ReadableMap next,
      boolean firstCall
  );

  void prepareOptionsMenu(
      ReactInterface component,
      ReactToolbar toolbar,
      ActionBar bar,
      Menu menu,
      ReadableMap previous,
      ReadableMap next
  );

  boolean onOptionsItemSelected(
      ReactInterface component,
      ReactToolbar toolbar,
      ActionBar bar,
      MenuItem item,
      ReadableMap config
  );

  float getBarHeight(
      ReactInterface component,
      ReactToolbar toolbar,
      ActionBar actionBar,
      ReadableMap config,
      boolean firstCall
  );

  void makeTabItem(
      ReactBottomNavigation bottomNavigation,
      Menu menu,
      int index,
      Integer itemId,
      ReadableMap config
  );

  void reconcileTabBarProperties(
      ReactBottomNavigation bottomNavigation,
      Menu menu,
      ReadableMap prev,
      ReadableMap next
  );
}
```

If you have a navigation implementation that you would like to use, you have to inject it into the 
`ReactNavigationCoordinator`:

```java
NavigationImplementation implementation = new MyCustomNavigationImplementation();
ReactNavigationCoordinator.sharedInstance.injectImplementation(implementation);
```

