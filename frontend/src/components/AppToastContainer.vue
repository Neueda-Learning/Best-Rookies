<!-- 此文件是全局提示消息容器组件，用于显示和关闭 toast 通知。 -->
<template>
  <TransitionGroup name="toast" tag="aside" class="toast-stack">
    <article
      v-for="toast in toasts"
      :key="toast.id"
      class="toast"
      :class="`toast--${toast.type}`"
    >
      <div class="toast__body">
        <p class="toast__title">{{ toast.title }}</p>
        <p class="toast__message">{{ toast.message }}</p>
      </div>

      <button type="button" class="toast__close" @click="removeToast(toast.id)">
        {{ t('toastClose') }}
      </button>
    </article>
  </TransitionGroup>
</template>

<script setup>
import { useI18n } from '../composables/useI18n'
import { useToast } from '../composables/useToast'

const { t } = useI18n()
const { toasts, removeToast } = useToast()
</script>