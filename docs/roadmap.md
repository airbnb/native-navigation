# Road map to 1.0

As indicated in the README of this project, **Native Navigation is currently in "beta"**, and not 
recommended for production use in its current state.

We wanted to make Native Navigation public so that we could collaborate in the open and make sure 
that we are making API decisions that can be useful for more people.

In the process of decoupling this library from Airbnb's internal navigation infrastructure, we have 
had to make a lot of changes to the code in order to make it extensible to work both for Airbnb and 
for others. In order to accomplish the level of extensibility we wanted, we had to refactor a lot
of the code base, and so the library now has a lot of code that has not yet been tested in a 
production environment.

There are several things that we would like to do before we can recommend it for use in production 
apps. Right now, those things are primarily the following:


### Ship it in Airbnb's App
 
The most important test that we can have is the test of us actually using this library in the main
Airbnb app for every React Native flow we have in production, completely moving over our current 
navigation solution to this library.


### Proper Extension Points

One of the primary goals of Native Navigation is to allow for 
[custom navigation implementations](/docs/guides/custom-navigation-implementations.md) to be 
injected into the library and allow for really custom branded navigation implementations to easily 
integrate with React Native through Native Navigation, while successfully abstracting away a lot of 
the non-trivial parts of navigation.

Note: This is where YOU can help! If you are wanting to use Native Navigation for exactly this 
purpose but have some non-trivial requirements, please file an issue so we can talk about it!


### Deep Linking

We need to figure out the right way to expose deep linking with this library in a way that doesn't 
lock people in to any one deep linking library.


## 1.0 and Beyond

Beyond a 1.0 release, there are a lot of ideas we have for this library that we are excited to work 
on! While these are not necessary for a 1.0 release, we hope to work on them in parallel, or have 
interested commmunity members help out!

These features include (but are certainly not limited to!):


### Lottie Transitions

On iOS, ViewController transitions can be animated using [Lottie](https://github.com/airbnb/lottie-ios).
It's possible we could add support for such a feature into Native Navigation. 


### React-rendered NavigationBar and TabBar

A second navigation implementation could be shipped with this library that allows for navigation 
components such as the navigation bar and tab bars be rendered using React Native just like the 
rest of your React Native app.


### Web version

OK, We realize it's a little wonky for a "Native Navigation" library to have a web version... but 
why not?!

Just like the iOS and Android implementations of Native Navigation hide behind a common JS 
interface, a JS-based web navigator could also implement the same interface, helping pave a path 
towards cross-platform development!
