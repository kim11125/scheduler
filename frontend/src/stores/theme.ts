import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ThemeKey } from '@/types'

export const useThemeStore = defineStore('theme', () => {
  const current = ref<ThemeKey>('lavender')

  function setTheme(theme: ThemeKey) {
    current.value = theme
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }

  function init() {
    const saved = localStorage.getItem('theme') as ThemeKey | null
    setTheme(saved && ['lavender','peach','mint','dark','rose-milk'].includes(saved) ? saved : 'lavender')
  }

  function cycle() {
    const order: ThemeKey[] = ['lavender', 'peach', 'mint', 'dark', 'rose-milk']
    const next = order[(order.indexOf(current.value) + 1) % order.length]
    setTheme(next)
  }

  return { current, setTheme, init, cycle }
})
