// REVIEWERS: gabriel-peal
package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Shared element helper which will automatically find and coordinate shared element transitions.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AutoSharedElementCallback
        extends SharedElementCallback {

    /**
     * Target > 5.0 because of: https://app.bugsnag .com/airbnb/android-1/errors/576174ba26963cde6fd02002?filters[error
     * .status][]=in%20progress&filters[event.severity][]=error&filters[error
     * .assigned_to][]=me&pivot_tab=event http://stackoverflow .com/questions/34658911/entertransitioncoordinator-causes-npe-in-android
     * -5-0 Target > 5.1 because of: https://app.bugsnag .com/airbnb/android-1/errors/57e594ab2f7103a1e02c1a61
     * https://app.bugsnag.com/airbnb/android-1/errors/57ed9a742f7103a1e02c9225
     * https://app.bugsnag.com/airbnb/android-1/errors/57d13d8d2f7103a1e029b988
     */
    private static final int TARGET_API = VERSION_CODES.M;

    private static final String TAG = AutoSharedElementCallback.class.getSimpleName();

    private static final long ASYNC_VIEWS_TIMEOUT_MS = 500;

    private static final int DEFAULT_WINDOW_ENTER_FADE_DURATION_MS = 350;

    private static final int DEFAULT_WINDOW_RETURN_FADE_DURATION_MS = 200;

    private static final int DEFAULT_SHARED_ELEMENT_ENTER_DURATION_MS = 300;

    private static final int DEFAULT_SHARED_ELEMENT_RETURN_DURATION_MS = 200;

    /**
     * 2 frames
     */
    private static final int ASYNC_VIEW_POLL_MS = 32;

    /**
     * Copied from {@link SharedElementCallback}
     */
    public static final String BUNDLE_SNAPSHOT_BITMAP = "sharedElement:snapshot:bitmap";

    public static final String BUNDLE_SNAPSHOT_IMAGE_SCALETYPE =
            "sharedElement:snapshot:imageScaleType";

    public static final String BUNDLE_SNAPSHOT_IMAGE_MATRIX = "sharedElement:snapshot:imageMatrix";

    private static Transition sDefaultEnterTransition;

    private static Transition sDefaultReturnTransition;

    /**
     * @see #getActivityOptions(Activity, String, long)
     */
    public static Bundle getActivityOptionsBundle(Activity activity, String type, long id) {
        return getActivityOptions(activity, type, id).toBundle();
    }

    /**
     * Automatically configure the activity options. This will walk the Activity view hierarchy and
     * look for any potential transition views. It will then throw out any transition views with the
     * same type but a different id.
     */
    public static ActivityOptionsCompat getActivityOptions(Activity activity, String type, long id) {
        List<Pair<View, String>> transitionViews = new ArrayList<>();
        ViewUtils.findTransitionViews(activity.getWindow().getDecorView(), transitionViews);

        Iterator<Pair<View, String>> it = transitionViews.iterator();
        while (it.hasNext()) {
            Pair<View, String> tv = it.next();
            String transitionName = ViewCompat.getTransitionName(tv.first);
            TransitionName tn = TransitionName.parse(transitionName);
            // If a transition view has the same type but a different ID then remove it.
            if (tn.id() != id && tn.type().equals(type)) {
                it.remove();
            }
        }

        //noinspection unchecked
        return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionViews.toArray
                (new Pair[transitionViews.size()]));
    }

    /**
     * Walks the given view group and adds all view with a set transition name to the fragment
     * transaction.
     */
    public static void addSharedElementsToFragmentTransaction(
            FragmentTransaction ft, ViewGroup viewGroup) {
        List<Pair<View, String>> transitionViews = new ArrayList<>();
        ViewUtils.findTransitionViews(viewGroup, transitionViews);

        for (Pair<View, String> tv : transitionViews) {
            ft.addSharedElement(tv.first, tv.second);
        }
    }

    /**
     * @see #getActivityOptions(Activity, View)
     */
    public static Bundle getActivityOptionsBundle(Activity activity, View view) {
        return getActivityOptions(activity, view).toBundle();
    }

    public static ActivityOptionsCompat getActivityOptions(Activity activity, View view) {
        return getActivityOptions(activity, view, true);
    }

    /**
     * Automatically creates activity options with all of the transition views within view.
     */
    public static ActivityOptionsCompat getActivityOptions(Activity activity, View view, boolean
            includeSystemUi) {
        List<Pair<View, String>> transitionViews = new ArrayList<>();

        if (VERSION.SDK_INT >= TARGET_API) {
            ViewUtils.findTransitionViews(view, transitionViews);
            if (includeSystemUi) {
                addSystemUi(activity, transitionViews);
            }
        }

        //noinspection unchecked
        return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionViews.toArray
                (new Pair[transitionViews.size()]));
    }

    @TargetApi(TARGET_API)
    private static void addSystemUi(Activity activity, List<Pair<View, String>> transitionViews) {
        View decor = activity.getWindow().getDecorView();
        View statusBar = decor.findViewById(android.R.id.statusBarBackground);
        if (statusBar != null) {
            transitionViews.add(Pair.create(statusBar, ViewCompat.getTransitionName(statusBar)));
        }
        View navBar = decor.findViewById(android.R.id.navigationBarBackground);
        if (navBar != null) {
            transitionViews.add(Pair.create(navBar, ViewCompat.getTransitionName(navBar)));
        }
    }

    /**
     * Delegate to intercept and control the behavior of shared elements and {@link
     * AutoSharedElementCallback}.
     */
    @SuppressWarnings("UnusedParameters")
    public static class AutoSharedElementCallbackDelegate {

        /**
         * Called before {@link AutoSharedElementCallback} runs its mapSharedElements logic. Return
         * whether the mapping was fully handled and no further mappings should be automatically
         * attempted.
         */
        public boolean onPreMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            return false;
        }

        /**
         * Called after {@link AutoSharedElementCallback} has run its auto mapping. If {@link
         * #onPreMapSharedElements(List, Map)} returns true, this will be called immediately after.
         */
        public void onPostMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        }
    }

    private final Runnable checkForAsyncViewsRunnable;

    private final Runnable cancelAsyncViewsRunnable;

    private final AppCompatActivity activity;

    private final List<TransitionName> asyncTransitionViews;

    private Transition sharedElementEnterTransition;

    private Transition sharedElementReturnTransition;

    private AutoSharedElementCallbackDelegate delegate;

    /**
     * Whether or not onSharedElementEnd has been called since the last onMapSharedElements. In an
     * entering transition, onSharedElementStart will be called before onSharedElementEnd. In a
     * returning transition, onSharedElementEnd will be called before onSharedElementStart.
     */
    private boolean endCalledSinceOnMap;

    private long enterBackgroundFadeDuration = DEFAULT_WINDOW_ENTER_FADE_DURATION_MS;

    private long returnBackgroundFadeDuration = DEFAULT_WINDOW_RETURN_FADE_DURATION_MS;

    public AutoSharedElementCallback(AppCompatActivity activity) {
        this.activity = activity;
        checkForAsyncViewsRunnable = null;
        cancelAsyncViewsRunnable = null;
        asyncTransitionViews = null;
    }

    /**
     * Sets up the {@link SharedElementCallback} for the given activity.
     * <p>
     * However, some views may not be available immediately such as views inside of a RecyclerView or
     * in a toolbar. Use asyncTransitionViews to postpone the shared element transition until all
     * async views are ready.
     * <p>
     * However, it will only look for type, id, and subtype and will instead do a crossfade if the
     * subid doesn't match.
     */
    public AutoSharedElementCallback(AppCompatActivity activity, TransitionName...
            asyncTransitionViews) {
        this.activity = activity;
        if (VERSION.SDK_INT >= TARGET_API) {
            // Using Arrays.asList() by itself doesn't support iterator.remove().
            this.asyncTransitionViews = new LinkedList<>(Arrays.asList(asyncTransitionViews));
            activity.supportPostponeEnterTransition();
            startPostponedTransitionsIfReady();

            checkForAsyncViewsRunnable = new Runnable() {
                @Override
                public void run() {
                    startPostponedTransitionsIfReady();
                }
            };
            cancelAsyncViewsRunnable = new Runnable() {
                @Override
                public void run() {
                    if (AutoSharedElementCallback.this.hasActivityStopped()) {
                        return;
                    }
                    AutoSharedElementCallback.this.scheduleStartPostponedTransition();
                    Log.w(TAG, "Timed out waiting for async views to load!");
                }
            };

            startPostponedTransitionsIfReady();
            getDecorView().postDelayed(cancelAsyncViewsRunnable, ASYNC_VIEWS_TIMEOUT_MS);
        } else {
            checkForAsyncViewsRunnable = null;
            cancelAsyncViewsRunnable = null;
            this.asyncTransitionViews = null;
        }
    }

    private boolean hasActivityStopped() {
        // Attempt to fix https://app.bugsnag
        // .com/airbnb/android-1/errors/5784e47d26963cde6fd22bb5?filters%5Berror
        // .status%5D%5B%5D=in%20progress&filters%5Bevent.since%5D%5B%5D=7d&filters%5Bevent
        // .severity%5D%5B%5D=error&filters%5Berror.assigned_to%5D%5B%5D=me
        return activity.getWindow() == null || activity.isFinishing();
    }

    /**
     * Scans all transition views for a partial match with all remaining async transition views.
     */
    private void startPostponedTransitionsIfReady() {
        List<Pair<View, String>> transitionViewPairs = new ArrayList<>();
        ViewUtils.findTransitionViews(getDecorView(), transitionViewPairs);

        for (Pair<View, String> p : transitionViewPairs) {
            if (p.first.getParent() == null) {
                // Attempt to fix https://app.bugsnag
                // .com/airbnb/android-1/errors/57ed9a742f7103a1e02c9225?filters%5Berror
                // .status%5D%5B%5D=in%20progress&filters%5Bevent.since%5D%5B%5D=7d&filters%5Bevent
                // .severity%5D%5B%5D=error&filters%5Berror.assigned_to%5D%5B%5D=me
                return;
            }
        }

        for (Iterator<TransitionName> it = asyncTransitionViews.iterator(); it.hasNext(); ) {
            TransitionName tn = it.next();

            for (Pair<View, String> p : transitionViewPairs) {
                // We only look for a partial match which doesn't match on subid because we can crossfade
                // views that match everything
                // except for subid.
                if (tn.partialEquals(TransitionName.parse(ViewCompat.getTransitionName(p.first)))) {
                    it.remove();
                    break;
                }
            }
        }

        if (asyncTransitionViews.isEmpty()) {
            getDecorView().removeCallbacks(checkForAsyncViewsRunnable);
            getDecorView().removeCallbacks(cancelAsyncViewsRunnable);
            scheduleStartPostponedTransition();
        } else {
            getDecorView().postDelayed(checkForAsyncViewsRunnable, ASYNC_VIEW_POLL_MS);
        }
    }

    public void scheduleStartPostponedTransition() {
        activity.getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        activity.getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                        activity.supportStartPostponedEnterTransition();
                        return true;
                    }
                });
    }

    public AutoSharedElementCallback setSharedElementEnterTransition(Transition transition) {
        sharedElementEnterTransition = transition;
        return this;
    }

    public AutoSharedElementCallback setEnterBackgroundFadeDuration(long duration) {
        enterBackgroundFadeDuration = duration;
        return this;
    }

    public AutoSharedElementCallback setSharedElementReturnTransition(Transition transition) {
        sharedElementReturnTransition = transition;
        return this;
    }

    public AutoSharedElementCallback setReturnBackgroundFadeDuration(long duration) {
        returnBackgroundFadeDuration = duration;
        return this;
    }

    public AutoSharedElementCallback setDelegate(AutoSharedElementCallbackDelegate delegate) {
        this.delegate = delegate;
        return this;
    }

    @Override
    public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix,
                                                     RectF screenBounds) {
        // This is a replacement for the platform's overzealous onCaptureSharedElementSnapshot.
        // If you look at what it's doing, it's creating an ARGB_8888 copy of every single shared
        // element.
        // This was causing us to allocate 7+mb of bitmaps on every P3 load even though we didn't
        // need any of them...
        // They're slow to garbage collect and lead to OOMs too....
        // This just pulls the bitmap from the ImageView that we're already using and shoves it into
        // the a bundle formatted all nice
        // and pretty like the platform wants it to be and never has to know the difference.
        if (sharedElement instanceof ImageView) {
            ImageView imageView = (ImageView) sharedElement;
            Drawable drawable = ((ImageView) sharedElement).getDrawable();
            if (drawable != null && drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                Bundle bundle = new Bundle();
                bundle.putParcelable(BUNDLE_SNAPSHOT_BITMAP, bitmap);
                bundle.putString(BUNDLE_SNAPSHOT_IMAGE_SCALETYPE, imageView.getScaleType().toString());
                if (imageView.getScaleType() == ImageView.ScaleType.MATRIX) {
                    Matrix matrix = imageView.getImageMatrix();
                    float[] values = new float[9];
                    matrix.getValues(values);
                    bundle.putFloatArray(BUNDLE_SNAPSHOT_IMAGE_MATRIX, values);
                }
                return bundle;
            }
        }
        return null;
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        getDecorView().removeCallbacks(cancelAsyncViewsRunnable);
        endCalledSinceOnMap = false;
        boolean handled = delegate != null && delegate.onPreMapSharedElements(names, sharedElements);

        if (!handled) {
            mapBestPartialMatches(names, sharedElements);
        }

        if (delegate != null) {
            delegate.onPostMapSharedElements(names, sharedElements);
        }
    }

    private void mapBestPartialMatches(List<String> names, Map<String, View> sharedElements) {
        List<Pair<View, String>> allTransitionViews = new ArrayList<>();
        ViewUtils.findTransitionViews(getDecorView(), allTransitionViews);

        List<View> partialMatches = new ArrayList<>();
        for (String name : names) {
            if (sharedElements.containsKey(name)) {
                // Exact match
                continue;
            }
            TransitionName tn = TransitionName.parse(name);

            findAllPartialMatches(tn, allTransitionViews, partialMatches);
            if (!partialMatches.isEmpty()) {
                View mostVisibleView = ViewUtils.getMostVisibleView(partialMatches);
                sharedElements.put(name, mostVisibleView);
            }
        }
        if (delegate != null) {
            delegate.onPostMapSharedElements(names, sharedElements);
        }
    }

    /**
     * Clears and populates partialMatches with all views from transitionViews that is a partial match
     * with the supplied transition name.
     */
    private void findAllPartialMatches(TransitionName tn, List<Pair<View, String>> transitionViews,
                                       List<View> partialMatches) {
        partialMatches.clear();
        for (Pair<View, String> p : transitionViews) {
            TransitionName tn2 = TransitionName.parse(p.second /* transition name */);
            // If there is no views that perfectly matches the transition name but there is one that is
            // a partial match, we will automatically
            // map it. This will commonly occur when the user is viewing pictures and swipes to a
            // different one.
            if (tn.partialEquals(tn2)) {
                // Partial match
                partialMatches.add(p.first);
            }
        }
    }

    @TargetApi(TARGET_API)
    @Override
    public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements,
                                     List<View> sharedElementSnapshots) {

        Transition enterTransition =
                sharedElementEnterTransition == null ? getDefaultSharedElementEnterTransition() :
                        sharedElementEnterTransition;
        Transition returnTransition =
                sharedElementReturnTransition == null ? getDefaultSharedElementReturnTransition() :
                        sharedElementReturnTransition;

        crossFadePartialMatchImageViews(sharedElementNames, sharedElements, sharedElementSnapshots,
                (int) returnTransition.getDuration());

        Window window = activity.getWindow();
        window.setSharedElementEnterTransition(enterTransition);
        window.setSharedElementReturnTransition(returnTransition);
        boolean entering = !endCalledSinceOnMap;
        window.setTransitionBackgroundFadeDuration(
                entering ? enterBackgroundFadeDuration : returnBackgroundFadeDuration);
    }

    @Override
    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements,
                                   List<View> sharedElementSnapshots) {
        endCalledSinceOnMap = true;
    }

    /**
     * Iterates through all shared elements and all mapp shared elements. If there is a mapped shared
     * element that is only a partial match with its shared element then we will cross fade from the
     * shared element to the shared element snapshot which is a bitmap created by Activity A that
     * represents the appearance of the view that the shared element is transitioning back to.
     */
    private void crossFadePartialMatchImageViews(List<String> sharedElementNames, List<View>
            sharedElements, List<View> sharedElementSnapshots, int duration) {
        // Fixes a crash in which sharedElementNames and sharedElementSnapshots are different lengths.
        // According to the javadocs, these should be 1:1 but for some reason they are not sometimes.
        // I have no idea why or what it means when they are
        // different. However, the crossfading relies on the assumption that they are so we'll just
        // ignore that case.
        // https://bugsnag.com/airbnb/android-1/errors/563d370d8203f6a6502fe8fc?filters[event
        // .file][]=AutoSharedElementCallback.java&filters[event.since][]=7d
        // Also, either of these lists can be null ¯\_(ツ)_/¯
        if (sharedElementNames == null || sharedElementSnapshots == null ||
                sharedElementNames.size() != sharedElementSnapshots.size()) {
            return;
        }

        for (int i = sharedElementNames.size() - 1; i >= 0; i--) {
            View snapshotView = sharedElementSnapshots.get(i);
            if (snapshotView == null || !(snapshotView instanceof ImageView)) {
                continue;
            }

            TransitionName tn1 = TransitionName.parse(sharedElementNames.get(i));
            for (View se : sharedElements) {
                // We need to be able to get the drawable from the ImageView to do the crossfade so if
                // it's not an ImageView then there isn't much we can do.
                if (!(se instanceof ImageView)) {
                    continue;
                }

                String transitionName = ViewCompat.getTransitionName(se);
                TransitionName tn2 = TransitionName.parse(transitionName);
                if (tn1.partialEquals(tn2) && tn1.subId() != tn2.subId()) {
                    // If The views are the same except for the subId then we can attempt to crossfade them.
                    Drawable sharedElementDrawable = ((ImageView) se).getDrawable();
                    if (sharedElementDrawable == null) {
                        sharedElementDrawable = new ColorDrawable(Color.TRANSPARENT);
                    }
                    Drawable sharedElementSnapshotDrawable = ((ImageView) snapshotView).getDrawable();
                    if (sharedElementSnapshotDrawable == null) {
                        sharedElementSnapshotDrawable = new ColorDrawable(Color.TRANSPARENT);
                    }
                    TransitionDrawable transitionDrawable =
                            new TransitionDrawable(new Drawable[]{sharedElementDrawable,
                                    sharedElementSnapshotDrawable});
                    ((ImageView) se).setImageDrawable(transitionDrawable);
                    transitionDrawable.startTransition(duration);
                }
            }
        }
    }

    @TargetApi(TARGET_API)
    private Transition getDefaultSharedElementEnterTransition() {
        if (sDefaultEnterTransition == null) {
            sDefaultEnterTransition = getDefaultTransition();
            sDefaultEnterTransition.setDuration(DEFAULT_SHARED_ELEMENT_ENTER_DURATION_MS);
        }
        return sDefaultEnterTransition;
    }

    @TargetApi(TARGET_API)
    private Transition getDefaultSharedElementReturnTransition() {
        if (sDefaultReturnTransition == null) {
            sDefaultReturnTransition = getDefaultTransition();
            sDefaultReturnTransition.setDuration(DEFAULT_SHARED_ELEMENT_RETURN_DURATION_MS);
        }
        return sDefaultReturnTransition;
    }

    @TargetApi(TARGET_API)
    private Transition getDefaultTransition() {
        TransitionSet set = new TransitionSet();
        set.addTransition(new ChangeBounds());
        set.addTransition(new Fade());
        set.addTransition(new ChangeImageTransform());
        set.setInterpolator(new FastOutSlowInInterpolator());
        return set;
    }

    private View getDecorView() {
        return activity.getWindow().getDecorView();
    }
}
