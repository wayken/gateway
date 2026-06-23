/**
 * 判断是否为邮箱格式
 */
export function isEmail(value: string) {
  const pattern = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/
  return pattern.test(value)
}

/**
 * 判断是否为手机格式
 */
export function isMobile(value: string) {
  const pattern = /^1\d{10}$/
  return pattern.test(value)
}

/**
 * 判断是否为域名格式，如xxx.com/xxx.com.cn/www.xxx.com/*.xxx.com
 */
export function isDomain(value: string) {
  const pattern = /^((\*\.)?[\w-]+(\.[\w-]+)+)$/
  return pattern.test(value)
}

/**
 * 判断是否为IP格式，包括IPv4和IPv6，同时支持掩码CIDR格式
 */
export function isIpCidr(value: string) {
  const ipv4CIDRRegex = /^(([0-9]{1,3}\.){3}[0-9]{1,3})(\/([0-9]|[1-2][0-9]|3[0-2]))?$/
  const ipv6CIDRRegex = /^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|::|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4})(\/([0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8]))?$/
  return ipv4CIDRRegex.test(value) || ipv6CIDRRegex.test(value)
}

/**
 * 判断是否为IP格式，包括IPv4和IPv6，不支持掩码CIDR格式
 */
export function isIp(value: string) {
  const ipv4Regex = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/
  const ipv6Regex = /^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$/
  return ipv4Regex.test(value) || ipv6Regex.test(value)
}
