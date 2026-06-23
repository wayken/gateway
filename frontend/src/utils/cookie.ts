// 获取指定 Cookie 的值
export function getCookie(cookieName: string) {
  const allCookies: string = document.cookie
  const cookiesArray: string[] = allCookies.split(';')
  const cookies: { [key: string]: string } = {}
  for (const cookie of cookiesArray) {
    const [name, value] = cookie.trim().split('=')
    cookies[name] = value
  }
  return cookies[cookieName]
}

/**
 * 写入浏览器cookie信息
 *
 * @param {String} key   Cookie键
 * @param {String} value Cookie值
 * @param {String} value Cookie域名
 */
export function setCookie(key: string, value: string, domain: string) {
  const expires = new Date()
  expires.setTime(expires.getTime() + 24 * 60 * 60 * 1000)
  document.cookie = `${key}=${value};expires=${expires.toUTCString()};domain=${domain};path=/`
}


/**
 * 删除浏览器cookie信息
 * 
 * @param {String} key   Cookie键
 * @param {String} value Cookie域名
 */
export function removeCookie(key: string, domain: string) {
  document.cookie = `${key}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;domain=${domain};path=/`
}
