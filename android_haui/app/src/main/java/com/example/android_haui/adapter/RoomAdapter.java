
package com.example.android_haui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android_haui.R;
import com.example.android_haui.model.Room;
import com.example.android_haui.utils.Constants;

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

        TextView txtCode =
                convertView.findViewById(R.id.txtRoomCode);

        TextView txtName =
                convertView.findViewById(R.id.txtRoomName);

        TextView txtFloor =
                convertView.findViewById(R.id.txtRoomFloor);

        TextView txtStatus =
                convertView.findViewById(R.id.txtRoomStatus);

        Room room = rooms.get(position);

        // Hiển thị thông tin phòng
        txtCode.setText(room.getRoomCode());

        txtName.setText("Phòng học " + room.getRoomCode());

        txtFloor.setText("Tầng " + room.getFloor());

        // Tạo nền trạng thái
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(20);

        String status = room.getStatus();

        if (Constants.EMPTY.equals(status)) {

            txtStatus.setText("Trống");

            txtStatus.setTextColor(
                    Color.parseColor("#239653"));

            background.setColor(
                    Color.parseColor("#E3F5EA"));

        } else if (Constants.IN_USE.equals(status)) {

            txtStatus.setText("Đang sử dụng");

            txtStatus.setTextColor(
                    Color.parseColor("#3264BB"));

            background.setColor(
                    Color.parseColor("#E5EEFF"));

        } else if (Constants.MAINTENANCE.equals(status)) {

            txtStatus.setText("Bảo trì");

            txtStatus.setTextColor(
                    Color.parseColor("#B77B18"));

            background.setColor(
                    Color.parseColor("#FFF1D5"));

        } else {

            txtStatus.setText("Không xác định");

            txtStatus.setTextColor(Color.DKGRAY);

            background.setColor(
                    Color.parseColor("#EEEEEE"));
        }

        txtStatus.setBackground(background);

        return convertView;
    }

    // Cập nhật dữ liệu hiển thị
    public void updateData(ArrayList<Room> newRooms) {
        rooms.clear();
        rooms.addAll(newRooms);
        notifyDataSetChanged();
    }
}
