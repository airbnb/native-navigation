package com.airbnb.android.react.navigation;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import com.facebook.react.views.text.ReactFontManager;

/**
 * Style a {@link android.text.Spannable} with a custom {@link android.graphics.Typeface}.
 *
 * @author Tristan Waddington
 *         Taken from https://stackoverflow.com/questions/8607707/how-to-set-a-custom-font-in-the-actionbar-title/15181195#15181195
 *         <p>
 *         Adapted for React Native usage
 */
public class TypefaceSpan extends MetricAffectingSpan {

    private Typeface typeface;

    /**
     * Load the {@link Typeface} and apply to a {@link android.text.Spannable}.
     */
    public TypefaceSpan(Context context, String fontFamilyName, int style) {
        typeface = ReactFontManager.getInstance().getTypeface(fontFamilyName, style, context.getApplicationContext().getAssets());
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(typeface);

        // Note: This flag is required for proper typeface rendering
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(typeface);

        // Note: This flag is required for proper typeface rendering
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}
