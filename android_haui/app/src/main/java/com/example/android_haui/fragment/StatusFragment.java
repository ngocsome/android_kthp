package com.example.android_haui.fragment;

import com.example.android_haui.R;
import com.example.android_haui.activity.RoomDetailActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.chip.Chip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_haui.adapter.RoomAdapter;
import com.example.android_haui.database.DatabaseHelper;
import com.example.android_haui.model.Room;
import com.example.android_haui.utils.StatusUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

// Màn hình sửa nhanh trạng thái phòng
public class StatusFragment extends Fragment {

    private DatabaseHelper dbHelper;

    private ListView lvStatusRooms;
    private TextView txtStatusCount;
    private Chip[] floorChips;

    private RoomAdapter adapter;
    private final ArrayList<Room> rooms = new ArrayList<>();

    // 0 = tất cả tầng, còn lại là số tầng
    private final int[] floors = {0, 14, 15, 16};
    private int selectedFloorIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());

        lvStatusRooms = view.findViewById(R.id.lvStatusRooms);
        txtStatusCount = view.findViewById(R.id.txtStatusCount);

        floorChips = new Chip[]{
                view.findViewById(R.id.btnFloorAll),
                view.findViewById(R.id.btnFloor14),
                view.findViewById(R.id.btnFloor15),
                view.findViewById(R.id.btnFloor16)
        };

        if (savedInstanceState != null) {
            selectedFloorIndex = savedInstanceState.getInt("floor_filter", 0);
        }
        floorChips[selectedFloorIndex].setChecked(true);
        lvStatusRooms.setEmptyView(view.findViewById(R.id.txtManagementEmpty));

        // Chọn tầng
        for (int i = 0; i < floorChips.length; i++) {
            final int index = i;
            floorChips[i].setOnClickListener(v -> {
                selectedFloorIndex = index;
                loadRooms();
            });
        }

        adapter = new RoomAdapter(requireContext(), rooms);
        lvStatusRooms.setAdapter(adapter);

        // Chạm vào phòng -> hiện hộp thoại đổi trạng thái
        lvStatusRooms.setOnItemClickListener((parent, itemView, position, id) ->
                showChangeStatusDialog(rooms.get(position)));

        // Nhấn giữ -> mở chi tiết phòng
        lvStatusRooms.setOnItemLongClickListener((parent, itemView, position, id) -> {
            RoomDetailActivity.open(requireContext(), rooms.get(position).getRoomCode());
            return true;
        });
    }

    // Lấy danh sách phòng theo tầng đang chọn
    private void loadRooms() {
        if (dbHelper == null) return;

        rooms.clear();

        int floor = floors[selectedFloorIndex];
        if (floor == 0) {
            rooms.addAll(dbHelper.getAllRooms());
        } else {
            rooms.addAll(dbHelper.getRoomsByFloor(floor));
        }

        adapter.notifyDataSetChanged();
        txtStatusCount.setText(getString(R.string.room_result_count, rooms.size()));
    }

    // Hộp thoại chọn trạng thái mới
    private void showChangeStatusDialog(Room room) {
        String[] labels = new String[StatusUtils.STATUSES.length];
        int checked = -1;

        for (int i = 0; i < StatusUtils.STATUSES.length; i++) {
            labels[i] = getString(StatusUtils.label(StatusUtils.STATUSES[i]));
            if (StatusUtils.STATUSES[i].equals(room.getStatus())) {
                checked = i;
            }
        }

        final int[] selected = {checked};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.room_short, room.getRoomCode()))
                .setSingleChoiceItems(labels, checked, (dialog, which) -> selected[0] = which)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    if (selected[0] < 0) return;

                    String newStatus = StatusUtils.STATUSES[selected[0]];

                    if (newStatus.equals(room.getStatus())) {
                        Toast.makeText(requireContext(),
                                R.string.status_unchanged, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (dbHelper != null
                            && dbHelper.updateRoomStatus(room.getRoomCode(), newStatus) > 0) {
                        Toast.makeText(requireContext(),
                                getString(R.string.status_updated, room.getRoomCode(),
                                        getString(StatusUtils.label(newStatus))),
                                Toast.LENGTH_SHORT).show();
                        loadRooms();
                    } else {
                        Toast.makeText(requireContext(),
                                R.string.update_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("floor_filter", selectedFloorIndex);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRooms();
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
