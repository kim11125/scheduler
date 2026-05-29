import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ThemeKey } from '@/types'

export const useThemeStore = defineStore('theme', () => {
  const current = ref<ThemeKey>('light')

  function setTheme(theme: ThemeKey) {
    current.value = theme
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }

  function init() {
    const saved = localStorage.getItem('theme') as ThemeKey | null
    setTheme(saved && ['light','dark','orange'].includes(saved) ? saved : 'light')
  }

  function cycle() {
    const order: ThemeKey[] = ['light', 'dark', 'orange']
    const next = order[(order.indexOf(current.value) + 1) % order.length]
    setTheme(next)
  }

  return { current, setTheme, init, cycle }
})
