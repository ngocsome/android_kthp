package com.example.android_haui.utils;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import com.example.android_haui.R;

// Tiện ích hiển thị trạng thái phòng và bộ lọc dạng chip
public class StatusUtils {

    public static final String[] STATUSES = {
            Constants.EMPTY,
            Constants.IN_USE,
            Constants.MAINTENANCE
    };

    // Tên trạng thái hiển thị
    public static String getLabel(String status) {
        if (Constants.EMPTY.equals(status)) return "Trống";
        if (Constants.IN_USE.equals(status)) return "Đang sử dụng";
        if (Constants.MAINTENANCE.equals(status)) return "Bảo trì";
        return "Không xác định";
    }

    // Màu chữ của trạng thái
    public static int getTextColor(String status) {
        if (Constants.EMPTY.equals(status)) return Color.parseColor("#239653");
        if (Constants.IN_USE.equals(status)) return Color.parseColor("#3264BB");
        if (Constants.MAINTENANCE.equals(status)) return Color.parseColor("#B77B18");
        return Color.DKGRAY;
    }

    // Màu nền nhạt của trạng thái
    public static int getBgColor(String status) {
        if (Constants.EMPTY.equals(status)) return Color.parseColor("#E3F5EA");
        if (Constants.IN_USE.equals(status)) return Color.parseColor("#E5EEFF");
        if (Constants.MAINTENANCE.equals(status)) return Color.parseColor("#FFF1D5");
        return Color.parseColor("#EEEEEE");
    }

    // Hiển thị nhãn trạng thái dạng badge bo góc
    public static void applyBadge(TextView textView, String status) {
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(20);
        background.setColor(getBgColor(status));

        textView.setText(getLabel(status));
        textView.setTextColor(getTextColor(status));
        textView.setBackground(background);
    }

    // Badge tình trạng thiết bị: Tốt (xanh) / Hỏng (đỏ)
    public static void applyConditionBadge(TextView textView, String condition) {
        boolean broken = Constants.BROKEN.equals(condition);

        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(20);
        background.setColor(Color.parseColor(broken ? "#FDE7E6" : "#E3F5EA"));

        textView.setText(broken ? "Hỏng" : "Tốt");
        textView.setTextColor(Color.parseColor(broken ? "#C8423B" : "#239653"));
        textView.setBackground(background);
    }

    // Cập nhật giao diện nhóm chip lọc (chip được chọn màu xanh đậm)
    public static void updateChips(TextView[] chips, int selectedIndex) {
        for (int i = 0; i < chips.length; i++) {
            if (i == selectedIndex) {
                chips[i].setBackgroundResource(R.drawable.bg_filter_selected);
                chips[i].setTextColor(Color.WHITE);
            } else {
                chips[i].setBackgroundResource(R.drawable.bg_filter_normal);
                chips[i].setTextColor(Color.parseColor("#526078"));
            }
        }
    }
}
