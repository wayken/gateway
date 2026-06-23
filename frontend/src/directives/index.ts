import focus from './focus'
import permit from './permit'
import clickoutside from './clickoutside'

const directives: any = {
  focus,
  permit,
  clickoutside
}

export default {
  install(app: any) {
    Object.keys(directives).forEach((key) => {
      app.directive(key, directives[key])
    })
  }
}
