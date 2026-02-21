-- 修复评论评分字段精度问题

-- 1. 检查当前评论表结构
DESCRIBE `comment`;

-- 2. 确保 rating 字段是 DECIMAL(2,1) 类型以支持半星
ALTER TABLE `comment` 
MODIFY COLUMN `rating` DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分 0.5-5，支持半星';

-- 3. 检查现有评论的评分值
SELECT id, user_id, game_id, rating, content, create_time 
FROM `comment` 
ORDER BY id DESC 
LIMIT 10;

-- 4. 测试插入一条 3.5 星的评论（测试用）
-- INSERT INTO `comment` (user_id, game_id, content, rating) 
-- VALUES (1, 1, '测试半星评分', 3.5);
