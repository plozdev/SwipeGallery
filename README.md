<div align="center">

  # 🎴 Swipe Gallery

  **A modern, Tinder-style photo triage & gallery management app built with Compose Multiplatform.**

  [![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.3-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
  [![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](https://github.com/plozdev/SwipeGallery)
  [![Design](https://img.shields.io/badge/Design-Material%203%20OLED-black.svg?style=for-the-badge)](https://m3.material.io)
  [![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

  <br />

  <a href="https://github.com/plozdev/SwipeGallery/releases/latest">
    <img src="https://img.shields.io/badge/Download%20APK-v1.0.0%20Release-00C853?style=for-the-badge&logo=android&logoColor=white" height="42" alt="Download APK" />
  </a>

</div>

---

## 🎬 Feature Demos

<details open>
<summary><b>1. 🎴 Tinder-style Swiping & Album Triage (Vuốt phân loại & Chọn Album)</b></summary>

> *Quẹt phải để giữ lại, quẹt trái để đưa vào hàng chờ xóa, tính năng Undo mượt mà và theo dõi tiến độ "Còn X / Y ảnh" theo từng album.*

https://github.com/user-attachments/assets/7fb24982-ff1c-43d6-9361-d971a6cba793

</details>

<details open>
<summary><b>2. 🗑️ Pending Deletion & Safe Review (Kiểm duyệt hàng chờ xóa an toàn)</b></summary>

> *Xem lại danh sách ảnh đã quẹt xóa, khôi phục ảnh nhầm vào album gốc hoặc xác nhận dọn dẹp vĩnh viễn an toàn.*

https://github.com/user-attachments/assets/615a63d1-6322-40c0-b43f-e7e3dc2425a7

</details>

<details open>
<summary><b>3. ⚙️ Settings, Native Sharing & Privacy (Cài đặt & Chia sẻ Báo cáo)</b></summary>

> *Xác nhận xóa lịch sử an toàn, chia sẻ báo cáo dọn dẹp qua Native Android Share Sheet, xóa bộ nhớ đệm và giao diện OLED Dark Theme.*

https://github.com/user-attachments/assets/d989937c-e19f-4dda-a914-c92cca00e3af

</details>

---

## ✨ Key Features

- **🎴 Tinder-style Card Triage**:
  - Quẹt phải để **Giữ lại (Keep)**, quẹt trái để **Đưa vào hàng chờ xóa (Delete)**.
  - Xử lý cử chỉ vật lý mượt mà đạt chuẩn 60fps với độ nghiêng xoay theo lực kéo và ngưỡng vuốt tùy biến.
- **🔄 Physics-based Fly-in Undo Animation**:
  - Khi ấn nút **Hoàn tác (Undo)**, thẻ ảnh không xuất hiện đột ngột mà **lướt và xoay ngược trở lại ngăn xếp** từ đúng hướng vừa bị quẹt ra ngoài màn hình (sử dụng spring physics không gây nhấp nháy).
- **🛡️ Safe Staging Review (Hàng đợi an toàn)**:
  - Ảnh quẹt xóa được gom vào hộp *"Ảnh Chờ Xóa"* thay vì xóa vĩnh viễn ngay lập tức.
  - Thanh tác vụ kép (Dual Action Bar): Cho phép người dùng **Khôi phục (Restore)** để giữ lại an toàn hoặc **Xóa vĩnh viễn (Delete)** kèm hộp thoại cảnh báo chống thao tác nhầm.
- **📳 Hardware Haptic Feedback**:
  - Tích hợp engine rung phần cứng đa nền tảng (`Vibrator` trên Android và `UIImpactFeedbackGenerator` trên iOS).
  - Rung phản hồi tức thì khi vượt ngưỡng quẹt thẻ, khi bấm các nút hành động và khi kích hoạt toggle cài đặt.
- **🔥 Streak & Storage Metrics**:
  - Gamification thúc đẩy thói quen dọn dẹp hàng ngày với chuỗi ngày liên tục (Streak) tính chuẩn xác theo múi giờ địa phương (Local Timezone).
  - Thống kê trực quan tổng dung lượng bộ nhớ đã giải phóng và tỷ lệ giữ lại ảnh.
- **🔒 Privacy-First & 100% Offline**:
  - Không yêu cầu quyền Internet, không gửi dữ liệu ra máy chủ bên ngoài.
  - Đọc và đồng bộ trực tiếp với thư viện ảnh hệ thống qua Android MediaStore API.

---

## 🛠️ Architecture & Tech Stack

Dự án được xây dựng theo kiến trúc Clean Architecture kết hợp mô hình MVI/MVVM:

```
SwipeGallery/
├── composeApp/                     # 100% Shared UI & Business Logic (KMP)
│   ├── commonMain/
│   │   ├── components/            # SwipeableCard, FullscreenViewer, PhotoView
│   │   ├── domain/models/         # PhotoItem, Album
│   │   ├── screens/               # DiscoverScreen, AlbumScreen, PendingReviewScreen, SettingsScreen
│   │   └── data/repository/       # PhotoRepo, SwipePreferences
│   ├── androidMain/               # Android Platform Implementations (Haptics, BackHandler)
│   └── iosMain/                   # iOS Platform Implementations (Haptics, BackHandler)
└── androidApp/                    # Android Application Entry Point
    └── src/main/                  # MainActivity, Permissions, Manifest
```

* **Core Language**: Kotlin 2.3.20 (Kotlin Multiplatform)
* **UI Toolkit**: Compose Multiplatform 1.10.3 (Jetpack Compose shared across platforms)
* **Design System**: Material Design 3 OLED Dark Theme
* **Asynchronous**: Kotlin Coroutines & StateFlow / SharedFlow
* **Image Loading**: Coil 2.7.0 (Async image fetching & cache management)
* **Local Storage**: Multiplatform Key-Value Preferences

---

## 🚀 Getting Started

### Yêu cầu môi trường
* **JDK**: 17 hoặc mới hơn
* **Android SDK**: Compile SDK 37, Min SDK 30
* **Android Studio**: Ladybug / Koala hoặc IntelliJ IDEA mới nhất

### Cài đặt & Build APK

1. **Clone repository**:
   ```bash
   git clone https://github.com/plozdev/SwipeGallery.git
   cd SwipeGallery
   ```

2. **Chạy kiểm thử Unit Tests**:
   ```bash
   ./gradlew testAndroidHostTest
   ```

3. **Build file APK Debug / Demo**:
   ```bash
   ./gradlew :androidApp:assembleDebug
   ```
   *File APK hoàn chỉnh sẽ được tạo tại:*
   `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

---

## 📄 License

Dự án được phân phối dưới giấy phép [MIT License](LICENSE).
