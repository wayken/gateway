import { defineStore } from 'pinia'
import { MenuState } from '@/store/interface'

export const useMenuStore = defineStore({
  id: 'menu',
  state: (): MenuState => {
    return {
      isMenuCollapsed: false
    }
  },
  actions: {
    dispatchSetMenuCollapsed(value: boolean) {
      this.isMenuCollapsed = value
    }
  }
})
