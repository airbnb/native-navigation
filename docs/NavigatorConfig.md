# Navigator.Config


## Props

```js

type Image = {
  uri: string;
  width: number;
  height: number;
}

type Button = {
  // shared
  title: string;
  image: Image;
  
  // ios-only-but-should-share
  enabled: boolean;
  tintColor: boolean;
  fontName: string;
  fontSize: number;
  
  // ios-only
  style: 'plain' | 'default';
  
  // android-only
};

type NavigatorConfigProps = {

  // shared
  title: string;
  titleColor: Color;
  subtitle: string;
  subtitleColor: Color;
  alpha: number;
  rightTitle: string;
  rightImage: Image;
  rightButtons: Array<Button>;
  screenColor: Color;
  hidden: boolean;
  backgroundColor: Color; 
  foregroundColor: Color;
  statusBarHidden: boolean;
  statusBarAnimation: 'slide' | 'none' | 'fade'; // 'fade' is default
  statusBarStyle: 'light' | 'default';
  
  // ios-only-but-should-share
  tintColor: Color;
  backIndicatorImage: Image;
  titleFontName: string;
  titleFontSize: number;
  subtitleFontName: string;
  subtitleFontSize: number;
  
  // android-only-but-should-share
  statusBarColor: Color;
  statusBarTranslucent: boolean;
  navIcon: Image;
  logo: Image;
  textAlign: 'left' | 'center' | 'right';
  leftButtons: Array<Button>;
  
  // ios-only
  prompt: string;
  hidesBackButton: boolean;
  hidesBarsOnTap: boolean;
  hidesBarsOnSwipe: boolean;
  hidesBarsWhenKeyboardAppears: boolean;
  isToolbarHidden: boolean;
  backIndicatorTransitionMaskImage: Image;
  translucent: boolean;
  
  // android-only
  windowTitle: string;
  elevation: number;
  overflowIcon: Image;
  displayHomeAsUp: boolean;
  homeButtonEnabled: boolean;
  showHome: boolean;
  showTitle: boolean;
  showCustom: boolean;
  useLogo: boolean;
  useShowHideAnimation: boolean;
  hideOnScroll: boolean;
  hideOffset: number;
}






```





## Events
