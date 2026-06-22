# 🔍 CẠCH CẠCH - SE330 Mini Search Engine

**CẠCH CẠCH** là một công cụ tìm kiếm thu nhỏ (Mini Search Engine) dành cho tiếng Việt, được phát triển trong khuôn khổ môn học SE330. Hệ thống tối ưu hóa việc tìm kiếm dữ liệu nhờ tích hợp bộ phân tích ngôn ngữ tự nhiên, quản lý lịch sử tìm kiếm, và hệ thống tài khoản người dùng thực tế.

Dự án bao gồm 2 phần chính:
* **Backend (Java):** [PhDuy2005/SE330_MiniSearchEngine](https://github.com/PhDuy2005/SE330_MiniSearchEngine)
* **Frontend (Java/CSS):** [Quanle2k6/SE330_MiniSearchEngine_FrontEnd](https://github.com/Quanle2k6/SE330_MiniSearchEngine_FrontEnd)

---

## 👥 Thành viên phát triển

Dự án được hoàn thiện bởi sự đóng góp từ các thành viên:
* **Lê Hoàng Quân** ([@Quanle2k6](https://github.com/Quanle2k6))
* **Phạm Trần Khánh Duy** ([@PhDuy2005](https://github.com/PhDuy2005))
* **Từ Thị Tú Uyên** ([@TuUyen038](https://github.com/TuUyen038))
* **Nguyễn Công Thiết** ([@BalaenopteraMusculus1231](https://github.com/BalaenopteraMusculus1231))

---

## 🛠️ Công nghệ sử dụng

### 🖥️ Backend (BE)
* **Ngôn ngữ:** Java (100%)
* **Công cụ quản lý:** Gradle (Kotlin DSL - `build.gradle.kts`)
* **Thư viện lõi:** * `VietnameseAnalyzer` tích hợp **VnCoreNLP** để xử lý tách từ tiếng Việt chính xác.
    * **JavaMail Sender** cho cấu hình hệ thống SMTP gửi email tự động.

### 🎨 Frontend (FE)
* **Ngôn ngữ:** Java (92.0%), CSS (8.0%)
* **Môi trường tích hợp:** Hỗ trợ cấu hình nhanh qua `.metals` (Scala/Java ecosystem).

---

## 📌 Các tính năng nổi bật

- [x] **Tìm kiếm Tiếng Việt chuyên sâu:** Phân tích ngữ nghĩa và tách từ tiếng Việt thông minh thay vì tìm kiếm thô.
- [x] **Xác thực người dùng (Authentication):** Đăng nhập và đăng ký tài khoản (Hệ thống tối giản, không yêu cầu phân quyền phức tạp - No Authz).
- [x] **Quản lý lịch sử:** Tự động lưu trữ lịch sử tìm kiếm gắn liền với từng tài khoản người dùng sau khi tra cứu thành công.
- [x] **Bản địa hóa thông báo:** Toàn bộ các thông báo lỗi, ngoại lệ (Exception messages) hệ thống được Việt hóa 100% để tăng trải nghiệm người dùng.
- [x] **Cơ sở dữ liệu mẫu:** Tích hợp sẵn bộ dữ liệu phong phú về **Game** (`game dataset`) trong thư mục dữ liệu phục vụ test tìm kiếm trực quan.
- [x] **Hệ thống Mail:** Cấu hình SMTP hoạt động mượt mà để gửi thông báo/mã xác nhận.

---

## 📂 Cấu trúc thư mục dự án

### Backend (`SE330_MiniSearchEngine`)
```text
├── data/                  # Bộ dữ liệu mẫu về Game (.txt, .json, ...)
├── docs/                  # Tài liệu phân tích và hướng dẫn dự án
├── src/                   # Mã nguồn xử lý Logic, Engine và API Backend
├── build.gradle.kts       # File cấu hình thư viện dependencies và SMTP
└── gradlew / gradlew.bat  # Gradle wrapper hỗ trợ đóng gói ứng dụng
```

### Frontend (`SE330_MiniSearchEngine_FrontEnd`)
```text
├── .metals/               # Cấu hình môi trường runtime/IDE 
├── MiniSearchEngine_FrontEnd/ # Source code giao diện ứng dụng (Java & CSS)
└── README.md              # File hướng dẫn này
```

---

## 💻 Hướng dẫn cài đặt và khởi chạy

### 1. Bản sao mã nguồn (Clone)
Tải cả hai kho lưu trữ về máy cục bộ của bạn:

```bash
# Clone Backend
git clone [https://github.com/PhDuy2005/SE330_MiniSearchEngine.git](https://github.com/PhDuy2005/SE330_MiniSearchEngine.git)

# Clone Frontend
git clone [https://github.com/Quanle2k6/SE330_MiniSearchEngine_FrontEnd.git](https://github.com/Quanle2k6/SE330_MiniSearchEngine_FrontEnd.git)
```

### 2. Khởi chạy Backend
'__Yêu cầu__: Đã cài đặt Java JDK 17 (hoặc mới hơn) trên thiết bị.'
Di chuyển vào thư mục Backend:
```bash
cd SE330_MiniSearchEngine
```
Thực hiện biên dịch dự án bằng Gradle:
```
./gradlew build
```

### 3. Khởi chạy Frontend
1. Mở thư mục SE330_MiniSearchEngine_FrontEnd bằng các IDE hỗ trợ Java (như IntelliJ IDEA hoặc Eclipse).

2. Kiểm tra phần cấu hình kết nối Endpoint API trong mã nguồn để đảm bảo trỏ đúng về địa chỉ chạy của Backend.

3. Kích hoạt chạy Main file của Frontend để hiển thị giao diện CẠCH CẠCH và bắt đầu trải nghiệm tìm kiếm thực tế (fetch real data).

`⚠️ __Lưu ý quan trọng__: Để bộ công cụ tách từ VietnameseAnalyzer hoạt động chuẩn xác không bị lỗi runtime, hãy đảm bảo các file tài nguyên liên quan đến bộ dữ liệu và mô hình phân tích ngôn ngữ trong thư mục data đã nằm đúng đường dẫn yêu cầu của mã nguồn.`
