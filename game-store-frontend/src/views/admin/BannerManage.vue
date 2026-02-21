<template>
  <div class="admin-page banner-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Picture /></el-icon>
        轮播图管理
      </h1>
      <p class="toolbar-tip">提示：点击轮播图可跳转到对应游戏详情页</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加轮播图
      </el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
      <el-table :data="banners" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="220">
          <template #default="{ row }">
            <el-image :src="row.image" style="width: 200px; height: 90px;" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column label="关联游戏" min-width="150">
          <template #default="{ row }">
            <span v-if="getGameName(row.link)" class="game-link">
              <el-icon><Link /></el-icon>
              {{ getGameName(row.link) }}
            </span>
            <span v-else class="no-link">未关联</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">
              {{ row.status === 0 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="editBanner(row)">编辑</el-button>
            <el-button type="danger" link @click="deleteBanner(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态 -->
      <div v-if="banners.length === 0 && !loading" class="empty-state">
        <el-icon :size="48" color="#606266"><Picture /></el-icon>
        <p>暂无轮播图数据</p>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="关联游戏" prop="link">
          <el-select 
            v-model="form.link" 
            placeholder="请选择关联游戏" 
            style="width: 100%"
            filterable
            clearable
            @visible-change="(val) => { if(val) filterGames() }"
          >
            <el-option
              v-for="game in filteredGames"
              :key="game.id"
              :label="`ID：${game.id}  ${game.name}    ¥${game.price}`"
              :value="game.id.toString()"
            >
              <div class="game-option-simple">
                <span class="opt-id">ID：{{ game.id }}</span>
                <span class="opt-name">{{ game.name }}</span>
                <span class="opt-price">¥{{ game.price }}</span>
              </div>
            </el-option>
          </el-select>
          <span class="form-tip">不选择则仅作为展示，不跳转</span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">显示</el-radio>
            <el-radio :label="1">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="轮播图" prop="image">
          <el-upload
            class="banner-uploader"
            action="/api/file/upload"
            name="file"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <img v-if="form.image" :src="form.image" class="banner-preview" />
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <span>点击上传</span>
            </div>
          </el-upload>
          <div v-if="uploadLoading" class="upload-loading">上传中...</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBanner" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Plus, Link } from '@element-plus/icons-vue'
import { getBannerList, addBanner, updateBanner, deleteBanner } from '../../api/banner'
import { getGameListForAdmin } from '../../api/game'

export default {
  name: 'BannerManage',
  components: { Picture, Plus, Link },
  setup() {
    const banners = ref([])
    const games = ref([])
    const filteredGames = ref([])
    const loading = ref(false)
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const form = ref({ status: 0, sort: 0 })
    const formRef = ref(null)
    const saving = ref(false)
    const uploadLoading = ref(false)

    const rules = {
      title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
      image: [{ required: true, message: '请上传图片', trigger: 'change' }]
    }

    onMounted(() => {
      loadBanners()
      loadGames()
    })

    const loadBanners = async () => {
      loading.value = true
      try {
        const res = await getBannerList()
        banners.value = res
      } finally {
        loading.value = false
      }
    }

    const loadGames = async () => {
      const res = await getGameListForAdmin({ page: 1, size: 1000 })
      games.value = res.records || []
      filteredGames.value = games.value
    }

    const filterGames = () => {
      filteredGames.value = games.value
    }

    const getGameName = (gameId) => {
      if (!gameId) return ''
      const game = games.value.find(g => g.id.toString() === gameId.toString())
      return game ? game.name : ''
    }

    const showAddDialog = () => {
      dialogTitle.value = '添加轮播图'
      form.value = { status: 0, sort: 0 }
      dialogVisible.value = true
    }

    const editBanner = (row) => {
      dialogTitle.value = '编辑轮播图'
      form.value = { ...row }
      dialogVisible.value = true
    }

    const saveBanner = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return

      saving.value = true
      try {
        if (form.value.id) {
          await updateBanner(form.value)
        } else {
          await addBanner(form.value)
        }
        ElMessage.success('保存成功')
        dialogVisible.value = false
        loadBanners()
      } finally {
        saving.value = false
      }
    }

    const deleteBanner = async (id) => {
      try {
        await ElMessageBox.confirm('确定要删除这个轮播图吗？', '确认删除', { type: 'warning' })
        await deleteBanner(id)
        ElMessage.success('删除成功')
        loadBanners()
      } catch (e) {
        if (e !== 'cancel') console.error(e)
      }
    }

    const handleUploadSuccess = (res) => {
      uploadLoading.value = false
      console.log('上传响应:', res)
      if (res.code === 200) {
        form.value.image = res.data.url
        ElMessage.success('上传成功')
      } else {
        ElMessage.error(res.message || '上传失败')
      }
    }

    const handleUploadError = (err) => {
      uploadLoading.value = false
      console.error('上传错误:', err)
      ElMessage.error('上传失败，请检查网络或联系管理员')
    }

    const beforeUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        ElMessage.error('请上传图片文件')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB')
        return false
      }
      uploadLoading.value = true
      return true
    }

    return {
      banners,
      games,
      filteredGames,
      loading,
      dialogVisible,
      dialogTitle,
      form,
      formRef,
      rules,
      saving,
      uploadLoading,
      getGameName,
      filterGames,
      showAddDialog,
      editBanner,
      saveBanner,
      deleteBanner,
      handleUploadSuccess,
      handleUploadError,
      beforeUpload
    }
  }
}
</script>

<style scoped>
.banner-manage {
  padding: 20px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  margin-bottom: 16px;
}

.toolbar-tip {
  color: #909399;
  font-size: 12px;
}

.table-wrapper {
  background: #2a2f38;
  border: 1px solid #3d444d;
  border-radius: 8px;
  padding: 16px;
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
  background: #1e2329 !important;
  color: #fff !important;
  border-bottom: 1px solid #3d444d !important;
}

.table-wrapper :deep(.el-table__body-wrapper) {
  background: #2a2f38 !important;
}

.table-wrapper :deep(.el-table__body tr) {
  background: #2a2f38 !important;
}

.table-wrapper :deep(.el-table__body td) {
  background: #2a2f38 !important;
  color: #c0c0c0 !important;
  border-bottom: 1px solid #3d444d !important;
}

.table-wrapper :deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background: #353b45 !important;
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

/* 游戏链接样式 */
.game-link {
  color: #1a9fff;
  display: flex;
  align-items: center;
  gap: 4px;
}

.no-link {
  color: #606266;
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

:deep(.el-select .el-input__wrapper) {
  background: #1e2329 !important;
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

:deep(.el-radio) {
  color: #c0c0c0;
}

.form-tip {
  color: #909399;
  font-size: 12px;
}

/* 上传组件 */
.banner-uploader {
  border: 1px dashed #3d444d;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 300px;
  height: 135px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1e2329;
}

.banner-uploader:hover {
  border-color: #1a9fff;
}

.banner-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #606266;
}

.upload-loading {
  margin-top: 8px;
  color: #1a9fff;
  font-size: 12px;
}

/* 游戏选择下拉框样式 */
.game-option-simple {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 0;
  font-size: 14px;
}

.opt-id {
  color: #1a9fff;
  font-weight: 600;
  background: rgba(26, 159, 255, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.opt-name {
  color: #e0e0e0;
  flex: 1;
}

.opt-price {
  color: #a4d007;
  font-weight: bold;
}
</style>
