# 游戏商城系统 - 接口测试文档

> 生成时间：2026-03-09
> 基础URL：http://localhost:8080/api
> 认证方式：JWT Token (Bearer Token)

---

## 目录

1. [项目概述](#1-项目概述)
2. [基础信息](#2-基础信息)
3. [全局响应格式](#3-全局响应格式)
4. [认证说明](#4-认证说明)
5. [接口详情](#5-接口详情)
   - [用户模块](#51-用户模块)
   - [游戏模块](#52-游戏模块)
   - [购物车模块](#53-购物车模块)
   - [订单模块](#54-订单模块)
   - [分类模块](#55-分类模块)
   - [轮播图模块](#56-轮播图模块)
   - [评论模块](#57-评论模块)
   - [仪表盘模块](#58-仪表盘模块)
   - [文件模块](#59-文件模块)
   - [图片模块](#510-图片模块)
6. [实体类字段说明](#6-实体类字段说明)
7. [测试用例建议](#7-测试用例建议)

---

## 1. 项目概述

这是一个基于Spring Boot的游戏商城后端系统，提供用户管理、游戏展示、购物车、订单管理、评论系统等核心功能。

### 技术栈
- Spring Boot 2.7.14
- MyBatis Plus 3.5.3.1
- JWT 认证 (java-jwt 4.4.0)
- MySQL 8.0
- Druid 连接池

---

## 2. 基础信息

| 项目 | 说明 |
|------|------|
| 服务地址 | `http://localhost:8080` |
| 上下文路径 | `/api` |
| 完整基础URL | `http://localhost:8080/api` |
| 内容类型 | `application/json` (文件上传除外) |
| 编码格式 | UTF-8 |

### 无需认证的接口（公开访问）

| 接口 | 说明 |
|------|------|
| POST /user/register | 用户注册 |
| POST /user/login | 用户登录 |
| GET /game/list | 游戏列表（用户端） |
| GET /game/detail/{id} | 游戏详情 |
| GET /game/top | 热门游戏 |
| GET /game/new | 新游戏 |
| GET /banner/list | 轮播图列表 |
| GET /comment/list | 评论列表 |
| GET /api/img/{filename} | 图片访问 |
| POST /api/file/upload | 文件上传 |

---

## 3. 全局响应格式

所有接口统一返回以下JSON格式：

```json
{
  "code": 200,        // 状态码：200成功，401未认证，403无权，500错误
  "message": "success",  // 响应消息
  "data": {}          // 响应数据（类型可变）
}
```

### 常见状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 401 | 未登录或Token已过期 |
| 403 | 无权操作（需要管理员权限） |
| 500 | 服务器错误/业务逻辑错误 |

---

## 4. 认证说明

### Token获取
登录成功后，在响应data中返回token：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": { ... }
  }
}
```

### Token使用
在请求Header中添加：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

### Token有效期
默认24小时（86400000毫秒）

---

## 5. 接口详情

### 5.1 用户模块

#### 5.1.1 用户注册
- **接口**：`POST /user/register`
- **认证**：无需
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（唯一） |
| password | String | 是 | 密码 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| avatar | String | 否 | 头像URL |
| bio | String | 否 | 个人简介 |

**请求示例**：
```json
{
  "username": "testuser",
  "password": "123456",
  "phone": "13800138000",
  "email": "test@example.com"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "phone": "13800138000",
    "email": "test@example.com",
    "role": 0,
    "status": 0
  }
}
```

---

#### 5.1.2 用户登录
- **接口**：`POST /user/login`
- **认证**：无需
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**请求示例**：
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "username": "testuser",
      "avatar": "",
      "role": 0,
      "status": 0
    }
  }
}
```

---

#### 5.1.3 获取当前用户信息
- **接口**：`GET /user/info`
- **认证**：需要

**请求参数**：无

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "phone": "13800138000",
    "email": "test@example.com",
    "avatar": "",
    "bio": "",
    "role": 0,
    "status": 0,
    "createTime": "2024-01-01 12:00:00"
  }
}
```

---

#### 5.1.4 更新用户信息
- **接口**：`PUT /user/update`
- **认证**：需要
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| avatar | String | 否 | 头像URL |
| bio | String | 否 | 个人简介 |

**请求示例**：
```json
{
  "phone": "13900139000",
  "email": "newemail@example.com",
  "bio": "这是一个测试用户"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": { ... }
}
```

---

#### 5.1.5 修改密码
- **接口**：`PUT /user/password`
- **认证**：需要
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |

**请求示例**：
```json
{
  "oldPassword": "123456",
  "newPassword": "newpassword123"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

#### 5.1.6 获取用户列表（管理员）
- **接口**：`GET /user/list`
- **认证**：需要（管理员权限 role=1）

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

---

#### 5.1.7 冻结用户（管理员）
- **接口**：`POST /user/freeze/{id}`
- **认证**：需要（管理员权限）

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |

**响应示例**：
```json
{
  "code": 200,
  "message": "冻结成功",
  "data": null
}
```

---

#### 5.1.8 解冻用户（管理员）
- **接口**：`POST /user/unfreeze/{id}`
- **认证**：需要（管理员权限）

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |

**响应示例**：
```json
{
  "code": 200,
  "message": "解冻成功",
  "data": null
}
```

---

### 5.2 游戏模块

#### 5.2.1 获取游戏列表（用户端）
- **接口**：`GET /game/list`
- **认证**：无需

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |
| keyword | String | 否 | - | 搜索关键词（游戏名称） |
| categoryId | Long | 否 | - | 分类ID |
| sortBy | String | 否 | - | 排序方式：price_desc/price_asc/sales/rating/new |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "赛博朋克2077",
        "cover": "/api/img/xxx.jpg",
        "price": 298.00,
        "originalPrice": 398.00,
        "categoryId": 1,
        "categoryName": "角色扮演",
        "rating": 4.5,
        "sales": 1000,
        "status": 1
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1
  }
}
```

---

#### 5.2.2 获取游戏列表（管理端）
- **接口**：`GET /game/admin/list`
- **认证**：需要（管理员权限）

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |
| keyword | String | 否 | - | 搜索关键词 |

**响应示例**：同用户端列表，包含status=0（下架）的游戏

---

#### 5.2.3 获取游戏详情
- **接口**：`GET /game/detail/{id}`
- **认证**：无需

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 游戏ID |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "赛博朋克2077",
    "cover": "/api/img/cover.jpg",
    "description": "游戏描述...",
    "requirements": "配置要求...",
    "screenshots": "/api/img/ss1.jpg,/api/img/ss2.jpg",
    "price": 298.00,
    "originalPrice": 398.00,
    "categoryId": 1,
    "categoryName": "角色扮演",
    "stock": 100,
    "sales": 1000,
    "status": 1,
    "rating": 4.5,
    "createTime": "2024-01-01 12:00:00"
  }
}
```

---

#### 5.2.4 获取热门游戏
- **接口**：`GET /game/top`
- **认证**：无需

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 6 | 返回数量 |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": 1, "name": "游戏1", "cover": "...", "price": 100.00 },
    { "id": 2, "name": "游戏2", "cover": "...", "price": 200.00 }
  ]
}
```

---

#### 5.2.5 获取新游戏
- **接口**：`GET /game/new`
- **认证**：无需

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 6 | 返回数量 |

**响应示例**：同热门游戏

---

#### 5.2.6 添加游戏（管理员）
- **接口**：`POST /game/add`
- **认证**：需要（管理员权限）
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 游戏名称 |
| cover | String | 是 | 封面图URL |
| description | String | 否 | 游戏描述 |
| requirements | String | 否 | 配置要求 |
| screenshots | String | 否 | 截图URL，逗号分隔 |
| price | BigDecimal | 是 | 售价 |
| originalPrice | BigDecimal | 否 | 原价 |
| categoryId | Long | 是 | 分类ID |
| stock | Integer | 是 | 库存 |
| status | Integer | 否 | 0-下架 1-上架，默认1 |

**请求示例**：
```json
{
  "name": "新游戏",
  "cover": "/api/img/cover.jpg",
  "description": "这是游戏描述",
  "price": 199.00,
  "originalPrice": 299.00,
  "categoryId": 1,
  "stock": 100,
  "status": 1
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

---

#### 5.2.7 更新游戏（管理员）
- **接口**：`PUT /game/update`
- **认证**：需要（管理员权限）
- **Content-Type**：`application/json`

**请求参数**：同添加游戏，需包含id字段

**请求示例**：
```json
{
  "id": 1,
  "name": "更新的游戏名",
  "price": 150.00
}
```

---

#### 5.2.8 删除游戏（管理员）
- **接口**：`DELETE /game/delete/{id}`
- **认证**：需要（管理员权限）

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 游戏ID |

---

### 5.3 购物车模块

#### 5.3.1 获取购物车列表
- **接口**：`GET /cart/list`
- **认证**：需要

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "gameId": 1,
      "gameName": "赛博朋克2077",
      "gameCover": "/api/img/cover.jpg",
      "quantity": 2,
      "price": 298.00,
      "totalPrice": 596.00
    }
  ]
}
```

---

#### 5.3.2 添加商品到购物车
- **接口**：`POST /cart/add`
- **认证**：需要
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| gameId | Long | 是 | - | 游戏ID |
| quantity | Integer | 否 | 1 | 数量 |

**请求示例**：
```json
{
  "gameId": 1,
  "quantity": 2
}
```

---

#### 5.3.3 更新购物车商品数量
- **接口**：`PUT /cart/update/{id}`
- **认证**：需要
- **Content-Type**：`application/json`

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 购物车记录ID |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| quantity | Integer | 是 | 新数量 |

**请求示例**：
```json
{
  "quantity": 3
}
```

---

#### 5.3.4 删除购物车商品
- **接口**：`DELETE /cart/delete/{id}`
- **认证**：需要

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 购物车记录ID |

---

#### 5.3.5 清空购物车
- **接口**：`DELETE /cart/clear`
- **认证**：需要

---

### 5.4 订单模块

#### 5.4.1 获取订单列表
- **接口**：`GET /order/list`
- **认证**：需要

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |
| status | Integer | 否 | - | 状态筛选：0-未支付 1-已支付 2-已取消 |

**说明**：管理员(role=1)可查看所有订单，普通用户只能查看自己的订单

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "20240309123456",
        "userId": 1,
        "username": "testuser",
        "totalAmount": 596.00,
        "status": 0,
        "remark": "备注",
        "createTime": "2024-03-09 12:34:56",
        "items": [
          {
            "id": 1,
            "gameId": 1,
            "gameName": "赛博朋克2077",
            "gameCover": "/api/img/cover.jpg",
            "quantity": 2,
            "price": 298.00,
            "totalPrice": 596.00
          }
        ]
      }
    ]
  }
}
```

---

#### 5.4.2 获取订单详情
- **接口**：`GET /order/detail/{id}`
- **认证**：需要

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单ID |

---

#### 5.4.3 创建订单
- **接口**：`POST /order/create`
- **认证**：需要
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| remark | String | 否 | "" | 订单备注 |

**请求示例**：
```json
{
  "remark": "请尽快发货"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "创建订单成功",
  "data": {
    "id": 1,
    "orderNo": "20240309123456",
    "totalAmount": 596.00,
    "status": 0,
    "createTime": "2024-03-09 12:34:56"
  }
}
```

---

#### 5.4.4 支付订单
- **接口**：`POST /order/pay`
- **认证**：需要
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | String | 是 | 订单号 |

**请求示例**：
```json
{
  "orderNo": "20240309123456"
}
```

---

#### 5.4.5 取消订单
- **接口**：`POST /order/cancel/{id}`
- **认证**：需要

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单ID |

---

### 5.5 分类模块

#### 5.5.1 获取分类列表
- **接口**：`GET /category/list`
- **认证**：无需

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "角色扮演",
      "description": "RPG游戏",
      "sort": 1
    },
    {
      "id": 2,
      "name": "动作冒险",
      "description": "ACT游戏",
      "sort": 2
    }
  ]
}
```

---

#### 5.5.2 添加分类（管理员）
- **接口**：`POST /category/add`
- **认证**：需要（管理员权限）
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 分类名称 |
| description | String | 否 | 分类描述 |
| sort | Integer | 否 | 排序号 |

---

#### 5.5.3 更新分类（管理员）
- **接口**：`PUT /category/update`
- **认证**：需要（管理员权限）
- **Content-Type**：`application/json`

---

#### 5.5.4 删除分类（管理员）
- **接口**：`DELETE /category/delete/{id}`
- **认证**：需要（管理员权限）

---

### 5.6 轮播图模块

#### 5.6.1 获取轮播图列表（用户端）
- **接口**：`GET /banner/list`
- **认证**：无需

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "热门推荐",
      "image": "/api/img/banner1.jpg",
      "link": "/game/detail/1",
      "sort": 1,
      "status": 0
    }
  ]
}
```

---

#### 5.6.2 获取所有轮播图（管理员）
- **接口**：`GET /banner/all`
- **认证**：需要（管理员权限）

---

#### 5.6.3 添加轮播图（管理员）
- **接口**：`POST /banner/add`
- **认证**：需要（管理员权限）
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 标题 |
| image | String | 是 | 图片URL |
| link | String | 否 | 跳转链接 |
| sort | Integer | 否 | 排序号 |
| status | Integer | 否 | 0-显示 1-隐藏 |

---

#### 5.6.4 更新轮播图（管理员）
- **接口**：`PUT /banner/update`
- **认证**：需要（管理员权限）
- **Content-Type**：`application/json`

---

#### 5.6.5 删除轮播图（管理员）
- **接口**：`DELETE /banner/delete/{id}`
- **认证**：需要（管理员权限）

---

### 5.7 评论模块

#### 5.7.1 获取评论列表
- **接口**：`GET /comment/list`
- **认证**：无需

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |
| gameId | Long | 否 | - | 游戏ID（筛选） |
| userId | Long | 否 | - | 用户ID（筛选） |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "username": "testuser",
        "avatar": "/api/img/avatar.jpg",
        "gameId": 1,
        "gameName": "赛博朋克2077",
        "content": "很好玩的游戏！",
        "rating": 4.5,
        "createTime": "2024-03-09 12:00:00"
      }
    ]
  }
}
```

---

#### 5.7.2 获取当前用户对某游戏的评论
- **接口**：`GET /comment/user-comment`
- **认证**：需要

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| gameId | Long | 是 | 游戏ID |

---

#### 5.7.3 添加评论
- **接口**：`POST /comment/add`
- **认证**：需要
- **Content-Type**：`application/json`

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| gameId | Long | 是 | - | 游戏ID |
| content | String | 是 | - | 评论内容 |
| rating | Double | 否 | 5.0 | 评分（0.5-5.0，支持半星） |

**请求示例**：
```json
{
  "gameId": 1,
  "content": "这个游戏太棒了！",
  "rating": 4.5
}
```

---

#### 5.7.4 更新评论
- **接口**：`PUT /comment/update/{id}`
- **认证**：需要
- **Content-Type**：`application/json`

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 评论ID |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | String | 是 | 评论内容 |
| rating | Double | 否 | 评分 |

---

#### 5.7.5 删除评论
- **接口**：`DELETE /comment/delete/{id}`
- **认证**：需要

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 评论ID |

---

#### 5.7.6 管理员删除评论
- **接口**：`DELETE /comment/admin/delete/{id}`
- **认证**：需要（管理员权限）

---

### 5.8 仪表盘模块

#### 5.8.1 获取统计数据（管理员）
- **接口**：`GET /dashboard/stats`
- **认证**：需要（管理员权限）

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userCount": 100,
    "gameCount": 50,
    "orderCount": 200,
    "todayOrderCount": 10,
    "totalSales": 50000.00,
    "todaySales": 2000.00
  }
}
```

---

### 5.9 文件模块

#### 5.9.1 文件上传
- **接口**：`POST /file/upload`
- **认证**：无需（但通常需要登录后使用）
- **Content-Type**：`multipart/form-data`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 要上传的文件 |

**请求示例**（Form Data）：
```
file: [二进制文件数据]
```

**响应示例**：
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "/api/img/550e8400-e29b-41d4-a716-446655440000.jpg",
    "filename": "550e8400-e29b-41d4-a716-446655440000.jpg"
  }
}
```

**注意**：上传路径配置在 `E:/game-store/upload/`

---

### 5.10 图片模块

#### 5.10.1 获取图片
- **接口**：`GET /img/{filename}`
- **认证**：无需
- **响应**：图片二进制数据

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| filename | String | 文件名（含扩展名） |

**支持的格式**：png, jpg, jpeg, gif

**访问示例**：
```
GET http://localhost:8080/api/img/550e8400-e29b-41d4-a716-446655440000.jpg
```

---

## 6. 实体类字段说明

### 6.1 User（用户）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID（主键） |
| username | String | 用户名（唯一） |
| password | String | 密码（加密存储） |
| phone | String | 手机号 |
| email | String | 邮箱 |
| avatar | String | 头像URL |
| bio | String | 个人简介 |
| status | Integer | 0-正常 1-冻结 |
| role | Integer | 0-普通用户 1-管理员 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| deleted | Integer | 逻辑删除标志 |

### 6.2 Game（游戏）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 游戏ID（主键） |
| name | String | 游戏名称 |
| cover | String | 封面图URL |
| description | String | 游戏描述 |
| requirements | String | 配置要求 |
| screenshots | String | 截图URL，逗号分隔 |
| price | BigDecimal | 售价 |
| originalPrice | BigDecimal | 原价 |
| categoryId | Long | 分类ID |
| stock | Integer | 库存 |
| sales | Integer | 销量 |
| status | Integer | 1-上架 0-下架 |
| rating | Double | 平均评分 0-5 |
| categoryName | String | 分类名称（非数据库字段） |

### 6.3 Cart（购物车）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 购物车ID（主键） |
| userId | Long | 用户ID |
| gameId | Long | 游戏ID |
| quantity | Integer | 数量 |
| price | BigDecimal | 单价 |
| totalPrice | BigDecimal | 总价 |
| gameName | String | 游戏名称（非数据库字段） |
| gameCover | String | 游戏封面（非数据库字段） |

### 6.4 Order（订单）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单ID（主键） |
| orderNo | String | 订单号（唯一） |
| userId | Long | 用户ID |
| totalAmount | BigDecimal | 订单总金额 |
| status | Integer | 0-未支付 1-已支付 2-已取消 |
| remark | String | 订单备注 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| payTime | LocalDateTime | 支付时间 |
| username | String | 用户名（非数据库字段） |
| items | List<OrderItem> | 订单项列表（非数据库字段） |

### 6.5 OrderItem（订单项）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单项ID（主键） |
| orderId | Long | 订单ID |
| gameId | Long | 游戏ID |
| gameName | String | 游戏名称 |
| gameCover | String | 游戏封面 |
| quantity | Integer | 数量 |
| price | BigDecimal | 单价 |
| totalPrice | BigDecimal | 总价 |

### 6.6 Category（分类）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 分类ID（主键） |
| name | String | 分类名称 |
| description | String | 分类描述 |
| sort | Integer | 排序号 |

### 6.7 Banner（轮播图）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 轮播图ID（主键） |
| title | String | 标题 |
| image | String | 图片URL |
| link | String | 跳转链接 |
| sort | Integer | 排序号 |
| status | Integer | 0-显示 1-隐藏 |

### 6.8 Comment（评论）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 评论ID（主键） |
| userId | Long | 用户ID |
| gameId | Long | 游戏ID |
| content | String | 评论内容 |
| rating | Double | 评分 0.5-5 |
| username | String | 用户名（非数据库字段） |
| avatar | String | 用户头像（非数据库字段） |
| gameName | String | 游戏名称（非数据库字段） |

---

## 7. 测试用例建议

### 7.1 用户模块测试

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 正常注册 | POST /user/register 正确参数 | code=200, 返回用户信息 |
| 重复注册 | 同一用户名注册两次 | 第二次返回错误信息 |
| 正常登录 | POST /user/login 正确凭据 | code=200, 返回token |
| 错误密码登录 | 使用错误密码 | code=500, 提示密码错误 |
| Token验证 | 使用正确Token访问/user/info | code=200 |
| 无效Token | 使用错误Token | code=401 |
| 权限测试 | 普通用户访问/user/list | code=403 |
| 自冻结测试 | 管理员冻结自己账号 | code=500, 提示不能冻结自己 |

### 7.2 游戏模块测试

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 列表分页 | GET /game/list page=1 size=5 | 返回5条数据 |
| 搜索功能 | GET /game/list keyword="赛博" | 返回匹配游戏 |
| 分类筛选 | GET /game/list categoryId=1 | 返回该分类游戏 |
| 排序测试 | GET /game/list sortBy=price_desc | 价格降序排列 |
| 管理员列表 | GET /game/admin/list | 包含status=0的游戏 |

### 7.3 购物车模块测试

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 添加商品 | POST /cart/add gameId=1 quantity=2 | 购物车增加商品 |
| 重复添加 | 再次添加同一游戏 | 数量累加 |
| 更新数量 | PUT /cart/update/{id} quantity=5 | 数量更新为5 |
| 删除商品 | DELETE /cart/delete/{id} | 商品从购物车移除 |
| 清空购物车 | DELETE /cart/clear | 购物车为空 |
| 权限测试 | 修改他人购物车项 | code=403 |

### 7.4 订单模块测试

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 创建订单 | POST /order/create | 从购物车生成订单 |
| 支付订单 | POST /order/pay orderNo=xxx | status变为1 |
| 重复支付 | 再次支付已支付订单 | 返回错误 |
| 取消订单 | POST /order/cancel/{id} | status变为2，库存回滚 |
| 订单权限 | 查看他人订单详情 | code=403 |

### 7.5 评论模块测试

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 添加评论 | POST /comment/add | 评论成功，游戏评分更新 |
| 评分边界 | rating=6 | 被限制为5.0 |
| 未购买评论 | 未购买游戏添加评论 | 返回错误 |
| 重复评论 | 再次评论同一游戏 | 更新原评论 |

---

## 附录

### Postman测试环境变量建议

```json
{
  "baseUrl": "http://localhost:8080/api",
  "token": "{{登录后获取}}",
  "adminToken": "{{管理员登录后获取}}"
}
```

### 常用请求头

```
Content-Type: application/json
Authorization: Bearer {{token}}
```

---

**文档版本**：1.0  
**最后更新**：2026-03-09
