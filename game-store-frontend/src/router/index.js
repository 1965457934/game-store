import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import store from '../store'

const routes = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue')
      },
      {
        path: 'games',
        name: 'Games',
        component: () => import('../views/game/GameList.vue')
      },
      {
        path: 'game/:id',
        name: 'GameDetail',
        component: () => import('../views/game/GameDetail.vue')
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('../views/order/Cart.vue'),
        meta: { requireAuth: true }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('../views/order/OrderList.vue'),
        meta: { requireAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/user/Profile.vue'),
        meta: { requireAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/user/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/user/Register.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/UserManage.vue')
      },
      {
        path: 'games',
        name: 'AdminGames',
        component: () => import('../views/admin/GameManage.vue')
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('../views/admin/CategoryManage.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/OrderManage.vue')
      },
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('../views/admin/CommentManage.vue')
      },
      {
        path: 'banners',
        name: 'AdminBanners',
        component: () => import('../views/admin/BannerManage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = store.state.token
  const user = store.state.user

  if (to.meta.requireAuth && !token) {
    ElMessage.warning('请先登录')
    next('/login')
  } else if (to.meta.requireAdmin && user?.role !== 1) {
    ElMessage.error('无权访问')
    next('/')
  } else {
    next()
  }
})

export default router
