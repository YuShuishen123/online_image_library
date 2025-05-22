# 🖼️ Online Image Library 在线图片管理系统

一个基于 **Spring Boot + Vue.js** 构建的前后端分离在线图片管理系统，支持用户注册登录、图片上传浏览删除、图片分类管理等功能，适用于学习实践或小型图像资源平台建设。


---

## 🚀 项目亮点

- 🌐 前后端分离架构，技术栈现代化
- 🖼️ 支持图片上传、分类管理、分页浏览、模糊搜索
- 🔒 登录注册、权限校验、安全性基本保障
- ☁️ 支持本地或对象存储（可拓展）
- 📦 多级缓存策略优化查询效率（基于图片 MD5）
- 📷 图片自动审核（模拟/拓展接口）


---

## 🏗️ 技术选型

### 后端

| 技术       | 说明                         |
|------------|------------------------------|
| Spring Boot 2.7.6 | 容器+MVC框架，简化开发配置 |
| Spring MVC | RESTful 接口开发             |
| MyBatis-Plus    | ORM 框架，数据库操作简洁清晰 |
| Redis      | 用作缓存，提高访问性能       |
| Lombok     | 简化 POJO 类的开发            |
| Swagger    | 接口文档自动生成              |
| 腾讯云COS  |图片存储服务|

### 前端

| 技术       | 说明                             |
|------------|----------------------------------|
| Vue.js     | 渐进式前端框架，响应式编程       |
| Vue Router | 单页应用路由控制                 |
| Axios      | 异步通信库，用于前后端通信       |
| Element UI | 基于 Vue 的 UI 框架，快速美化页面 |

### 数据库

| 技术    | 说明           |
|---------|----------------|
| MySQL   | 关系型数据库   |
| Redis   | 键值对缓存系统 |

---

## 📁 项目结构
```bash
online_image_library/
├── online_image_library_backend/      # 后端：Spring Boot 服务
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── controller/        # 控制器层（接口定义）
│   │   │   │   ├── service/           # 服务层（业务逻辑）
│   │   │   │   ├── mapper/            # MyBatis 映射接口
│   │   │   │   ├── entity/            # 实体类（数据库映射）
│   │   │   │   └── config/            # 配置类（CORS、拦截器等）
│   │   │   └── resources/
│   │   │       └── application.yml    # 应用配置文件
│   └── pom.xml                        # Maven 构建配置
│
├── online_image_library_vue/          # 前端：Vue 项目
│   ├── public/                        # 公共资源（index.html等）
│   └── src/
│       ├── views/                     # 页面视图组件
│       ├── components/                # 通用 Vue 组件
│       ├── router/                    # 路由配置（vue-router）
│       └── api/                       # 封装的前后端接口调用
│
├── sql/
│   └── init.sql                       # 数据库初始化脚本
└── README.md                          # 项目说明文档
```

---

## ⚙️ 环境准备

### 本地环境依赖

- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8+
- Redis 6+
- Vue CLI（建议全局安装）: `npm install -g @vue/cli`

---

## 🛠️ 项目部署

### 后端部署

```bash
cd online_image_library_backend
# 配置 application.yml，设置数据库/Redis 连接
mvn clean install
mvn spring-boot:run
```
📘 API 文档
访问地址（启动后）: http://localhost:6789/api/doc.html

---

### 前端部署

```bash
cd online_image_library_vue
npm install
npm run serve
```
默认访问前端地址: http://localhost:5173

默认后端接口地址: http://localhost:6789

### 📸 缓存与优化策略
图片查询采用 MD5 签名作为缓存 Key

缓存结果自动过期与多级缓存设计（如本地 + Redis）

查询结果强制限制分页数，避免大规模内存占用

这是一个典型的前后端分离架构的图片管理系统，具备完整的用户权限管理、图片CRUD操作、审核流程和性能优化机制。项目采用现代化的技术栈，支持云存储和多级缓存，适合作为企业级图片管理平台使用。

---

### 🧑‍💻 作者
GitHub: YuShuishen123

