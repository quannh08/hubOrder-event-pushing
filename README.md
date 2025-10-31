# 🧩 Hub Order Event Pushing
**Hướng dẫn sử dụng đẩy sự kiện vào Kafka**

---

## 📘 Mục lục
- [A. Mô tả chung](#a-mô-tả-chung)
    - [1. Chức năng](#1-chức-năng)
    - [2. Các thành phần chính](#2-các-thành-phần-chính)
- [B. Cài đặt và sử dụng](#b-cài-đặt-và-sử-dụng)
    - [1. Cấu trúc dữ liệu trong database](#1-cấu-trúc-dữ-liệu-trong-database)
    - [2. Hướng dẫn sử dụng](#2-hướng-dẫn-sử-dụng)
        - [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
        - [Vị trí service](#vị-trí-service)
        - [Cách module hoạt động](#cách-module-hoạt-động)
        - [Cách khởi chạy module](#cách-khởi-chạy-module)

---

## A. Mô tả chung

### 1. Chức năng
Module này có nhiệm vụ đẩy các **sự kiện đơn hàng (order event)** từ cơ sở dữ liệu lên các **topic Kafka tương ứng** và cập nhật trạng thái đẩy trong database.

### 2. Các thành phần chính
- **ORDER_EVENT:** Bảng chứa các event cần đẩy
- **ORDER_EVENT_KAFKA_CONFIG:** Cấu hình các event được phép đẩy
- **CONFIG:** Lưu thông tin cấu hình hệ thống
- **10 worker thread:** Xử lý song song
- **Kafka Producer:** Gửi message tới Kafka
- **Telegram bot:** Cảnh báo lỗi đẩy Kafka

---

## B. Cài đặt và sử dụng

### 1. Cấu trúc dữ liệu trong database

#### 📄 Danh sách sự kiện hỗ trợ:
| Event Type | Mô tả |
|-------------|--------|
| HOLD | Đơn hàng hold thành công |
| HOLD_FAILED | Hold thất bại |
| PAID | Ghi nhận thanh toán |
| COMPLETED | Xuất vé thành công |
| ISSUE_FAILED | Xuất vé lỗi |
| REFUNDED | Hoàn tiền thành công |
| REFUND_REQUESTED | Ghi nhận yêu cầu hoàn tiền |
| CANCELLED | Khách hoặc đại lý hủy đơn |
| EXPIRED | Đơn bị hết hạn |
| CC_receive | CSKH nhận đơn hàng |
| CC_transfer | CSKH chuyển đơn hàng cho CSKH khác |
| CC_cancel | CSKH bỏ nhận đơn hàng |
| INVOICE_ISSUED | Xuất hóa đơn thành công |

---

#### 🧱 Bảng `ORDER_EVENT`

| STT | Tên trường | Kiểu | Nullable | Mô tả |
|-----|-------------|------|-----------|--------|
| 1 | ID | Number |  | ID của bảng, dùng `ORDER_EVENT_SEQ` để gen |
| 2 | DATE_ONLY | Date |  | Ngày tạo sự kiện, phục vụ báo cáo |
| 3 | DATETIME | Datetime |  | Thời gian tạo sự kiện |
| 4 | AGENT_CODE | String |  | Mã đại lý trực tiếp |
| 5 | SERVICE | String | X | Mã dịch vụ của đơn hàng/đơn hàng con |
| 6 | OBJECT_TYPE | String |  | Kiểu đối tượng dịch vụ (`order`, `sub`) |
| 7 | ORDER_ID | Number |  | ID của đơn hàng |
| 8 | ORDER_SUB_ID | Number | X | ID của đơn hàng con |
| 9 | EVENT_TYPE | String |  | Kiểu sự kiện |
| 10 | CONTENT | String | X | Nội dung sự kiện (ví dụ như lỗi nếu là sự kiện lỗi) |
| 11 | AMOUNT | Number | X | Tổng số tiền của đơn hàng |
| 12 | AMOUNT_PRICE | Number | X | Tổng giá gốc của đơn hàng |
| 13 | AMOUNT_FEE | Number | X | Tổng phí dịch vụ |
| 14 | AMOUNT_DISCOUNT | Number | X | Tổng tiền giảm giá |
| 15 | DISCOUNT_CODE | String | X | Mã giảm giá áp dụng |
| 16 | PUSH_STATUS | Number |  | Trạng thái đẩy Kafka:<br/>• 0: Chờ đẩy<br/>• 1: Đẩy thành công<br/>• 2: Đẩy thất bại<br/>• 5: Đang đẩy<br/>• 9: Không đẩy |
| 17 | PUSH_DATETIME | Datetime | X | Thời gian đẩy |
| 18 | PUSH_ERROR | String | X | Nội dung lỗi khi đẩy Kafka thất bại |

---

#### ⚙️ Bảng `ORDER_EVENT_KAFKA_CONFIG`

| STT | Tên trường | Kiểu | Nullable | Mô tả |
|-----|-------------|------|-----------|--------|
| 1 | ID | Number |  | ID của bảng |
| 2 | SERVICE | String |  | Dịch vụ tương ứng với bảng `ORDER_EVENT` (`*` nghĩa là chấp nhận tất cả) |
| 3 | AGENT_CODE | String |  |  |
| 4 | OBJECT_TYPE | String |  |  |
| 5 | EVENT_TYPE | String |  |  |
| 6 | KAFKA_TOPIC | String |  | Topic của Kafka |
| 7 | STATUS | Number |  | Trạng thái cấu hình:<br/>• 0: active<br/>• 1: block |

---

#### 🔧 Bảng `CONFIG`

| STT | Giá trị key | Ý nghĩa của value |
|-----|--------------|------------------|
| 1 | event_kafka_server | Link kết nối Kafka |
| 2 | event_telegram_key | Key cảnh báo lỗi của Telegram |
| 3 | event_telegram_group_id | Group chat gửi cảnh báo lỗi của Telegram |

---

### 2. Hướng dẫn sử dụng

#### 💻 Yêu cầu hệ thống:

| Thành phần | Phiên bản khuyến nghị | Ghi chú |
|-------------|------------------------|----------|
| Java | 17 trở lên | Dùng cho Spring Boot 3.x |
| Spring Boot | 3.2.x | Framework chính |
| Database | Oracle 19c+ | Lưu event và cấu hình |
| Telegram API | Có sẵn bot token hợp lệ | Dùng để báo lỗi |

---

#### 📂 Vị trí service:
- **Package:** `com.kafka.hubordereventpushing.service`
- **Class:** `KafkaPushService`

---

#### ⚙️ Cách module hoạt động:
- Hệ thống có **10 worker thread** xử lý song song các sự kiện có `PUSH_STATUS = 0`.
- Sau mỗi **10 phút**, hệ thống **tự động reload cấu hình**.
- Khi có lỗi đẩy Kafka, hệ thống **gửi cảnh báo qua Telegram**.

---

## ⚙️ Sơ đồ luồng xử lý (Flowchart)

```mermaid
flowchart TD

A[Start] --> B[Khởi tạo Thread Pool]
B --> C[Lấy 10 event chưa được xử lý từ Database]
C --> D{Còn event để xử lý?}
D -->|Không| E[Sleep / Chờ vòng lặp kế tiếp]
D -->|Có| F[Phân luồng xử lý từng event song song]

subgraph Thread_xu_ly_1_event
    F1[Đọc thông tin event]
    F2[Lấy danh sách cấu hình từ bảng ORDER_EVENT_KAFKA_CONFIG]
    F1 --> F2
    F2 --> G{Event có cấu hình phù hợp?}
    G -->|Không| H[Cập nhật push_status = 9<br/>Không phù hợp cấu hình]
    G -->|Có| I[Gửi event đến topic Kafka tương ứng]
    I --> J{Gửi thành công?}
    J -->|Có| K[Cập nhật trạng thái thành công trong DB]
    J -->|Lỗi| L[Cập nhật push_status lỗi và lưu push_error]
    L --> M[Gửi thông báo lỗi về Telegram (CONFIG)]
end

F --> F1
K --> N[Hoàn thành xử lý event]
H --> N
M --> N
N --> D
E --> D
```
#### 🚀 Cách khởi chạy module:

1. **Cấu hình database** trong file `application.yml`:
   ```yaml
   spring:
     datasource:
       # Thông tin kết nối đến database Oracle
       url: jdbc:oracle:thin:@//<DATABASE_HOST>:<PORT>/<SERVICE_NAME>
       username: <DATABASE_USERNAME>
       password: <DATABASE_PASSWORD>
       driver-class-name: oracle.jdbc.OracleDriver
cd <thư_mục_chứa_file_jar> và chạy 
<pre>java -jar hubOrder-event-pushing-0.0.1.jar</pre>
