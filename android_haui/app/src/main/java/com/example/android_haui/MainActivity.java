package com.example.android_haui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_haui.database.DatabaseHelper;
import com.example.android_haui.utils.Constants;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hiển thị giao diện chính
        setContentView(R.layout.activity_main);

        // Khởi tạo database
        dbHelper = new DatabaseHelper(this);

        // Kiểm tra database
        checkDatabase();
    }

    private void checkDatabase() {

        // Lấy tổng số phòng
        int totalRooms = dbHelper.getTotalRoomCount();

        // Đếm phòng theo trạng thái
        int emptyRooms = dbHelper.countRoomsByStatus(Constants.EMPTY);

        int inUseRooms = dbHelper.countRoomsByStatus(Constants.IN_USE);

        int maintenanceRooms = dbHelper.countRoomsByStatus(Constants.MAINTENANCE);

        // In kết quả ra Logcat
        Log.d("DATABASE_TEST", "Tổng số phòng: " + totalRooms);
        Log.d("DATABASE_TEST", "Phòng trống: " + emptyRooms);
        Log.d("DATABASE_TEST", "Đang sử dụng: " + inUseRooms);
        Log.d("DATABASE_TEST", "Đang bảo trì: " + maintenanceRooms);

        // Kiểm tra kết quả
        if (totalRooms == 15) {
            Toast.makeText(
                    this,
                    "Khởi tạo database thành công!",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    "Database chưa khởi tạo đúng!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
