package com.example.android_haui.activity;

import com.example.android_haui.R;
import com.example.android_haui.fragment.RoomListFragment;
import com.example.android_haui.fragment.OverviewFragment;
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
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });

        bottomNav = findViewById(R.id.bottomNav);

        // Chuyển màn hình khi chọn mục trên thanh điều hướng
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_overview) {
                showFragment(new OverviewFragment());
            } else if (id == R.id.nav_rooms) {
                showFragment(new RoomListFragment());

            } else if (id == R.id.nav_status) {
                showFragment(new StatusFragment());
            } else if (id == R.id.nav_statistics) {
                showFragment(new StatisticsFragment());
            }
            return true;
        });

        // Chọn lại mục đang mở thì không làm gì
        bottomNav.setOnItemReselectedListener(item -> { });

        // Mặc định hiển thị tổng quan; giữ Fragment được khôi phục khi xoay màn hình.
        if (savedInstanceState == null) {
            showFragment(new OverviewFragment());
        }
    }

    public void selectDestination(int itemId) {
        bottomNav.setSelectedItemId(itemId);
    }

    public void openStatistics(String status) {
        bottomNav.setSelectedItemId(R.id.nav_statistics);
        StatisticsFragment fragment = new StatisticsFragment();
        Bundle arguments = new Bundle();
        arguments.putString("initial_status", status);
        fragment.setArguments(arguments);
        showFragment(fragment);
    }

    // Thay Fragment đang hiển thị
    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
