<template>
  <div class="admin-page game-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Grid /></el-icon>
        游戏管理
      </h1>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索游戏名称"
          clearable
          class="search-input"
          @keyup.enter="loadGames"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="loadGames">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加游戏
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
      <el-table :data="games" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <div class="cover-wrapper" @click="previewImage(row.cover)">
              <el-image 
                :src="row.cover || '/default-game.png'" 
                class="game-cover"
                fit="cover"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="cover-overlay">
                <el-icon><ZoomIn /></el-icon>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="游戏名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <span class="price-tag">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="dark" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="editGame(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'" 
              link 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link @click="deleteGameHandler(row)">
              <el-icon><Delete /></el-icon>
              删除
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
        @current-change="loadGames"
        @size-change="handleSizeChange"
        class="pagination"
      />
      
      <!-- 空状态 -->
      <div v-if="games.length === 0 && !loading" class="empty-state">
        <el-icon :size="48" color="#606266"><Grid /></el-icon>
        <p>暂无游戏数据</p>
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="100px" :rules="formRules" ref="formRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="游戏名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入游戏名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option 
                  v-for="cat in categories" 
                  :key="cat.id" 
                  :label="cat.name" 
                  :value="cat.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="封面图片">
          <image-upload v-model="form.cover" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number 
                v-model="form.price" 
                :precision="2" 
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价">
              <el-input-number 
                v-model="form.originalPrice" 
                :precision="2" 
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number 
                v-model="form.stock" 
                :min="0"
                :step="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="折扣">
              <el-input-number 
                v-model="form.discount" 
                :min="0"
                :max="100"
                :step="1"
                style="width: 100%"
              >
                <template #append>%</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="游戏简介" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入游戏简介"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="配置要求">
          <el-input 
            v-model="form.requirements" 
            type="textarea" 
            :rows="3"
            placeholder="请输入配置要求"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveGame">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="previewList"
      @close="previewVisible = false"
    />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Grid } from '@element-plus/icons-vue'
import { getGameListForAdmin, addGame, updateGame, deleteGame } from '../../api/game'
import { getCategoryList } from '../../api/category'
import ImageUpload from '../../components/ImageUpload.vue'

export default {
  name: 'GameManage',
  components: {
    ImageUpload,
    Grid
  },
  setup() {
    const games = ref([])
    const categories = ref([])
    const loading = ref(false)
    const saving = ref(false)
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const formRef = ref(null)
    const searchKeyword = ref('')
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)
    const previewVisible = ref(false)
    const previewList = ref([])
    
    const form = ref({
      name: '',
      categoryId: null,
      price: 0,
      originalPrice: 0,
      stock: 0,
      discount: 0,
      cover: '',
      description: '',
      requirements: '',
      status: 1
    })
    
    const formRules = {
      name: [
        { required: true, message: '请输入游戏名称', trigger: 'blur' },
        { min: 2, max: 50, message: '名称长度应为 2-50 个字符', trigger: 'blur' }
      ],
      categoryId: [
        { required: true, message: '请选择分类', trigger: 'change' }
      ],
      price: [
        { required: true, message: '请输入价格', trigger: 'blur' }
      ],
      stock: [
        { required: true, message: '请输入库存', trigger: 'blur' }
      ]
    }

    onMounted(() => {
      loadGames()
      loadCategories()
    })

    const loadGames = async () => {
      loading.value = true
      try {
        const res = await getGameListForAdmin({ 
          page: page.value, 
          size: size.value,
          keyword: searchKeyword.value
        })
        games.value = res.records || []
        total.value = res.total || 0
      } catch (error) {
        console.error('加载游戏失败:', error)
      } finally {
        loading.value = false
      }
    }

    const loadCategories = async () => {
      try {
        const res = await getCategoryList()
        categories.value = res || []
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    }
    
    const handleSizeChange = (newSize) => {
      size.value = newSize
      page.value = 1
      loadGames()
    }

    const showAddDialog = () => {
      dialogTitle.value = '添加游戏'
      form.value = {
        name: '',
        categoryId: null,
        price: 0,
        originalPrice: 0,
        stock: 0,
        discount: 0,
        cover: '',
        description: '',
        requirements: '',
        status: 1
      }
      dialogVisible.value = true
    }

    const editGame = (row) => {
      dialogTitle.value = '编辑游戏'
      form.value = { ...row }
      dialogVisible.value = true
    }
    
    const toggleStatus = async (row) => {
      const newStatus = row.status === 1 ? 0 : 1
      const actionText = newStatus === 1 ? '上架' : '下架'
      try {
        await ElMessageBox.confirm(
          `确定要${actionText}游戏 "${row.name}" 吗？`,
          `确认${actionText}`,
          { type: 'warning' }
        )
        await updateGame({ ...row, status: newStatus })
        ElMessage.success(`${actionText}成功`)
        loadGames()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('操作失败:', error)
        }
      }
    }

    const saveGame = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return
      
      saving.value = true
      try {
        if (form.value.id) {
          await updateGame(form.value)
          ElMessage.success('更新成功')
        } else {
          await addGame(form.value)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadGames()
      } catch (error) {
        console.error('保存失败:', error)
      } finally {
        saving.value = false
      }
    }

    const deleteGameHandler = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除游戏 "${row.name}" 吗？此操作不可恢复！`,
          '确认删除',
          { type: 'error' }
        )
        await deleteGame(row.id)
        ElMessage.success('删除成功')
        loadGames()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
        }
      }
    }
    
    const previewImage = (url) => {
      if (!url) {
        ElMessage.info('暂无图片')
        return
      }
      previewList.value = [url]
      previewVisible.value = true
    }

    return {
      games,
      categories,
      loading,
      saving,
      dialogVisible,
      dialogTitle,
      formRef,
      form,
      formRules,
      searchKeyword,
      page,
      size,
      total,
      previewVisible,
      previewList,
      loadGames,
      handleSizeChange,
      showAddDialog,
      editGame,
      toggleStatus,
      saveGame,
      deleteGame: deleteGameHandler,
      previewImage
    }
  }
}
</script>

<style scoped>
.game-manage {
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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 8px;
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

/* 封面图片 */
.cover-wrapper {
  position: relative;
  width: 100px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.game-cover {
  width: 100%;
  height: 100%;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(4, 10, 16, 0.25) 0%, rgba(4, 10, 16, 0.75) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.cover-overlay .el-icon {
  color: white;
  font-size: 20px;
}

.cover-wrapper:hover .cover-overlay {
  opacity: 1;
}

.image-error {
  width: 100%;
  height: 100%;
  background: #353b45;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #606266;
}

.price-tag {
  color: #a4d007;
  font-weight: bold;
}

/* 搜索框 */
.search-input :deep(.el-input__wrapper) {
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

/* 对话框深色主题 */
:deep(.el-dialog) {
  background: #1e2329 !important;
  border: 1px solid #3d444d;
}

:deep(.el-dialog__header) {
  background: #1e2329 !important;
  border-bottom: 1px solid #3d444d;
  margin-right: 0;
  padding: 16px 20px;
}

:deep(.el-dialog__title) {
  color: #fff !important;
  font-size: 16px;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  background: #1e2329 !important;
  color: #c0c0c0;
  padding: 20px;
}

:deep(.el-form-item__label) {
  color: #a0a0a0;
  font-weight: 500;
}

/* 统一所有输入框背景色 */
:deep(.el-input .el-input__wrapper) {
  background: #2a2f38 !important;
  box-shadow: 0 0 0 1px #3d444d inset !important;
  border-radius: 4px;
}

:deep(.el-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #4a5568 inset !important;
}

:deep(.el-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1a9fff inset !important;
}

:deep(.el-input__inner) {
  color: #e0e0e0;
  background: transparent !important;
}

:deep(.el-input__inner::placeholder) {
  color: #606266;
}

/* 文本域深色主题 */
:deep(.el-textarea__inner) {
  background: #2a2f38 !important;
  border-color: #3d444d !important;
  color: #e0e0e0;
  border-radius: 4px;
}

:deep(.el-textarea__inner:hover) {
  border-color: #4a5568 !important;
}

:deep(.el-textarea__inner:focus) {
  border-color: #1a9fff !important;
}

/* 字数统计样式 */
:deep(.el-input__count) {
  background: transparent !important;
  color: #606266;
}

/* 选择器深色主题 */
:deep(.el-select .el-input__wrapper) {
  background: #2a2f38 !important;
  box-shadow: 0 0 0 1px #3d444d inset !important;
}

:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #1a9fff inset !important;
}

:deep(.el-select-dropdown) {
  background: #2a2f38 !important;
  border: 1px solid #3d444d;
}

:deep(.el-select-dropdown__item) {
  color: #c0c0c0;
}

:deep(.el-select-dropdown__item.hover),
:deep(.el-select-dropdown__item:hover) {
  background: #353b45 !important;
  color: #fff;
}

:deep(.el-select-dropdown__item.selected) {
  color: #1a9fff !important;
}

/* 数字输入器深色主题 */
:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  background: #2a2f38 !important;
  box-shadow: 0 0 0 1px #3d444d inset !important;
  padding-left: 40px;
  padding-right: 40px;
}

:deep(.el-input-number .el-input__inner) {
  color: #e0e0e0;
  text-align: center;
}

:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
  background: #353b45 !important;
  border-color: #3d444d !important;
  color: #c0c0c0 !important;
  width: 32px;
}

:deep(.el-input-number__decrease:hover),
:deep(.el-input-number__increase:hover) {
  color: #1a9fff !important;
  background: #3d444d !important;
}

:deep(.el-input-number__decrease.is-disabled),
:deep(.el-input-number__increase.is-disabled) {
  color: #606266 !important;
  background: #2a2f38 !important;
}

/* 上传组件 */
:deep(.el-upload) {
  border: 1px dashed #3d444d;
  background: #2a2f38;
  border-radius: 4px;
  width: 100%;
}

:deep(.el-upload:hover) {
  border-color: #1a9fff;
  background: #353b45;
}

/* 按钮样式 */
:deep(.el-dialog__footer) {
  background: #1e2329 !important;
  border-top: 1px solid #3d444d;
  padding: 16px 20px;
}

:deep(.el-button:not(.el-button--primary)) {
  background: #2a2f38 !important;
  border-color: #3d444d !important;
  color: #c0c0c0 !important;
}

:deep(.el-button:not(.el-button--primary):hover) {
  border-color: #1a9fff !important;
  color: #1a9fff !important;
  background: #353b45 !important;
}

:deep(.el-button--primary) {
  background: #1a9fff !important;
  border-color: #1a9fff !important;
}

:deep(.el-button--primary:hover) {
  background: #4db3ff !important;
  border-color: #4db3ff !important;
}
</style>
