<template>
  <div class="a-coupon"
    :class="[
      used ? 'is-used' : '',
      'theme-' + theme
    ]"
    @click="handleClick"
  >
    <div class="context">
      <div class="name">{{ name }}</div>
      <div class="price">
        <span class="name">{{ $t('components.coupon.price-unit') }}</span>
        <span class="value">{{ price }}</span>
      </div>
    </div>
    <div class="metadata">
      <template v-if="!used">
        <div class="infomation">
          <div class="date">
            {{ $t('components.coupon.expire-at') }}：{{ getExpireDate(date) }}
          </div>
          <div class="amount">
            {{ amount }} {{ $t('components.coupon.amount-unit') }}
          </div>
        </div>
        <div class="operation">
          {{ $t('components.coupon.use') }}
        </div>
      </template>
      <div class="infomation" v-else>
        <div class="date">
          {{ $t('components.coupon.used') }}
        </div>
        <div class="amount">
          {{ amount }} {{$t('components.coupon.amount-unit') }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'ACoupon'
})

const props = defineProps({
  name: String,
  price: Number,
  theme: {
    type: Number,
    default: 0
  },
  date: Number,
  amount: Number,
  used: {
    type: Boolean,
    default: false
  }
})

const i18n = useI18n()
const getExpireDate = (timestamp: number | undefined) => {
  if (!timestamp) {
    return '-'
  }
  if (timestamp < 0) {
    return i18n.t('components.coupon.long-term')
  }
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = ('0' + (date.getMonth() + 1)).slice(-2)
  const day = ('0' + date.getDate()).slice(-2)
  return year + '-' + month + '-' + day
}
const handleEmit = defineEmits(['click'])
const handleClick = () => {
  if (!props.used) {
    handleEmit('click')
  }
}
</script>
