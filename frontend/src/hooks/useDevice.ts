import pinia from '@/store/index'
import { useAppStore } from '@/store/modules/app'
import constant from '@/config/constant'

export const {
  device
} = storeToRefs(useAppStore(pinia))

export const isMobile = () => {
  return device.value === constant.DEVICE.MOBILE
}

export const isDesktop = () => {
  return device.value === constant.DEVICE.DESKTOP
}

export const isTablet = () => {
  return device.value === constant.DEVICE.TABLET
}
