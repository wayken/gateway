import { defineStore } from 'pinia'
import { AppState } from '@/store/interface'
import Constant from '@/config/constant'

export const useAppStore = defineStore({
  id: 'app',
  state: (): AppState => {
    return {
      theme: 'blue',
      device: Constant.DEVICE.DESKTOP,
      deviceId: ''
    }
  },
  actions: {
    dispatchSetTheme(value: string) {
      this.theme = value
    },
    dispatchSetDevice(value: string) {
      this.device = value
    },
    dispatchSetDeviceId(value: string) {
      this.deviceId = value
    }
  }
})
