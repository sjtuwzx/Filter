package com.wzx.filter.sample.tools;

import android.content.Context;

public final class ViewUtils {

    private ViewUtils() {

    }

    public static int dip2px(Context context, float dipValue) {
        if (context == null) {
            return (int) dipValue;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

}

