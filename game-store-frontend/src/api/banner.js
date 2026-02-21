import request from '../utils/request'

export const getBannerList = () => request.get('/banner/list')
export const getAllBanners = () => request.get('/banner/all')
export const addBanner = (data) => request.post('/banner/add', data)
export const updateBanner = (data) => request.put('/banner/update', data)
export const deleteBanner = (id) => request.delete(`/banner/delete/${id}`)
