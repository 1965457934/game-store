import request from '../utils/request'

export const getCartList = () => request.get('/cart/list')
export const addToCart = (data) => request.post('/cart/add', data)
export const updateCart = (id, data) => request.put(`/cart/update/${id}`, data)
export const deleteCart = (id) => request.delete(`/cart/delete/${id}`)
export const clearCart = () => request.delete('/cart/clear')
