<template>
  <div
    ref="pupilRef"
    class="pupil"
    :style="{
      width: `${size}px`,
      height: `${size}px`,
      backgroundColor: pupilColor,
      transform: `translate(${pos.x}px, ${pos.y}px)`,
    }"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = withDefaults(defineProps<{
  size?: number
  maxDistance?: number
  pupilColor?: string
  forceLookX?: number
  forceLookY?: number
}>(), { size: 12, maxDistance: 5, pupilColor: 'black' })

const pupilRef = ref<HTMLElement | null>(null)
const mx = ref(0); const my = ref(0)

const onMove = (e: MouseEvent) => { mx.value = e.clientX; my.value = e.clientY }
onMounted(() => window.addEventListener('mousemove', onMove))
onUnmounted(() => window.removeEventListener('mousemove', onMove))

const pos = computed(() => {
  if (props.forceLookX !== undefined && props.forceLookY !== undefined)
    return { x: props.forceLookX, y: props.forceLookY }
  if (!pupilRef.value) return { x: 0, y: 0 }
  const r = pupilRef.value.getBoundingClientRect()
  const cx = r.left + r.width / 2; const cy = r.top + r.height / 2
  const dx = mx.value - cx; const dy = my.value - cy
  const dist = Math.min(Math.sqrt(dx*dx + dy*dy), props.maxDistance)
  const angle = Math.atan2(dy, dx)
  return { x: Math.cos(angle) * dist, y: Math.sin(angle) * dist }
})
</script>

<style scoped>
.pupil {
  border-radius: 50%;
  transition: transform 0.1s ease-out;
  flex-shrink: 0;
}
</style>
