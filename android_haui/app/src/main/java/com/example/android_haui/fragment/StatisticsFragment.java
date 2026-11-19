package com.example.android_haui.fragment;

import com.example.android_haui.R;
import com.example.android_haui.activity.RoomDetailActivity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_haui.adapter.RoomAdapter;
import com.example.android_haui.database.DatabaseHelper;
import com.example.android_haui.model.Equipment;
import com.example.android_haui.model.Room;
import com.example.android_haui.utils.Constants;
import com.example.android_haui.utils.StatusUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

// Màn hình thống kê phòng theo trạng thái
public class StatisticsFragment extends Fragment {

    private DatabaseHelper dbHelper;

    private TextView txtTotalRooms;
    private TextView txtSelectedStatusTitle;
    private TextView txtStatusRoomsEmpty;
    private LinearLayout layoutFloorStats;
    private LinearLayout layoutStatusRooms;
    private LinearLayout layoutBrokenRooms;
    private TextView txtBrokenSummary;

    // Thẻ thống kê ứng với mảng StatusUtils.STATUSES
    private View[] statCards;

    // Trạng thái đang được chọn để xem danh sách
    private String selectedStatus = Constants.EMPTY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());

        txtTotalRooms = view.findViewById(R.id.txtTotalRooms);
        txtSelectedStatusTitle = view.findViewById(R.id.txtSelectedStatusTitle);
        txtStatusRoomsEmpty = view.findViewById(R.id.txtStatusRoomsEmpty);
        layoutFloorStats = view.findViewById(R.id.layoutFloorStats);
        layoutStatusRooms = view.findViewById(R.id.layoutStatusRooms);
        layoutBrokenRooms = view.findViewById(R.id.layoutBrokenRooms);
        txtBrokenSummary = view.findViewById(R.id.txtBrokenSummary);

        statCards = new View[]{
                view.findViewById(R.id.statEmpty),
                view.findViewById(R.id.statInUse),
                view.findViewById(R.id.statMaintenance)
        };

        // Chạm vào thẻ trạng thái -> xem danh sách phòng của trạng thái đó
        for (int i = 0; i < statCards.length; i++) {
            final String status = StatusUtils.STATUSES[i];
            statCards[i].setOnClickListener(v -> {
                selectedStatus = status;
                loadStatistics();
            });
        }
    }

    // Tải toàn bộ số liệu thống kê
    private void loadStatistics() {
        if (dbHelper == null) return;

        int total = dbHelper.getTotalRoomCount();
        txtTotalRooms.setText(String.valueOf(total));

        // 1. Thống kê theo trạng thái
        for (int i = 0; i < statCards.length; i++) {
            String status = StatusUtils.STATUSES[i];
            int count = dbHelper.countRoomsByStatus(status);
            int percent = total == 0 ? 0 : Math.round(count * 100f / total);

            View card = statCards[i];
            TextView txtLabel = card.findViewById(R.id.txtStatLabel);
            TextView txtCount = card.findViewById(R.id.txtStatCount);
            TextView txtPercent = card.findViewById(R.id.txtStatPercent);
            LinearProgressIndicator progress = card.findViewById(R.id.progressStat);

            StatusUtils.applyBadge(txtLabel, status);
            txtCount.setText(String.valueOf(count));
            txtPercent.setText("phòng · " + percent + "%");
            progress.setIndicatorColor(StatusUtils.getTextColor(status));
            progress.setProgressCompat(percent, false);

            // Viền nổi bật cho thẻ đang chọn
            card.setBackground(createCardBackground(
                    status.equals(selectedStatus) ? StatusUtils.getTextColor(status) : 0));
        }

        // 2. Thống kê theo tầng
        showFloorStatistics(dbHelper.getAllRooms());

        // 3. Thiết bị hỏng theo phòng
        showBrokenEquipments();

        // 4. Danh sách phòng thuộc trạng thái đang chọn
        showRoomsOfSelectedStatus();
    }

    // Đếm số phòng mỗi trạng thái trên từng tầng
    private void showFloorStatistics(ArrayList<Room> rooms) {
        // tầng -> {trống, đang sử dụng, bảo trì}
        TreeMap<Integer, int[]> floorMap = new TreeMap<>();

        for (Room room : rooms) {
            int[] counts = floorMap.get(room.getFloor());
            if (counts == null) {
                counts = new int[3];
                floorMap.put(room.getFloor(), counts);
            }
            for (int i = 0; i < StatusUtils.STATUSES.length; i++) {
                if (StatusUtils.STATUSES[i].equals(room.getStatus())) {
                    counts[i]++;
                }
            }
        }

        layoutFloorStats.removeAllViews();

        for (Map.Entry<Integer, int[]> entry : floorMap.entrySet()) {
            int[] counts = entry.getValue();

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(10), 0, dp(10));

            TextView txtFloor = new TextView(requireContext());
            txtFloor.setText("Tầng " + entry.getKey());
            txtFloor.setTextColor(Color.parseColor("#263247"));
            txtFloor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            txtFloor.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(txtFloor, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            for (int i = 0; i < counts.length; i++) {
                TextView badge = new TextView(requireContext());
                StatusUtils.applyBadge(badge, StatusUtils.STATUSES[i]);
                badge.setText(String.valueOf(counts[i]));
                badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                badge.setTypeface(null, android.graphics.Typeface.BOLD);
                badge.setGravity(Gravity.CENTER);
                badge.setMinWidth(dp(36));
                badge.setPadding(dp(8), dp(4), dp(8), dp(4));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMarginStart(dp(6));
                row.addView(badge, lp);
            }

            layoutFloorStats.addView(row);
        }
    }

    // Gom thiết bị hỏng theo từng phòng, chạm để mở chi tiết phòng
    private void showBrokenEquipments() {
        ArrayList<Equipment> brokenList = dbHelper.getBrokenEquipments();

        // mã phòng -> danh sách thiết bị hỏng
        LinkedHashMap<String, ArrayList<Equipment>> byRoom = new LinkedHashMap<>();
        int totalBroken = 0;

        for (Equipment equipment : brokenList) {
            ArrayList<Equipment> list = byRoom.get(equipment.getRoomCode());
            if (list == null) {
                list = new ArrayList<>();
                byRoom.put(equipment.getRoomCode(), list);
            }
            list.add(equipment);
            totalBroken += equipment.getQuantity();
        }

        txtBrokenSummary.setText(totalBroken + " thiết bị · " + byRoom.size() + " phòng");

        layoutBrokenRooms.removeAllViews();

        boolean first = true;
        for (Map.Entry<String, ArrayList<Equipment>> entry : byRoom.entrySet()) {
            String roomCode = entry.getKey();

            if (!first) {
                View divider = new View(requireContext());
                divider.setBackgroundColor(Color.parseColor("#EEF0F5"));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1);
                lp.setMarginStart(dp(16));
                lp.setMarginEnd(dp(16));
                layoutBrokenRooms.addView(divider, lp);
            }
            first = false;

            // Tên các thiết bị hỏng: "Máy chiếu (1), Loa (2)"
            StringBuilder names = new StringBuilder();
            int roomBroken = 0;
            for (Equipment equipment : entry.getValue()) {
                if (names.length() > 0) names.append(", ");
                names.append(equipment.getName()).append(" (").append(equipment.getQuantity()).append(")");
                roomBroken += equipment.getQuantity();
            }

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(16), dp(12), dp(16), dp(12));
            TypedValue outValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(
                    android.R.attr.selectableItemBackground, outValue, true);
            row.setBackgroundResource(outValue.resourceId);

            TextView txtRoom = new TextView(requireContext());
            txtRoom.setText("Phòng " + roomCode + " · " + roomBroken + " hỏng");
            txtRoom.setTextColor(Color.parseColor("#19243A"));
            txtRoom.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            txtRoom.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(txtRoom);

            TextView txtNames = new TextView(requireContext());
            txtNames.setText(names.toString());
            txtNames.setTextColor(Color.parseColor("#C8423B"));
            txtNames.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            txtNames.setPadding(0, dp(3), 0, 0);
            row.addView(txtNames);

            row.setOnClickListener(v -> RoomDetailActivity.open(requireContext(), roomCode));

            layoutBrokenRooms.addView(row);
        }

        if (byRoom.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Không có thiết bị hỏng");
            empty.setTextColor(Color.parseColor("#8992A5"));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            empty.setPadding(dp(16), dp(14), dp(16), dp(14));
            layoutBrokenRooms.addView(empty);
        }
    }

    // Hiển thị các phòng có trạng thái đang chọn
    private void showRoomsOfSelectedStatus() {
        ArrayList<Room> rooms = dbHelper.getRoomsByStatus(selectedStatus);

        txtSelectedStatusTitle.setText("Phòng " + StatusUtils.getLabel(selectedStatus).toLowerCase()
                + " (" + rooms.size() + ")");

        layoutStatusRooms.removeAllViews();

        // Dùng lại RoomAdapter để tạo giao diện từng phòng
        RoomAdapter adapter = new RoomAdapter(requireContext(), rooms);

        for (int i = 0; i < rooms.size(); i++) {
            View item = adapter.getView(i, null, layoutStatusRooms);
            final String roomCode = rooms.get(i).getRoomCode();
            item.setOnClickListener(v -> RoomDetailActivity.open(requireContext(), roomCode));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(10);
            layoutStatusRooms.addView(item, lp);
        }

        txtStatusRoomsEmpty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Nền thẻ trắng bo góc, có viền màu khi được chọn
    private GradientDrawable createCardBackground(int highlightColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(14));
        if (highlightColor != 0) {
            bg.setStroke(dp(2), highlightColor);
        } else {
            bg.setStroke(dp(1), Color.parseColor("#EEF0F5"));
        }
        return bg;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }
}
