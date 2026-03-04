<template>
  <div class="comment-manage">
    <div class="table-container">
      <el-table 
        :data="comments" 
        style="width: 100%"
        :header-cell-style="headerStyle"
        :cell-style="cellStyle"
        :row-style="rowStyle"
      >
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="gameName" label="游戏" width="120" />
        <el-table-column label="评分" width="130">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="deleteComment(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态 -->
      <div v-if="comments.length === 0" class="empty-state">
        <el-icon :size="48" color="#606266"><ChatDotRound /></el-icon>
        <p>暂无评论数据</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { getCommentList, adminDeleteComment } from '../../api/comment'

export default {
  name: 'CommentManage',
  components: { ChatDotRound },
  setup() {
    const comments = ref([])

    onMounted(() => {
      loadComments()
    })

    const loadComments = async () => {
      try {
        const res = await getCommentList({ page: 1, size: 100 })
        comments.value = res.records || []
      } catch (error) {
        console.error('加载评论失败:', error)
      }
    }

    const deleteCommentHandler = async (id) => {
      try {
        await ElMessageBox.confirm('确定删除这条评论吗？', '确认删除', { type: 'warning' })
        await adminDeleteComment(id)
        ElMessage.success('删除成功')
        loadComments()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
        }
      }
    }

    // 表格样式
    const headerStyle = () => ({
      background: '#1e2329',
      color: '#fff',
      borderBottom: '1px solid #3d444d'
    })

    const cellStyle = () => ({
      background: '#2a2f38',
      color: '#c0c0c0',
      borderBottom: '1px solid #3d444d'
    })

    const rowStyle = () => ({
      background: '#2a2f38'
    })

    return {
      comments,
      deleteComment: deleteCommentHandler,
      headerStyle,
      cellStyle,
      rowStyle
    }
  }
}
</script>

<style>
/* 全局样式 - 确保限制宽度 */
.comment-manage {
  padding: 20px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.table-container {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.84) 0%, rgba(20, 31, 44, 0.96) 100%);
  border: 1px solid rgba(84, 123, 157, 0.36);
  border-radius: 8px;
  padding: 16px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(8px);
}

/* 直接选择器 - 不影响布局计算 */
.comment-manage .el-table {
  background: #2a2f38 !important;
}

.comment-manage .el-table__header-wrapper {
  background: #1e2329 !important;
}

.comment-manage .el-table__header {
  background: #1e2329 !important;
}

.comment-manage .el-table__header th {
  background: rgba(20, 33, 46, 0.96) !important;
  color: #fff !important;
  border-bottom: 1px solid rgba(84, 123, 157, 0.45) !important;
}

.comment-manage .el-table__body-wrapper {
  background: #2a2f38 !important;
}

.comment-manage .el-table__body tr {
  background: #2a2f38 !important;
}

.comment-manage .el-table__body td {
  background: rgba(22, 34, 48, 0.94) !important;
  color: #c0c0c0 !important;
  border-bottom: 1px solid rgba(70, 101, 129, 0.34) !important;
}

.comment-manage .el-table--enable-row-hover .el-table__body tr:hover > td {
  background: rgba(35, 57, 77, 0.95) !important;
}

.comment-manage .el-table__empty-block {
  background: #2a2f38 !important;
}

.comment-manage .el-table__empty-text {
  color: #808080 !important;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
  background: #2a2f38;
}

.empty-state p {
  margin-top: 16px;
  font-size: 14px;
}
</style>
