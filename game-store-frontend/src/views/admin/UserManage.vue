<template>
  <div class="admin-page user-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><User /></el-icon>
        用户管理
      </h1>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
      <el-table :data="users" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar || '/default-avatar.png'" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="createTime" label="注册时间" min-width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" effect="dark" size="small">
              {{ row.status === 0 ? '正常' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 0" 
              type="danger" 
              link 
              @click="freezeUser(row)"
              :disabled="row.id === currentUserId"
            >
              <el-icon><Lock /></el-icon>
              冻结
            </el-button>
            <el-button 
              v-else 
              type="success" 
              link 
              @click="unfreezeUser(row)"
            >
              <el-icon><Unlock /></el-icon>
              解冻
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination 
        v-if="total > 0"
        v-model:current-page="page" 
        v-model:page-size="size" 
        :total="total" 
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper" 
        @current-change="loadUsers"
        @size-change="handleSizeChange"
        class="pagination"
      />
      
      <!-- 空状态 -->
      <div v-if="users.length === 0 && !loading" class="empty-state">
        <el-icon :size="48" color="#606266"><User /></el-icon>
        <p>暂无用户数据</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Unlock } from '@element-plus/icons-vue'
import { getUserList, freezeUser, unfreezeUser } from '../../api/user'

export default {
  name: 'UserManage',
  components: { User, Lock, Unlock },
  setup() {
    const store = useStore()
    const users = ref([])
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)
    const loading = ref(false)
    
    const currentUserId = computed(() => store.state.user?.id)

    onMounted(() => {
      loadUsers()
    })

    const loadUsers = async () => {
      loading.value = true
      try {
        const res = await getUserList({ page: page.value, size: size.value })
        users.value = res.records || []
        total.value = res.total || 0
      } catch (error) {
        console.error('加载用户失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    const handleSizeChange = (newSize) => {
      size.value = newSize
      page.value = 1
      loadUsers()
    }

    const freezeUserHandler = async (row) => {
      if (row.id === currentUserId.value) {
        ElMessage.warning('不能冻结自己的账号')
        return
      }
      
      try {
        await ElMessageBox.confirm(
          `确定要冻结用户 "${row.username}" 吗？`,
          '确认冻结',
          { type: 'warning' }
        )
        await freezeUser(row.id)
        ElMessage.success('冻结成功')
        loadUsers()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('冻结失败:', error)
        }
      }
    }

    const unfreezeUserHandler = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要解冻用户 "${row.username}" 吗？`,
          '确认解冻',
          { type: 'info' }
        )
        await unfreezeUser(row.id)
        ElMessage.success('解冻成功')
        loadUsers()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('解冻失败:', error)
        }
      }
    }

    return {
      users,
      page,
      size,
      total,
      loading,
      currentUserId,
      loadUsers,
      handleSizeChange,
      freezeUser: freezeUserHandler,
      unfreezeUser: unfreezeUserHandler
    }
  }
}
</script>

<style scoped>
.user-manage {
  padding: 20px;
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  color: #fff;
  margin: 0;
}

.table-wrapper {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.84) 0%, rgba(20, 31, 44, 0.96) 100%);
  border: 1px solid rgba(84, 123, 157, 0.36);
  border-radius: 8px;
  padding: 16px;
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(8px);
}

/* 表格深色主题 */
.table-wrapper :deep(.el-table) {
  background: #2a2f38 !important;
}

.table-wrapper :deep(.el-table__header-wrapper),
.table-wrapper :deep(.el-table__header) {
  background: #1e2329 !important;
}

.table-wrapper :deep(.el-table__header th) {
  background: rgba(20, 33, 46, 0.96) !important;
  color: #fff !important;
  border-bottom: 1px solid rgba(84, 123, 157, 0.45) !important;
}

.table-wrapper :deep(.el-table__body-wrapper) {
  background: #2a2f38 !important;
}

.table-wrapper :deep(.el-table__body tr) {
  background: #2a2f38 !important;
}

.table-wrapper :deep(.el-table__body td) {
  background: rgba(22, 34, 48, 0.94) !important;
  color: #c0c0c0 !important;
  border-bottom: 1px solid rgba(70, 101, 129, 0.34) !important;
}

.table-wrapper :deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background: rgba(35, 57, 77, 0.95) !important;
}

.table-wrapper :deep(.el-table__empty-block) {
  background: #2a2f38 !important;
}

.table-wrapper :deep(.el-table__empty-text) {
  color: #808080 !important;
}

/* 分页深色主题 */
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.pagination :deep(.el-pager li) {
  background: #1e2329 !important;
  color: #c0c0c0 !important;
  border: 1px solid #3d444d;
}

.pagination :deep(.el-pager li.active) {
  background: #1a9fff !important;
  color: #fff !important;
  border-color: #1a9fff;
}

.pagination :deep(.el-pagination button) {
  background: #1e2329 !important;
  color: #c0c0c0 !important;
  border: 1px solid #3d444d;
}

.pagination :deep(.el-pagination__total),
.pagination :deep(.el-pagination__jump) {
  color: #a0a0a0 !important;
}

.pagination :deep(.el-input__wrapper) {
  background: #1e2329 !important;
  box-shadow: 0 0 0 1px #3d444d inset;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-state p {
  margin-top: 16px;
  font-size: 14px;
}
</style>
