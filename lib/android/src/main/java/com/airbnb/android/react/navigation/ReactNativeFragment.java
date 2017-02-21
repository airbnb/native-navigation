package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import com.airbnb.android.R;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.airbnb.android.react.navigation.NavigatorModule.EXTRA_IS_DISMISS;
import static com.airbnb.android.react.navigation.ReactNativeIntents.REACT_MODULE_NAME;
import static com.airbnb.android.react.navigation.ReactNativeIntents.REACT_PROPS;
import static com.airbnb.android.react.navigation.ReactNativeUtils.maybeEmitEvent;

public class ReactNativeFragment extends Fragment implements ReactInterface,
    DefaultHardwareBackBtnHandler {

  private static final String ON_DISAPPEAR = "onDisappear";
  private static final String ON_APPEAR = "onAppear";
  private static final String INSTANCE_ID_PROP = "nativeNavigationInstanceId";
  private static final String ON_BUTTON_PRESS = "onButtonPress";
  private static final String ON_LINK_PRESS = "onLinkPress";

  // An incrementing ID to identify each ReactNativeActivity instance (used in `instanceId`)
  private static int UUID = 1;

  ReactNavigationCoordinator reactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance;
  ReactInstanceManager reactInstanceManager;

  private String instanceId;
  private ReactInterfaceManager activityManager;
  private ReadableMap initialConfig = ConversionUtil.EMPTY_MAP;
  private ReadableMap previousConfig = ConversionUtil.EMPTY_MAP;
  private ReadableMap renderedConfig = ConversionUtil.EMPTY_MAP;
  private ReactRootView reactRootView;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initReactNative();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_react_native, container, false);
    ReactToolbar toolbar = (ReactToolbar) v.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);
    initReactNative();
    return v;
  }

  private void initReactNative() {
    if (reactRootView != null || getView() == null) {
      return;
    }
    if (!isSuccessfullyInitialized()) {
      // TODO(lmr): need a different way of doing this
      // TODO(lmr): move to utils
      reactInstanceManager.addReactInstanceEventListener(
          new ReactInstanceManager.ReactInstanceEventListener() {
            @Override public void onReactContextInitialized(ReactContext context) {
              onCreateWithReactContext();
            }
          });
    } else {
      onCreateWithReactContext();
    }
    activityManager = new ReactInterfaceManager(this);
    reactNavigationCoordinator.registerComponent(this, instanceId);
  }

  private void onCreateWithReactContext() {
    if (getView() == null) {
      return;
    }
    // TODO(lmr): should we make the "loading" XML configurable?
    View loadingView = getView().findViewById(R.id.loading_view);
    if (loadingView != null) {
      loadingView.setVisibility(View.GONE);
    } else {
      // TODO(lmr): this shouldn't happen...
    }

    if (!isSuccessfullyInitialized()) {
      // TODO(lmr): should we make this configurable?
      ReactNativeUtils.showAlertBecauseChecksFailed(getActivity(), null);
      return;
    }
    String moduleName = getArguments().getString(REACT_MODULE_NAME);
    instanceId = String.format(Locale.ENGLISH, "%1s_fragment_%2$d", moduleName, UUID++);
    Bundle props = getArguments().getBundle(REACT_PROPS);
    if (props == null) {
      props = new Bundle();
    }
    props.putString(INSTANCE_ID_PROP, instanceId);

    if (reactRootView == null) {
      ViewStub reactViewStub = (ViewStub) getView().findViewById(R.id.react_root_view_stub);
      reactViewStub.setLayoutResource(R.layout.view_holder_react_root_view);
      reactRootView = (ReactRootView) reactViewStub.inflate();
    }
    reactRootView.startReactApplication(reactInstanceManager, moduleName, props);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    activityManager.onActivityResult(requestCode, resultCode, data);
  }

  @Override public void invokeDefaultOnBackPressed() {
    getActivity().onBackPressed();
  }

  @Override public void onPause() {
    super.onPause();
    reactInstanceManager.onHostPause(getActivity());
    emitEvent(ON_DISAPPEAR, null);
  }

  @Override public void onResume() {
    super.onResume();
    reactInstanceManager.onHostResume(getActivity(), this);
    emitEvent(ON_APPEAR, null);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    reactNavigationCoordinator.unregisterComponent(instanceId);
    if (reactRootView != null) {
      reactRootView.unmountReactApplication();
    }
  }

  @Override public boolean isDismissible() {
    return reactNavigationCoordinator.getDismissCloseBehavior(this);
  }

  public void dismiss() {
    Intent intent = new Intent()
        .putExtra(EXTRA_IS_DISMISS, isDismissible());
    getActivity().setResult(Activity.RESULT_OK, intent);
    getActivity().finish();
  }

  @Override public String getInstanceId() {
    return instanceId;
  }

  @Override public ReactRootView getReactRootView() {
    return reactRootView;
  }

  @Override public ReactToolbar getToolbar() {
    // TODO
    return null;
  }

  @Override public void signalFirstRenderComplete() {
  }

  @Override public void notifySharedElementAddition() {
    // TODO: shared element transitions probably not quite supported with RN fragments just yet.
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    ReactToolbar toolbar = getToolbar();
    if (toolbar != null) {
      // 0 will prevent menu from getting inflated, since we are inflating manually
      toolbar.onCreateOptionsMenu(0, menu, inflater);
    }
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    getImplementation().prepareOptionsMenu(
        this,
        getToolbar(),
        null,
        menu,
        this.previousConfig,
        this.renderedConfig
    );
    super.onPrepareOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // it's the link
    emitEvent(ON_LINK_PRESS, null);
    return false;
  }

  protected boolean isSuccessfullyInitialized() {
    return reactNavigationCoordinator.isSuccessfullyInitialized();
  }

  private NavigationImplementation getImplementation() {
    return reactNavigationCoordinator.getImplementation();
  }

  public void emitEvent(String eventName, Object object) {
    if (isSuccessfullyInitialized()) {
      String key =
          String.format(Locale.ENGLISH, "AirbnbNavigatorScreen.%s.%s", eventName, instanceId);
      maybeEmitEvent(reactInstanceManager.getCurrentReactContext(), key, object);
    }
  }

  private void reconcileNavigationProperties() {
    getImplementation().reconcileNavigationProperties(
        this,
        getToolbar(),
        null,
        this.previousConfig,
        this.renderedConfig,
        false
    );
  }

  @Override
  public void receiveNavigationProperties(ReadableMap properties) {
    this.previousConfig = this.renderedConfig;
    this.renderedConfig = ConversionUtil.combine(this.initialConfig, properties);
    reconcileNavigationProperties();
  }
}
