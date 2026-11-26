
package com.example.android_haui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android_haui.R;
import com.example.android_haui.model.Room;
import com.example.android_haui.utils.StatusUtils;

import java.util.ArrayList;

public class RoomAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Room> rooms;

    public RoomAdapter(Context context, ArrayList<Room> rooms) {
        this.context = context;
        this.rooms = rooms;
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public Object getItem(int position) {
        return rooms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rooms.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView,
                        ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_room, parent, false);
        }

        Room room = rooms.get(position);
        TextView code = convertView.findViewById(R.id.txtRoomCode);
        TextView floor = convertView.findViewById(R.id.txtRoomFloor);
        code.setText(context.getString(R.string.overview_room_name, room.getRoomCode()));
        floor.setText(context.getString(R.string.overview_floor, room.getFloor()));
        StatusUtils.applyBadge(convertView.findViewById(R.id.txtRoomStatus), room.getStatus());

        return convertView;
    }

    // Cập nhật dữ liệu hiển thị
    public void updateData(ArrayList<Room> newRooms) {
        rooms.clear();
        rooms.addAll(newRooms);
        notifyDataSetChanged();
    }
}
