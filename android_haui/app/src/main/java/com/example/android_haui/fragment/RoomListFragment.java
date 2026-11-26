
package com.example.android_haui.fragment;

import com.example.android_haui.R;
import com.example.android_haui.activity.RoomDetailActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.material.chip.Chip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_haui.adapter.RoomAdapter;
import com.example.android_haui.database.DatabaseHelper;
import com.example.android_haui.model.Room;
import com.example.android_haui.utils.Constants;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class RoomListFragment extends Fragment {

    private DatabaseHelper dbHelper;

    private EditText edtSearchRoom;

    private ListView lvRoomList;

    private TextView txtRoomCount;
    private TextView txtEmpty;

    private Chip btnFilterAll;
    private Chip btnFilterEmpty;
    private Chip btnFilterInUse;
    private Chip btnFilterMaintenance;

    private RoomAdapter adapter;

    private ArrayList<Room> allRooms;
    private ArrayList<Room> filteredRooms;

    // Trạng thái đang được chọn
    private String currentFilter = "ALL";
    private ImageButton btnClearSearch;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_room_list,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            currentFilter = savedInstanceState.getString("room_status", "ALL");
        }

        // Khởi tạo database
        dbHelper = new DatabaseHelper(requireContext());

        // Ánh xạ giao diện
        initViews(view);

        // Khởi tạo danh sách
        allRooms = new ArrayList<>();
        filteredRooms = new ArrayList<>();

        // Khởi tạo Adapter
        adapter = new RoomAdapter(
                requireContext(),
                filteredRooms
        );

        lvRoomList.setAdapter(adapter);

        // Lấy dữ liệu từ SQLite
        loadRooms();

        // Xử lý tìm kiếm
        setupSearch();

        // Xử lý bộ lọc
        setupFilters();
        updateFilterUI();

        // Sự kiện chọn phòng
        lvRoomList.setOnItemClickListener(
                (parent, itemView, position, id) -> {

                    Room room = filteredRooms.get(position);

                    // Mở màn hình chi tiết phòng
                    RoomDetailActivity.open(
                            requireContext(),
                            room.getRoomCode()
                    );
                }
        );
    }

    // 1. Ánh xạ các thành phần giao diện
    private void initViews(View view) {

        edtSearchRoom = view.findViewById(
                R.id.edtSearchRoom);
        btnClearSearch = view.findViewById(R.id.btnClearRoomSearch);

        lvRoomList = view.findViewById(
                R.id.lvRoomList);

        txtRoomCount = view.findViewById(
                R.id.txtRoomCount);

        txtEmpty = view.findViewById(
                R.id.txtEmpty);

        btnFilterAll = view.findViewById(
                R.id.btnFilterAll);

        btnFilterEmpty = view.findViewById(
                R.id.btnFilterEmpty);

        btnFilterInUse = view.findViewById(
                R.id.btnFilterInUse);

        btnFilterMaintenance = view.findViewById(
                R.id.btnFilterMaintenance);
    }

    // 2. Lấy toàn bộ phòng từ SQLite
    private void loadRooms() {

        allRooms.clear();

        allRooms.addAll(dbHelper.getAllRooms());

        filterRooms();
    }

    // 3. Tìm kiếm phòng theo mã
    private void setupSearch() {
        btnClearSearch.setOnClickListener(v -> edtSearchRoom.setText(""));
        edtSearchRoom.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            androidx.core.view.WindowInsetsControllerCompat controller =
                    WindowCompat.getInsetsController(requireActivity().getWindow(), edtSearchRoom);
            controller.hide(WindowInsetsCompat.Type.ime());
            edtSearchRoom.clearFocus();
            return true;
        });

        edtSearchRoom.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        filterRooms();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
    }

    // 4. Thiết lập bộ lọc trạng thái
    private void setupFilters() {

        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterUI();
            filterRooms();
        });

        btnFilterEmpty.setOnClickListener(v -> {
            currentFilter = Constants.EMPTY;
            updateFilterUI();
            filterRooms();
        });

        btnFilterInUse.setOnClickListener(v -> {
            currentFilter = Constants.IN_USE;
            updateFilterUI();
            filterRooms();
        });

        btnFilterMaintenance.setOnClickListener(v -> {
            currentFilter = Constants.MAINTENANCE;
            updateFilterUI();
            filterRooms();
        });
    }

    // 5. Cập nhật giao diện bộ lọc
    private void updateFilterUI() {

        Chip selected = btnFilterAll;
        if (Constants.EMPTY.equals(currentFilter)) selected = btnFilterEmpty;
        if (Constants.IN_USE.equals(currentFilter)) selected = btnFilterInUse;
        if (Constants.MAINTENANCE.equals(currentFilter)) selected = btnFilterMaintenance;
        selected.setChecked(true);
    }

    // 6. Tìm kiếm và lọc danh sách phòng
    private void filterRooms() {

        String keyword = edtSearchRoom.getText()
                .toString()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

        filteredRooms.clear();

        for (Room room : allRooms) {

            // Kiểm tra mã phòng
            boolean matchKeyword = room.getRoomCode()
                    .toLowerCase(java.util.Locale.ROOT)
                    .contains(keyword);

            // Kiểm tra trạng thái
            boolean matchStatus =
                    currentFilter.equals("ALL")
                            || room.getStatus().equals(currentFilter);

            if (matchKeyword && matchStatus) {
                filteredRooms.add(room);
            }
        }

        btnClearSearch.setVisibility(
                edtSearchRoom.length() > 0 ? View.VISIBLE : View.GONE);

        // Cập nhật Adapter
        adapter.notifyDataSetChanged();

        // Cập nhật số lượng phòng
        updateRoomCount();

        // Hiển thị thông báo nếu không có dữ liệu
        if (filteredRooms.isEmpty()) {

            txtEmpty.setVisibility(View.VISIBLE);
            lvRoomList.setVisibility(View.GONE);

        } else {

            txtEmpty.setVisibility(View.GONE);
            lvRoomList.setVisibility(View.VISIBLE);
        }
    }

    // 7. Cập nhật số lượng phòng
    private void updateRoomCount() {

        txtRoomCount.setText(
                getString(R.string.room_result_count, filteredRooms.size())
        );
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("room_keyword", edtSearchRoom.getText().toString());
        outState.putString("room_status", currentFilter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            edtSearchRoom.setText(savedInstanceState.getString("room_keyword", ""));
            edtSearchRoom.setSelection(edtSearchRoom.length());
        }
        updateFilterUI();
        filterRooms();
    }

    // Làm mới dữ liệu khi quay lại màn hình
    @Override
    public void onResume() {
        super.onResume();

        if (dbHelper != null && allRooms != null) {
            allRooms.clear();
            allRooms.addAll(dbHelper.getAllRooms());
            filterRooms();
        }
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
