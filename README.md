
# Hướng dẫn nhanh — Backend Badminton Chain
Link API (Swagger): http://14.225.198.75:8080/swagger-ui/index.html

Tệp này hướng dẫn cách cài đặt, cấu hình môi trường và chạy API backend.

## Những chức năng đã làm
- Xác thực & phân quyền: đăng ký, đăng nhập, cấp JWT, phân quyền theo vai trò.
- Quản lý người dùng & khách hàng.
- Quản lý sân (courts): tạo/cập nhật sân, quản lý trạng thái, giá theo giờ, lưu ảnh.
- Hệ thống đặt sân (bookings): sinh mã đặt duy nhất, quản lý trạng thái booking, thông tin thanh toán.
- Quản lý dịch vụ & giao dịch: quản lý các dịch vụ phụ trợ và ghi nhận giao dịch liên quan.
- Chat AI (RAG): trả lời theo ngữ cảnh sử dụng embeddings với model của Google Gemini.
- Xử lý nhắc lịch & thông báo: sử dụng RabbitMQ để quản lý task và event-driven.
- Upload ảnh: lưu ảnh upload vào thư mục uploads/.
- Tìm kiếm ngữ nghĩa: sử dụng pgvector để index và tìm kiếm nhanh.

## Cần chuẩn bị
- Java 17+
- Maven
- PostgreSQL
- RabbitMQ
- Tài khoản SMTP để gửi email
- Google Gemini model AI

## File cấu hình (.env)
Tạo file `.env` ở thư mục gốc.

Mẫu `.env` :

```
# Database
DB_URL=jdbc:postgresql://localhost:5432/DB_MAIN
DB_USERNAME=postgres
DB_PASSWORD=your_db_password

# JWT
JWT_SECRET=someVeryLongAndSecureSecretKey

# Email SMTP
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-email-app-password
MAIL_SMTP_AUTH=true
MAIL_STARTTLS=true
MAIL_SSL_TRUST=smtp.gmail.com

# RabbitMQ
RABBITMQ_URL=amqp://user:pass@localhost:5672

# OpenAI
OPENAI_API_KEY=sk-...
OPENAI_EMBEDDING_MODEL=gemini-2.0-flash
OPENAI_CHAT_MODEL=gemini-2.5

# Ngoại vi khác
WEATHER_API_KEY=...
```

## Tạo database và import dữ liệu mẫu
Trong thư mục `Data/` có các file SQL mẫu. Ví dụ dùng psql:

PowerShell:

```powershell
# tạo database (đổi user nếu cần)
psql -U postgres -c "CREATE DATABASE DB_MAIN;"

# import dữ liệu
psql -U postgres -d DB_MAIN -f Data/data.sql
```

## Chạy ứng dụng

1) Tải biến từ `.env` vào shell (PowerShell hoặc bash)

PowerShell:

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -and ($_ -notmatch '^#')) {
    $parts = $_ -split "=",2
    if ($parts.Length -eq 2) { Set-Item -Path Env:\$($parts[0].Trim()) -Value $parts[1].Trim() }
  }
}

# chạy app (profile dev)
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```

Bash:

```bash
set -a; source .env; set +a
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```

## Kiểm tra API (Swagger)
Mở: http://14.225.198.75:8080/swagger-ui/index.html

Ở đây sẽ thấy danh sách endpoint, mẫu request/response và có thể thử gọi trực tiếp.

## Cơ bản về xác thực (JWT)
- Đăng nhập (xem Swagger) để lấy access token.
- Gửi token trong header `Authorization: Bearer <token>` cho các endpoint cần auth.

Ví dụ curl:

```bash
curl -H "Authorization: Bearer <ACCESS_TOKEN>" http://localhost:8080/api/your-protected-endpoint
```

## Một số ghi chú nhanh
- Thư mục `uploads/` lưu ảnh — đảm bảo ứng dụng có quyền ghi.
- Nếu dùng Gmail để gửi mail, tốt nhất tạo App Password chứ không dùng password tài khoản chính.
- Kiểm tra `application-dev.properties` nếu cần thay đổi chính sách migration (`spring.jpa.hibernate.ddl-auto`).
