import { createStore } from 'vuex'
import { getCartList } from '../api/cart'
import { getUserInfo } from '../api/user'

export default createStore({
  state: {
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || '{}'),
    cartCount: 0
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_USER(state, user) {
      state.user = user
      localStorage.setItem('user', JSON.stringify(user))
    },
    CLEAR_AUTH(state) {
      state.token = ''
      state.user = {}
      state.cartCount = 0
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    SET_CART_COUNT(state, count) {
      state.cartCount = count
    }
  },
  actions: {
    login({ commit }, data) {
      commit('SET_TOKEN', data.token)
      commit('SET_USER', data.user)
    },
    logout({ commit }) {
      commit('CLEAR_AUTH')
    },
    async getCartCount({ commit, state }) {
      if (!state.token) {
        commit('SET_CART_COUNT', 0)
        return
      }
      try {
        const res = await getCartList()
        const count = res ? res.reduce((sum, item) => sum + item.quantity, 0) : 0
        commit('SET_CART_COUNT', count)
      } catch (error) {
        console.error('获取购物车数量失败:', error)
      }
    },
    async getUserInfo({ commit, state }) {
      if (!state.token) return
      try {
        const res = await getUserInfo()
        commit('SET_USER', res)
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }
  }
})
