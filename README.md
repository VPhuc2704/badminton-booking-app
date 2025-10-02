
# Hướng dẫn nhanh — Backend Badminton Chain

Tệp này hướng dẫn cách cài đặt, cấu hình môi trường và chạy API backend. Tôi viết gọn, thực tế để bạn làm theo nhanh.

## Cần chuẩn bị
- Java 17+
- Maven
- PostgreSQL (port mặc định 5432)
- RabbitMQ để dùng message queue
- Tài khoản SMTP để gửi email
- OpenAI key nếu dùng tính năng AI

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
APP_FRONTEND_DOMAIN=http://localhost:5173
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

Bash/Linux:

```bash
psql -U postgres -c "CREATE DATABASE DB_MAIN;"
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

2) Hoặc build JAR và chạy:

```powershell
mvn clean package -DskipTests
java -jar target/badminton-chain-management-ai-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

Muốn override nhanh một biến khi chạy JAR, dùng `-D` hoặc `--`:

```powershell
java -jar target/app.jar --DB_URL=jdbc:postgresql://... 
```

## Kiểm tra API (Swagger)
Mở: http://localhost:8080/swagger-ui.html

Ở đó bạn sẽ thấy danh sách endpoint, mẫu request/response và có thể thử gọi trực tiếp.

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

## Triển khai nhanh lên server (IP: 14.225.198.75)

Dưới đây là hai cách đơn giản để deploy ứng dụng để nhà tuyển dụng truy cập Swagger tại `http://14.225.198.75/swagger-ui.html` mà không cần họ cài đặt.

Chuẩn bị trên server:
- Ubuntu 20.04+ (hoặc tương đương)
- Docker & docker-compose (nếu deploy bằng Docker)
- Nginx nếu muốn reverse-proxy từ port 80 sang app
- Mở port 80 (HTTP) trên firewall / cloud security group

1) Deploy bằng Docker (khuyến nghị nhanh)

Trên máy local build image rồi push lên server, hoặc build trực tiếp trên server.

Ví dụ build + chạy trên server (SSH vào 14.225.198.75):

```bash
# copy source lên server (ví dụ dùng scp hoặc git clone trên server)
git clone <repo-url> app && cd app/backend/badminton-chain-management-ai

# tạo .env trên server (theo .env.example)
cp .env.example .env
# chỉnh .env cho phù hợp (DB nếu dùng local/managed DB)

# build image
mvn clean package -DskipTests
docker build -t badminton-backend:latest .

# chạy docker-compose
docker-compose up -d --build
```

Sau khi container chạy, ứng dụng lắng nghe trên port 8080 và `docker-compose.yml` map tới port 8080 trên host. Truy cập:

http://14.225.198.75:8080/swagger-ui.html

Để ẩn port 8080 và cho truy cập qua HTTP trên port 80, cấu hình Nginx trên server để reverse-proxy (xem phần Nginx bên dưới).

2) Deploy bằng JAR + systemd + Nginx

Trên server:

```bash
# copy jar và .env lên /opt/badminton
sudo mkdir -p /opt/badminton
sudo chown $USER /opt/badminton
cp target/badminton-chain-management-ai-0.0.1-SNAPSHOT.jar /opt/badminton/
cp .env /opt/badminton/

# tạo systemd service (ví dụ /etc/systemd/system/badminton.service)
# (nội dung mẫu có trong file deploy/badminton.service trong repo)
sudo cp deploy/badminton.service /etc/systemd/system/badminton.service
sudo systemctl daemon-reload
sudo systemctl enable --now badminton.service
```

Sau đó ứng dụng sẽ chạy ở `http://127.0.0.1:8080`. Cài Nginx để proxy ra port 80.

Nginx (ví dụ):

```nginx
server {
  listen 80;
  server_name 14.225.198.75;

  location / {
    proxy_pass http://127.0.0.1:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
  }
}
```

Enable Nginx and reload:

```bash
sudo systemctl enable --now nginx
sudo nginx -t && sudo systemctl reload nginx
```

Bây giờ mở: http://14.225.198.75/swagger-ui.html

Lưu ý bảo mật:
- Không để `.env` chứa secrets public; dùng biến môi trường trên server hoặc secrets manager.
- Nếu public cho nhà tuyển dụng, cân nhắc bật HTTPS (Let's Encrypt) và giới hạn IP nếu cần.

