import session from './modules/session'
import audit from './modules/audit'
import zone from './modules/zone'
import service from './modules/service'
import database from './modules/database'
import ip from './modules/ip'
import certificate from './modules/certificate'
import auth from './modules/auth'
import provider from './modules/provider'
import plugin from './modules/plugin'

const request: {
  [key: string]: any
} = {
  session,
  audit,
  zone,
  service,
  database,
  ip,
  certificate,
  auth,
  provider,
  plugin
}
export default request
