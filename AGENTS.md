# Game Store - 游戏商城系统

## 项目概述

这是无锡学院计算机科学与技术专业的毕业设计项目，基于 SpringBoot + Vue 的游戏商城系统。

## 技术规范

### 后端规范
- 统一返回格式: `Result<T>` 包装类
- RESTful API 设计
- JWT Token 认证
- MyBatis-Plus 数据访问
- 全局异常处理

### 前端规范
- Vue 3 Composition API
- Element Plus UI组件库
- Axios 统一请求封装
- Vuex 状态管理
- Vue Router 路由管理

### 代码结构
```
后端:
- entity: 实体类，对应数据库表
- mapper: 数据访问层接口
- service: 业务逻辑层
- controller: 控制器层
- config: 配置类
- utils: 工具类

前端:
- views: 页面组件
- components: 公共组件
- api: API接口封装
- router: 路由配置
- store: Vuex状态管理
```

## 开发指南

### 添加新功能步骤

1. 后端:
   - 创建/修改实体类 (entity)
   - 创建Mapper接口
   - 创建Service接口和实现
   - 创建Controller
   - 添加SQL映射(如有复杂查询)

2. 前端:
   - 添加API接口 (api/)
   - 创建页面组件 (views/)
   - 添加路由配置 (router/)
   - 更新菜单

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式
- refactor: 重构

## 测试账号

- 管理员: admin / 123456
- 普通用户: test / 123456

## 注意事项

1. 后端配置在 application.yml 中
2. 数据库默认配置: root / 123456
3. 上传文件存储在 E:/game-store/upload/
4. 前端代理配置在 vue.config.js 中
