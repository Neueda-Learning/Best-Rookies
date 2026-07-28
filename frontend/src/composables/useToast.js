// 此文件是 toast 组合式函数，用于集中管理提示消息的状态和操作方法。
import { reactive } from 'vue'

const state = reactive({
  toasts: []
})

let nextToastId = 1

export function useToast() {
  function removeToast(id) {
    const index = state.toasts.findIndex((toast) => toast.id === id)

    if (index >= 0) {
      state.toasts.splice(index, 1)
    }
  }

  function pushToast(type, title, message, duration = 4000) {
    const id = nextToastId
    nextToastId += 1

    state.toasts.push({ id, type, title, message })
    window.setTimeout(() => removeToast(id), duration)
  }

  return {
    toasts: state.toasts,
    removeToast,
    success(title, message, duration) {
      pushToast('success', title, message, duration)
    },
    error(title, message, duration) {
      pushToast('error', title, message, duration)
    },
    info(title, message, duration) {
      pushToast('info', title, message, duration)
    }
  }
}