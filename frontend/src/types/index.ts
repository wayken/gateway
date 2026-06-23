export const ProviderTypeEnum = {
  Silicon: 'silicon',
  Deepseek: 'deepseek',
  Ollama: 'ollama',
  Qwenlm: 'qwenlm',
  Openai: 'openai',
  Hunyuan: 'hunyuan',
  Doubao: 'doubao',
  Gemini: 'gemini',
  Llama: 'llama',
  BaiduCloud: 'baiducloud',
  BGE: 'bge'
} as const

export type ProviderType = (typeof ProviderTypeEnum)[keyof typeof ProviderTypeEnum]
