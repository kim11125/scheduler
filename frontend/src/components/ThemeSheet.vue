<template>
  <Teleport to="body">
    <div class="overlay" v-if="open" @click.self="emit('close')">
      <div class="sheet" style="max-height:280px">
        <div class="sheet-handle"></div>
        <div class="sheet-header">
          <span class="sheet-title">테마 선택</span>
          <button class="sheet-close" @click="emit('close')">
            <AppIcon name="close" size="sm" />
          </button>
        </div>
        <div class="sheet-body" style="padding:16px 20px 24px">
          <ThemePicker />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import AppIcon from './AppIcon.vue'
import ThemePicker from './ThemePicker.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits<{ close: [] }>()
</script>
