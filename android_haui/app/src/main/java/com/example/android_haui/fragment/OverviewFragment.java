package com.example.android_haui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.android_haui.R;
import com.example.android_haui.activity.MainActivity;
import com.example.android_haui.activity.RoomDetailActivity;
import com.example.android_haui.database.DatabaseHelper;
import com.example.android_haui.model.Room;
import com.example.android_haui.utils.StatusUtils;
import java.util.ArrayList;
import java.util.HashSet;

public class OverviewFragment extends Fragment {
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        database = new DatabaseHelper(requireContext());
        view.findViewById(R.id.overviewRooms).setOnClickListener(v -> navigate(R.id.nav_rooms));
        view.findViewById(R.id.overviewStats).setOnClickListener(v -> navigate(R.id.nav_statistics));
        view.findViewById(R.id.overviewAll).setOnClickListener(v -> navigate(R.id.nav_status));
        int[] cards = {R.id.overviewStatus0, R.id.overviewStatus1, R.id.overviewStatus2};
        for (int i = 0; i < cards.length; i++) {
            final String status = StatusUtils.STATUSES[i];
            view.findViewById(cards[i]).setOnClickListener(v ->
                    ((MainActivity) requireActivity()).openStatistics(status));
        }
    }

    private void navigate(int id) {
        ((MainActivity) requireActivity()).selectDestination(id);
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view == null || database == null) return;
        ArrayList<Room> rooms = database.getAllRooms();
        HashSet<Integer> floors = new HashSet<>();
        for (Room room : rooms) floors.add(room.getFloor());
        ((TextView) view.findViewById(R.id.overviewTotal)).setText(String.valueOf(rooms.size()));
        ((TextView) view.findViewById(R.id.overviewFloors)).setText(
                getString(R.string.overview_floors, floors.size()));
        int[] counts = {R.id.overviewCount0, R.id.overviewCount1, R.id.overviewCount2};
        for (int i = 0; i < counts.length; i++) {
            ((TextView) view.findViewById(counts[i])).setText(
                    String.valueOf(database.countRoomsByStatus(StatusUtils.STATUSES[i])));
        }
        LinearLayout list = view.findViewById(R.id.overviewRecent);
        list.removeAllViews();
        ArrayList<Room> recent = database.getRecentlyUpdatedRooms(3);
        view.findViewById(R.id.overviewNoHistory).setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
        for (Room room : recent) {
            View item = getLayoutInflater().inflate(R.layout.item_overview_room, list, false);
            ((TextView) item.findViewById(R.id.recentCode)).setText(
                    getString(R.string.overview_room_name, room.getRoomCode()));
            ((TextView) item.findViewById(R.id.recentFloor)).setText(
                    getString(R.string.overview_floor, room.getFloor()));
            StatusUtils.applyBadge(item.findViewById(R.id.recentStatus), room.getStatus());
            item.setOnClickListener(v -> RoomDetailActivity.open(requireContext(), room.getRoomCode()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = dp(16);
            list.addView(item, params);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (database != null) database.close();
        database = null;
    }
}
