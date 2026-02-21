import axios from 'axios'
import { ElMessage } from 'element-plus'
import store from '../store'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = store.state.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 公开接口列表（不需要登录）
const publicApis = [
  '/banner/list',
  '/game/list',
  '/game/detail',
  '/game/top',
  '/game/new',
  '/category/list',
  '/comment/list',
  '/user/login',
  '/user/register',
  '/upload/',
  '/file/upload'
]

const isPublicApi = (url) => {
  if (!url) return false
  return publicApis.some(api => url.includes(api))
}

request.interceptors.response.use(
  response => {
    const res = response.data
    // 业务错误码处理
    if (res.code !== 200) {
      // 登录接口的错误需要显示
      if (response.config.url.includes('/user/login')) {
        ElMessage.error(res.message || '登录失败')
      } else if (!isPublicApi(response.config.url)) {
        ElMessage.error(res.message || '请求失败')
      }
      // 创建一个包含完整响应的错误对象
      const error = new Error(res.message || '请求失败')
      error.response = {
        data: res,
        status: response.status,
        config: response.config
      }
      return Promise.reject(error)
    }
    return res.data
  },
  error => {
    const { response } = error
    if (response) {
      const url = response.config?.url || ''
      
      // 401 错误处理
      if (response.status === 401) {
        // 公开接口的401不处理
        if (isPublicApi(url)) {
          return Promise.reject(error)
        }
        // 非公开接口且非登录页，跳转登录
        const currentPath = router.currentRoute.value.path
        if (currentPath !== '/login' && currentPath !== '/register') {
          ElMessage.error('登录已过期，请重新登录')
          store.dispatch('logout')
          router.push('/login')
        }
      } else if (response.status === 403) {
        // 权限不足，显示错误但不跳转
        ElMessage.error(response.data?.message || '权限不足')
      } else {
        // 其他错误，非公开接口才显示
        if (!isPublicApi(url)) {
          ElMessage.error(response.data?.message || '请求失败')
        }
      }
    } else {
      // 网络错误只在非公开接口显示
      const url = error.config?.url || ''
      if (!isPublicApi(url)) {
        ElMessage.error('网络错误')
      }
    }
    return Promise.reject(error)
  }
)

export default request
