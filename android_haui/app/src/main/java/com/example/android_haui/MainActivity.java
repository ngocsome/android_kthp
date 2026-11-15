
package com.example.android_haui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hiển thị giao diện chính
        setContentView(R.layout.activity_main);

        // Hiển thị danh sách phòng
        if (savedInstanceState == null) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragmentContainer,
                            new RoomListFragment()
                    )
                    .commit();
        }
    }
}
