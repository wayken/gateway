<template>
  <div class="a-zone-protection-semantics device inline-flex-c-n-n">
    <div class="card" v-for="(data, index) in loadSemanticsList" :key="index">
      <div class="source inline-flex-r-c-n">
        <el-icon :style="{ color: data.color }">
          <component :is="data.icon"></component>
        </el-icon>
        <div class="name">{{ $t('protection.semantics.' + data.name) }}</div>
      </div>
      <div class="matched inline-flex-r-c-n">
        <div class="title">{{ $t('protection.semantics.matched-route') }}</div>
        <div class="value">
          {{ $t('protection.semantics.matched-global') }}
        </div>
      </div>
      <div class="status inline-flex-r-c-n">
        <div class="title">{{ $t('extension.status') }}</div>
        <div class="value inline-flex-r-c-n">
          <el-icon><Check /></el-icon>
          <div class="name">{{ $t('extension.enable') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Check,
  DocumentCopy,
  Reading,
  DocumentChecked
} from '@element-plus/icons-vue'

const loadSemanticsList = [
  {
    name: 'sql-injection',
    icon: markRaw(DocumentChecked),
    color: '#22d7bb'
  },
  {
    name: 'xss-injection',
    icon: markRaw(Reading),
    color: '#fc587b'
  },
  {
    name: 'cmd-injection',
    icon: markRaw(DocumentCopy),
    color: '#ff8801'
  }
]
</script>
