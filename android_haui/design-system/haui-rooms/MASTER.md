# HaUI Rooms · Hệ thống thiết kế Android

Nguồn tham chiếu là kết quả ui-ux-pro-max cho “operations management dashboard minimal”, cùng các tra cứu UX về vùng chạm, tương phản, nhãn dài và biểu đồ so sánh. Các quyết định dưới đây áp dụng cho giao diện tiếng Việt của ứng dụng quản lý phòng học và thiết bị.

## Màu và kiểu chữ

| Vai trò | Sáng (chữ / nền) | Tối (chữ / nền) |
|---|---|---|
| Nội dung trên thẻ | #134E4A / #FFFFFF | #E0F3EF / #172827 |
| Nội dung phụ trên nền trang | #475569 / #F0FDFA | #B5C9C5 / #0F1C1B |
| Nút chính | #FFFFFF / #0F766E | #003732 / #70DCCB |
| Phòng trống, thiết bị tốt | #18603F / #DCF5E6 | #99E6B2 / #1A3D30 |
| Phòng đang sử dụng | #184E92 / #E3EEFF | #AACBFF / #213958 |
| Phòng bảo trì | #814900 / #FFECC5 | #FFD18A / #493614 |
| Thiết bị hỏng | #A32B29 / #FFE6E2 | #FFBAB0 / #522C29 |

Dùng sans-serif hệ thống với tiêu đề màn hình 24sp, tiêu đề nhóm 20sp, nội dung 16sp, nhãn 14sp. Số tổng nổi bật dùng 44sp. Các cặp chữ và nền phải đạt 4.5:1. Sắc thái xem tại res/values/colors.xml và res/values-night/colors.xml.

## Thành phần

- Bốn tab có nhãn và icon vector 24dp. Tab hiện tại có nền chỉ báo. Ưu tiên hiệu ứng nhấn sẵn có của Material.
- Thẻ bo 16dp, viền nhẹ. Lề trang 16dp, tăng lên 32dp từ chiều rộng nhỏ nhất 600dp. Dùng khoảng cách 4, 8, 12, 16 và 24dp.
- Thẻ phòng ưu tiên mã phòng, tầng và nhãn trạng thái. Nhãn phòng kết hợp biểu tượng, tên và màu. Tình trạng thiết bị có từ vựng riêng.
- Mọi nút, chip và hàng có thao tác có vùng chạm ít nhất 48×48dp. Tên dài xuống dòng, nhóm bộ lọc tự ngắt hàng. Dùng nhãn tiếng Việt cho nút biểu tượng và trạng thái được chọn.
- Thống kê dùng ba thanh ngang cùng thang 0–100%, nhãn trực tiếp gồm trạng thái, số phòng và tỷ lệ. Bấm một hàng để xem các phòng tương ứng. Cho phép đọc toàn bộ số liệu bằng TalkBack.
- Ở chế độ dọc, đặt các bộ lọc trên danh sách. Khi xoay ngang, Phòng học và Quản lý đặt bộ lọc ở cột trái, danh sách ở cột phải. Mỗi vùng cuộn độc lập khi nội dung vượt chiều cao màn hình.
- Giữ nội dung và nút thao tác truy cập được khi xoay màn hình, mở bàn phím hoặc tăng cỡ chữ. Dùng cuộn dọc cho nội dung dài, tôn trọng vùng thanh hệ thống.
