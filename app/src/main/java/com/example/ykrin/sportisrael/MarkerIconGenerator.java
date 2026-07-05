package com.example.ykrin.sportisrael;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders circular map markers with a sport icon on a colored background.
 *
 * The background color defaults to the sport's identity color, but callers
 * can pass an override (e.g. green/red for available/occupied) once
 * occupancy status is displayed - the sport icon stays the same.
 */
public class MarkerIconGenerator {

    private static final int MARKER_SIZE_DP = 40;
    private static final int ICON_SIZE_DP = 22;
    private static final int STROKE_WIDTH_DP = 2;

    private static final Map<String, BitmapDescriptor> cache = new HashMap<>();

    /** Marker in the sport's identity color. */
    public static BitmapDescriptor getMarker(Context context, SportType sport) {
        return getMarker(context, sport, ContextCompat.getColor(context, sport.getColorRes()));
    }

    /** Marker with an explicit background color (future occupancy states). */
    public static BitmapDescriptor getMarker(Context context, SportType sport, int backgroundColor) {
        String key = sport.name() + "_" + backgroundColor;
        BitmapDescriptor cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        float density = context.getResources().getDisplayMetrics().density;
        int size = (int) (MARKER_SIZE_DP * density);
        int iconSize = (int) (ICON_SIZE_DP * density);
        float strokeWidth = STROKE_WIDTH_DP * density;
        float center = size / 2f;

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Colored circle background.
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(backgroundColor);
        fill.setStyle(Paint.Style.FILL);
        canvas.drawCircle(center, center, center - strokeWidth, fill);

        // White outline for contrast against the map.
        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setColor(Color.WHITE);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(strokeWidth);
        canvas.drawCircle(center, center, center - strokeWidth, stroke);

        // White sport icon centered in the circle.
        Drawable icon = AppCompatResources.getDrawable(context, sport.getIconRes());
        if (icon != null) {
            int left = (size - iconSize) / 2;
            int top = (size - iconSize) / 2;
            icon.setBounds(left, top, left + iconSize, top + iconSize);
            icon.draw(canvas);
        }

        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
        cache.put(key, descriptor);
        return descriptor;
    }
}
