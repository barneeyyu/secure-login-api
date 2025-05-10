# Secure Login API

A Spring Boot RESTful API for user registration, login (with 2FA email verification), and querying last login time.

## 🚀 Features

- 使用 Email 註冊帳號，需驗證 Email 開通帳號
- 登入需輸入帳號密碼 + Email 驗證碼（二階段驗證）
- 使用者可查詢「自己的」最後登入時間（無法查詢他人）
- 使用 Mailjet API 發送驗證信
- 可部署於 AWS Elastic Beanstalk 或其他雲端環境

---

## 📘 API Documentation

API 支援標準 RESTful 呼叫，並提供 Swagger UI 文件頁面。

### 🔗 Swagger UI

- 開發環境 URL：`http://localhost:8080/swagger-ui/index.html`
- 部署後 URL：`http://<your-eb-url>/swagger-ui/index.html`

---

## ⚙️ Tech Stack

| 技術        | 版本            |
|-------------|-----------------|
| Java        | 17              |
| Spring Boot | 3.2.x（建議使用最新） |
| Gradle      | 8.x             |
| PostgreSQL  | 12+             |
| Mailjet API | RESTful HTTP    |

---

## 🛠 Build & Run

### 🔧 Build using Gradle

```bash
./gradlew clean build
```

### ▶️ Run locally (Spring Boot dev)

```bash
./gradlew bootRun
```

### 📦 Deploy to AWS Elastic Beanstalk

// todo
