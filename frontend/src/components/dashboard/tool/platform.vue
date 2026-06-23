<template>
  <div class="a-tool platform">
    <el-row :gutter="18">
      <el-col :xs="8" :sm="8" :md="8" :lg="8" :xl="6" v-for="(data, key) in loadToolList" :key="key">
      <div class="card" @click="handleClick(data)">
        <div class="image">
          <a-svg-icon :icon-class="data.icon" size="88px"
            :style="{
              color: data.color
            }"
          />
        </div>
        <div class="source">
          <div class="title">
            <div class="name">{{ $t('tool.' + data.name) }}</div>
            <div class="label" v-if="data.develop">
              {{ $t('tool.in-development') }}
            </div>
          </div>
          <div class="description">
            {{ $t('tool.' + data.name + '-description') }}
          </div>
        </div>
      </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
const loadToolList = [
  {
    name: 'route-matcher',
    icon: 'router-minimalistic',
    color: '#30b8ea'
  },
  {
    name: 'http-simulator',
    icon: 'http',
    develop: true,
    color: '#34c724'
  },
  {
    name: 'websocket-simulator',
    icon: 'lightbulb-bolt',
    color: '#9884ff'
  },
  {
    name: 'socketio-simulator',
    icon: 'socketio',
    color: '#1ecdcf'
  },
  {
    name: 'dns-lookup',
    icon: 'globus',
    color: '#e3423e'
  },
  {
    name: 'tcp-ping',
    icon: 'posts-carousel-horizontal',
    color: '#f6821f'
  },
  {
    name: 'udp-ping',
    icon: 'posts-carousel-vertical',
    color: '#3f85ff'
  }
]

const router = useRouter()
const handleClick = (data: any) => {
  if (data.name === 'route-matcher') {
    router.push('/tool/route')
  }
}
</script>
