<template>
  <RouterView />
  <!-- Toast container -->
  <div class="toast-container">
    <div v-for="t in toasts" :key="t.id"
      :class="['toast', t.type === 'success' ? 'toast-success' : t.type === 'error' ? 'toast-error' : '']">
      {{ t.message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { useThemeStore } from '@/stores/theme'
import { useToast } from '@/composables/useToast'

const themeStore = useThemeStore()
themeStore.init()

const { toasts } = useToast()
</script>
