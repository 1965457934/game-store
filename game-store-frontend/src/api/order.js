import request from '../utils/request'

export const getOrderList = (params) => request.get('/order/list', { params })
export const getOrderDetail = (id) => request.get(`/order/detail/${id}`)
export const createOrder = (data) => request.post('/order/create', data)
export const payOrder = (data) => request.post('/order/pay', data)
export const cancelOrder = (id) => request.post(`/order/cancel/${id}`)
