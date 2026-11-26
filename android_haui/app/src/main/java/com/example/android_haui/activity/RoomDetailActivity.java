package com.example.android_haui.activity;

import com.example.android_haui.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.android_haui.database.DatabaseHelper;
import com.example.android_haui.model.Equipment;
import com.example.android_haui.model.Room;
import com.example.android_haui.model.StatusHistory;
import com.example.android_haui.utils.Constants;
import com.example.android_haui.utils.StatusUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RoomDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_CODE = "room_code";

    private DatabaseHelper dbHelper;
    private Room room;

    private TextView txtDetailCode;
    private TextView txtDetailStatus;
    private TextView txtDetailFloor;
    private RadioGroup rgStatus;

    // Thiết bị
    private LinearLayout layoutEquipments;
    private TextView txtEquipmentSummary;
    private TextView txtEquipmentHint;

    // Lịch sử trạng thái
    private LinearLayout layoutHistory;
    private static final int HISTORY_LIMIT = 10;

    // Gợi ý tên thiết bị khi nhập
    private static final String[] EQUIPMENT_SUGGESTIONS = {
            "Máy chiếu", "Màn chiếu", "Điều hòa", "Quạt trần", "Bàn ghế sinh viên",
            "Bàn giáo viên", "Bảng trắng", "Loa", "Micro", "Máy tính", "Bóng đèn"
    };

    // Mở màn hình chi tiết của một phòng
    public static void open(Context context, String roomCode) {
        Intent intent = new Intent(context, RoomDetailActivity.class);
        intent.putExtra(EXTRA_ROOM_CODE, roomCode);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        // Chừa khoảng trống cho thanh trạng thái / thanh điều hướng hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        dbHelper = new DatabaseHelper(this);

        initViews();

        // Lấy phòng theo mã được truyền sang
        String roomCode = getIntent().getStringExtra(EXTRA_ROOM_CODE);
        room = roomCode == null ? null : dbHelper.getRoomByCode(roomCode);

        if (room == null) {
            Toast.makeText(this, R.string.room_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showRoom();
        loadEquipments();
        loadHistory();
    }

    // 1. Ánh xạ giao diện
    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnSaveStatus = findViewById(R.id.btnSaveStatus);

        txtDetailCode = findViewById(R.id.txtDetailCode);
        txtDetailStatus = findViewById(R.id.txtDetailStatus);
        txtDetailFloor = findViewById(R.id.txtDetailFloor);
        rgStatus = findViewById(R.id.rgStatus);

        layoutEquipments = findViewById(R.id.layoutEquipments);
        txtEquipmentSummary = findViewById(R.id.txtEquipmentSummary);
        txtEquipmentHint = findViewById(R.id.txtEquipmentHint);
        layoutHistory = findViewById(R.id.layoutHistory);

        findViewById(R.id.btnAddEquipment).setOnClickListener(v -> showEquipmentDialog(null));

        btnBack.setOnClickListener(v -> finish());
        btnSaveStatus.setOnClickListener(v -> saveStatus());
    }

    // 2. Hiển thị thông tin phòng
    private void showRoom() {
        String status = room.getStatus();

        txtDetailCode.setText(getString(R.string.overview_room_name, room.getRoomCode()));
        txtDetailFloor.setText(getString(R.string.overview_floor, room.getFloor()));

        StatusUtils.applyBadge(txtDetailStatus, status);


        // Chọn sẵn trạng thái hiện tại
        if (Constants.EMPTY.equals(status)) {
            rgStatus.check(R.id.rbEmpty);
        } else if (Constants.IN_USE.equals(status)) {
            rgStatus.check(R.id.rbInUse);
        } else if (Constants.MAINTENANCE.equals(status)) {
            rgStatus.check(R.id.rbMaintenance);
        } else {
            rgStatus.clearCheck();
        }
    }

    // 3. Lưu trạng thái mới vào SQLite
    private void saveStatus() {
        int checkedId = rgStatus.getCheckedRadioButtonId();
        String newStatus;

        if (checkedId == R.id.rbEmpty) {
            newStatus = Constants.EMPTY;
        } else if (checkedId == R.id.rbInUse) {
            newStatus = Constants.IN_USE;
        } else if (checkedId == R.id.rbMaintenance) {
            newStatus = Constants.MAINTENANCE;
        } else {
            Toast.makeText(this, R.string.choose_status, Toast.LENGTH_SHORT).show();
            return;
        }

        if (newStatus.equals(room.getStatus())) {
            Toast.makeText(this, R.string.status_unchanged, Toast.LENGTH_SHORT).show();
            return;
        }

        int result = dbHelper.updateRoomStatus(room.getRoomCode(), newStatus);

        if (result > 0) {
            room.setStatus(newStatus);
            showRoom();
            loadHistory();
            Toast.makeText(this,
                    getString(R.string.status_updated, room.getRoomCode(),
                            getString(StatusUtils.label(newStatus))),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // 4. Hiển thị danh sách thiết bị của phòng
    private void loadEquipments() {
        ArrayList<Equipment> equipments = dbHelper.getEquipmentsByRoom(room.getRoomCode());

        layoutEquipments.removeAllViews();

        int total = 0;
        int broken = 0;
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < equipments.size(); i++) {
            Equipment equipment = equipments.get(i);
            total += equipment.getQuantity();
            if (Constants.BROKEN.equals(equipment.getCondition())) {
                broken += equipment.getQuantity();
            }

            // Đường kẻ ngăn cách giữa các dòng
            if (i > 0) {
                View divider = new View(this);
                divider.setBackgroundColor(getColor(R.color.border));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                lp.setMarginStart(dp(16));
                lp.setMarginEnd(dp(16));
                layoutEquipments.addView(divider, lp);
            }

            View row = inflater.inflate(R.layout.item_equipment, layoutEquipments, false);

            TextView txtName = row.findViewById(R.id.txtEquipmentName);
            TextView txtQuantity = row.findViewById(R.id.txtEquipmentQuantity);
            TextView txtCondition = row.findViewById(R.id.txtEquipmentCondition);

            txtName.setText(equipment.getName());
            txtQuantity.setText(getString(R.string.quantity_fmt, equipment.getQuantity()));
            StatusUtils.applyConditionBadge(txtCondition, equipment.getCondition());

            row.setOnClickListener(v -> showEquipmentDialog(equipment));
            row.setOnLongClickListener(v -> {
                confirmDeleteEquipment(equipment);
                return true;
            });

            layoutEquipments.addView(row);
        }

        if (equipments.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.no_equipment);
            empty.setTextColor(getColor(R.color.text_gray));
            empty.setTextSize(14);
            empty.setPadding(dp(16), dp(16), dp(16), dp(16));
            layoutEquipments.addView(empty);
            txtEquipmentHint.setVisibility(View.GONE);
        } else {
            txtEquipmentHint.setVisibility(View.VISIBLE);
        }

        String summary = getString(R.string.equipment_summary, equipments.size(), total);
        if (broken > 0) {
            summary = getString(R.string.equipment_summary_broken, summary, broken);
        }
        txtEquipmentSummary.setText(summary);
    }

    // 5. Hộp thoại thêm mới (equipment == null) hoặc sửa thiết bị
    private void showEquipmentDialog(Equipment equipment) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_equipment, null);

        AutoCompleteTextView edtName = dialogView.findViewById(R.id.edtEquipmentName);
        EditText edtQuantity = dialogView.findViewById(R.id.edtEquipmentQuantity);
        RadioGroup rgCondition = dialogView.findViewById(R.id.rgCondition);

        edtName.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, EQUIPMENT_SUGGESTIONS));

        boolean isEdit = equipment != null;

        if (isEdit) {
            edtName.setText(equipment.getName());
            edtName.setSelection(edtName.getText().length());
            edtQuantity.setText(String.valueOf(equipment.getQuantity()));
            rgCondition.check(Constants.BROKEN.equals(equipment.getCondition())
                    ? R.id.rbBroken : R.id.rbGood);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? getString(R.string.edit_equipment)
                        : getString(R.string.equipment_add_title, room.getRoomCode()))
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .show();

        // Tự xử lý nút Lưu để không đóng hộp thoại khi nhập sai
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String quantityText = edtQuantity.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                edtName.setError(getString(R.string.name_required));
                edtName.requestFocus();
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                quantity = 0;
            }

            if (quantity <= 0) {
                edtQuantity.setError(getString(R.string.positive_quantity));
                edtQuantity.requestFocus();
                return;
            }

            String condition = rgCondition.getCheckedRadioButtonId() == R.id.rbBroken
                    ? Constants.BROKEN : Constants.GOOD;

            boolean success;
            if (isEdit) {
                equipment.setName(name);
                equipment.setQuantity(quantity);
                equipment.setCondition(condition);
                success = dbHelper.updateEquipment(equipment) > 0;
            } else {
                Equipment newEquipment = new Equipment(
                        0, room.getRoomCode(), name, quantity, condition);
                success = dbHelper.addEquipment(newEquipment) != -1;
            }

            if (success) {
                Toast.makeText(this,
                        isEdit ? R.string.equipment_updated : R.string.equipment_added,
                        Toast.LENGTH_SHORT).show();
                loadEquipments();
                dialog.dismiss();
            } else {
                Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 6. Xác nhận xóa thiết bị
    private void confirmDeleteEquipment(Equipment equipment) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_equipment)
                .setMessage(getString(R.string.confirm_delete, equipment.getName(), room.getRoomCode()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    if (dbHelper.deleteEquipment(equipment.getId()) > 0) {
                        Toast.makeText(this, R.string.equipment_deleted, Toast.LENGTH_SHORT).show();
                        loadEquipments();
                    }
                })
                .show();
    }

    // 7. Hiển thị lịch sử thay đổi trạng thái (10 lần gần nhất)
    private void loadHistory() {
        ArrayList<StatusHistory> histories =
                dbHelper.getStatusHistory(room.getRoomCode(), HISTORY_LIMIT);

        layoutHistory.removeAllViews();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < histories.size(); i++) {
            StatusHistory history = histories.get(i);

            if (i > 0) {
                View divider = new View(this);
                divider.setBackgroundColor(getColor(R.color.border));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                lp.setMarginStart(dp(16));
                lp.setMarginEnd(dp(16));
                layoutHistory.addView(divider, lp);
            }

            View row = inflater.inflate(R.layout.item_history, layoutHistory, false);

            StatusUtils.applyBadge(row.findViewById(R.id.txtHistoryOld), history.getOldStatus());
            StatusUtils.applyBadge(row.findViewById(R.id.txtHistoryNew), history.getNewStatus());

            TextView txtTime = row.findViewById(R.id.txtHistoryTime);
            txtTime.setText(format.format(new Date(history.getChangedAt())));

            layoutHistory.addView(row);
        }

        if (histories.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.no_history);
            empty.setTextColor(getColor(R.color.text_gray));
            empty.setTextSize(14);
            empty.setPadding(dp(16), dp(16), dp(16), dp(16));
            layoutHistory.addView(empty);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
