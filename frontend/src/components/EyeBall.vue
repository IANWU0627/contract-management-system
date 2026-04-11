<template>
  <div
    ref="eyeRef"
    class="eyeball"
    :style="{
      width: `${size}px`,
      height: isBlinking ? '2px' : `${size}px`,
      backgroundColor: eyeColor,
    }"
  >
    <div
      v-if="!isBlinking"
      class="pupil"
      :style="{
        width: `${pupilSize}px`,
        height: `${pupilSize}px`,
        backgroundColor: pupilColor,
        transform: `translate(${pos.x}px, ${pos.y}px)`,
      }"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = withDefaults(defineProps<{
  size?: number
  pupilSize?: number
  maxDistance?: number
  eyeColor?: string
  pupilColor?: string
  isBlinking?: boolean
  forceLookX?: number
  forceLookY?: number
}>(), {
  size: 48, pupilSize: 16, maxDistance: 10,
  eyeColor: 'white', pupilColor: 'black', isBlinking: false
})

const eyeRef = ref<HTMLElement | null>(null)
const mx = ref(0); const my = ref(0)

const onMove = (e: MouseEvent) => { mx.value = e.clientX; my.value = e.clientY }
onMounted(() => window.addEventListener('mousemove', onMove))
onUnmounted(() => window.removeEventListener('mousemove', onMove))

const pos = computed(() => {
  if (props.forceLookX !== undefined && props.forceLookY !== undefined)
    return { x: props.forceLookX, y: props.forceLookY }
  if (!eyeRef.value) return { x: 0, y: 0 }
  const r = eyeRef.value.getBoundingClientRect()
  const cx = r.left + r.width / 2; const cy = r.top + r.height / 2
  const dx = mx.value - cx; const dy = my.value - cy
  const dist = Math.min(Math.sqrt(dx*dx + dy*dy), props.maxDistance)
  const angle = Math.atan2(dy, dx)
  return { x: Math.cos(angle) * dist, y: Math.sin(angle) * dist }
})
</script>

<style scoped>
.eyeball {
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  overflow: hidden;
  transition: height 0.15s ease-out;
  flex-shrink: 0;
}
.pupil {
  border-radius: 50%;
  transition: transform 0.1s ease-out;
  flex-shrink: 0;
}
</style>
