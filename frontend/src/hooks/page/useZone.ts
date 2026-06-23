/**
 * 根据网域节点判断是否是全局站点
 */
export const isGlobalZone = (name: string | string[]) => {
  return name === 'zone.default'
}
