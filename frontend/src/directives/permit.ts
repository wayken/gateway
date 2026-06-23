import {
  DirectiveBinding,
  ObjectDirective
} from 'vue'
import { useAuthStore } from '@/store/modules/auth'

const Permit: ObjectDirective = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const account = useAuthStore().account
    const admin = account.admin || false
    const permissions = account.permissions || []
    if (value && value instanceof Array && value.length > 0) {
      const permissionFlag = value

      const hasPermissions = admin || permissions.some(permission => {
        return permissionFlag.includes(permission)
      })

      if (!hasPermissions) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      console.warn('v-permit directive requires an array of permissions as value.')
    }
  }
}

export default Permit
