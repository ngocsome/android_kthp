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
    private TextView txtDetailName;
    private TextView txtDetailRoomCode;
    private TextView txtDetailBuilding;
    private TextView txtDetailFloor;
    private TextView txtDetailStatusText;
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
            Toast.makeText(this, "Không tìm thấy phòng", Toast.LENGTH_SHORT).show();
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
        txtDetailName = findViewById(R.id.txtDetailName);
        txtDetailRoomCode = findViewById(R.id.txtDetailRoomCode);
        txtDetailBuilding = findViewById(R.id.txtDetailBuilding);
        txtDetailFloor = findViewById(R.id.txtDetailFloor);
        txtDetailStatusText = findViewById(R.id.txtDetailStatusText);
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

        txtDetailCode.setText(room.getRoomCode());
        txtDetailName.setText("Phòng học " + room.getRoomCode());
        txtDetailRoomCode.setText(room.getRoomCode());
        txtDetailBuilding.setText("Khu A - CS1 HaUI");
        txtDetailFloor.setText("Tầng " + room.getFloor());

        StatusUtils.applyBadge(txtDetailStatus, status);

        txtDetailStatusText.setText(StatusUtils.getLabel(status));
        txtDetailStatusText.setTextColor(StatusUtils.getTextColor(status));

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
            Toast.makeText(this, "Vui lòng chọn trạng thái", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newStatus.equals(room.getStatus())) {
            Toast.makeText(this, "Trạng thái không thay đổi", Toast.LENGTH_SHORT).show();
            return;
        }

        int result = dbHelper.updateRoomStatus(room.getRoomCode(), newStatus);

        if (result > 0) {
            room.setStatus(newStatus);
            showRoom();
            loadHistory();
            Toast.makeText(this,
                    "Đã cập nhật phòng " + room.getRoomCode() + ": "
                            + StatusUtils.getLabel(newStatus),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
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
                divider.setBackgroundColor(0xFFEEF0F5);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                lp.setMarginStart(dp(16));
                lp.setMarginEnd(dp(16));
                layoutEquipments.addView(divider, lp);
            }

            View row = inflater.inflate(R.layout.item_equipment, layoutEquipments, false);

            TextView txtName = row.findViewById(R.id.txtEquipmentName);
            TextView txtQuantity = row.findViewById(R.id.txtEquipmentQuantity);
            TextView txtCondition = row.findViewById(R.id.txtEquipmentCondition);

            txtName.setText(equipment.getName());
            txtQuantity.setText("Số lượng: " + equipment.getQuantity());
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
            empty.setText("Chưa có thiết bị nào");
            empty.setTextColor(0xFF8992A5);
            empty.setTextSize(14);
            empty.setPadding(dp(16), dp(16), dp(16), dp(16));
            layoutEquipments.addView(empty);
            txtEquipmentHint.setVisibility(View.GONE);
        } else {
            txtEquipmentHint.setVisibility(View.VISIBLE);
        }

        String summary = equipments.size() + " loại · " + total + " thiết bị";
        if (broken > 0) {
            summary += " · " + broken + " hỏng";
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
                .setTitle(isEdit ? "Sửa thiết bị" : "Thêm thiết bị - Phòng " + room.getRoomCode())
                .setView(dialogView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .show();

        // Tự xử lý nút Lưu để không đóng hộp thoại khi nhập sai
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String quantityText = edtQuantity.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                edtName.setError("Vui lòng nhập tên thiết bị");
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                quantity = 0;
            }

            if (quantity <= 0) {
                edtQuantity.setError("Số lượng phải lớn hơn 0");
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
                        isEdit ? "Đã cập nhật thiết bị" : "Đã thêm thiết bị",
                        Toast.LENGTH_SHORT).show();
                loadEquipments();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Lưu thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 6. Xác nhận xóa thiết bị
    private void confirmDeleteEquipment(Equipment equipment) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa thiết bị")
                .setMessage("Xóa \"" + equipment.getName() + "\" khỏi phòng "
                        + room.getRoomCode() + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    if (dbHelper.deleteEquipment(equipment.getId()) > 0) {
                        Toast.makeText(this, "Đã xóa thiết bị", Toast.LENGTH_SHORT).show();
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
                divider.setBackgroundColor(0xFFEEF0F5);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
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
            empty.setText("Chưa có thay đổi nào");
            empty.setTextColor(0xFF8992A5);
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
