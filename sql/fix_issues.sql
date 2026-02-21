-- 修复所有问题

-- 1. 修复购物车表字段默认值
ALTER TABLE `cart` 
MODIFY COLUMN `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
MODIFY COLUMN `total_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- 2. 检查用户表是否有头像字段（应该有）
-- 已经有 avatar 字段，无需修改

-- 3. 确保游戏表封面字段允许空（用于上传前）
ALTER TABLE `game` 
MODIFY COLUMN `cover` VARCHAR(255) NULL;

-- 4. 确保轮播图表图片字段允许空
ALTER TABLE `banner` 
MODIFY COLUMN `image` VARCHAR(255) NULL;

-- 5. 确保用户表头像字段允许空
ALTER TABLE `user` 
MODIFY COLUMN `avatar` VARCHAR(255) NULL;

-- 6. 添加游戏评分字段
ALTER TABLE `game` 
ADD COLUMN IF NOT EXISTS `rating` DOUBLE DEFAULT 0 COMMENT '平均评分 0-5';

-- 7. 修改评论评分字段为支持半星（小数）
ALTER TABLE `comment` 
MODIFY COLUMN `rating` DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分 0.5-5，支持半星';

-- 查看修复后的表结构
DESCRIBE `cart`;
