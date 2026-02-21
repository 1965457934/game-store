import request from '../utils/request'

export const uploadFile = (data) => request.post('/file/upload', data, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
