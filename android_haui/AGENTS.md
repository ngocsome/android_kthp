# Repository guidance

## Project overview

Single-module Android application for room and equipment management with Vietnamese UI text. Uses Java, XML layouts, AndroidX/AppCompat, Material components, and local SQLite storage. Follow this architecture for changes.

## Code map

- `app/src/main/java/com/example/android_haui/` contains application code.
- `activity/`: MainActivity hosts bottom navigation; RoomDetailActivity manages room details.
- `fragment/`: OverviewFragment (dashboard), RoomListFragment (room list with integrated code search and status filtering), StatusFragment (status management), and StatisticsFragment.
- Bottom navigation has four destinations: Tổng quan, Phòng học, Thống kê, Quản lý. Search is part of Phòng học; there is no separate SearchFragment or search tab.
- RoomListFragment combines case-insensitive room-code matching with the selected status. Its floor filter has been removed; keep floor information in room data and cards. Preserve keyword/status state on recreation and reload data when returning from room details.
- `adapter/`: room list binding; `model/`: room, equipment, and status history models.
- `database/DatabaseHelper.java`: schema, seed data, queries, and updates for RoomManagement.db (currently version 3), containing Room, Equipment, and StatusHistory. SQLite stores it in the installed app's private databases directory; it is not a source asset.
- `utils/Constants.java`: persisted room statuses and equipment conditions. StatusUtils maps these to Vietnamese labels and badge styles.
- `app/src/main/res/`: layouts, drawables, colors, themes, and strings. Update resource IDs and Java references together.
- `app/src/test/`: local JUnit tests. `app/src/androidTest/`: instrumentation tests. Existing tests are template examples, not comprehensive coverage.

## Build and validation

Run from the repository root using the checked-in wrapper. Windows PowerShell commands:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

The last command requires an emulator or connected device. On Unix, use `./gradlew` with the same tasks. Choose checks relevant to the change and report environment blockers or unrun checks accurately.

Current configuration: Gradle 9.5.0, Android Gradle Plugin 9.3.3, compile/target SDK 37, minimum SDK 24. The Gradle daemon toolchain requests JDK 25; application Java source/target compatibility is Java 11. Dependency versions and plugin aliases live in `gradle/libs.versions.toml`; module configuration is in `app/build.gradle.kts`.

## Change conventions

- Follow existing Java style: four-space indentation, PascalCase classes, camelCase methods/fields, and uppercase constants. Android resources use snake_case.
- Preserve Vietnamese text and UTF-8 encoding. Prefer string resources for new user-facing text and existing utilities/resources for shared styling.
- Reuse Constants for stored statuses and conditions; keep display labels in the presentation layer.
- Keep database operations in DatabaseHelper. For schema changes, review onCreate and onUpgrade, increment the version as needed, and preserve user data. Verify fresh installation and upgrade behavior when changing migrations.
- For UI changes, check affected navigation, filtering, room details, and data refresh behavior on a device/emulator when available.
- Keep changes focused and preserve unrelated user edits, including IDE configuration changes. Do not commit generated build outputs, local SDK paths, or secrets.
