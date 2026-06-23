/**
 * 深度拷贝JSON对象
 *
 * @param {Object} source 要拷贝的对象
 */
export function useDeepClone(source: any) {
  if (null == source) {
    return source
  }
  let newObject: any
  let isArray = false
  if ((source as any).length) {
    newObject = []
    isArray = true
  } else {
    newObject = {}
    isArray = false
  }
  for (const key of Object.keys(source)) {
    if (null == source[key]) {
      if (isArray) {
        newObject.push(null)
      } else {
        newObject[key] = null
      }
    } else {
      const sub = (typeof source[key] == 'object') ? useDeepClone(source[key]) : source[key]
      if (isArray) {
        newObject.push(sub)
      }
      else {
        newObject[key] = sub
      }
    }
  }
  return newObject
}

/**
 * 生成唯一ID
 */
export function uuid() {
  const compose = []
  const hexDigits = '0123456789abcdef'
  for (let i = 0; i < 36; i++) {
    const index = Math.floor(Math.random() * 0x10)
    compose[i] = hexDigits.substring(index, index + 1)
  }
  compose[14] = '4'
  compose[8] = compose[13] = compose[18] = compose[23] = '-'
  return compose.join('')
}

/**
 * 文件弹窗下载
 *
 * @param data 文本数据
 * @param filename 保存的文件名
 */
export const useFileDownload = (data: string, filename?: string) => {
  const blob = new Blob([data], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  if (!filename) {
    filename = new Date().toISOString().replace(/[-:]/g, '').replace('T', '_').split('.')[0]
  }
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
}
