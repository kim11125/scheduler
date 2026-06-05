<template>
  <div class="theme-picker">
    <button
      v-for="t in themes"
      :key="t.key"
      :class="['theme-opt', { active: themeStore.current === t.key }]"
      :title="t.label"
      @click="themeStore.setTheme(t.key)"
    >
      <span class="theme-swatch" :style="{ background: t.color }">
        <AppIcon v-if="themeStore.current === t.key" name="check" size="sm" style="color:#fff" />
      </span>
      <span class="theme-opt-label">{{ t.label }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { useThemeStore } from '@/stores/theme'
import type { ThemeKey } from '@/types'
import AppIcon from './AppIcon.vue'

const themeStore = useThemeStore()

const themes: { key: ThemeKey; color: string; label: string }[] = [
  { key: 'lavender',  color: '#8B7FD4', label: '라벤더' },
  { key: 'peach',     color: '#E8836A', label: '피치' },
  { key: 'mint',      color: '#4DB896', label: '민트' },
  { key: 'dark',      color: '#3B3F52', label: '다크' },
  { key: 'rose-milk', color: '#D4789A', label: '로즈' },
]
</script>

<style scoped>
.theme-picker { display: flex; gap: 12px; flex-wrap: wrap; }
.theme-opt { display: flex; flex-direction: column; align-items: center; gap: 5px; cursor: pointer; background: none; border: none; padding: 0; }
.theme-swatch {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.15s, box-shadow 0.15s;
}
.theme-opt.active .theme-swatch { box-shadow: 0 0 0 2.5px var(--color-text-1, #1A1A2E); transform: scale(1.1); }
.theme-opt-label { font-size: 11px; font-weight: 500; color: var(--color-text-2, #6B6B80); }
</style>
