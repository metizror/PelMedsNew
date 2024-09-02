package com.metiz.pelconnect.notification;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.metiz.pelconnect.R;


public class SetNotificationCount {
    public static void setBadgeCount(Context context, int count) {

        BadgeDrawable badge;

        // Reuse drawable if possible

            badge = new BadgeDrawable(context);


        badge.setCount(count);
    }
}
