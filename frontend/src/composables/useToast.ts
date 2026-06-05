import { ref } from 'vue'

interface Toast {
  id: number
  message: string
  type: 'default' | 'success' | 'error'
}

const toasts = ref<Toast[]>([])
let nextId = 0

export function useToast() {
  function show(message: string, type: Toast['type'] = 'default', duration = 3000) {
    const id = nextId++
    toasts.value.push({ id, message, type })
    setTimeout(() => {
      toasts.value = toasts.value.filter(t => t.id !== id)
    }, duration)
  }
  function success(msg: string) { show(msg, 'success') }
  function error(msg: string) { show(msg, 'error') }
  return { toasts, show, success, error }
}
