package com.lpz.dragerview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

public final class PageTransitionHelper {

    public static void startTransition(Intent intent, View v, String transitionName) {
        startTransition(intent, -1, v, transitionName);
    }

    public static void startTransition(Intent intent, int requestCode,
                                       View v, String transitionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.setTransitionName(transitionName);
        }
        Context ctx = v.getContext();
        if (ctx instanceof Activity) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation((Activity) ctx, v, transitionName);
            ((Activity) ctx).startActivityForResult(intent, requestCode, options.toBundle());
        } else {
            ctx.startActivity(intent);
        }
    }

}
