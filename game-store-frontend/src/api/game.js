import request from '../utils/request'

export const getGameList = (params) => request.get('/game/list', { params })
export const getGameListForAdmin = (params) => request.get('/game/admin/list', { params })
export const getGameDetail = (id) => request.get(`/game/detail/${id}`)
export const getTopGames = (limit) => request.get('/game/top', { params: { limit } })
export const getNewGames = (limit) => request.get('/game/new', { params: { limit } })
export const getRecommendGames = (limit) => request.get('/game/recommend', { params: { limit } })
export const addGame = (data) => request.post('/game/add', data)
export const updateGame = (data) => request.put('/game/update', data)
export const deleteGame = (id) => request.delete(`/game/delete/${id}`)
