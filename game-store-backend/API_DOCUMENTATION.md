# Game Store Backend - 接口文档

## 项目概述

| 项目信息 | 内容 |
|---------|------|
| 项目名称 | Game Store Backend（游戏商城系统后端） |
| 技术栈 | SpringBoot 2.7.14 + MyBatis Plus + MySQL + JWT |
| 基础路径 | `/api` |
| 服务端口 | `8080` |
| 接口规范 | RESTful API |

---

## 目录

1. [通用说明](#通用说明)
2. [用户模块](#用户模块)
3. [游戏模块](#游戏模块)
4. [分类模块](#分类模块)
5. [购物车模块](#购物车模块)
6. [订单模块](#订单模块)
7. [轮播图模块](#轮播图模块)
8. [评论模块](#评论模块)
9. [仪表盘模块](#仪表盘模块)
10. [文件模块](#文件模块)
11. [数据模型](#数据模型)

---

## 通用说明

### 响应格式

所有接口统一返回 `Result<T>` 格式：

```json
{
  "code": 200,      // 状态码：200成功，500错误，401未登录，403无权限
  "message": "success",  // 提示信息
  "data": {}        // 返回数据
}
```

### 认证方式

- 使用 **JWT Token** 进行身份认证
- 登录后获取 Token，后续请求在 Header 中携带：
  ```
  Authorization: Bearer {token}
  ```
- Token 有效期：24小时

### 权限说明

| 角色值 | 说明 |
|-------|------|
| 0 | 普通用户 |
| 1 | 管理员 |

### 免登录接口

以下接口无需携带 Token：
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/game/list` - 游戏列表
- `GET /api/game/detail/{id}` - 游戏详情
- `GET /api/banner/list` - 轮播图列表
- `GET /api/comment/list` - 评论列表
- `GET /api/img/{filename}` - 图片访问
- `POST /api/file/upload` - 文件上传

---

## 用户模块

**基础路径：** `/api/user`

### 1. 用户注册

- **接口：** `POST /user/register`
- **描述：** 新用户注册
- **请求体：**
  ```json
  {
    "username": "string",   // 用户名（必填）
    "password": "string",   // 密码（必填）
    "phone": "string",      // 手机号（可选）
    "email": "string"       // 邮箱（可选）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": {
      "id": 1,
      "username": "test",
      "phone": "13800138000",
      "email": "test@example.com",
      "role": 0,
      "status": 0,
      "createTime": "2024-01-01 12:00:00"
    }
  }
  ```

### 2. 用户登录

- **接口：** `POST /user/login`
- **描述：** 用户登录获取 Token
- **请求体：**
  ```json
  {
    "username": "string",   // 用户名（必填）
    "password": "string"    // 密码（必填）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "eyJhbGc...",
      "user": {
        "id": 1,
        "username": "test",
        "avatar": "...",
        "role": 0
      }
    }
  }
  ```

### 3. 获取用户信息

- **接口：** `GET /user/info`
- **描述：** 获取当前登录用户详细信息
- **认证：** 需要
- **响应：**
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "username": "test",
      "phone": "13800138000",
      "email": "test@example.com",
      "avatar": "/api/img/xxx.jpg",
      "bio": "个人简介",
      "role": 0,
      "status": 0,
      "createTime": "2024-01-01 12:00:00"
    }
  }
  ```

### 4. 更新用户信息

- **接口：** `PUT /user/update`
- **描述：** 更新当前用户信息
- **认证：** 需要
- **请求体：**
  ```json
  {
    "phone": "string",      // 手机号（可选）
    "email": "string",      // 邮箱（可选）
    "avatar": "string",     // 头像URL（可选）
    "bio": "string"         // 个人简介（可选）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": { /* 更新后的用户信息 */ }
  }
  ```

### 5. 修改密码

- **接口：** `PUT /user/password`
- **描述：** 修改当前用户密码
- **认证：** 需要
- **请求体：**
  ```json
  {
    "oldPassword": "string",  // 旧密码（必填）
    "newPassword": "string"   // 新密码（必填）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "密码修改成功",
    "data": null
  }
  ```

### 6. 用户列表（管理员）

- **接口：** `GET /user/list`
- **描述：** 获取用户列表（分页）
- **认证：** 需要
- **权限：** 管理员
- **请求参数：**
  | 参数 | 类型 | 必填 | 说明 |
  |-----|------|-----|------|
  | page | int | 否 | 页码，默认1 |
  | size | int | 否 | 每页条数，默认10 |
- **响应：** 分页用户列表

### 7. 冻结用户（管理员）

- **接口：** `POST /user/freeze/{id}`
- **描述：** 冻结指定用户账号
- **认证：** 需要
- **权限：** 管理员
- **路径参数：** `id` - 用户ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "冻结成功",
    "data": null
  }
  ```

### 8. 解冻用户（管理员）

- **接口：** `POST /user/unfreeze/{id}`
- **描述：** 解冻指定用户账号
- **认证：** 需要
- **权限：** 管理员
- **路径参数：** `id` - 用户ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "解冻成功",
    "data": null
  }
  ```

---

## 游戏模块

**基础路径：** `/api/game`

### 1. 游戏列表（用户端）

- **接口：** `GET /game/list`
- **描述：** 获取上架的游戏列表（分页）
- **请求参数：**
  | 参数 | 类型 | 必填 | 说明 |
  |-----|------|-----|------|
  | page | int | 否 | 页码，默认1 |
  | size | int | 否 | 每页条数，默认10 |
  | keyword | string | 否 | 搜索关键词 |
  | categoryId | long | 否 | 分类ID筛选 |
  | sortBy | string | 否 | 排序方式 |
- **响应：** 分页游戏列表

### 2. 游戏列表（管理端）

- **接口：** `GET /game/admin/list`
- **描述：** 获取所有游戏列表（包括下架的）
- **认证：** 需要
- **权限：** 管理员
- **请求参数：**
  | 参数 | 类型 | 必填 | 说明 |
  |-----|------|-----|------|
  | page | int | 否 | 页码，默认1 |
  | size | int | 否 | 每页条数，默认10 |
  | keyword | string | 否 | 搜索关键词 |
- **响应：** 分页游戏列表

### 3. 游戏详情

- **接口：** `GET /game/detail/{id}`
- **描述：** 获取游戏详细信息
- **路径参数：** `id` - 游戏ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "name": "赛博朋克2077",
      "cover": "/api/img/xxx.jpg",
      "description": "游戏描述...",
      "requirements": "配置要求...",
      "screenshots": "[...]",
      "price": 298.00,
      "originalPrice": 398.00,
      "categoryId": 1,
      "categoryName": "角色扮演",
      "stock": 100,
      "sales": 500,
      "status": 0,
      "rating": 4.5,
      "createTime": "2024-01-01 12:00:00"
    }
  }
  ```

### 4. 热门游戏

- **接口：** `GET /game/top`
- **描述：** 获取热门游戏（按销量排序）
- **请求参数：**
  | 参数 | 类型 | 必填 | 说明 |
  |-----|------|-----|------|
  | limit | int | 否 | 数量，默认6 |
- **响应：** 游戏列表数组

### 5. 新游推荐

- **接口：** `GET /game/new`
- **描述：** 获取最新上架游戏
- **请求参数：**
  | 参数 | 类型 | 必填 | 说明 |
  |-----|------|-----|------|
  | limit | int | 否 | 数量，默认6 |
- **响应：** 游戏列表数组

### 6. 添加游戏（管理员）

- **接口：** `POST /game/add`
- **描述：** 添加新游戏
- **认证：** 需要
- **权限：** 管理员
- **请求体：**
  ```json
  {
    "name": "string",           // 游戏名称（必填）
    "cover": "string",          // 封面图URL（必填）
    "description": "string",    // 游戏描述（可选）
    "requirements": "string",   // 配置要求（可选）
    "screenshots": "string",    // 截图JSON数组（可选）
    "price": 299.00,            // 现价（必填）
    "originalPrice": 399.00,    // 原价（可选）
    "categoryId": 1,            // 分类ID（必填）
    "stock": 100,               // 库存（必填）
    "status": 0                 // 状态：0-上架，1-下架（默认0）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "添加成功",
    "data": null
  }
  ```

### 7. 更新游戏（管理员）

- **接口：** `PUT /game/update`
- **描述：** 更新游戏信息
- **认证：** 需要
- **权限：** 管理员
- **请求体：** Game 对象（需包含 id）
- **响应：**
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 8. 删除游戏（管理员）

- **接口：** `DELETE /game/delete/{id}`
- **描述：** 删除游戏（逻辑删除）
- **认证：** 需要
- **权限：** 管理员
- **路径参数：** `id` - 游戏ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

---

## 分类模块

**基础路径：** `/api/category`

### 1. 分类列表

- **接口：** `GET /category/list`
- **描述：** 获取所有分类
- **响应：**
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "name": "角色扮演",
        "description": "RPG游戏",
        "sort": 1,
        "createTime": "2024-01-01 12:00:00"
      }
    ]
  }
  ```

### 2. 添加分类（管理员）

- **接口：** `POST /category/add`
- **描述：** 添加新分类
- **认证：** 需要
- **权限：** 管理员
- **请求体：**
  ```json
  {
    "name": "string",        // 分类名称（必填）
    "description": "string", // 分类描述（可选）
    "sort": 1                // 排序值（可选）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "添加成功",
    "data": null
  }
  ```

### 3. 更新分类（管理员）

- **接口：** `PUT /category/update`
- **描述：** 更新分类信息
- **认证：** 需要
- **权限：** 管理员
- **请求体：** Category 对象（需包含 id）
- **响应：**
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 4. 删除分类（管理员）

- **接口：** `DELETE /category/delete/{id}`
- **描述：** 删除分类
- **认证：** 需要
- **权限：** 管理员
- **路径参数：** `id` - 分类ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

---

## 购物车模块

**基础路径：** `/api/cart`

### 1. 购物车列表

- **接口：** `GET /cart/list`
- **描述：** 获取当前用户购物车列表
- **认证：** 需要
- **响应：**
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
        "gameCover": "/api/img/xxx.jpg",
        "quantity": 2,
        "price": 298.00,
        "totalPrice": 596.00,
        "createTime": "2024-01-01 12:00:00"
      }
    ]
  }
  ```

### 2. 添加购物车

- **接口：** `POST /cart/add`
- **描述：** 添加商品到购物车
- **认证：** 需要
- **请求体：**
  ```json
  {
    "gameId": 1,      // 游戏ID（必填）
    "quantity": 1     // 数量（可选，默认1）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "加入购物车成功",
    "data": null
  }
  ```

### 3. 更新购物车

- **接口：** `PUT /cart/update/{id}`
- **描述：** 更新购物车商品数量
- **认证：** 需要
- **路径参数：** `id` - 购物车ID
- **请求体：**
  ```json
  {
    "quantity": 3    // 数量（必填）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 4. 删除购物车项

- **接口：** `DELETE /cart/delete/{id}`
- **描述：** 删除购物车中的商品
- **认证：** 需要
- **路径参数：** `id` - 购物车ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

### 5. 清空购物车

- **接口：** `DELETE /cart/clear`
- **描述：** 清空当前用户购物车
- **认证：** 需要
- **响应：**
  ```json
  {
    "code": 200,
    "message": "清空成功",
    "data": null
  }
  ```

---

## 订单模块

**基础路径：** `/api/order`

### 1. 订单列表

- **接口：** `GET /order/list`
- **描述：** 获取订单列表（用户看自己的，管理员看全部）
- **认证：** 需要
- **请求参数：**
  | 参数 | 类型 | 必填 | 说明 |
  |-----|------|-----|------|
  | page | int | 否 | 页码，默认1 |
  | size | int | 否 | 每页条数，默认10 |
  | status | int | 否 | 状态筛选：0-未支付，1-已支付，2-已取消 |
- **响应：** 分页订单列表

### 2. 订单详情

- **接口：** `GET /order/detail/{id}`
- **描述：** 获取订单详细信息
- **认证：** 需要
- **路径参数：** `id` - 订单ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "orderNo": "202401011200001",
      "userId": 1,
      "username": "test",
      "totalAmount": 596.00,
      "status": 1,
      "remark": "订单备注",
      "createTime": "2024-01-01 12:00:00",
      "payTime": "2024-01-01 12:05:00",
      "items": [
        {
          "id": 1,
          "orderId": 1,
          "gameId": 1,
          "gameName": "赛博朋克2077",
          "gameCover": "/api/img/xxx.jpg",
          "quantity": 2,
          "price": 298.00,
          "totalPrice": 596.00,
          "createTime": "2024-01-01 12:00:00"
        }
      ]
    }
  }
  ```

### 3. 创建订单

- **接口：** `POST /order/create`
- **描述：** 从购物车创建订单
- **认证：** 需要
- **请求体：**
  ```json
  {
    "remark": "string"    // 订单备注（可选）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "创建订单成功",
    "data": {
      "id": 1,
      "orderNo": "202401011200001",
      "totalAmount": 596.00,
      "status": 0,
      "createTime": "2024-01-01 12:00:00"
    }
  }
  ```

### 4. 支付订单

- **接口：** `POST /order/pay`
- **描述：** 支付订单（模拟支付）
- **认证：** 需要
- **请求体：**
  ```json
  {
    "orderNo": "202401011200001"    // 订单号（必填）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "支付成功",
    "data": null
  }
  ```

### 5. 取消订单

- **接口：** `POST /order/cancel/{id}`
- **描述：** 取消未支付订单
- **认证：** 需要
- **路径参数：** `id` - 订单ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "取消成功",
    "data": null
  }
  ```

---

## 轮播图模块

**基础路径：** `/api/banner`

### 1. 轮播图列表（用户端）

- **接口：** `GET /banner/list`
- **描述：** 获取显示的轮播图列表
- **响应：**
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "title": "新年特惠",
        "image": "/api/img/banner1.jpg",
        "link": "/game/detail/1",
        "sort": 1
      }
    ]
  }
  ```

### 2. 全部轮播图（管理员）

- **接口：** `GET /banner/all`
- **描述：** 获取所有轮播图（包括隐藏的）
- **认证：** 需要
- **权限：** 管理员
- **响应：** 轮播图列表

### 3. 添加轮播图（管理员）

- **接口：** `POST /banner/add`
- **描述：** 添加轮播图
- **认证：** 需要
- **权限：** 管理员
- **请求体：**
  ```json
  {
    "title": "string",     // 标题（必填）
    "image": "string",     // 图片URL（必填）
    "link": "string",      // 跳转链接（可选）
    "sort": 1,             // 排序值（可选）
    "status": 0            // 状态：0-显示，1-隐藏（默认0）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "添加成功",
    "data": null
  }
  ```

### 4. 更新轮播图（管理员）

- **接口：** `PUT /banner/update`
- **描述：** 更新轮播图信息
- **认证：** 需要
- **权限：** 管理员
- **请求体：** Banner 对象（需包含 id）
- **响应：**
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 5. 删除轮播图（管理员）

- **接口：** `DELETE /banner/delete/{id}`
- **描述：** 删除轮播图
- **认证：** 需要
- **权限：** 管理员
- **路径参数：** `id` - 轮播图ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

---

## 评论模块

**基础路径：** `/api/comment`

### 1. 评论列表

- **接口：** `GET /comment/list`
- **描述：** 获取评论列表（分页）
- **请求参数：**
  | 参数 | 类型 | 必填 | 说明 |
  |-----|------|-----|------|
  | page | int | 否 | 页码，默认1 |
  | size | int | 否 | 每页条数，默认10 |
  | gameId | long | 否 | 游戏ID筛选 |
  | userId | long | 否 | 用户ID筛选 |
- **响应：**
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "userId": 1,
          "username": "test",
          "avatar": "/api/img/avatar.jpg",
          "gameId": 1,
          "gameName": "赛博朋克2077",
          "content": "很好玩的游戏！",
          "rating": 4.5,
          "createTime": "2024-01-01 12:00:00"
        }
      ],
      "total": 100,
      "size": 10,
      "current": 1,
      "pages": 10
    }
  }
  ```

### 2. 获取用户评论

- **接口：** `GET /comment/user-comment`
- **描述：** 获取当前用户对某游戏的评论
- **认证：** 需要
- **请求参数：**
  | 参数 | 类型 | 必填 | 说明 |
  |-----|------|-----|------|
  | gameId | long | 是 | 游戏ID |
- **响应：**
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "content": "很好玩！",
      "rating": 5.0,
      "createTime": "2024-01-01 12:00:00"
    }
  }
  ```

### 3. 添加评论

- **接口：** `POST /comment/add`
- **描述：** 添加游戏评论
- **认证：** 需要
- **请求体：**
  ```json
  {
    "gameId": 1,              // 游戏ID（必填）
    "content": "string",      // 评论内容（必填）
    "rating": 4.5             // 评分 0.5-5.0（可选，默认5.0）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "评论成功",
    "data": null
  }
  ```

### 4. 更新评论

- **接口：** `PUT /comment/update/{id}`
- **描述：** 更新自己的评论
- **认证：** 需要
- **路径参数：** `id` - 评论ID
- **请求体：**
  ```json
  {
    "gameId": 1,              // 游戏ID（可选）
    "content": "string",      // 评论内容（必填）
    "rating": 4.5             // 评分 0.5-5.0（可选）
  }
  ```
- **响应：**
  ```json
  {
    "code": 200,
    "message": "评论更新成功",
    "data": null
  }
  ```

### 5. 删除评论（用户）

- **接口：** `DELETE /comment/delete/{id}`
- **描述：** 删除自己的评论
- **认证：** 需要
- **路径参数：** `id` - 评论ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

### 6. 删除评论（管理员）

- **接口：** `DELETE /comment/admin/delete/{id}`
- **描述：** 管理员删除任意评论
- **认证：** 需要
- **权限：** 管理员
- **路径参数：** `id` - 评论ID
- **响应：**
  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

---

## 仪表盘模块

**基础路径：** `/api/dashboard`

### 1. 统计数据

- **接口：** `GET /dashboard/stats`
- **描述：** 获取仪表盘统计数据
- **响应：**
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "userCount": 1000,      // 用户总数
      "gameCount": 500,       // 游戏总数
      "orderCount": 2000,     // 订单总数
      "salesAmount": 50000.00 // 总销售额
    }
  }
  ```

---

## 文件模块

**基础路径：** `/api/file` 和 `/api/img`

### 1. 文件上传

- **接口：** `POST /file/upload`
- **描述：** 上传图片文件
- **Content-Type：** `multipart/form-data`
- **请求参数：**
  | 参数 | 类型 | 必填 | 说明 |
  |-----|------|-----|------|
  | file | File | 是 | 图片文件（支持jpg/png/gif） |
- **响应：**
  ```json
  {
    "code": 200,
    "message": "上传成功",
    "data": {
      "url": "/api/img/xxx.jpg",
      "filename": "xxx.jpg"
    }
  }
  ```
- **限制：** 单个文件最大10MB

### 2. 图片访问

- **接口：** `GET /img/{filename}`
- **描述：** 获取上传的图片文件
- **路径参数：** `filename` - 文件名
- **响应：** 图片文件流

---

## 数据模型

### User（用户）

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | Long | 主键ID |
| username | String | 用户名 |
| password | String | 密码（加密存储） |
| phone | String | 手机号 |
| email | String | 邮箱 |
| avatar | String | 头像URL |
| bio | String | 个人简介 |
| status | Integer | 状态：0-正常，1-冻结 |
| role | Integer | 角色：0-普通用户，1-管理员 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| deleted | Integer | 逻辑删除标志 |

### Game（游戏）

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | Long | 主键ID |
| name | String | 游戏名称 |
| cover | String | 封面图URL |
| description | String | 游戏描述 |
| requirements | String | 配置要求 |
| screenshots | String | 截图JSON数组 |
| price | BigDecimal | 现价 |
| originalPrice | BigDecimal | 原价 |
| categoryId | Long | 分类ID |
| stock | Integer | 库存 |
| sales | Integer | 销量 |
| status | Integer | 状态：0-上架，1-下架 |
| rating | Double | 平均评分 0-5 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| deleted | Integer | 逻辑删除标志 |
| categoryName | String | 分类名称（非数据库字段） |

### Category（分类）

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | Long | 主键ID |
| name | String | 分类名称 |
| description | String | 分类描述 |
| sort | Integer | 排序值 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| deleted | Integer | 逻辑删除标志 |

### Cart（购物车）

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | Long | 主键ID |
| userId | Long | 用户ID |
| gameId | Long | 游戏ID |
| quantity | Integer | 数量 |
| price | BigDecimal | 单价 |
| totalPrice | BigDecimal | 总价 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| gameName | String | 游戏名称（非数据库字段） |
| gameCover | String | 游戏封面（非数据库字段） |

### Order（订单）

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | Long | 主键ID |
| orderNo | String | 订单号 |
| userId | Long | 用户ID |
| totalAmount | BigDecimal | 订单总金额 |
| status | Integer | 状态：0-未支付，1-已支付，2-已取消 |
| remark | String | 订单备注 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| payTime | LocalDateTime | 支付时间 |
| username | String | 用户名（非数据库字段） |
| items | List<OrderItem> | 订单项列表（非数据库字段） |

### OrderItem（订单项）

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | Long | 主键ID |
| orderId | Long | 订单ID |
| gameId | Long | 游戏ID |
| gameName | String | 游戏名称 |
| gameCover | String | 游戏封面 |
| quantity | Integer | 数量 |
| price | BigDecimal | 单价 |
| totalPrice | BigDecimal | 总价 |
| createTime | LocalDateTime | 创建时间 |

### Banner（轮播图）

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | Long | 主键ID |
| title | String | 标题 |
| image | String | 图片URL |
| link | String | 跳转链接 |
| sort | Integer | 排序值 |
| status | Integer | 状态：0-显示，1-隐藏 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

### Comment（评论）

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | Long | 主键ID |
| userId | Long | 用户ID |
| gameId | Long | 游戏ID |
| content | String | 评论内容 |
| rating | Double | 评分 0.5-5.0 |
| createTime | LocalDateTime | 创建时间 |
| username | String | 用户名（非数据库字段） |
| avatar | String | 用户头像（非数据库字段） |
| gameName | String | 游戏名称（非数据库字段） |

---

## 项目结构

```
game-store-backend/
├── pom.xml                          # Maven配置文件
├── API_DOCUMENTATION.md             # 接口文档（本文件）
└── src/
    └── main/
        ├── java/com/gamestore/
        │   ├── GameStoreApplication.java      # 启动类
        │   ├── config/
        │   │   ├── MyBatisPlusConfig.java     # MyBatis Plus配置
        │   │   └── WebConfig.java             # Web配置（跨域、拦截器）
        │   ├── controller/                    # 控制器层
        │   │   ├── BannerController.java
        │   │   ├── CartController.java
        │   │   ├── CategoryController.java
        │   │   ├── CommentController.java
        │   │   ├── DashboardController.java
        │   │   ├── FileController.java
        │   │   ├── GameController.java
        │   │   ├── ImageController.java
        │   │   ├── OrderController.java
        │   │   └── UserController.java
        │   ├── entity/                        # 实体类
        │   │   ├── Banner.java
        │   │   ├── Cart.java
        │   │   ├── Category.java
        │   │   ├── Comment.java
        │   │   ├── Game.java
        │   │   ├── Order.java
        │   │   ├── OrderItem.java
        │   │   └── User.java
        │   ├── interceptor/
        │   │   └── AuthInterceptor.java       # JWT认证拦截器
        │   ├── mapper/                        # MyBatis Mapper接口
        │   │   ├── BannerMapper.java
        │   │   ├── CartMapper.java
        │   │   ├── CategoryMapper.java
        │   │   ├── CommentMapper.java
        │   │   ├── GameMapper.java
        │   │   ├── OrderItemMapper.java
        │   │   ├── OrderMapper.java
        │   │   └── UserMapper.java
        │   ├── service/                       # 服务层
        │   │   ├── BannerService.java
        │   │   ├── CartService.java
        │   │   ├── CategoryService.java
        │   │   ├── CommentService.java
        │   │   ├── DashboardService.java
        │   │   ├── GameService.java
        │   │   ├── OrderService.java
        │   │   ├── UserService.java
        │   │   └── impl/                      # 服务实现
        │   │       ├── BannerServiceImpl.java
        │   │       ├── CartServiceImpl.java
        │   │       ├── CategoryServiceImpl.java
        │   │       ├── CommentServiceImpl.java
        │   │       ├── DashboardServiceImpl.java
        │   │       ├── GameServiceImpl.java
        │   │       ├── OrderServiceImpl.java
        │   │       └── UserServiceImpl.java
        │   └── utils/
        │       ├── JwtUtil.java               # JWT工具类
        │       └── Result.java                # 统一响应结果
        └── resources/
            └── application.yml                # 应用配置文件
```

---

## 配置文件说明

### application.yml 主要配置

| 配置项 | 说明 | 默认值 |
|-------|------|--------|
| server.port | 服务端口 | 8080 |
| server.servlet.context-path | 上下文路径 | /api |
| spring.datasource.url | 数据库连接URL | jdbc:mysql://localhost:3306/game_store |
| spring.datasource.username | 数据库用户名 | root |
| spring.datasource.password | 数据库密码 | 123456 |
| jwt.secret | JWT密钥 | game-store-secret-key-2026 |
| jwt.expiration | Token有效期 | 86400000ms (24小时) |
| file.upload.path | 文件上传路径 | E:/game-store/upload/ |

---

**文档生成时间：** 2026-03-03  
**文档版本：** 1.0.0
