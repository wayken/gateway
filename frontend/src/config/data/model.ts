import {
  ProviderTypeEnum
} from '@/types/index'
import IconSilicon from '@/assets/images/providers/silicon.png'
import IconDeepseek from '@/assets/images/providers/deepseek.png'
import IconOllama from '@/assets/images/providers/ollama.png'
import IconQwenlm from '@/assets/images/providers/qwenlm.png'
import IconOpenai from '@/assets/images/providers/openai.jpeg'
import IconHunyuan from '@/assets/images/providers/hunyuan.png'
import IconDoubao from '@/assets/images/providers/doubao.png'
import IconGemini from '@/assets/images/providers/gemini.png'
import IconLlama from '@/assets/images/providers/llama.png'
import IconBaiduCloud from '@/assets/images/providers/baidu-cloud.svg'
import IconUnknow from '@/assets/images/providers/unknow.png'
import IconBgeModel from '@/assets/images/models/bge.png'

export const loadProviderIconMapping: any = {
  [ProviderTypeEnum.Silicon]: IconSilicon,
  [ProviderTypeEnum.Deepseek]: IconDeepseek,
  [ProviderTypeEnum.Ollama]: IconOllama,
  [ProviderTypeEnum.Qwenlm]: IconQwenlm,
  [ProviderTypeEnum.Openai]: IconOpenai,
  [ProviderTypeEnum.Hunyuan]: IconHunyuan,
  [ProviderTypeEnum.Doubao]: IconDoubao,
  [ProviderTypeEnum.Gemini]: IconGemini,
  [ProviderTypeEnum.Llama]: IconLlama,
  [ProviderTypeEnum.BaiduCloud]: IconBaiduCloud,
  [ProviderTypeEnum.BGE]: IconBgeModel
} as const

export const loadProviderList = [
  {
    type: ProviderTypeEnum.Silicon,
    icon: IconSilicon
  },
  {
    type: ProviderTypeEnum.Deepseek,
    icon: IconDeepseek
  },
  {
    type: ProviderTypeEnum.Ollama,
    icon: IconOllama
  },
  {
    type: ProviderTypeEnum.Qwenlm,
    icon: IconQwenlm
  },
  {
    type: ProviderTypeEnum.Openai,
    icon: IconOpenai
  },
  {
    type: ProviderTypeEnum.Hunyuan,
    icon: IconHunyuan
  },
  {
    type: ProviderTypeEnum.Doubao,
    icon: IconDoubao
  },
  {
    type: ProviderTypeEnum.Gemini,
    icon: IconGemini
  }
]

// 加载模型对应的图标
export function loadProviderIcon(type: string) {
  return loadProviderIconMapping[type] || IconUnknow
}
