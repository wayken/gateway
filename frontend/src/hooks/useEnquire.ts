import enquireJs from 'enquire.js'
import Constant from '@/config/constant'

export default function useDevice(callback: (...args: any[]) => any) {
  // 匹配电脑视图
  const matchDesktop = {
    match: () => {
      callback && callback(Constant.DEVICE.DESKTOP)
    }
  }
  // 匹配平板视图
  const matchLablet = {
    match: () => {
      callback && callback(Constant.DEVICE.TABLET)
    }
  }
  // 匹配手机视图
  const matchMobile = {
    match: () => {
      callback && callback(Constant.DEVICE.MOBILE)
    }
  }
  enquireJs
    .register('screen and (max-width: 576px)', matchMobile)
    .register('screen and (min-width: 576px) and (max-width: 1199px)', matchLablet)
    .register('screen and (min-width: 1200px)', matchDesktop)
}
