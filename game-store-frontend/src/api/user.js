import request from '../utils/request'

export const register = (data) => request.post('/user/register', data)
export const login = (data) => request.post('/user/login', data)
export const getUserInfo = () => request.get('/user/info')
export const updateUserInfo = (data) => request.put('/user/update', data)
export const updatePassword = (data) => request.put('/user/password', data)
export const getUserList = (params) => request.get('/user/list', { params })
export const freezeUser = (id) => request.post(`/user/freeze/${id}`)
export const unfreezeUser = (id) => request.post(`/user/unfreeze/${id}`)
