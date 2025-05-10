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
- 部署後 URL：`http://Secure-login-api-env-1.eba-ng8unjmn.us-east-1.elasticbeanstalk.com/swagger-ui/index.html`

---

## ⚙️ Tech Stack

| 技術        | 版本            |
|-------------|-----------------|
| Java        | 17              |
| Spring Boot | 3.4.5           |
| Gradle      | 8.x             |
| PostgreSQL  | 17+             |
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

**Domain:** `http://Secure-login-api-env-1.eba-ng8unjmn.us-east-1.elasticbeanstalk.com`

我選用了 AWS Elastic Beanstalk (EB) 應用程式管理服務，它可以自動處理部署、容量佈建、負載平衡、自動擴展和應用程式運作狀態監控等細節。當你透過 Elastic Beanstalk 部署一個 Spring Boot 應用程式時，它會在幕後為你建立和管理一系列 AWS 資源。主要包括：

| AWS 資源                                  | 說明                                                                                                                                                                                             |
| :---------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Amazon EC2 Instance (虛擬伺服器)**        | 你的 Spring Boot 應用程式 (JAR 檔案) 會在這台 EC2 實例上執行。EB 會根據你的設定（例如環境類型、實例類型）來啟動和管理這個實例。                                                                                  |
| **Auto Scaling Group (for single instance)** | 即使你目前只運行一個 EC2 實例，EB 通常也會設定一個最小和最大實例數均為 1 的 Auto Scaling Group。這有助於保持實例的健康（如果實例失敗，Auto Scaling Group 會嘗試替換它）並為將來的擴展提供基礎。                               |
| **Security Groups (SG)**                  | 這些是虛擬防火牆，用於控制進出你的 EC2 實例和 RDS 資料庫實例的網路流量。EB 會建立預設的安全群組規則（例如，允許來自網際網路的 HTTP (80) 和/或 HTTPS (443) 流量進入你的 EC2 實例；允許 EC2 實例存取 RDS PostgreSQL 資料庫）。 |
| **Amazon S3 Bucket**                      | 即使你手動上傳 JAR 檔案，Elastic Beanstalk 仍會使用一個 S3 Bucket 來儲存你上傳的應用程式版本（JAR 檔案）以及可能的環境日誌檔案。                                                                                   |
| **Amazon CloudWatch Alarms**              | EB 會設定 CloudWatch 警報來監控你環境的運作狀態和效能指標（例如 EC2 實例的 CPU 使用率、網路流量），並在出現問題時通知你。                                                                                       |
| **Amazon RDS Database Instance (PostgreSQL)** | 你已選擇使用 RDS，EB 會為你佈建和管理一個 PostgreSQL 資料庫實例。這個 RDS 實例本身運行在 AWS 管理的 EC2 基礎設施上，但你透過 RDS 服務與其互動。                                                                      |
| **Amazon VPC (Virtual Private Cloud)**    | 你的 Elastic Beanstalk 環境和 RDS 資料庫都部署在你指定的 VPC 中。VPC 為你的 AWS 資源提供了一個邏輯上隔離的網路環境，包含子網路、路由表、網路閘道等元件，讓你可以更好地控制網路配置。                                              |

**手動部署步驟 (使用 JAR 檔案):**

1.  **建置應用程式的 JAR 檔案:**
    在你的專案根目錄下執行以下 Gradle 命令，這會產生一個可執行的 JAR 檔案 (通常位於 `build/libs/` 目錄下，例如 `secure-login-api-0.0.1-SNAPSHOT.jar`)。
    ```bash
    ./gradlew bootJar
    ```
    或者，如果你想執行清理並建置:
    ```bash
    ./gradlew clean bootJar
    ```

2.  **登入 AWS 管理控制台並導覽至 Elastic Beanstalk。**

3.  **選擇你的應用程式 (Application) 和環境 (Environment)。**

4.  **上傳並部署新的應用程式版本:**
    *   在環境的儀表板中，點擊「上傳和部署」(Upload and Deploy) 按鈕。
    *   選擇「選擇檔案」(Choose file) 並上傳你在步驟 1 中產生的 JAR 檔案。
    *   為這個版本提供一個「版本標籤」(Version label)，例如 `v1.0.0` 。
    *   點擊「部署」(Deploy) 按鈕。

Elastic Beanstalk 會接收你的 JAR 檔案，建立一個新的應用程式版本，然後將其部署到你的環境中的 EC2 實例上。部署過程可能需要幾分鐘時間。你可以在 EB 主控台上監控部署狀態。

//TODO (因為要快速完成此需求，如有時間可以用 EB CLI 搭配 CI/CD 達成自動部)