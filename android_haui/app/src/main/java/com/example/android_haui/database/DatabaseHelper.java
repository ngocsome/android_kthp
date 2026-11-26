package com.example.android_haui.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android_haui.model.Equipment;
import com.example.android_haui.model.Room;
import com.example.android_haui.model.StatusHistory;
import com.example.android_haui.utils.Constants;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RoomManagement.db";
    // Version 2: thêm bảng Equipment (thiết bị trong phòng)
    // Version 3: thêm bảng StatusHistory (lịch sử đổi trạng thái)
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_ROOM = "Room";
    private static final String TABLE_EQUIPMENT = "Equipment";
    private static final String TABLE_HISTORY = "StatusHistory";

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

        createEquipmentTable(db);

        createHistoryTable(db);
    }

    // Tạo bảng lịch sử thay đổi trạng thái
    private void createHistoryTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "room_code TEXT NOT NULL, " +
                "old_status TEXT NOT NULL, " +
                "new_status TEXT NOT NULL, " +
                "changed_at INTEGER NOT NULL)");
    }

    // Tạo bảng thiết bị và thêm thiết bị mặc định cho các phòng
    private void createEquipmentTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EQUIPMENT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "room_code TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "condition TEXT NOT NULL)");

        String[] names = {"Máy chiếu", "Điều hòa", "Bàn ghế sinh viên", "Bảng trắng"};
        int[] quantities = {1, 2, 40, 1};

        Cursor cursor = db.rawQuery("SELECT room_code FROM " + TABLE_ROOM, null);

        while (cursor.moveToNext()) {
            String roomCode = cursor.getString(0);

            for (int i = 0; i < names.length; i++) {
                ContentValues values = new ContentValues();
                values.put("room_code", roomCode);
                values.put("name", names[i]);
                values.put("quantity", quantities[i]);
                values.put("condition", Constants.GOOD);
                db.insert(TABLE_EQUIPMENT, null, values);
            }
        }

        cursor.close();
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

    // Các phòng có thay đổi trạng thái gần đây, mỗi phòng chỉ xuất hiện một lần.
    public ArrayList<Room> getRecentlyUpdatedRooms(int limit) {
        ArrayList<Room> rooms = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT r.* FROM Room r JOIN StatusHistory h ON h.room_code = r.room_code " +
                        "GROUP BY r.id ORDER BY MAX(h.changed_at) DESC, MAX(h.id) DESC LIMIT ?",
                new String[]{String.valueOf(Math.max(0, limit))})) {
            while (cursor.moveToNext()) rooms.add(cursorToRoom(cursor));
        }
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

        Room room = getRoomByCode(roomCode);

        if (room == null) {
            return 0;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("status", status);

        int result = db.update(
                TABLE_ROOM,
                values,
                "room_code = ?",
                new String[]{roomCode}
        );

        // Ghi lại lịch sử nếu trạng thái thực sự thay đổi
        if (result > 0 && !status.equals(room.getStatus())) {

            ContentValues history = new ContentValues();
            history.put("room_code", roomCode);
            history.put("old_status", room.getStatus());
            history.put("new_status", status);
            history.put("changed_at", System.currentTimeMillis());

            db.insert(TABLE_HISTORY, null, history);
        }

        return result;
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

    // 10. Lấy danh sách phòng theo trạng thái
    public ArrayList<Room> getRoomsByStatus(String status) {

        ArrayList<Room> rooms = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM Room WHERE status = ? ORDER BY floor, room_code",
                new String[]{status}
        );

        while (cursor.moveToNext()) {
            rooms.add(cursorToRoom(cursor));
        }

        cursor.close();

        return rooms;
    }

    // 11. Lấy danh sách thiết bị của một phòng
    public ArrayList<Equipment> getEquipmentsByRoom(String roomCode) {

        ArrayList<Equipment> list = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_EQUIPMENT + " WHERE room_code = ? ORDER BY id",
                new String[]{roomCode}
        );

        while (cursor.moveToNext()) {
            list.add(new Equipment(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("room_code")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    cursor.getString(cursor.getColumnIndexOrThrow("condition"))
            ));
        }

        cursor.close();

        return list;
    }

    // 12. Thêm thiết bị
    public long addEquipment(Equipment equipment) {

        ContentValues values = new ContentValues();
        values.put("room_code", equipment.getRoomCode());
        values.put("name", equipment.getName());
        values.put("quantity", equipment.getQuantity());
        values.put("condition", equipment.getCondition());

        return getWritableDatabase().insert(TABLE_EQUIPMENT, null, values);
    }

    // 13. Sửa thiết bị
    public int updateEquipment(Equipment equipment) {

        ContentValues values = new ContentValues();
        values.put("name", equipment.getName());
        values.put("quantity", equipment.getQuantity());
        values.put("condition", equipment.getCondition());

        return getWritableDatabase().update(
                TABLE_EQUIPMENT,
                values,
                "id = ?",
                new String[]{String.valueOf(equipment.getId())}
        );
    }

    // 14. Xóa thiết bị
    public int deleteEquipment(int id) {

        return getWritableDatabase().delete(
                TABLE_EQUIPMENT,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // 15. Lấy lịch sử đổi trạng thái của phòng (mới nhất trước)
    public ArrayList<StatusHistory> getStatusHistory(String roomCode, int limit) {

        ArrayList<StatusHistory> list = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_HISTORY +
                        " WHERE room_code = ? ORDER BY changed_at DESC, id DESC LIMIT " + limit,
                new String[]{roomCode}
        );

        while (cursor.moveToNext()) {
            list.add(new StatusHistory(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("room_code")),
                    cursor.getString(cursor.getColumnIndexOrThrow("old_status")),
                    cursor.getString(cursor.getColumnIndexOrThrow("new_status")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("changed_at"))
            ));
        }

        cursor.close();

        return list;
    }

    // 16. Lấy toàn bộ thiết bị hỏng (sắp xếp theo phòng)
    public ArrayList<Equipment> getBrokenEquipments() {

        ArrayList<Equipment> list = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_EQUIPMENT +
                        " WHERE condition = ? ORDER BY room_code, id",
                new String[]{Constants.BROKEN}
        );

        while (cursor.moveToNext()) {
            list.add(new Equipment(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("room_code")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    cursor.getString(cursor.getColumnIndexOrThrow("condition"))
            ));
        }

        cursor.close();

        return list;
    }

    // 17. Chuyển Cursor thành đối tượng Room
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
        // Nâng cấp từng bước, giữ nguyên dữ liệu cũ
        if (oldVersion < 2) {
            createEquipmentTable(db);
        }

        if (oldVersion < 3) {
            createHistoryTable(db);
        }
    }
}