# Frequently Asked Questions

## Is Native Navigation used in the public Airbnb app?

Not yet. This repository started out as a direct rewrite of the Navigation infrastructure we 
currently use in our public app. It started out very coupled to our own Navigation components, and 
we are currently attempting to replace our current navigation infrastructure with this library 
using a [custom implementation](/docs/guides/custom-navigation-implementations.md). We consider 
this one of our criteria in our [Roadmap to 1.0](/docs/roadmap.md), and will not consider the 
library to be "production ready" until we ourselves are using it in production. 


## I want to use React Native in my app, but we have custom Navigation. Can this library work for me?

Maybe. We have explicitly designed this library to be extensible so that apps with existing infrastructure 
that's *not* in React Native can integrate with our library and have their React Native screens and
"Native" screens use the same navigation components. This is precisely why we built the library for 
Airbnb in the first place.  With that said, we can't guarantee that this library will integrate 
seamlessly with yours. If you are having any trouble with this, please file an issue. We are still 
learning what points of extensibility are needed. See our 
[custom implementation guide](/docs/guides/custom-navigation-implementations.md) for more info.


## Why is Native Navigation written in Swift?

Native Navigation is written using Swift because Airbnb is written with Swift, and we are likely 
not going to change that any time soon. This makes integrating with React Native slightly more 
difficult, but we are hoping that this will get better over time.

