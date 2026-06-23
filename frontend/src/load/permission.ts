import router from '@/router/index'
import Constant from '@/config/constant'
import request from '@/api/index'
import { getCookie } from '@/utils/cookie'
import { useAuthStore } from '@/store/modules/auth'

const whiteUrlList = [
  '/auth/login',
  '/error/400.html',
  '/error/400.html'
]

const isOffline = import.meta.env.VITE_DATA_OFFLINE === 'true'

// 应用程序登录权限校验入口，若没有登录则跳转登录页
router.beforeEach((to, from, next) => {
  // 如果上本地离线则直接进入平台
  if (isOffline) {
    return next()
  }
  // 白名单 URL 直接跳过
  if (whiteUrlList.includes(to.path)) {
    return next()
  }
  // 如果开启登录验证则进行登录会话验证
  const authToken = getCookie(Constant.X_Auth_Token)
  if (!authToken) {
    // 没有会话登录则直接跳转登录页面
    window.location.href = import.meta.env.VITE_LOGIN_URL
    return
  }
  // 如果用户已经登录，直接跳转
  const authStore = useAuthStore()
  if (authStore.account.acct) {
    return next()
  }
  // 没有用户会话数据则从后端请求获取
  const token = decodeURIComponent(authToken)
  request['session'].loadSessionInfo({
    token: token
  }).then((data: any) => {
    authStore.dispatchSetSession(token)
    authStore.dispatchSetAccount(data.result)
    return next()
  }).catch((error: any) => {
    // 会话登录异常则跳转异常页面
    console.error(error)
    if (error.code === Constant.RESP_CODE.NEED_LOGIN) {
      window.location.href = import.meta.env.VITE_LOGIN_URL
      return
    } else {
      return next('/error/400.html')
    }
  })
})

router.afterEach(() => {
})
