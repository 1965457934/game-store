import request from '../utils/request'

export const getCommentList = (params) => request.get('/comment/list', { params })
export const addComment = (data) => request.post('/comment/add', data)
export const updateComment = (id, data) => request.put(`/comment/update/${id}`, data)
export const deleteComment = (id) => request.delete(`/comment/delete/${id}`)
export const getUserComment = (gameId) => request.get('/comment/user-comment', { params: { gameId } })
