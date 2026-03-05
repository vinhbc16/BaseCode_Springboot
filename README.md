# Basecode - Spring Boot Backend Base Project

> Base backend Spring Boot chuẩn production, thiết kế theo kiến trúc **Package by Feature** (Domain-driven), sẵn sàng mở rộng và bóc tách thành Microservices.

---

## Tech Stack

| Công nghệ | Phiên bản | Mục đích |
|---|---|---|
| Java | 21 | Ngôn ngữ chính |
| Spring Boot | 4.0.x | Framework backend |
| Spring Data JPA | - | ORM & Data Access |
| MySQL | 8.x+ | Cơ sở dữ liệu |
| Lombok | - | Giảm boilerplate code |
| MapStruct | 1.6.3 | Mapping Entity ↔ DTO |
| Jakarta Validation | - | Validation đầu vào |
| Maven | - | Build tool |

---

## Kiến trúc dự án

### Package by Feature (Domain-driven)

Mỗi feature/domain nằm trong 1 package riêng biệt, chứa đầy đủ các layer, dễ dàng tách thành microservice khi cần.

```
src/main/java/com/caovinh/
├── BasecodeApplication.java          # Entry point
│
├── common/                           # ── Shared / Cross-cutting ──
│   ├── dto/
│   │   ├── ApiResponse.java          # Response wrapper chuẩn {success, message, data}
│   │   └── PageResponse.java         # Wrapper cho Pagination response
│   ├── entity/
│   │   └── BaseEntity.java           # Base entity (id, createdAt, updatedAt)
│   └── exception/
│       ├── BadRequestException.java
│       ├── ResourceNotFoundException.java
│       └── GlobalExceptionHandler.java   # @RestControllerAdvice - bắt mọi lỗi
│
└── user/                             # ── Feature Module mẫu ──
    ├── controller/
    │   └── UserController.java       # REST API endpoints
    ├── dto/
    │   ├── UserRequestDto.java       # DTO + Jakarta Validation
    │   └── UserResponseDto.java      # DTO trả về client
    ├── entity/
    │   └── User.java                 # JPA Entity
    ├── mapper/
    │   └── UserMapper.java           # MapStruct mapper
    ├── repository/
    │   └── UserRepository.java       # Spring Data JPA Repository
    └── service/
        ├── UserService.java          # Service interface
        └── UserServiceImpl.java      # Business logic implementation
```

### Design Patterns & Quy tắc đã áp dụng

- **Controller → Service → Repository**: tách rõ từng layer.
- **DTO Pattern**: Entity **không bao giờ** trả trực tiếp ra Controller.
- **MapStruct**: tự động generate code mapping Entity ↔ DTO tại compile-time.
- **Global Exception Handler**: mọi exception đều trả về JSON chuẩn.
- **SOLID**: Controller chỉ điều phối, business logic 100% ở Service.
- **Pagination**: sử dụng `Pageable` + `PageResponse` wrapper.

### Response format chuẩn

Tất cả API đều trả về format thống nhất:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... }
}
```

Khi lỗi validation:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email must be valid",
    "fullName": "Full name is required"
  }
}
```

---

## API Endpoints (User CRUD mẫu)

| Method | URL | Mô tả |
|---|---|---|
| `POST` | `/api/v1/users` | Tạo user mới |
| `GET` | `/api/v1/users/{id}` | Lấy user theo ID |
| `GET` | `/api/v1/users?page=0&size=10&sort=id,asc` | Danh sách user (Pagination) |
| `PUT` | `/api/v1/users/{id}` | Cập nhật user |
| `DELETE` | `/api/v1/users/{id}` | Xóa user |

---

## Cài đặt & Chạy dự án

### Yêu cầu

- **JDK 21** trở lên
- **MySQL 8.x** trở lên
- **Maven 3.9+** (hoặc dùng `./mvnw` có sẵn trong dự án)

### Bước 1: Tạo database

```sql
CREATE DATABASE basecode_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Bước 2: Cấu hình kết nối

Mở file `src/main/resources/application.properties` và chỉnh thông tin phù hợp:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/basecode_db
spring.datasource.username=root
spring.datasource.password=root
```

### Bước 3: Build & Run

```bash
# Build project
mvn clean compile

# Chạy ứng dụng
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

### Test nhanh với cURL

```bash
# Tạo user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"fullName": "Cao Vinh", "email": "vinh@example.com", "phone": "0901234567"}'

# Lấy danh sách user (page 0, size 10)
curl http://localhost:8080/api/v1/users?page=0&size=10
```

---

## Hướng dẫn phát triển

### Thêm Feature mới

Khi cần thêm 1 feature mới (VD: `Product`), tạo package mới cùng cấp với `user/`:

```
src/main/java/com/caovinh/
├── common/
├── user/
└── product/              ← Package mới
    ├── controller/
    │   └── ProductController.java
    ├── dto/
    │   ├── ProductRequestDto.java
    │   └── ProductResponseDto.java
    ├── entity/
    │   └── Product.java          ← extends BaseEntity
    ├── mapper/
    │   └── ProductMapper.java    ← @Mapper(componentModel = "spring")
    ├── repository/
    │   └── ProductRepository.java
    └── service/
        ├── ProductService.java
        └── ProductServiceImpl.java
```

### Lưu ý quan trọng khi phát triển

#### 1. Không trả Entity ra ngoài Controller
```java
// ❌ SAI
@GetMapping("/{id}")
public Product getProduct(@PathVariable Long id) { ... }

// ✅ ĐÚNG
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(@PathVariable Long id) { ... }
```

#### 2. Lombok + MapStruct - Thứ tự Annotation Processor
Thứ tự trong `pom.xml` phải là: **Lombok → lombok-mapstruct-binding → MapStruct**. Đã được cấu hình sẵn, **không thay đổi thứ tự**.

#### 3. Entity luôn kế thừa BaseEntity
```java
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    // Tự có id, createdAt, updatedAt
}
```

#### 4. Luôn dùng ApiResponse wrapper
```java
// Trả thành công
return ResponseEntity.ok(ApiResponse.success(data));
return ResponseEntity.ok(ApiResponse.success("Custom message", data));

// Trả lỗi (tự động qua GlobalExceptionHandler)
throw new ResourceNotFoundException("Product", "id", id);
throw new BadRequestException("SKU already exists");
```

#### 5. Validation trên DTO, không trên Entity
```java
public class ProductRequestDto {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
}
```

#### 6. Pagination
Dùng `Pageable` parameter + `PageResponse` wrapper. Spring tự parse query params `?page=0&size=10&sort=name,asc`.

#### 7. Quan hệ giữa các Feature
Nếu feature A cần dùng data của feature B, inject **Service** (không inject Repository trực tiếp):
```java
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final UserService userService;  
}
```

### Các mở rộng gợi ý cho production

- [ ] **Spring Security + JWT** — Authentication & Authorization
- [ ] **Spring Profiles** — Tách config theo environment (dev / staging / prod)
- [ ] **Flyway / Liquibase** — Database migration versioning
- [ ] **Swagger / SpringDoc OpenAPI** — API documentation
- [ ] **Docker + Docker Compose** — Containerization
- [ ] **Logging** — SLF4J + cấu hình log level
- [ ] **Caching** — Spring Cache + Redis
- [ ] **Unit Tests + Integration Tests** — JUnit 5 + Mockito
- [ ] **CI/CD** — GitHub Actions / Jenkins pipeline
- [ ] **Monitoring** — Micrometer + Prometheus + Grafana
- [ ] **Distributed Tracing** — Spring Cloud Sleuth + Zipkin
- [ ] **Event-driven architecture** — Spring Events / Kafka
- [ ] **GraphQL** — Spring GraphQL
- [ ] **gRPC** — Spring gRPC starter
- [ ] **WebSocket** — Spring WebSocket
- [ ] **Rate Limiting** — Bucket4j / Spring Cloud Gateway
- [ ] **API Versioning** — URL versioning / Header versioning
- [ ] **Internationalization (i18n)** — Spring MessageSource
- [ ] **Feature Flags** — Togglz / Unleash
- [ ] **Code Quality** — SonarQube / Checkstyle
- [ ] **Security Hardening** — OWASP best practices
- [ ] **Performance Optimization** — Caching, DB indexing, async processing
- [ ] **Cloud Deployment** — AWS / Azure / GCP
- [ ] **Microservices** — Spring Cloud / Kubernetes
- [ ] **Serverless** — Spring Cloud Function + AWS Lambda
- [ ] **GraphQL** — Spring GraphQL


