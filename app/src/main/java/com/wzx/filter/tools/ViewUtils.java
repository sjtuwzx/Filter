package com.wzx.filter.tools;

import android.content.Context;

public class ViewUtils {
	
	public static int dip2px(Context context, float dipValue) {
		if (context == null) {
			return (int) dipValue;
		}
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

}
