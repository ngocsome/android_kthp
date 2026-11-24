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
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_haui.adapter.RoomAdapter;
import com.example.android_haui.database.DatabaseHelper;
import com.example.android_haui.model.Room;
import com.example.android_haui.utils.StatusUtils;

import java.util.ArrayList;

// Màn hình tìm kiếm phòng theo mã phòng và tầng
public class SearchFragment extends Fragment {

    private DatabaseHelper dbHelper;

    private EditText edtSearchKeyword;
    private ListView lvSearchResult;
    private TextView txtSearchResult;
    private TextView txtSearchEmpty;
    private TextView[] floorChips;

    private RoomAdapter adapter;
    private final ArrayList<Room> results = new ArrayList<>();

    // 0 = tất cả tầng
    private final int[] floors = {0, 14, 15, 16};
    private int selectedFloorIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());

        edtSearchKeyword = view.findViewById(R.id.edtSearchKeyword);
        lvSearchResult = view.findViewById(R.id.lvSearchResult);
        txtSearchResult = view.findViewById(R.id.txtSearchResult);
        txtSearchEmpty = view.findViewById(R.id.txtSearchEmpty);

        floorChips = new TextView[]{
                view.findViewById(R.id.btnSearchFloorAll),
                view.findViewById(R.id.btnSearchFloor14),
                view.findViewById(R.id.btnSearchFloor15),
                view.findViewById(R.id.btnSearchFloor16)
        };

        adapter = new RoomAdapter(requireContext(), results);
        lvSearchResult.setAdapter(adapter);

        // Tìm kiếm ngay khi gõ
        edtSearchKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Chọn tầng
        for (int i = 0; i < floorChips.length; i++) {
            final int index = i;
            floorChips[i].setOnClickListener(v -> {
                selectedFloorIndex = index;
                StatusUtils.updateChips(floorChips, selectedFloorIndex);
                search();
            });
        }

        // Chạm vào kết quả -> xem chi tiết
        lvSearchResult.setOnItemClickListener((parent, itemView, position, id) ->
                RoomDetailActivity.open(requireContext(), results.get(position).getRoomCode()));
    }

    // Tìm phòng trong SQLite theo mã, sau đó lọc theo tầng
    private void search() {
        if (dbHelper == null) return;

        String keyword = edtSearchKeyword.getText().toString().trim();
        int floor = floors[selectedFloorIndex];

        results.clear();

        for (Room room : dbHelper.searchRooms(keyword)) {
            if (floor == 0 || room.getFloor() == floor) {
                results.add(room);
            }
        }

        adapter.notifyDataSetChanged();

        txtSearchResult.setText("Kết quả: " + results.size() + " phòng");

        boolean empty = results.isEmpty();
        txtSearchEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        lvSearchResult.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        search();
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
