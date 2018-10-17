package com.airbnb.android.react.navigation;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

/**
 * Root ViewGroup for {@link ReactNativeFragment} that allows it to get KeyEvents.
 */
public class ReactNativeFragmentViewGroup extends FrameLayout {

    public interface KeyListener {

        boolean onKeyDown(int keyCode, KeyEvent event);

        boolean onKeyUp(int keyCode, KeyEvent event);
    }

    @Nullable
    private KeyListener keyListener;

    public ReactNativeFragmentViewGroup(Context context) {
        super(context);
    }

    public ReactNativeFragmentViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReactNativeFragmentViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setKeyListener(@Nullable KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        if (!handled && keyListener != null) {
            handled = keyListener.onKeyDown(keyCode, event);
        }
        return handled;
    }
}
