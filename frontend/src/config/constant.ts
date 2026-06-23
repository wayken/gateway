/**
 * 全局存储变量名称
 */
const LOCAL_LANG = 'Locale-Lang'
const LOCAL_APPERANCE = 'Locale-Appearance'
const LOCAL_THEME = 'Locale-Theme'
const X_Auth_Token = 'X-Auth-Token'

export default {
  LOCAL_LANG,
  LOCAL_APPERANCE,
  LOCAL_THEME,
  X_Auth_Token,
  DEVICE: {
    DESKTOP: 'desktop',
    TABLET: 'tablet',
    MOBILE: 'mobile'
  },
  RESP_CODE: {
    NEED_LOGIN: 1004001
  },
  WAF_ACTION_TYPE: {
    BLOCK: 'block',
    LOG: 'log',
    COOKIE_CHALLENGE: 'cookie_challenge',
    JS_CHALLENGE: 'js_challenge',
    INTERACTIVE_CHALLENGE: 'interactive_challenge',
    SKIP: 'skip'
  }
}
