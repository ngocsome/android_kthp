package com.example.android_haui.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android_haui.model.Room;
import com.example.android_haui.utils.Constants;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RoomManagement.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ROOM = "Room";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 1. Tạo database và bảng
    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + TABLE_ROOM + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "room_code TEXT NOT NULL UNIQUE, " +
                "floor INTEGER NOT NULL, " +
                "status TEXT NOT NULL)";

        db.execSQL(sql);

        insertInitialRooms(db);
    }

    // 2. Thêm 15 phòng ban đầu
    private void insertInitialRooms(SQLiteDatabase db) {

        for (int floor = 14; floor <= 16; floor++) {

            for (int i = 1; i <= 5; i++) {

                String roomCode = floor + "0" + i;

                ContentValues values = new ContentValues();

                values.put("room_code", roomCode);
                values.put("floor", floor);
                values.put("status", Constants.EMPTY);

                db.insert(TABLE_ROOM, null, values);
            }
        }
    }

    // 3. Lấy tất cả phòng
    public ArrayList<Room> getAllRooms() {

        ArrayList<Room> rooms = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM Room ORDER BY floor, room_code",
                null
        );

        while (cursor.moveToNext()) {
            rooms.add(cursorToRoom(cursor));
        }

        cursor.close();

        return rooms;
    }

    // 4. Lấy phòng theo tầng
    public ArrayList<Room> getRoomsByFloor(int floor) {

        ArrayList<Room> rooms = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM Room WHERE floor = ? ORDER BY room_code",
                new String[]{String.valueOf(floor)}
        );

        while (cursor.moveToNext()) {
            rooms.add(cursorToRoom(cursor));
        }

        cursor.close();

        return rooms;
    }

    // 5. Lấy chi tiết phòng theo mã
    public Room getRoomByCode(String roomCode) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM Room WHERE room_code = ?",
                new String[]{roomCode}
        );

        Room room = null;

        if (cursor.moveToFirst()) {
            room = cursorToRoom(cursor);
        }

        cursor.close();

        return room;
    }

    // 6. Cập nhật trạng thái phòng
    public int updateRoomStatus(String roomCode, String status) {

        if (!Constants.EMPTY.equals(status)
                && !Constants.IN_USE.equals(status)
                && !Constants.MAINTENANCE.equals(status)) {
            return 0;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("status", status);

        return db.update(
                TABLE_ROOM,
                values,
                "room_code = ?",
                new String[]{roomCode}
        );
    }

    // 7. Tìm kiếm phòng
    public ArrayList<Room> searchRooms(String keyword) {

        ArrayList<Room> rooms = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM Room WHERE room_code LIKE ? ORDER BY room_code",
                new String[]{"%" + keyword + "%"}
        );

        while (cursor.moveToNext()) {
            rooms.add(cursorToRoom(cursor));
        }

        cursor.close();

        return rooms;
    }

    // 8. Đếm phòng theo trạng thái
    public int countRoomsByStatus(String status) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM Room WHERE status = ?",
                new String[]{status}
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    // 9. Đếm tổng số phòng
    public int getTotalRoomCount() {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM Room",
                null
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    // 10. Chuyển Cursor thành đối tượng Room
    private Room cursorToRoom(Cursor cursor) {

        int id = cursor.getInt(
                cursor.getColumnIndexOrThrow("id")
        );

        String roomCode = cursor.getString(
                cursor.getColumnIndexOrThrow("room_code")
        );

        int floor = cursor.getInt(
                cursor.getColumnIndexOrThrow("floor")
        );

        String status = cursor.getString(
                cursor.getColumnIndexOrThrow("status")
        );

        return new Room(id, roomCode, floor, status);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        // Xử lý nâng cấp database khi thay đổi cấu trúc.
    }
}