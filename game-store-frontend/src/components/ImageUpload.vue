<template>
  <div class="image-upload">
    <el-upload
      class="avatar-uploader"
      :action="uploadAction"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :headers="headers"
    >
      <img v-if="modelValue" :src="modelValue" class="avatar" />
      <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
    </el-upload>
    <div class="tip">点击上传图片</div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

export default {
  name: 'ImageUpload',
  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const store = useStore()
    
    const uploadAction = '/api/file/upload'
    
    const headers = computed(() => {
      return {
        Authorization: `Bearer ${store.state.token}`
      }
    })

    const beforeUpload = (file) => {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
      const isLt2M = file.size / 1024 / 1024 < 5

      if (!isJPG) {
        ElMessage.error('只支持 JPG/PNG/GIF 格式的图片!')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过 5MB!')
        return false
      }
      return true
    }

    const handleSuccess = (res) => {
      if (res.code === 200) {
        emit('update:modelValue', res.data.url)
        ElMessage.success('上传成功')
      } else {
        ElMessage.error(res.message || '上传失败')
      }
    }

    const handleError = () => {
      ElMessage.error('上传失败')
    }

    return {
      uploadAction,
      headers,
      beforeUpload,
      handleSuccess,
      handleError
    }
  }
}
</script>

<style scoped>
.image-upload {
  display: inline-block;
}
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}
.avatar-uploader:hover {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
.tip {
  font-size: 12px;
  color: #999;
  text-align: center;
  margin-top: 8px;
}
</style>
