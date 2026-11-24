package com.example.android_haui.activity;

import com.example.android_haui.R;
import com.example.android_haui.fragment.RoomListFragment;
import com.example.android_haui.fragment.SearchFragment;
import com.example.android_haui.fragment.StatisticsFragment;
import com.example.android_haui.fragment.StatusFragment;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hiển thị giao diện chính
        setContentView(R.layout.activity_main);

        // Chừa khoảng trống cho thanh trạng thái / thanh điều hướng hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        bottomNav = findViewById(R.id.bottomNav);

        // Chuyển màn hình khi chọn mục trên thanh điều hướng
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_rooms) {
                showFragment(new RoomListFragment());
            } else if (id == R.id.nav_search) {
                showFragment(new SearchFragment());
            } else if (id == R.id.nav_status) {
                showFragment(new StatusFragment());
            } else if (id == R.id.nav_statistics) {
                showFragment(new StatisticsFragment());
            }
            return true;
        });

        // Chọn lại mục đang mở thì không làm gì
        bottomNav.setOnItemReselectedListener(item -> { });

        // Mặc định hiển thị danh sách phòng
        if (savedInstanceState == null) {
            showFragment(new RoomListFragment());
        }
    }

    // Thay Fragment đang hiển thị
    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
