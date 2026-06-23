import {
  Rule,
  isFieldMultiple,
  isOperatorNumberic,
  isOperatorWithQuotes
} from './rule'

/**
 * 根据规则列表生成表达式字符串
 * 
 * @param ruleList 规则列表
 * @returns 表达式字符串
 */
export function generateExpression(ruleList: any[]): string {
  let expression = ''
  let prevLogical: string | undefined = undefined
  let conditions: string[] = []
  for (let i = 0, len = ruleList.length; i < len; i++) {
    // 先拼接表达式
    const rule = ruleList[i]
    const { field, operator, key, value, logical } = rule
    let ruleExpression = `${field} ${operator}`
    if (isFieldMultiple(rule.field)) {
      ruleExpression = `${field}["${key || ''}"] ${operator}`
    }
    if (isOperatorNumberic(operator)) {
      ruleExpression += ` ${value}`
    } else if (isOperatorWithQuotes(operator)) {
      ruleExpression += `("${value}")`
    } else {
      ruleExpression += ` "${value}"`
    }
    if (len === 1) {
      expression = `(${ruleExpression})`
      break
    }
    conditions.push(ruleExpression)
    // 如果有连续几个规则logical是and，那么自动加上括号
    if (logical === '||') {
      expression += `(${conditions.join(' && ')}) ${i === len - 1 ? '' : ' || '}`
      conditions = []
    } else if (prevLogical === 'and' && logical !== 'and') {
      expression += `(${conditions.join(' && ')})`
      conditions = []
    }
    if (i === len - 1 && conditions.length) {
      expression += `(${conditions.join(' && ')})`
    }
    prevLogical = logical
  }
  return expression.trim()
}

/**
 * 基于表达式字符串解析并生成规则列表
 * 
 * @param expression 表达式字符串
 * @returns 规则列表
 */
export function parseExpression(expression: string): Rule[] {
  const ruleList: Rule[] = []
  // 正则匹配常见的条件模式
  const regex = /\(([^()]+(?:\(.*?\))*)\)\s*(\|\||&&)?/g
  const matchExpression = /(\w+(\.\w+)*)(\[\s*"(.*?)"\s*\])?\s*(.*?)\s*(?:"([^"]*)"|(\d+))/
  let match: RegExpExecArray | null
  while ((match = regex.exec(expression)) !== null) {
    // 括号内的完整条件
    const conditions = match[1].trim()
    // 括号后的逻辑运算符，如 && 或 ||
    const logical = match[2]?.trim()
    // 将括号内的多个条件按 &&/|| 拆分成一个个条件
    const conditionParts = conditions.split(/\s+(&&|\|\|)\s+/)
    for (let i = 0; i < conditionParts.length; i += 2) {
      // 获取条件部分的字段、操作符、值
      const condition = conditionParts[i].trim()
      // 获取 and/or 逻辑符
      const nextLogical = conditionParts[i + 1]?.trim()
      const matchCondition = matchExpression.exec(condition)
      if (!matchCondition) {
        continue
      }
      // 匹配到的字段、操作符、值
      const field = matchCondition[1]
      const key = matchCondition[4]
      let operator = matchCondition[5]
      // 如果操作符包含括号，去掉括号，如.contains(、.startsWith(等
      if (operator.includes('(')) {
        operator = operator.split('(')[0]
      }
      const value = matchCondition[6] || parseFloat(matchCondition[7])
      ruleList.push({
        field,
        key,
        operator,
        value,
        logical: nextLogical || logical || '&&'
      })
    }
  }
  return ruleList
}
