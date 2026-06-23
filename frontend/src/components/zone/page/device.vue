<template>
  <div class="a-zone-page device inline-flex-c-n-n">
    <div class="card inline-flex-c-n-n" v-for="(data, index) in loadPageList" :key="index">
      <div class="title inline-flex-r-c-n">
        <el-icon :style="{ color: data.color }">
          <component :is="data.icon"></component>
        </el-icon>
        <div class="name">{{ $t('page.page-' + data.name) }}</div>
      </div>
      <div class="description">
        {{ $t('page.page-' + data.name + '-description') }}
      </div>
      <div class="metadata">
        <a-button :icon="Promotion">
          {{ $t('page.custom-page') }}
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Lock,
  Avatar,
  Refresh,
  Document,
  Position,
  Promotion,
  Notification,
  WarningFilled,
  WarnTriangleFilled
} from '@element-plus/icons-vue'

const loadPageList = [
  {
    name: 'waf',
    icon: markRaw(Lock),
    color: '#ffc107'
  },
  {
    name: '500',
    icon: markRaw(WarningFilled),
    color: '#fc587b'
  },
  {
    name: '404',
    icon: markRaw(Position),
    color: '#ff8801'
  },
  {
    name: 'interactive-challenge',
    icon: markRaw(Avatar),
    color: '#00b3db'
  },
  {
    name: 'js-challenge',
    icon: markRaw(Refresh),
    color: '#5660dc'
  },
  {
    name: '429',
    icon: markRaw(Notification),
    color: '#0fc6c2'
  },
  {
    name: 'maintenance',
    icon: markRaw(Document),
    color: '#fc587b'
  },
  {
    name: 'gateway-timeout',
    icon: markRaw(WarnTriangleFilled),
    color: '#f44336'
  }
]
</script>
