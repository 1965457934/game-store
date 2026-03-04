<template>
  <div class="admin-page category-manage">
    <div class="table-wrapper">
      <div class="toolbar">
        <el-button type="primary" @click="showAddDialog">添加分类</el-button>
      </div>
      <el-table :data="categories" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="editCategory(row)">编辑</el-button>
            <el-button type="danger" link @click="deleteCategory(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态 -->
      <div v-if="categories.length === 0" class="empty-state">
        <el-icon :size="48" color="#606266"><Folder /></el-icon>
        <p>暂无分类数据</p>
      </div>
    </div>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder } from '@element-plus/icons-vue'
import { getCategoryList, addCategory, updateCategory, deleteCategory } from '../../api/category'

export default {
  name: 'CategoryManage',
  components: { Folder },
  setup() {
    const categories = ref([])
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const form = ref({})

    onMounted(() => {
      loadCategories()
    })

    const loadCategories = async () => {
      const res = await getCategoryList()
      categories.value = res
    }

    const showAddDialog = () => {
      dialogTitle.value = '添加分类'
      form.value = {}
      dialogVisible.value = true
    }

    const editCategory = (row) => {
      dialogTitle.value = '编辑分类'
      form.value = { ...row }
      dialogVisible.value = true
    }

    const saveCategory = async () => {
      if (form.value.id) {
        await updateCategory(form.value)
      } else {
        await addCategory(form.value)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadCategories()
    }

    const deleteCategoryHandler = async (id) => {
      await deleteCategory(id)
      ElMessage.success('删除成功')
      loadCategories()
    }

    return {
      categories,
      dialogVisible,
      dialogTitle,
      form,
      showAddDialog,
      editCategory,
      saveCategory,
      deleteCategory: deleteCategoryHandler
    }
  }
}
</script>

<style scoped>
.category-manage {
  padding: 20px;
  box-sizing: border-box;
}

.table-wrapper {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.84) 0%, rgba(20, 31, 44, 0.96) 100%);
  border: 1px solid rgba(84, 123, 157, 0.36);
  border-radius: 8px;
  padding: 16px;
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(8px);
}

.toolbar {
  margin-bottom: 16px;
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

/* 对话框深色主题 */
:deep(.el-dialog) {
  background: #2a2f38 !important;
  border: 1px solid #3d444d;
}

:deep(.el-dialog__header) {
  background: #1e2329 !important;
  border-bottom: 1px solid #3d444d;
  margin-right: 0;
}

:deep(.el-dialog__title) {
  color: #fff !important;
}

:deep(.el-dialog__body) {
  background: #2a2f38 !important;
  color: #c0c0c0;
}

:deep(.el-form-item__label) {
  color: #c0c0c0;
}

:deep(.el-input__wrapper) {
  background: #1e2329 !important;
  box-shadow: 0 0 0 1px #3d444d inset;
}

:deep(.el-input__inner) {
  color: #e0e0e0;
}

:deep(.el-textarea__inner) {
  background: #1e2329 !important;
  border-color: #3d444d;
  color: #e0e0e0;
}
</style>
