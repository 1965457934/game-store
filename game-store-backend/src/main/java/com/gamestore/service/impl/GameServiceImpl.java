package com.gamestore.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gamestore.entity.Game;
import com.gamestore.mapper.CommentMapper;
import com.gamestore.mapper.GameMapper;
import com.gamestore.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl extends ServiceImpl<GameMapper, Game> implements GameService {
    
    @Autowired
    private GameMapper gameMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Override
    public Game getGameDetail(Long id) {
        Game game = gameMapper.getGameDetail(id);
        // 如果评分字段为空，实时计算评分
        if (game != null && (game.getRating() == null || game.getRating() == 0)) {
            Double avgRating = commentMapper.getAverageRating(id);
            if (avgRating != null) {
                game.setRating(Math.round(avgRating * 10) / 10.0);
            }
        }
        return game;
    }
    
    @Override
    public Page<Game> getGamePage(Integer page, Integer size, String keyword, Long categoryId, String sortBy) {
        Page<Game> pageInfo = new Page<>(page, size);
        List<Game> games = gameMapper.selectGameList(pageInfo, keyword, categoryId, sortBy);
        // 填充评分
        games.forEach(this::fillRating);
        return pageInfo.setRecords(games);
    }
    
    @Override
    public Page<Game> getGamePageForAdmin(Integer page, Integer size, String keyword) {
        Page<Game> pageInfo = new Page<>(page, size);
        List<Game> games = gameMapper.selectGameListForAdmin(pageInfo, keyword);
        // 填充评分
        games.forEach(this::fillRating);
        return pageInfo.setRecords(games);
    }
    
    /**
     * 【首页热门游戏推荐算法】
     * 
     * 核心目标：综合销量、评分、上架时间三个维度，通过加权随机算法推荐热门游戏。
     * 设计意图：避免单纯按销量排序导致老游戏长期霸榜，让新上架的优质游戏也能获得曝光机会。
     *
     * 算法流程：
     * ┌─────────────────────────────────────────────────────────────────────────────┐
     * │  Step 1: 候选池构建                                                          │
     * │  从数据库中取销量前 200 的上架游戏作为候选池。                                 │
     * │  取 200 而非全部是为了兼顾性能与多样性（limit 通常为 6）。                      │
     * ├─────────────────────────────────────────────────────────────────────────────┤
     * │  Step 2: 三维评分计算                                                        │
     * │  对每款游戏计算综合得分：score = salesScore × ratingScore × timeDecay          │
     * │                                                                             │
     * │  ├─ salesScore（销量分）= max(sales, 1)                                      │
     * │  │   说明：销量直接反映市场接受度。取 max(sales, 1) 避免 0 销量导致无法选中。    │
     * │  │                                                                             │
     * │  ├─ ratingScore（评分分）= rating / 5.0                                      │
     * │  │   说明：将 0~5 分映射到 0~1 区间。默认 3 分（0.6）避免无评分时权重归零。      │
     * │  │                                                                             │
     * │  └─ timeDecay（时间衰减系数）= 30 / (30 + daysSinceRelease)                   │
     * │      说明：上架越久衰减越大，给新游戏更多机会。                                 │
     * │      示例：上架 0 天 → 1.0；上架 30 天 → 0.5；上架 90 天 → 0.25               │
     * │      公式选择理由：倒数衰减比线性衰减更平滑，且永不为 0。                       │
     * ├─────────────────────────────────────────────────────────────────────────────┤
     * │  Step 3: 加权随机抽样（轮盘赌算法，Roulette Wheel Selection）                  │
     * │  目的：不是每次都返回固定前几名，增加推荐多样性，但高分游戏仍有更高概率被选中。    │
     * │                                                                             │
     * │  流程：                                                                       │
     * │  1. 计算当前候选池的总得分 totalScore                                          │
     * │  2. 生成 [0, totalScore) 之间的随机数 r                                       │
     * │  3. 遍历候选列表，累加得分直至累加和 ≥ r，选中该游戏                             │
     * │  4. 将选中的游戏从池中移除，确保不重复推荐                                      │
     * │  5. 重复 limit 次                                                             │
     * │                                                                             │
     * │  数学原理：每款游戏被选中的概率 = 该游戏得分 / 总得分                           │
     * │  示例：游戏 A 得分 9000，游戏 B 得分 4500，总得分 13500                        │
     * │         A 被选中概率 = 9000/13500 = 66.7%，B = 33.3%                          │
     * └─────────────────────────────────────────────────────────────────────────────┘
     *
     * @param limit 需要推荐的游戏数量（首页通常传 6）
     * @return 推荐游戏列表，每次调用结果可能不同（加权随机）
     */
    @Override
    public List<Game> getTopGames(Integer limit) {
        // Step 1: 构建候选池 —— 取销量前 200 的上架游戏，平衡性能与多样性
        List<Game> candidates = gameMapper.selectTopGames(Math.max(limit, 200));
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // 若数据库中评分字段为空，实时从 comment 表计算平均评分并回填
        candidates.forEach(this::fillRating);

        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();

        // Step 2: 三维评分 —— 销量 × 评分 × 时间衰减
        List<Map.Entry<Game, Double>> scoredList = candidates.stream()
            .map(game -> {
                // 销量分：直接取销量数值，最低保底 1 分，防止 0 销量游戏永远无法入选
                double sales = game.getSales() != null ? game.getSales().doubleValue() : 0;
                double salesScore = Math.max(sales, 1);

                // 评分分：将 0~5 分映射到 0~1 系数。若评分缺失默认 3.0 → 0.6
                double rating = game.getRating() != null ? game.getRating() : 3.0;
                double ratingScore = rating / 5.0;

                // 时间衰减分：倒数衰减模型。
                // 刚上架 decay=1.0（满分），上架 30 天 decay=0.5，上架 90 天 decay=0.25
                long daysSinceRelease = 0;
                if (game.getCreateTime() != null) {
                    daysSinceRelease = ChronoUnit.DAYS.between(game.getCreateTime(), now);
                    if (daysSinceRelease < 0) {
                        daysSinceRelease = 0;
                    }
                }
                double timeDecay = 30.0 / (30.0 + daysSinceRelease);

                // 综合得分 = 销量 × 评分系数 × 时间衰减
                // 例：销量 10000，评分 4.5，上架 7 天
                // score = 10000 × 0.9 × 0.81 = 7290
                double score = salesScore * ratingScore * timeDecay;
                return new AbstractMap.SimpleEntry<>(game, score);
            })
            .filter(entry -> entry.getValue() > 0)
            .collect(Collectors.toList());

        if (scoredList.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 3: 轮盘赌加权随机 —— 概率与得分正相关，保证多样性
        List<Game> result = new ArrayList<>();
        List<Map.Entry<Game, Double>> pool = new ArrayList<>(scoredList);

        for (int i = 0; i < limit && !pool.isEmpty(); i++) {
            // 计算当前候选池总得分
            double totalScore = pool.stream().mapToDouble(Map.Entry::getValue).sum();
            // 在 [0, totalScore) 区间生成随机数
            double r = random.nextDouble() * totalScore;
            double accum = 0;

            // 轮盘转动，选中落点所在区域对应的游戏
            for (int j = 0; j < pool.size(); j++) {
                accum += pool.get(j).getValue();
                if (accum >= r) {
                    result.add(pool.get(j).getKey());
                    pool.remove(j); // 移除已选中，保证本次返回列表不重复
                    break;
                }
            }
        }

        return result;
    }
    
    @Override
    public List<Game> getNewGames(Integer limit) {
        List<Game> games = gameMapper.selectNewGames(limit);
        // 填充评分
        games.forEach(this::fillRating);
        return games;
    }
    
    /**
     * 【购物车猜你喜欢推荐算法 —— 基于用户购买历史的协同过滤简化版】
     * 
     * 核心目标：分析用户历史购买记录中的游戏类型偏好，按类型权重推荐同类游戏。
     * 设计意图：让用户感觉"系统懂我"，提高转化率和用户粘性。
     *
     * 与 getTopGames 的区别：
     * ┌──────────────────────┬─────────────────────────────┬─────────────────────────────┐
     * │       维度           │      getTopGames            │     getRecommendGames       │
     * ├──────────────────────┼─────────────────────────────┼─────────────────────────────┤
     * │ 数据来源             │ 全局销量排序                 │ 用户个人订单历史             │
     * │ 核心特征             │ 销量+评分+时间衰减           │ 分类权重+销量+评分           │
     * │ 应用场景             │ 首页热门推荐（无差异化）      │ 购物车猜你喜欢（个性化）      │
     * │ 是否排除已购买       │ 否                          │ 是（避免重复推荐）           │
     * └──────────────────────┴─────────────────────────────┴─────────────────────────────┘
     *
     * 算法流程：
     * ┌─────────────────────────────────────────────────────────────────────────────┐
     * │  Step 1: 用户偏好提取（分类权重计算）                                         │
     * │  SQL 逻辑：                                                                  │
     * │    SELECT g.category_id, COUNT(*)                                            │
     * │    FROM order_info o                                                         │
     * │    JOIN order_item oi ON o.id = oi.order_id                                 │
     * │    JOIN game g ON oi.game_id = g.id                                         │
     * │    WHERE o.user_id = ? AND o.status = 1  -- 仅统计已支付订单                 │
     * │    GROUP BY g.category_id                                                    │
     * │                                                                             │
     * │  示例结果：用户买了 3 款 RPG、2 款 FPS、1 款策略                             │
     * │           → 原始权重：{ RPG:3, FPS:2, 策略:1 }                               │
     * │           → 归一化后：{ RPG:0.5, FPS:0.333, 策略:0.167 }                     │
     * │                                                                             │
     * │  归一化目的：将绝对计数转为概率分布，便于后续加权计算。                        │
     * ├─────────────────────────────────────────────────────────────────────────────┤
     * │  Step 2: 候选池构建                                                          │
     * │  SQL 逻辑：使用 LEFT JOIN + IS NULL 排除用户已购买的游戏                     │
     * │                                                                             │
     * │  排除已购买的原因：                                                          │
     * │  1. 用户已经拥有该游戏，重复推荐无意义                                         │
     * │  2. 提升用户体验，避免"我已经买了还推给我"的负面感受                           │
     * ├─────────────────────────────────────────────────────────────────────────────┤
     * │  Step 3: 个性化得分计算                                                      │
     * │  score = categoryWeight × (sales + 1) × (rating / 5.0)                       │
     * │                                                                             │
     * │  ├─ categoryWeight（分类权重）                                               │
     * │  │   来源：Step 1 计算的归一化偏好。若某分类未购买过，默认给极小值 0.005。      │
     * │  │   作用：决定推荐结果的类型分布，买得多的类型更容易被推荐。                   │
     * │  │                                                                             │
     * │  ├─ (sales + 1)（销量平滑项）                                                │
     * │  │   说明：+1 是拉普拉斯平滑，避免 0 销量导致权重归零。                         │
     * │  │   作用：在偏好类型内部，优先推荐卖得好的游戏。                               │
     * │  │                                                                             │
     * │  └─ (rating / 5.0)（评分系数）                                               │
     * │      作用：过滤低质游戏。即使属于偏好类型，低评分也会降低推荐概率。              │
     * │                                                                             │
     * │  示例计算：                                                                  │
     * │  游戏 A：分类 RPG(weight=0.5)，销量 5000，评分 4.5                            │
     * │  score = 0.5 × 5001 × 0.9 = 2250.45                                         │
     * │                                                                             │
     * │  游戏 B：分类 FPS(weight=0.333)，销量 8000，评分 4.0                          │
     * │  score = 0.333 × 8001 × 0.8 = 2131.47                                       │
     * │                                                                             │
     * │  结果：虽然游戏 B 销量更高，但用户偏好 RPG，因此 A 的得分略高。                │
     * ├─────────────────────────────────────────────────────────────────────────────┤
     * │  Step 4: 轮盘赌加权随机抽样（同 getTopGames）                                 │
     * │  原因：避免每次都返回同样的结果，增加惊喜感；同时高分候选仍有更高被选中概率。     │
     * └─────────────────────────────────────────────────────────────────────────────┘
     *
     * 降级策略（保证系统鲁棒性）：
     * ┌─────────────────────────────────────────────────────────────────────────────┐
     * │  条件                          │  行为                                      │
     * │  userId == null（未登录）       │  调用 getTopGames(limit) 返回全局热门       │
     * │  用户无已支付订单               │  调用 getTopGames(limit) 返回全局热门       │
     * │  权重计算后全为 0               │  调用 getTopGames(limit) 返回全局热门       │
     * │  候选池为空（全买过了）         │  调用 getTopGames(limit) 返回全局热门       │
     * └─────────────────────────────────────────────────────────────────────────────┘
     *
     * @param userId 当前用户 ID，从 JWT Token 中解析
     * @param limit  需要推荐的游戏数量（购物车页面通常传 4）
     * @return 个性化推荐游戏列表，每次调用结果可能不同
     */
    @Override
    public List<Game> getRecommendGames(Long userId, Integer limit) {
        // ==================== 降级策略：未登录时退化为全局热门推荐 ====================
        if (userId == null) {
            return getTopGames(limit);
        }
        
        // ==================== Step 1: 提取用户购买偏好（分类权重） ====================
        // 查询该用户所有已支付订单中各分类的购买数量
        List<Map<String, Object>> weights = gameMapper.selectUserCategoryWeights(userId);
        if (weights == null || weights.isEmpty()) {
            // 用户无购买记录，降级为热门推荐
            return getTopGames(limit);
        }
        
        // 构建分类权重映射并归一化（转为概率分布）
        Map<Long, Double> categoryWeightMap = new HashMap<>();
        double totalWeight = 0;
        for (Map<String, Object> w : weights) {
            Object categoryIdObj = w.get("categoryId");
            Object countObj = w.get("count");
            if (categoryIdObj == null || countObj == null) continue;
            Long categoryId = ((Number) categoryIdObj).longValue();
            double weight = ((Number) countObj).doubleValue();
            categoryWeightMap.put(categoryId, weight);
            totalWeight += weight;
        }
        
        if (totalWeight == 0 || categoryWeightMap.isEmpty()) {
            return getTopGames(limit);
        }
        
        // 归一化：weight_i = count_i / total_count
        // 例：{ RPG:3, FPS:2 } → { RPG:0.6, FPS:0.4 }
        for (Map.Entry<Long, Double> entry : categoryWeightMap.entrySet()) {
            entry.setValue(entry.getValue() / totalWeight);
        }
        
        // ==================== Step 2: 构建候选池（排除已购买） ====================
        // SQL 使用 LEFT JOIN + IS NULL 排除用户已拥有游戏
        List<Game> candidates = gameMapper.selectRecommendCandidates(userId);
        if (candidates == null || candidates.isEmpty()) {
            // 所有游戏都买过了，降级为热门推荐
            return getTopGames(limit);
        }
        
        // 回填评分（防止数据库 rating 字段为空）
        candidates.forEach(this::fillRating);
        
        // ==================== Step 3: 个性化得分计算 ====================
        // score = 分类权重 × (销量+1) × (评分/5)
        List<Map.Entry<Game, Double>> scored = candidates.stream()
            .map(game -> {
                // 分类权重：用户在该分类买得越多，权重越高；未购买过的分类给极小默认值
                double weight = categoryWeightMap.getOrDefault(game.getCategoryId(), 0.005);
                
                // 销量平滑：+1 防止 0 销量导致权重归零（拉普拉斯平滑思想）
                double sales = game.getSales() != null ? game.getSales().doubleValue() : 0;
                
                // 评分系数：0~1 区间，默认 3.0 → 0.6
                double rating = game.getRating() != null ? game.getRating() : 3.0;
                
                // 综合得分
                double score = weight * (sales + 1) * (rating / 5.0);
                return new AbstractMap.SimpleEntry<>(game, score);
            })
            .collect(Collectors.toList());
        
        // ==================== Step 4: 轮盘赌加权随机抽样 ====================
        List<Game> result = new ArrayList<>();
        List<Map.Entry<Game, Double>> pool = new ArrayList<>(scored);
        Random random = new Random();
        
        for (int i = 0; i < limit && !pool.isEmpty(); i++) {
            double totalScore = pool.stream().mapToDouble(Map.Entry::getValue).sum();
            double r = random.nextDouble() * totalScore;
            double accum = 0;
            for (int j = 0; j < pool.size(); j++) {
                accum += pool.get(j).getValue();
                if (accum >= r) {
                    result.add(pool.get(j).getKey());
                    pool.remove(j); // 移除已选中，保证不重复
                    break;
                }
            }
        }
        
        return result;
    }
    
    private void fillRating(Game game) {
        if (game != null && (game.getRating() == null || game.getRating() == 0)) {
            Double avgRating = commentMapper.getAverageRating(game.getId());
            if (avgRating != null) {
                game.setRating(Math.round(avgRating * 10) / 10.0);
            }
        }
    }
    
    @Override
    public boolean updateStock(Long gameId, Integer stock) {
        Game game = new Game();
        game.setId(gameId);
        game.setStock(stock);
        return gameMapper.updateById(game) > 0;
    }
    
    @Override
    public boolean updateSales(Long gameId, Integer sales) {
        Game game = getById(gameId);
        if (game != null) {
            game.setSales(game.getSales() + sales);
            return gameMapper.updateById(game) > 0;
        }
        return false;
    }
}
