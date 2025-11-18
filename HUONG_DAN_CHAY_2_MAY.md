# Hướng dẫn chạy RMI Banking trên 2 máy khác nhau

## Yêu cầu
- 2 máy tính trong cùng mạng LAN hoặc có thể kết nối với nhau qua Internet
- Java 17+ đã được cài đặt trên cả 2 máy
- Port 1099 phải được mở trên máy server

---

## Bước 1: Cấu hình máy Server

### 1.1. Tìm địa chỉ IP của máy Server

**Trên Windows:**
```cmd
ipconfig
```
Tìm dòng `IPv4 Address` (ví dụ: `192.168.1.100`)

**Trên Linux/Mac:**
```bash
ifconfig
# hoặc
ip addr
```

### 1.2. Chạy Server với địa chỉ IP cụ thể

**Cách 1: Dùng system property (Khuyến nghị)**
```bash
cd rmiserver
mvn compile exec:java -Dexec.mainClass="com.server.ServerMain" -Djava.rmi.server.hostname=<IP_SERVER>
```

Ví dụ nếu IP server là `192.168.1.100`:
```bash
mvn compile exec:java -Dexec.mainClass="com.server.ServerMain" -Djava.rmi.server.hostname=192.168.1.100
```

**Cách 2: Export biến môi trường trước khi chạy**
```bash
# Windows (PowerShell)
$env:JAVA_OPTS="-Djava.rmi.server.hostname=192.168.1.100"
mvn compile exec:java -Dexec.mainClass="com.server.ServerMain"

# Windows (CMD)
set JAVA_OPTS=-Djava.rmi.server.hostname=192.168.1.100
mvn compile exec:java -Dexec.mainClass="com.server.ServerMain"

# Linux/Mac
export JAVA_OPTS="-Djava.rmi.server.hostname=192.168.1.100"
mvn compile exec:java -Dexec.mainClass="com.server.ServerMain"
```

### 1.3. Mở Firewall cho port 1099

**Trên Windows:**
1. Mở Windows Defender Firewall
2. Chọn "Advanced settings"
3. Inbound Rules → New Rule
4. Chọn "Port" → Next
5. TCP, Specific local ports: `1099` → Next
6. Allow the connection → Next
7. Chọn tất cả profiles → Next
8. Đặt tên: "RMI Server Port 1099" → Finish

**Hoặc dùng PowerShell (chạy với quyền Administrator):**
```powershell
New-NetFirewallRule -DisplayName "RMI Server" -Direction Inbound -LocalPort 1099 -Protocol TCP -Action Allow
```

**Trên Linux (Ubuntu/Debian):**
```bash
sudo ufw allow 1099/tcp
```

**Trên Linux (CentOS/RHEL):**
```bash
sudo firewall-cmd --permanent --add-port=1099/tcp
sudo firewall-cmd --reload
```

---

## Bước 2: Cấu hình máy Client

### 2.1. Chạy Client

```bash
cd rmiclient
mvn compile exec:java -Dexec.mainClass="com.client.ClientMain"
```

### 2.2. Nhập địa chỉ Server

Trong giao diện Client:
- Ở field **"Địa chỉ Server"**, nhập IP của máy Server (ví dụ: `192.168.1.100`)
- Nhập thông tin tài khoản và mật khẩu
- Nhấn nút **"Kết nối"**

---

## Bước 3: Kiểm tra kết nối

### Kiểm tra từ Client máy

**Trên Windows:**
```cmd
telnet <IP_SERVER> 1099
```

**Trên Linux/Mac:**
```bash
nc -zv <IP_SERVER> 1099
# hoặc
telnet <IP_SERVER> 1099
```

Nếu kết nối thành công, có nghĩa là port đã mở và sẵn sàng.

---

## Xử lý lỗi thường gặp

### Lỗi: "Connection refused" hoặc "Connection timeout"

**Nguyên nhân:**
- Firewall chưa mở port 1099
- Địa chỉ IP nhập sai
- Server chưa chạy
- Không cùng mạng LAN

**Giải pháp:**
1. Kiểm tra server đã chạy chưa
2. Kiểm tra IP server có đúng không
3. Kiểm tra firewall đã mở port 1099 chưa
4. Thử ping từ client đến server: `ping <IP_SERVER>`

### Lỗi: "java.rmi.ConnectException: Connection refused to host"

**Nguyên nhân:**
- Server chưa set `java.rmi.server.hostname` đúng

**Giải pháp:**
- Chạy server với `-Djava.rmi.server.hostname=<IP_SERVER>` như hướng dẫn ở trên

### Lỗi: "java.security.AccessControlException"

**Nguyên nhân:**
- Security manager chặn RMI

**Giải pháp:**
- Với Java 17+, thường không cần security policy nếu không set security manager
- Nếu có lỗi, thêm vào lệnh chạy: `-Djava.security.policy=server.policy`

---

## Ví dụ hoàn chỉnh

### Máy Server (IP: 192.168.1.100)

```bash
cd rmiserver
mvn compile exec:java -Dexec.mainClass="com.server.ServerMain" -Djava.rmi.server.hostname=192.168.1.100
```

### Máy Client (IP: 192.168.1.50)

```bash
cd rmiclient
mvn compile exec:java -Dexec.mainClass="com.client.ClientMain"
```

Sau đó trong GUI:
- Địa chỉ Server: `192.168.1.100`
- Tài khoản: `12345`
- Mật khẩu: `pass2`

---

## Lưu ý quan trọng

1. **Cùng mạng LAN:** Đảm bảo 2 máy trong cùng mạng LAN hoặc có thể ping được nhau
2. **IP tĩnh:** Nên dùng IP tĩnh cho server để tránh IP thay đổi
3. **Port forwarding:** Nếu chạy qua Internet (không cùng LAN), cần cấu hình port forwarding trên router
4. **Security:** Trong môi trường production, nên thêm authentication và encryption

---

## Kiểm tra nhanh

1. Server chạy thành công nếu thấy: `"RMI Bank server đã khởi động trên port 1099"`
2. Client kết nối thành công nếu thấy trong log: `"Đã kết nối đến server: <IP>"` và `"Đăng nhập thành công"`

