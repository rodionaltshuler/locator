package com.ottamotta.locator.ui.anim;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class HeightChangeAnimation extends Animation {

    private static final int LIST_EXPAND_DURATION_MILLIS = 300;

    private int targetHeight;
    private int initialHeight;
    private View view;

    public HeightChangeAnimation(View view, int targetHeight) {
        this.view = view;
        this.initialHeight = view.getHeight();
        this.targetHeight = targetHeight;
        setDuration(LIST_EXPAND_DURATION_MILLIS);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        view.getLayoutParams().height = (int) (initialHeight + (targetHeight - initialHeight) * interpolatedTime);
        view.requestLayout();
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}