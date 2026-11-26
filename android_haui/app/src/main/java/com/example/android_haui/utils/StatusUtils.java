package com.example.android_haui.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.widget.TextViewCompat;

import com.example.android_haui.R;

public class StatusUtils {

    public static final String[] STATUSES = {
            Constants.EMPTY, Constants.IN_USE, Constants.MAINTENANCE
    };

    @StringRes
    public static int label(String status) {
        if (Constants.EMPTY.equals(status)) return R.string.overview_empty;
        if (Constants.IN_USE.equals(status)) return R.string.overview_in_use;
        if (Constants.MAINTENANCE.equals(status)) return R.string.overview_maintenance;
        return R.string.unknown;
    }

    @ColorRes
    public static int foreground(String status) {
        if (Constants.EMPTY.equals(status)) return R.color.empty_fg;
        if (Constants.IN_USE.equals(status)) return R.color.use_fg;
        if (Constants.MAINTENANCE.equals(status)) return R.color.maintenance_fg;
        return R.color.text_gray;
    }

    @ColorRes
    private static int background(String status) {
        if (Constants.EMPTY.equals(status)) return R.color.empty_bg;
        if (Constants.IN_USE.equals(status)) return R.color.use_bg;
        if (Constants.MAINTENANCE.equals(status)) return R.color.maintenance_bg;
        return R.color.surface;
    }

    @DrawableRes
    private static int icon(String status) {
        if (Constants.EMPTY.equals(status)) return R.drawable.ic_status_empty;
        if (Constants.IN_USE.equals(status)) return R.drawable.ic_status_use;
        if (Constants.MAINTENANCE.equals(status)) return R.drawable.ic_status_maintenance;
        return 0;
    }

    public static void applyBadge(TextView view, String status) {
        bindBadge(view, label(status), foreground(status), background(status), icon(status));
    }

    public static void applyConditionBadge(TextView view, String condition) {
        if (Constants.BROKEN.equals(condition)) {
            bindBadge(view, R.string.broken, R.color.broken_fg, R.color.broken_bg, 0);
        } else if (Constants.GOOD.equals(condition)) {
            bindBadge(view, R.string.good, R.color.empty_fg, R.color.empty_bg, 0);
        } else {
            bindBadge(view, R.string.unknown, R.color.text_gray, R.color.surface, 0);
        }
    }

    private static void bindBadge(TextView view, @StringRes int label, @ColorRes int foreground,
                                  @ColorRes int background, @DrawableRes int icon) {
        Context context = view.getContext();
        ColorStateList ink = context.getColorStateList(foreground);
        view.setText(label);
        view.setTextColor(ink);
        view.setBackgroundResource(R.drawable.bg_status_badge);
        view.setBackgroundTintList(context.getColorStateList(background));
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
        TextViewCompat.setCompoundDrawableTintList(view, ink);
    }
}
