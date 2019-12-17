package com.lpz.dragerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Lisen.Liu on 2018/11/2.
 */

public class Utils {

    private static Canvas sCanvas = new Canvas();

    public static Bitmap getViewSnapshot(View view, int alpha) {
        if (view == null) {
            return null;
        }
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        //return Bitmap.createBitmap(bitmap);

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        sCanvas.setBitmap(result);
        Paint paint = new Paint();
        paint.setAlpha(alpha);
        sCanvas.drawBitmap(bitmap, 0, 0, paint);
        return result;
    }

    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{100, 100}, -1);
    }


    public static void setAllChildrenLongClickListener(View view, View.OnLongClickListener listener) {
        view.setOnLongClickListener(listener);
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0, N = vp.getChildCount(); i < N; i++) {
                View child = vp.getChildAt(i);
                if (child.getVisibility() == View.VISIBLE) {
                    setAllChildrenLongClickListener(child, listener);
                }
            }
        }
    }
}
