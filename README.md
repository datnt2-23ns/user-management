# User Management System

Hệ thống quản lý người dùng được xây dựng bằng Spring Boot, Spring Security, JWT, MySQL và JavaScript.

Dự án hỗ trợ các chức năng đăng ký, đăng nhập, quản lý hồ sơ cá nhân, upload ảnh đại diện và quản trị tài khoản người dùng theo vai trò `USER` và `ADMIN`.

---

## 1. Chức năng chính

### 1.1. Chức năng dành cho người dùng

- Đăng ký tài khoản.
- Đăng nhập bằng email và mật khẩu.
- Đăng xuất khỏi hệ thống.
- Xem hồ sơ cá nhân.
- Cập nhật thông tin cá nhân.
- Upload và thay đổi ảnh đại diện.
- Đổi mật khẩu.
- Không cho phép tài khoản đã khóa hoặc đã xóa tiếp tục sử dụng hệ thống.

### 1.2. Chức năng dành cho quản trị viên

- Xem danh sách tài khoản.
- Tìm kiếm tài khoản theo tên, email hoặc số điện thoại.
- Lọc tài khoản theo vai trò và trạng thái.
- Sắp xếp và phân trang danh sách.
- Xem chi tiết tài khoản.
- Khóa và mở khóa tài khoản.
- Xóa mềm tài khoản.
- Nâng vai trò từ `USER` thành `ADMIN`.
- Hạ vai trò từ `ADMIN` thành `USER`.
- Bảo đảm hệ thống luôn còn ít nhất một Admin đang hoạt động.
- Theo dõi người cập nhật, người khóa và người xóa tài khoản.

---

## 2. Công nghệ sử dụng

### Backend

- Java 21
- Spring Boot 4.1.0
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- JWT Authentication
- Bean Validation
- Flyway Migration
- Springdoc OpenAPI
- Lombok

### Frontend

- HTML5
- CSS3
- JavaScript
- Fetch API

### Database

- MySQL

### Công cụ phát triển

- IntelliJ IDEA
- Git
- GitHub
- Postman
- Swagger UI

---

## 3. Kiến trúc thư mục

```text
src/main/java/org/example/usermanagement
├── config
├── controller
├── converter
├── dto
│   ├── request
│   └── response
├── entity
├── enums
├── exception
├── initializer
├── repository
├── security
├── service
│   └── impl
└── UserManagementApplication.java
```

Frontend được đặt tại:

```text
src/main/resources/static
├── css
├── images
├── js
└── pages
```

Các file migration database được đặt tại:

```text
src/main/resources/db/migration
```

---

## 4. Yêu cầu môi trường

Cần cài đặt:

- JDK 21 trở lên.
- MySQL.
- Git.
- IntelliJ IDEA hoặc IDE hỗ trợ Spring Boot.
- Postman để kiểm thử API.

Cấu hình mặc định của dự án:

```text
Application port: 8081
Database port: 3307
Database name: user_management
```

Các giá trị trên có thể được thay đổi trong:

```text
src/main/resources/application.yml
```

---

## 5. Cài đặt và chạy dự án

### 5.1. Clone repository

```bash
git clone <https://github.com/datnt2-23ns/user-management>
cd UserManagement
```

### 5.2. Tạo database

Đăng nhập MySQL và chạy:

```sql
CREATE DATABASE user_management
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Flyway sẽ tự động chạy các migration khi ứng dụng khởi động.

### 5.3. Cấu hình kết nối database

Kiểm tra file:

```text
src/main/resources/application.yml
```

Cập nhật URL, username và password database theo môi trường đang sử dụng.

Không đưa mật khẩu database, JWT secret, token đăng nhập hoặc mật khẩu Admin thật lên GitHub.

### 5.4. Cấu hình tài khoản Admin ban đầu

Hệ thống hỗ trợ tạo tài khoản Admin ban đầu khi khởi động.

Cấu hình các biến môi trường tương ứng, ví dụ:

```text
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=Admin@12345
ADMIN_FIRST_NAME=Administrator
ADMIN_LAST_NAME=System
```

Không sử dụng mật khẩu ví dụ trên trong môi trường thực tế.

### 5.5. Build project

Trên Windows PowerShell:

```powershell
.\mvnw.cmd clean compile
```

Chạy test:

```powershell
.\mvnw.cmd clean test
```

### 5.6. Chạy ứng dụng

Có thể chạy bằng IntelliJ IDEA từ class:

```text
UserManagementApplication.java
```

Hoặc chạy bằng Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Ứng dụng mặc định chạy tại:

```text
http://localhost:8081
```

---

## 6. Các trang giao diện

| Trang                | Đường dẫn                                                        |
| -------------------- | ---------------------------------------------------------------- |
| Trang chủ            | `http://localhost:8081/`                                         |
| Đăng ký              | `http://localhost:8081/pages/register.html`                      |
| Đăng nhập            | `http://localhost:8081/pages/login.html`                         |
| Hồ sơ cá nhân        | `http://localhost:8081/pages/profile.html`                       |
| Danh sách người dùng | `http://localhost:8081/pages/admin-users.html`                   |
| Chi tiết người dùng  | `http://localhost:8081/pages/admin-user-detail.html?id={userId}` |

Các trang quản trị chỉ hiển thị dữ liệu khi người dùng đã đăng nhập bằng tài khoản có vai trò `ADMIN`.

---

## 7. Tài liệu Swagger/OpenAPI

Sau khi chạy ứng dụng, truy cập Swagger UI tại:

```text
http://localhost:8081/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8081/v3/api-docs
```

### Cách sử dụng JWT trong Swagger

1. Gọi API đăng nhập.
2. Sao chép giá trị `accessToken` trong response.
3. Bấm nút **Authorize** trên Swagger.
4. Dán JWT access token.
5. Bấm **Authorize** để xác thực.
6. Thực hiện các API cần quyền User hoặc Admin.

Không cần thêm chữ `Bearer` khi cửa sổ Swagger chỉ yêu cầu access token.

---

## 8. Các API chính

### 8.1. API xác thực

| Method | Endpoint             | Mô tả                 |
| ------ | -------------------- | --------------------- |
| POST   | `/api/auth/register` | Đăng ký tài khoản     |
| POST   | `/api/auth/login`    | Đăng nhập và nhận JWT |

### 8.2. API hồ sơ người dùng

| Method | Endpoint               | Mô tả                  |
| ------ | ---------------------- | ---------------------- |
| GET    | `/api/users/me`        | Xem hồ sơ cá nhân      |
| PUT    | `/api/users/me`        | Cập nhật hồ sơ cá nhân |
| PUT    | `/api/users/me/avatar` | Cập nhật ảnh đại diện  |

API đổi mật khẩu được mô tả đầy đủ trong Swagger UI theo mapping của `UserController`.

### 8.3. API quản trị tài khoản

| Method | Endpoint                           | Mô tả                       |
| ------ | ---------------------------------- | --------------------------- |
| GET    | `/api/admin/users`                 | Xem danh sách tài khoản     |
| GET    | `/api/admin/users/{userId}`        | Xem chi tiết tài khoản      |
| PATCH  | `/api/admin/users/{userId}/status` | Khóa hoặc mở khóa tài khoản |
| PATCH  | `/api/admin/users/{userId}/role`   | Thay đổi vai trò USER/ADMIN |
| DELETE | `/api/admin/users/{userId}`        | Xóa mềm tài khoản           |

---

## 9. Phân quyền

| Chức năng               | Guest | User  | Admin |
| ----------------------- | :---: | :---: | :---: |
| Đăng ký                 |  Có   |  Có   |  Có   |
| Đăng nhập               |  Có   |  Có   |  Có   |
| Xem hồ sơ cá nhân       | Không |  Có   |  Có   |
| Cập nhật hồ sơ          | Không |  Có   |  Có   |
| Đổi mật khẩu            | Không |  Có   |  Có   |
| Xem danh sách tài khoản | Không | Không |  Có   |
| Xem chi tiết tài khoản  | Không | Không |  Có   |
| Khóa/mở khóa tài khoản  | Không | Không |  Có   |
| Xóa tài khoản           | Không | Không |  Có   |
| Thay đổi vai trò        | Không | Không |  Có   |

Các API quản trị được bảo vệ bằng Spring Security và yêu cầu role `ADMIN`.

---

## 10. Quy tắc nghiệp vụ chính

### Tài khoản

- Email không được trùng.
- Tài khoản mới có vai trò mặc định là `USER`.
- Tài khoản mới có trạng thái mặc định là `ACTIVE`.
- Mật khẩu được mã hóa trước khi lưu vào database.
- API không trả trường password trong response.

### Khóa tài khoản

- Chỉ Admin được phép khóa hoặc mở khóa.
- Admin không được tự khóa chính mình.
- Tài khoản bị khóa không được đăng nhập.
- Token cũ của tài khoản bị khóa không được tiếp tục sử dụng.

### Xóa tài khoản

- Hệ thống sử dụng xóa mềm.
- Tài khoản bị xóa có trạng thái `DELETED`.
- Admin không được tự xóa chính mình.
- Tài khoản đã xóa không xuất hiện trong danh sách.
- Tài khoản đã xóa không được đăng nhập.
- Avatar cũ được xóa sau khi transaction thành công.

### Thay đổi vai trò

- Chỉ Admin được thay đổi vai trò.
- Có thể nâng `USER` thành `ADMIN`.
- Có thể hạ `ADMIN` thành `USER`.
- Hệ thống phải luôn còn ít nhất một Admin có trạng thái `ACTIVE`.

### Audit

Database lưu ID của người thực hiện tại các trường:

```text
updated_by
locked_by
deleted_by
```

API bổ sung họ tên tương ứng để giao diện hiển thị thân thiện hơn.

---

## 11. Mã lỗi HTTP

| HTTP Status | Ý nghĩa                                            |
| ----------: | -------------------------------------------------- |
|         200 | Yêu cầu được xử lý thành công                      |
|         201 | Tạo dữ liệu thành công                             |
|         204 | Xóa tài khoản thành công và không có response body |
|         400 | Dữ liệu đầu vào không hợp lệ                       |
|         401 | Chưa đăng nhập hoặc token không hợp lệ             |
|         403 | Không có quyền thực hiện                           |
|         404 | Không tìm thấy tài khoản hoặc API                  |
|         409 | Dữ liệu bị trùng hoặc xung đột nghiệp vụ           |
|         500 | Lỗi hệ thống                                       |

Cấu trúc response lỗi thống nhất:

```json
{
  "status": 400,
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2026-07-29T16:30:00",
  "errors": {
    "email": "Email không đúng định dạng"
  }
}
```

---

## 12. Upload ảnh đại diện

Hệ thống chỉ chấp nhận:

```text
.jpg
.jpeg
.png
```

Dung lượng tối đa:

```text
2 MB
```

Database chỉ lưu đường dẫn ảnh. File ảnh được lưu trong thư mục upload của ứng dụng.

Khi thay ảnh mới, hệ thống xóa ảnh cũ sau khi transaction cập nhật thành công.

---

## 13. Kiểm thử API bằng Postman

Tạo Environment với các biến:

```text
baseUrl=http://localhost:8081/api
adminToken=
userToken=
userId=
```

Với API cần xác thực, chọn:

```text
Authorization → Bearer Token
```

Sử dụng:

```text
{{adminToken}}
```

hoặc:

```text
{{userToken}}
```

Các trường hợp cần kiểm tra:

- Đăng ký hợp lệ và không hợp lệ.
- Đăng nhập đúng và sai mật khẩu.
- Không có token.
- Token không hợp lệ.
- User gọi API Admin.
- Admin xem danh sách và chi tiết tài khoản.
- Khóa và mở khóa.
- Thay đổi role.
- Xóa tài khoản.
- ID không tồn tại.
- Email trùng.
- Hạ Admin đang hoạt động cuối cùng.
- Response không chứa password.

---

## 14. Bảo mật

Hệ thống áp dụng:

- JWT Authentication.
- Spring Security.
- Phân quyền theo role.
- Mã hóa mật khẩu bằng PasswordEncoder.
- Không trả password trong API response.
- Không ghi log password hoặc JWT token.
- Không cho phép User tự thay đổi role và status.
- Không để lộ thông tin lỗi nội bộ trong response `500`.

Không commit các nội dung sau lên Git:

```text
Mật khẩu database
JWT secret
Mật khẩu Admin thật
Access token
File .env
File upload của người dùng
Log chứa dữ liệu nhạy cảm
```

---

## 15. Git workflow

Quy trình phát triển:

```text
main
  ↓
feature/fix/docs branch
  ↓
commit
  ↓
push
  ↓
Pull Request
  ↓
review
  ↓
merge vào main
```

Không phát triển chức năng trực tiếp trên nhánh `main`.

Ví dụ:

```bash
git switch main
git pull origin main
git switch -c feature/example
```

Sau khi hoàn thành:

```bash
git add .
git commit -m "feat: add example feature"
git push -u origin feature/example
```

Sau đó tạo Pull Request vào `main`.

---

## 16. Kiểm tra trước khi bàn giao

Chạy:

```powershell
git status
git diff --check
```

Kiểm tra conflict marker:

```powershell
git grep -n -e "<<<<<<<" -e "=======" -e ">>>>>>>"
```

Build và test:

```powershell
.\mvnw.cmd clean test
```

Kiểm tra thêm:

- Swagger UI truy cập được.
- Đăng ký và đăng nhập hoạt động.
- API User hoạt động.
- API Admin hoạt động.
- Phân quyền 401/403 chính xác.
- API không trả password.
- Không còn conflict marker.
- Không có secret trong repository.
- Nhánh `main` đã đồng bộ với `origin/main`.

---

## 17. Tác giả

Dự án được thực hiện trong quá trình thực tập về Scrum, Git và phát triển hệ thống quản lý người dùng bằng Java Spring Boot.
