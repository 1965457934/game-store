# 游戏商城系统 (Game Store)

基于 SpringBoot + Vue 的游戏商城系统，毕业设计项目。

## 技术栈

### 后端
- **框架**: SpringBoot 2.7.x
- **ORM**: MyBatis-Plus 3.5.x
- **数据库**: MySQL 8.0
- **连接池**: Druid
- **安全**: JWT Token
- **构建工具**: Maven

### 前端
- **框架**: Vue 3 + Vue Router 4 + Vuex 4
- **UI组件**: Element Plus
- **HTTP**: Axios
- **构建工具**: Vue CLI

## 项目结构

```
game-store/
├── game-store-backend/     # SpringBoot后端
│   ├── src/main/java/com/gamestore/
│   │   ├── entity/        # 实体类
│   │   ├── mapper/        # 数据访问层
│   │   ├── service/       # 业务逻辑层
│   │   ├── controller/    # 控制器层
│   │   ├── config/        # 配置类
│   │   ├── utils/         # 工具类
│   │   └── interceptor/   # 拦截器
│   └── src/main/resources/
│       ├── mapper/        # XML映射文件
│       └── application.yml
├── game-store-frontend/    # Vue前端
│   ├── src/
│   │   ├── api/           # API接口
│   │   ├── views/         # 页面组件
│   │   ├── router/        # 路由配置
│   │   ├── store/         # 状态管理
│   │   └── utils/         # 工具函数
│   └── package.json
└── sql/                    # 数据库脚本
    └── game_store.sql
```

## 功能模块

### 前台用户端
- ✅ 用户注册/登录
- ✅ 游戏浏览/搜索
- ✅ 游戏详情查看
- ✅ 购物车管理
- ✅ 订单管理
- ✅ 评论互动

### 后台管理端
- ✅ 用户管理
- ✅ 游戏商品管理
- ✅ 游戏分类管理
- ✅ 订单管理
- ✅ 评论管理
- ✅ 轮播图管理

## 快速开始

### 1. 数据库准备

```bash
# 登录MySQL
mysql -u root -p

# 执行SQL脚本
source E:/game-store/sql/game_store.sql
```

### 2. 启动后端

```bash
cd game-store-backend
mvn spring-boot:run
```

后端服务将运行在: http://localhost:8080/api

### 3. 启动前端

```bash
cd game-store-frontend
npm install
npm run serve
```

前端服务将运行在: http://localhost:8081

### 4. 访问系统

- 前台首页: http://localhost:8081
- 后台管理: http://localhost:8081/admin
- 管理员账号: admin / 123456
- 普通用户: test / 123456

## API 接口

所有接口统一前缀: `/api`

| 接口 | 说明 |
|------|------|
| POST /user/register | 用户注册 |
| POST /user/login | 用户登录 |
| GET /user/info | 获取用户信息 |
| GET /game/list | 游戏列表 |
| GET /game/detail/{id} | 游戏详情 |
| GET /cart/list | 购物车列表 |
| POST /cart/add | 加入购物车 |
| POST /order/create | 创建订单 |
| GET /order/list | 订单列表 |

## 开发计划

- [x] 基础架构搭建
- [x] 用户模块
- [x] 游戏模块
- [x] 购物车模块
- [x] 订单模块
- [x] 评论模块
- [x] 后台管理
- [ ] 支付集成
- [ ] 邮件通知

## 作者

- 王凯迪
- 无锡学院 - 计算机科学与技术专业
