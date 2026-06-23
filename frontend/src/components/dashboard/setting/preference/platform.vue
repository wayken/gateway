<template>
  <div class="a-setting-preference platform">
    <div class="card">
      <div class="field">
        <div class="label">{{ $t('setting.theme') }}</div>
        <div class="value theme">
          <div :class="key" v-for="(key) in Object.keys(themeList)" :key="key"
            @click="handleThemeSwitch(key)"
          >
            <el-icon :size="20" v-if="loadCurrentTheme == key">
              <Check />
            </el-icon>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Check
} from '@element-plus/icons-vue'
import Mitter from '@/utils/mitt'
import Constant from '@/config/constant'

const loadCurrentTheme = ref(localStorage.getItem(Constant.LOCAL_THEME) || 'blue')

// 主题配置列表
const themeList: any = {
  'blue': {
    '--var-theme-primary-1': 'rgba(64, 158, 255, 0.1)',
    '--var-theme-primary-2': 'rgba(64, 158, 255, 0.5)',
    '--var-theme-primary-3': 'rgba(64, 158, 255, 0.8)',
    '--var-theme-primary-4': '#409eff',
    '--var-theme-primary-5': '#2563eb',
    '--var-theme-primary-6': '#1d4ed8',
    '--var-theme-primary-7': '#1e40af',
    '--var-theme-primary-8': '#1e3a8a'
  },
  'emerald': {
    '--var-theme-primary-1': 'rgba(6, 185, 129, 0.1)',
    '--var-theme-primary-2': 'rgba(6, 185, 129, 0.5)',
    '--var-theme-primary-3': 'rgba(6, 185, 129, 0.8)',
    '--var-theme-primary-4': '#10b981',
    '--var-theme-primary-5': '#059669',
    '--var-theme-primary-6': '#047857',
    '--var-theme-primary-7': '#065F46',
    '--var-theme-primary-8': '#064E3B'
  },
  'rose': {
    '--var-theme-primary-1': 'rgba(255, 29, 72, 0.1)',
    '--var-theme-primary-2': 'rgba(255, 29, 72, 0.5)',
    '--var-theme-primary-3': 'rgba(255, 29, 72, 0.8)',
    '--var-theme-primary-4': '#F43F5E',
    '--var-theme-primary-5': '#E11D48',
    '--var-theme-primary-6': '#BE123C',
    '--var-theme-primary-7': '#9F1239',
    '--var-theme-primary-8': '#881337'
  },
  'orange': {
    '--var-theme-primary-1': 'rgba(234, 88, 12, 0.1)',
    '--var-theme-primary-2': 'rgba(234, 88, 12, 0.5)',
    '--var-theme-primary-3': 'rgba(234, 88, 12, 0.8)',
    '--var-theme-primary-4': '#f97316',
    '--var-theme-primary-5': '#EA580C',
    '--var-theme-primary-6': '#C2410C',
    '--var-theme-primary-7': '#9A3412',
    '--var-theme-primary-8': '#7C2D12'
  }
}

// 变换主题
const handleThemeSwitch = (payload: any) => {
  Mitter.emit('mitt-theme-switch', payload)
  loadCurrentTheme.value = payload
}
</script>
