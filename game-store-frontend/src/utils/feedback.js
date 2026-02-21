/**
 * 全局交互反馈工具
 * 确保所有用户操作都有明确的反馈
 */
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'

// 成功反馈
export const success = (message, options = {}) => {
  ElMessage.success({
    message,
    duration: 2000,
    ...options
  })
}

// 错误反馈
export const error = (message, options = {}) => {
  ElMessage.error({
    message,
    duration: 3000,
    ...options
  })
}

// 警告反馈
export const warning = (message, options = {}) => {
  ElMessage.warning({
    message,
    duration: 2500,
    ...options
  })
}

// 信息反馈
export const info = (message, options = {}) => {
  ElMessage.info({
    message,
    duration: 2000,
    ...options
  })
}

// 通知反馈（右上角）
export const notify = (title, message, type = 'info', options = {}) => {
  ElNotification({
    title,
    message,
    type,
    position: 'top-right',
    duration: 3000,
    ...options
  })
}

// 确认对话框
export const confirm = async (message, title = '确认操作', options = {}) => {
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      ...options
    })
    return true
  } catch {
    return false
  }
}

// 删除确认（专用）
export const confirmDelete = async (itemName = '该条目') => {
  return await confirm(
    `确定要删除${itemName}吗？此操作不可恢复！`,
    '确认删除',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'error'
    }
  )
}

// 提示输入
export const prompt = async (message, title = '请输入', options = {}) => {
  try {
    const value = await ElMessageBox.prompt(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      ...options
    })
    return value
  } catch {
    return null
  }
}

// 加载中提示
export const loading = (message = '加载中...') => {
  return ElMessage({
    message,
    duration: 0,
    showClose: true,
    icon: 'Loading'
  })
}

// 操作结果提示
export const result = (success, successMsg, failMsg) => {
  if (success) {
    ElMessage.success(successMsg)
  } else {
    ElMessage.error(failMsg)
  }
}

// 统一的点击反馈
export const clickFeedback = (action, feedback = {}) => {
  const {
    success: successMsg = '操作成功',
    error: errorMsg = '操作失败',
    loading: loadingMsg = '处理中...',
    showLoading = false
  } = feedback

  return async (...args) => {
    let loadingInstance = null
    
    if (showLoading) {
      loadingInstance = loading(loadingMsg)
    }
    
    try {
      const result = await action(...args)
      if (loadingInstance) {
        loadingInstance.close()
      }
      success(successMsg)
      return result
    } catch (err) {
      if (loadingInstance) {
        loadingInstance.close()
      }
      error(errorMsg)
      throw err
    }
  }
}

// 默认导出
export default {
  success,
  error,
  warning,
  info,
  notify,
  confirm,
  confirmDelete,
  prompt,
  loading,
  result,
  clickFeedback
}
