# 🎓 高校校园智能体终端 v2.0

> 基于 **Spring Boot 3.x + React 19 + MySQL** 的前后端分离架构  
> Dribbble 极简主义风格 · 按业务功能模块化组织 · RESTful API 驱动

---

## 📖 目录

1. [项目概述](#1-项目概述)
2. [架构设计](#2-架构设计)
3. [目录结构详解](#3-目录结构详解)
4. [技术栈](#4-技术栈)
5. [环境要求](#5-环境要求)
6. [快速启动](#6-快速启动)
7. [需要修改的配置](#7-需要修改的配置)
8. [API 接口文档](#8-api-接口文档)
9. [数据库设计](#9-数据库设计)
10. [功能模块说明](#10-功能模块说明)
11. [AI 对话系统](#11-ai-对话系统)
12. [生产部署](#12-生产部署)
13. [常见问题](#13-常见问题)

---

## 1. 项目概述

本项目将原 `高校校园智能体终端`（React + Express 单体架构）重构为**前后端完全分离**架构：

| 对比维度 | v1 原项目 | v2 重构后 |
|----------|----------|----------|
| 架构模式 | Express 单体服务 | Spring Boot + React 分离 |
| 数据存储 | 前端硬编码 mock 数据 | MySQL 数据库持久化 |
| 业务逻辑 | 前端处理本地操作 | 后端 Service 层统一处理 |
| AI 服务 | 仅支持 Gemini | Gemini / OpenAI / 演示模式三选一 |
| 接口风格 | 单一 /api/chat | 完整 RESTful API (20+ 接口) |
| 项目组织 | 按技术层分层 | 按业务功能模块化 |
| 前端数据 | 组件内 state 存储 | 纯展示层，无状态无存储 |

### 核心原则

> **前端不做任何逻辑和业务处理，所有数据通过 REST API 从后端获取，前端无状态、无存储。**

---

## 2. 架构设计

```
┌─────────────────────────────────┐          REST API (JSON)          ┌─────────────────────────────────┐
│        campus-web (前端)         │ ◄──────────────────────────────► │      campus-server (后端)         │
│                                  │                                  │                                  │
│  React 19 + Vite + Tailwind     │    GET/POST/PUT/DELETE           │  Spring Boot 3.3 + JPA           │
│  ─────────────────────────────  │    /api/courses                  │  ─────────────────────────────── │
│  • 纯展示层，无业务逻辑           │    /api/card                     │  • 所有业务逻辑在 Service 层      │
│  • 无本地数据存储                │    /api/presence                 │  • JPA Repository 操作 MySQL      │
│  • API 调用驱动 UI 渲染          │    /api/library                  │  • Spring AI 多通道支持           │
│  • 按功能模块组织组件             │    /api/academic                 │  • 全局异常拦截 + 统一响应格式     │
│  • shared/types.ts 类型定义      │    /api/chat                     │  • 按功能模块化组织代码            │
└─────────────────────────────────┘                                  └──────────────┬──────────────────┘
                                                                                    │
                                                                                    ▼
                                                                         ┌─────────────────────┐
                                                                         │   MySQL Database     │
                                                                         │   campus_db          │
                                                                         │   ───────────────    │
                                                                         │   tb_student         │
                                                                         │   tb_course          │
                                                                         │   tb_card            │
                                                                         │   tb_transaction     │
                                                                         │   tb_library_book    │
                                                                         │   tb_campus_moment   │
                                                                         │   tb_seat            │
                                                                         │   tb_chat_message    │
                                                                         └─────────────────────┘
```

---

## 3. 目录结构详解

### 3.1 项目根目录

```
高校校园智能体终端-v2/
├── campus-server/          ←【后端】Spring Boot 3.x 项目
├── campus-web/             ←【前端】React 19 + Vite 项目
├── README.md               ←📖 本文档
```

### 3.2 后端目录 (campus-server)

```
campus-server/
├── pom.xml                          ← Maven 依赖配置（Spring Boot / JPA / MySQL / Spring AI）
├── sql/
│   └── init.sql                     ← 数据库初始化脚本（建表 + 示例数据）
└── src/main/
    ├── resources/
    │   └── application.yml          ← ⚙️ 主配置文件（数据库/AI/端口等）
    └── java/com/starrycampus/
        ├── CampusApplication.java   ← Spring Boot 启动入口
        │
        ├── common/                  ← 📦 公共模块（跨模块共享）
        │   ├── config/
        │   │   ├── CorsConfig.java      ← 跨域配置（允许前端 5173 访问）
        │   │   └── AiConfig.java        ← AI 多通道配置（Gemini/OpenAI/Demo 切换）
        │   ├── exception/
        │   │   └── GlobalExceptionHandler.java ← 全局异常拦截
        │   ├── base/
        │   │   └── ApiResponse.java     ← 统一 API 响应格式 {code, message, data}
        │   └── entity/
        │       └── Student.java         ← 学生实体（被多个模块共享使用）
        │
        ├── course/                  ← 📅 课程模块
        │   ├── controller/CourseController.java  ← GET /api/courses, POST checkin
        │   ├── service/CourseService.java        ← 接口定义
        │   ├── service/impl/CourseServiceImpl.java ← 业务实现
        │   ├── repository/CourseRepository.java  ← JPA 数据访问
        │   ├── entity/Course.java                ← 课程 JPA 实体
        │   └── dto/CourseDTO.java                ← 课程响应 DTO
        │
        ├── card/                    ← 💳 一卡通模块
        │   ├── controller/CardController.java   ← GET /api/card, POST topup
        │   ├── service/CardService.java
        │   ├── service/impl/CardServiceImpl.java
        │   ├── repository/CardRepository.java
        │   ├── repository/TransactionRepository.java ← 交易记录查询
        │   ├── entity/Card.java
        │   ├── entity/Transaction.java
        │   └── dto/ (CardDTO, TransactionDTO, TopUpRequest)
        │
        ├── presence/                ← 📍 我在校园模块
        │   ├── controller/PresenceController.java ← 动态列表/发布/报名/签到
        │   ├── service/PresenceService.java
        │   ├── service/impl/PresenceServiceImpl.java
        │   ├── repository/CampusMomentRepository.java
        │   ├── entity/CampusMoment.java
        │   └── dto/ (CampusMomentDTO, MomentCreateRequest)
        │
        ├── library/                 ← 📚 图书馆模块
        │   ├── controller/LibraryController.java ← 图书列表/续借/座位预约
        │   ├── service/LibraryService.java
        │   ├── service/impl/LibraryServiceImpl.java
        │   ├── repository/LibraryBookRepository.java
        │   ├── repository/SeatRepository.java
        │   ├── entity/LibraryBook.java
        │   ├── entity/Seat.java
        │   └── dto/ (LibraryBookDTO, SeatDTO, SearchRequest)
        │
        ├── academic/                ← 🎓 学业成就模块
        │   ├── controller/AcademicController.java ← GET /api/academic/profile
        │   ├── service/AcademicService.java
        │   ├── service/impl/AcademicServiceImpl.java
        │   ├── repository/StudentRepository.java  ← 学生数据查询
        │   └── dto/ (AcademicProfileDTO, SubjectStrengthDTO)
        │
        └── chat/                    ← 🤖 AI 聊天模块
            ├── controller/AiChatController.java   ← POST /api/chat, GET history
            ├── service/AiChatService.java
            ├── service/impl/AiChatServiceImpl.java ← 意图识别 + AI 调用 + 操作拦截
            ├── repository/ChatMessageRepository.java
            ├── entity/ChatMessage.java
            └── dto/ (ChatRequest, ChatResponse, ChatMessageDTO)
```

> **设计原则**：每个业务模块自包含 — 拥有独立的 `controller/`、`service/`、`repository/`、`entity/`、`dto/`，实现**接口与实现分离**（`service/` 放接口，`service/impl/` 放实现）。

### 3.3 前端目录 (campus-web)

```
campus-web/
├── package.json              ← npm 依赖配置
├── vite.config.ts            ← Vite 配置（含 /api 代理到 localhost:8080）
├── tsconfig.json             ← TypeScript 配置
├── index.html                ← 入口 HTML
├── .env.example              ← 环境变量模板
└── src/
    ├── main.tsx              ← React 入口
    ├── App.tsx               ← 根组件（API 驱动，无本地业务逻辑）
    ├── index.css             ← Tailwind CSS + 全局样式
    │
    ├── shared/               ← 📦 共享模块
    │   ├── api/client.ts     ← fetch 通用封装（get/post/del）
    │   └── types.ts          ← TypeScript 类型定义（与后端 DTO 一一对应）
    │
    └── features/             ← 🎯 业务功能模块
        ├── course/           ← 📅 课程模块
        │   ├── api/courseApi.ts                 ← 课程 API 封装
        │   └── components/ScheduleCard.tsx       ← 课表展示组件
        │
        ├── card/             ← 💳 一卡通模块
        │   ├── api/cardApi.ts                   ← 一卡通 API 封装
        │   └── components/SmartCard.tsx          ← 一卡通展示组件
        │
        ├── presence/         ← 📍 我在校园模块
        │   ├── api/presenceApi.ts               ← 动态/报名/签到 API
        │   └── components/PresenceCard.tsx       ← 校园动态展示组件
        │
        ├── library/          ← 📚 图书馆模块
        │   ├── api/libraryApi.ts                ← 图书/座位 API
        │   └── components/LibraryCard.tsx        ← 图书馆展示组件
        │
        ├── academic/         ← 🎓 学业模块
        │   ├── api/academicApi.ts               ← 学业概况 API
        │   └── components/AcademicCompassCard.tsx ← 学业罗盘展示组件
        │
        └── chat/             ← 🤖 AI 聊天模块
            ├── api/chatApi.ts                   ← 聊天 API 封装
            └── components/AiAssistantPanel.tsx   ← AI 聊天面板组件
```

> **设计原则**：每个功能模块自包含 API 封装 + UI 组件，`shared/` 下的 `types.ts` 类型定义与后端 DTO 严格对应。

---

## 4. 技术栈

| 层面 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **后端框架** | Spring Boot | 3.3.5 | REST API 服务 |
| **Java** | OpenJDK | 17 LTS | 运行环境 |
| **ORM** | Spring Data JPA (Hibernate) | — | 对象关系映射 |
| **AI 框架** | Spring AI | 1.0.0-M4 | AI 服务集成 |
| **数据库** | MySQL | 8.0+ | 业务数据持久化 |
| **构建工具** | Maven | 3.8+ | 项目构建 |
| **前端框架** | React | 19 | 用户界面 |
| **构建工具** | Vite | 6 | 前端构建 |
| **CSS** | Tailwind CSS | 4 | 样式系统 |
| **图标库** | Lucide React | 0.546 | UI 图标 |
| **动画** | Motion | 12 | 过渡动画 |

---

## 5. 环境要求

| 工具 | 最低版本 | 检查命令 | 说明 |
|------|----------|----------|------|
| **JDK** | 17 | `java -version` | 后端运行环境 |
| **Maven** | 3.8+ | `mvn -v` | 后端构建工具 |
| **MySQL** | 8.0+ | `mysql --version` | 数据库服务 |
| **Node.js** | 18+ | `node -v` | 前端运行环境 |
| **npm** | 9+ | `npm -v` | 前端包管理器 |

---

## 6. 快速启动

### 第一步：初始化数据库

```bash
# 登录 MySQL 并执行初始化脚本
mysql -u root -p < campus-server/sql/init.sql
```

执行后会自动创建 `campus_db` 数据库，包含 8 张表和完整的示例数据。

### 第二步：配置并启动后端

```bash
cd campus-server

# 编辑 src/main/resources/application.yml 修改数据库连接（见第 7 节）

# 启动后端（默认端口 8080）
mvn spring-boot:run
```

### 第三步：启动前端

```bash
cd campus-web

# 安装依赖（仅首次）
npm install

# 启动前端开发服务器（默认端口 5173）
npm run dev
```

浏览器访问 **http://localhost:5173** 即可看到完整界面。

---

## 7. 需要修改的配置

### 🔴 必须修改

#### 7.1 数据库连接

**文件**：`campus-server/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    # ↓↓↓ 修改为你的 MySQL 用户名和密码 ↓↓↓
    username: root
    password: root
```

#### 7.2 AI 服务配置

**方式一：设置环境变量（推荐）**

```bash
# Windows (PowerShell)
$env:AI_PROVIDER="gemini"
$env:GEMINI_API_KEY="你的_API_Key"

# Linux / macOS
export AI_PROVIDER=gemini
export GEMINI_API_KEY="你的_API_Key"
```

**方式二：直接修改 application.yml**

```yaml
spring:
  ai:
    provider: gemini     # demo | gemini | openai
```

| AI_PROVIDER 值 | 效果 | 需要配置的 Key |
|---------------|------|---------------|
| `demo` | 演示模式，内置预设回复 | **无需任何 Key** |
| `gemini` | Google Gemini AI | `GEMINI_API_KEY` |
| `openai` | OpenAI / DeepSeek / 阿里百炼等 | `OPENAI_API_KEY` + `OPENAI_BASE_URL` |

> 💡 **推荐**：首次运行先使用 `demo` 模式验证项目能正常启动，再配置真实 AI 服务。

#### 7.3 获取 API Key

| AI 服务 | 获取地址 |
|---------|---------|
| Google Gemini | https://aistudio.google.com/apikey |
| OpenAI | https://platform.openai.com/api-keys |
| DeepSeek | https://platform.deepseek.com/api_keys |

### 🟡 可选修改

#### 7.4 服务端口

- 后端：`application.yml` → `server.port`（默认 8080）
- 前端：`vite.config.ts` → `server.port`（默认 5173）

#### 7.5 前端 API 代理

**文件**：`campus-web/vite.config.ts`

```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',  // ← 修改为后端实际地址
    changeOrigin: true,
  },
},
```

#### 7.6 系统提示词

**文件**：`campus-server/src/main/java/com/starrycampus/common/config/AiConfig.java`

修改 `getSystemPrompt()` 方法中的 `return` 内容可定制 AI 人设。

#### 7.7 演示模式学生 ID

**文件**：`application.yml` → `campus.demo-student-id`

默认为 `1`（对应 init.sql 中插入的第一个学生）。

---

## 8. API 接口文档

所有接口前缀：`/api`，返回格式：`{"code":200,"message":"success","data":{...}}`

### 8.1 课程模块

| 方法 | 路径 | 请求体 | 说明 |
|------|------|--------|------|
| GET | `/api/courses` | — | 获取全部课程 |
| GET | `/api/courses?weekday=1` | — | 按周几筛选(1-5) |
| GET | `/api/courses/{id}` | — | 课程详情 |
| POST | `/api/courses/{id}/checkin` | — | 课堂签到 |

### 8.2 一卡通模块

| 方法 | 路径 | 请求体 | 说明 |
|------|------|--------|------|
| GET | `/api/card` | — | 获取一卡通(余额+卡号+最近3条消费) |
| GET | `/api/card/transactions` | — | 全部消费账单 |
| POST | `/api/card/topup` | `{"amount": 50.00}` | 充值 |

### 8.3 我在校园模块

| 方法 | 路径 | 请求体 | 说明 |
|------|------|--------|------|
| GET | `/api/presence/moments` | — | 校园动态列表 |
| POST | `/api/presence/moments` | `{"title":"...","tag":"...","location":"..."}` | 发布新动态 |
| POST | `/api/presence/moments/{id}/join` | — | 报名活动 |
| GET | `/api/presence/checkin-status` | — | 签到状态 |
| POST | `/api/presence/checkin` | — | 入座签到 |

### 8.4 图书馆模块

| 方法 | 路径 | 请求体 | 说明 |
|------|------|--------|------|
| GET | `/api/library/books` | — | 在借图书列表 |
| POST | `/api/library/books/{id}/renew` | — | 续借 (+7天) |
| GET | `/api/library/seats` | — | 自习座位列表 |
| POST | `/api/library/seats/{id}/book` | — | 预约座位 |
| POST | `/api/library/seats/{id}/release` | — | 释放座位 |
| POST | `/api/library/search` | `{"keyword":"..."}` | 图书检索 |

### 8.5 学业模块

| 方法 | 路径 | 请求体 | 说明 |
|------|------|--------|------|
| GET | `/api/academic/profile` | — | 学业概览(GPA/学分/能力雷达) |

### 8.6 AI 聊天模块

| 方法 | 路径 | 请求体 | 说明 |
|------|------|--------|------|
| POST | `/api/chat` | `{"message":"..."}` | 发送消息 |
| GET | `/api/chat/history` | — | 获取聊天历史 |
| DELETE | `/api/chat/history` | — | 清空聊天记录 |

---

## 9. 数据库设计

数据库 `campus_db`，共 8 张表：

### tb_student（学生表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| student_no | VARCHAR(20) UNIQUE | 学号 |
| name | VARCHAR(50) | 姓名 |
| department | VARCHAR(100) | 院系 |
| gpa | DECIMAL(3,2) | GPA |
| credits_earned | INT | 已修学分 |
| credits_required | INT | 毕业学分 |
| created_at | DATETIME | 创建时间 |

### tb_course（课程表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(200) | 课程名 |
| code | VARCHAR(20) | 课程编号 |
| instructor | VARCHAR(100) | 教师 |
| course_time | VARCHAR(50) | 上课时间 |
| weekday | TINYINT | 周几(1-5) |
| location | VARCHAR(200) | 地点 |
| credits | INT | 学分 |
| category | VARCHAR(20) | Major/Elective/General |

### tb_card（一卡通表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| student_id | BIGINT FK | 关联学生 |
| balance | DECIMAL(10,2) | 余额 |
| card_no | VARCHAR(30) | 卡号 |

### tb_transaction（交易记录表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| card_id | BIGINT FK | 关联一卡通 |
| item | VARCHAR(200) | 事项 |
| amount | DECIMAL(10,2) | 金额 |
| type | VARCHAR(10) | expense/income |
| category | VARCHAR(20) | 分类 |
| location | VARCHAR(200) | 地点 |
| created_at | DATETIME | 交易时间 |

### tb_library_book（图书馆借阅表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| student_id | BIGINT FK | 借阅学生 |
| title | VARCHAR(300) | 书名 |
| author | VARCHAR(200) | 作者 |
| isbn | VARCHAR(20) | ISBN |
| due_date | DATE | 归还日期 |
| progress | INT | 阅读进度% |
| renewed | TINYINT | 是否续借 |

### tb_campus_moment（校园动态表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| student_id | BIGINT FK | 发布者 |
| title | VARCHAR(500) | 内容 |
| tag | VARCHAR(50) | 标签 |
| location | VARCHAR(200) | 位置 |
| likes | INT | 点赞数 |
| joined | TINYINT | 是否已报名 |
| max_attendees | INT | 最大参与人数 |
| current_attendees | INT | 当前参与人数 |

### tb_seat（自习座位表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| seat_code | VARCHAR(20) | 座位编号 |
| floor_area | VARCHAR(20) | 楼层区域 |
| status | VARCHAR(20) | available/occupied |
| student_id | BIGINT FK | 占用学生 |

### tb_chat_message（聊天记录表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| student_id | BIGINT FK | 学生 |
| role | VARCHAR(10) | user/assistant |
| content | TEXT | 消息内容 |
| created_at | DATETIME | 时间 |

---

## 10. 功能模块说明

### 10.1 课程表 (course)
- 显示周一至周五课程安排
- 点击课程查看详情弹窗（教师、时间、地点、学分）
- 支持"物理入座签到"和"生成课程大纲"快捷操作
- 数据来源：`tb_course` 表

### 10.2 一卡通 (card)
- Visa 风格虚拟校园卡展示
- 一键充值（30/50/100元）+ 自定义金额
- 最近 3 条消费记录
- 充值成功后自动刷新余额
- 数据来源：`tb_card` + `tb_transaction` 表

### 10.3 我在校园 (presence)
- 当前签到位置显示
- 校园动态 Feed（帖子列表）
- 发布新动态（内容/标签/位置）
- 活动报名（预挂号）
- 入座签到
- 数据来源：`tb_campus_moment` + `tb_seat` 表

### 10.4 数字图书馆 (library)
- 图书检索（演示模式返回预设结果）
- 在借文献管理（展示进度条、剩余天数）
- 一键续借（+7 天）
- 自习座位预约/释放
- 数据来源：`tb_library_book` + `tb_seat` 表

### 10.5 学业罗盘 (academic)
- GPA 绩点展示
- 学分攻读进度条
- 学术能力雷达图（4 个维度）
- 智能学术画像审计快捷操作
- 数据来源：`tb_student` 表

### 10.6 AI 智能助理 (chat)
- 独立右侧面板，可折叠
- 4 个快捷学术指令
- 聊天历史持久化（存 MySQL）
- 支持清空历史
- 详情见[第 11 节](#11-ai-对话系统)

---

## 11. AI 对话系统

### 11.1 工作流程

```
用户输入消息
    │
    ▼
AiChatController.chat()
    │
    ▼
AiChatServiceImpl.chat()
    │
    ├─ 1. 保存用户消息到 tb_chat_message
    ├─ 2. parseLocalActions() 解析本地操作意图
    │      ├─ 匹配"充值XX元" → CardService.topUp()
    │      ├─ 匹配"续期"/"续借" → LibraryService.renewBook()
    │      ├─ 匹配"音乐"/"报名" → PresenceService.joinEvent()
    │      └─ 匹配"签到"/"入座" → PresenceService.checkin()
    ├─ 3. 调用 AI 服务获取回复
    │      ├─ demo 模式：generateDemoResponse()
    │      ├─ gemini 模式：AiConfig.geminiClient()
    │      └─ openai 模式：AiConfig.openaiClient()
    ├─ 4. 合并本地操作反馈 + AI 回复
    └─ 5. 保存 AI 回复到 tb_chat_message
```

### 11.2 智能操作触发词

| 用户输入包含 | 自动执行操作 | 数据库影响 |
|-------------|-------------|-----------|
| "充值50元" | 一卡通余额 +50 | INSERT tb_transaction, UPDATE tb_card |
| "续借/续期" | 图书归还日期 +7 天 | UPDATE tb_library_book |
| "报名/音乐/草坪" | 活动报名 | UPDATE tb_campus_moment |
| "签到/入座" | 分配自习座位 | UPDATE tb_seat |

---

## 12. 生产部署

### 后端构建

```bash
cd campus-server
mvn clean package -DskipTests
# 产物：target/campus-server-1.0.0.jar
```

### 生产启动

```bash
# 设置环境变量
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mysql://生产数据库地址:3306/campus_db
export DB_USERNAME=生产用户名
export DB_PASSWORD=生产密码
export AI_PROVIDER=gemini
export GEMINI_API_KEY=生产环境Key

# 启动
java -jar target/campus-server-1.0.0.jar
```

### 前端构建

```bash
cd campus-web
npm run build
# 产物：dist/ 目录
# 将 dist/ 部署到 Nginx / CDN
```

### Nginx 反向代理配置示例

```nginx
server {
    listen 80;
    server_name campus.example.com;

    # 前端静态文件
    location / {
        root /var/www/campus-web/dist;
        try_files $uri /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 13. 常见问题

### Q1: 启动后端报 `Access denied for user 'root'@'localhost'`
> 修改 `application.yml` 中 `spring.datasource.username` 和 `password` 为正确的 MySQL 账号。

### Q2: 启动后端报 `Unknown database 'campus_db'`
> 未执行数据库初始化脚本。运行：`mysql -u root -p < campus-server/sql/init.sql`

### Q3: 前端页面打开但数据加载为空
> 1. 确认后端已启动（访问 http://localhost:8080/api/courses 看是否返回 JSON）
> 2. 确认 Vite 代理配置正确（vite.config.ts）
> 3. 浏览器 F12 → Network 查看请求状态

### Q4: AI 聊天不工作
> 1. 检查 `AI_PROVIDER` 环境变量是否正确设置
> 2. 默认使用 `demo` 演示模式无需 API Key
> 3. 使用 Gemini 需确保网络能访问 `generativelanguage.googleapis.com`

### Q5: 如何添加新课程/学生？
> 直接操作 MySQL：
> ```sql
> INSERT INTO tb_course (name, code, instructor, course_time, weekday, location, credits, category, color)
> VALUES ('量子计算导论', 'CS-501', '张教授', '10:00 - 11:30', 3, '量子实验室', 3, 'Major', 'bg-neutral-900');
> ```

### Q6: 端口冲突怎么办？
> - 后端端口修改：`application.yml` → `server.port: 8081`
> - 前端端口修改：`vite.config.ts` → `server.port: 3000`
> - 同时记得修改 Vite 代理 target 地址

### Q7: 想用 DeepSeek 作为 AI 后端？
> ```bash
> export AI_PROVIDER=openai
> export OPENAI_API_KEY=你的DeepSeek_API_Key
> export OPENAI_BASE_URL=https://api.deepseek.com
> export OPENAI_MODEL=deepseek-chat
> ```

### Q8: 项目结构为什么这样设计？
> 按业务功能模块化组织（而非按技术层），实现：
> - **高内聚**：每个模块的 controller/service/entity/dto 放在一起
> - **低耦合**：模块间通过 Service 接口调用
> - **见名知义**：`course/` 目录就是课程相关全部代码
> - **易于扩展**：新增功能只需新建一个模块目录
