<template>
  <div class="characters" ref="containerRef">
    <!-- Purple tall rectangle character - Back layer -->
    <div 
      class="character purple"
      :style="purpleStyle"
    >
      <div class="eyes" :style="purpleEyesStyle">
        <EyeBall 
          :size="22" 
          :pupilSize="8" 
          :maxDistance="6" 
          :isBlinking="isPurpleBlinking"
          :forceLookX="purpleForceLook.x"
          :forceLookY="purpleForceLook.y"
        />
        <EyeBall 
          :size="22" 
          :pupilSize="8" 
          :maxDistance="6" 
          :isBlinking="isPurpleBlinking"
          :forceLookX="purpleForceLook.x"
          :forceLookY="purpleForceLook.y"
        />
      </div>
    </div>

    <!-- Black tall rectangle character - Middle layer -->
    <div 
      class="character black"
      :style="blackStyle"
    >
      <div class="eyes" :style="blackEyesStyle">
        <EyeBall 
          :size="18" 
          :pupilSize="7" 
          :maxDistance="5" 
          :isBlinking="isBlackBlinking"
          :forceLookX="blackForceLook.x"
          :forceLookY="blackForceLook.y"
        />
        <EyeBall 
          :size="18" 
          :pupilSize="7" 
          :maxDistance="5" 
          :isBlinking="isBlackBlinking"
          :forceLookX="blackForceLook.x"
          :forceLookY="blackForceLook.y"
        />
      </div>
    </div>

    <!-- Orange semi-circle character - Front left -->
    <div 
      class="character orange"
      :style="orangeStyle"
    >
      <div class="eyes pupils-only" :style="orangeEyesStyle">
        <Pupil :size="14" :maxDistance="6" :forceLookX="orangeForceLook.x" :forceLookY="orangeForceLook.y" />
        <Pupil :size="14" :maxDistance="6" :forceLookX="orangeForceLook.x" :forceLookY="orangeForceLook.y" />
      </div>
    </div>

    <!-- Yellow tall rectangle character - Front right -->
    <div 
      class="character yellow"
      :style="yellowStyle"
    >
      <div class="eyes pupils-only" :style="yellowEyesStyle">
        <Pupil :size="14" :maxDistance="6" :forceLookX="yellowForceLook.x" :forceLookY="yellowForceLook.y" />
        <Pupil :size="14" :maxDistance="6" :forceLookX="yellowForceLook.x" :forceLookY="yellowForceLook.y" />
      </div>
      <div class="mouth" :style="yellowMouthStyle"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import EyeBall from './EyeBall.vue'
import Pupil from './Pupil.vue'

const props = defineProps<{
  isTyping?: boolean
  showPassword?: boolean
  passwordLength?: number
}>()

const containerRef = ref<HTMLElement | null>(null)
const mouseX = ref(0)
const mouseY = ref(0)
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)

// Track mouse position
onMounted(() => {
  const handleMouseMove = (e: MouseEvent) => {
    mouseX.value = e.clientX
    mouseY.value = e.clientY
  }
  window.addEventListener('mousemove', handleMouseMove)
  
  // Random blinking for purple
  const schedulePurpleBlink = () => {
    setTimeout(() => {
      isPurpleBlinking.value = true
      setTimeout(() => {
        isPurpleBlinking.value = false
        schedulePurpleBlink()
      }, 150)
    }, Math.random() * 4000 + 3000)
  }
  schedulePurpleBlink()
  
  // Random blinking for black
  const scheduleBlackBlink = () => {
    setTimeout(() => {
      isBlackBlinking.value = true
      setTimeout(() => {
        isBlackBlinking.value = false
        scheduleBlackBlink()
      }, 150)
    }, Math.random() * 4000 + 3000)
  }
  scheduleBlackBlink()
})

const isHidingPassword = computed(() => (props.passwordLength || 0) > 0 && !props.showPassword)
const isShowingPassword = computed(() => (props.passwordLength || 0) > 0 && props.showPassword)

// Watch typing state
watch(() => props.isTyping, (newVal) => {
  if (newVal) {
    isLookingAtEachOther.value = true
    setTimeout(() => {
      isLookingAtEachOther.value = false
    }, 800)
  }
})

// Watch password visibility for peeking
watch([() => props.showPassword, () => props.passwordLength], ([show, len]) => {
  if (show && len && len > 0) {
    // Schedule random peeks
    const schedulePeek = () => {
      setTimeout(() => {
        isPurplePeeking.value = true
        setTimeout(() => {
          isPurplePeeking.value = false
        }, 800)
        if (props.showPassword && props.passwordLength && props.passwordLength > 0) {
          schedulePeek()
        }
      }, Math.random() * 3000 + 2000)
    }
    schedulePeek()
  }
})

// Purple character styles
const purpleStyle = computed(() => {
  let height = 400
  let skew = 0
  let translateX = 0
  
  if (isShowingPassword.value) {
    height = 440
  } else if (props.isTyping || isHidingPassword.value) {
    height = 440
    skew = -12
    translateX = 40
  }
  
  return {
    height: `${height}px`,
    transform: `skewX(${skew}deg) translateX(${translateX}px)`
  }
})

const purpleEyesStyle = computed(() => {
  let left = 50
  let top = 50
  
  if (isShowingPassword.value) {
    left = 25
    top = 45
  } else if (isLookingAtEachOther.value) {
    left = 60
    top = 70
  }
  
  return { left: `${left}px`, top: `${top}px` }
})

const purpleForceLook = computed(() => {
  if (isShowingPassword.value) {
    return { x: isPurplePeeking.value ? 5 : -5, y: isPurplePeeking.value ? 6 : -5 }
  }
  if (isLookingAtEachOther.value) {
    return { x: 4, y: 5 }
  }
  return { x: undefined, y: undefined }
})

// Black character styles
const blackStyle = computed(() => {
  let skew = 0
  let translateX = 0
  
  if (isShowingPassword.value) {
    // neutral
  } else if (isLookingAtEachOther.value) {
    skew = 10
    translateX = 20
  } else if (props.isTyping || isHidingPassword.value) {
    // neutral
  }
  
  return {
    transform: `skewX(${skew}deg) translateX(${translateX}px)`
  }
})

const blackEyesStyle = computed(() => {
  let left = 32
  let top = 40
  
  if (isShowingPassword.value) {
    left = 15
    top = 35
  } else if (isLookingAtEachOther.value) {
    left = 38
    top = 18
  }
  
  return { left: `${left}px`, top: `${top}px` }
})

const blackForceLook = computed(() => {
  if (isShowingPassword.value) {
    return { x: -5, y: -5 }
  }
  if (isLookingAtEachOther.value) {
    return { x: 0, y: -5 }
  }
  return { x: undefined, y: undefined }
})

// Orange character styles (semi-circle, front left)
const orangeStyle = computed(() => ({
  // stays mostly neutral
}))

const orangeEyesStyle = computed(() => {
  if (isShowingPassword.value) {
    return { left: '55px', top: '100px' }
  }
  return { left: '90px', top: '100px' }
})

const orangeForceLook = computed(() => {
  if (isShowingPassword.value) {
    return { x: -6, y: -5 }
  }
  return { x: undefined, y: undefined }
})

// Yellow character styles (front right)
const yellowStyle = computed(() => ({
  // stays mostly neutral
}))

const yellowEyesStyle = computed(() => {
  if (isShowingPassword.value) {
    return { left: '25px', top: '50px' }
  }
  return { left: '58px', top: '50px' }
})

const yellowForceLook = computed(() => {
  if (isShowingPassword.value) {
    return { x: -6, y: -5 }
  }
  return { x: undefined, y: undefined }
})

const yellowMouthStyle = computed(() => {
  if (isShowingPassword.value) {
    return { left: '15px', top: '100px' }
  }
  return { left: '45px', top: '100px' }
})
</script>

<style scoped lang="scss">
.characters {
  position: relative;
  width: 450px;
  height: 350px;
  transform: scale(0.85);
  transform-origin: center bottom;
}

.character {
  position: absolute;
  bottom: 0;
  transition: all 0.7s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: bottom center;
  
  .eyes {
    position: absolute;
    display: flex;
    transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
    
    &.pupils-only {
      gap: 40px;
    }
  }
}

// Purple - tall back character
.purple {
  left: 40px;
  width: 180px;
  height: 400px;
  background: linear-gradient(180deg, #7C4DFF 0%, #6C3FF5 50%, #5B2FD5 100%);
  border-radius: 12px 12px 0 0;
  z-index: 1;
  box-shadow: 0 10px 40px rgba(108, 63, 245, 0.3);
  
  .eyes {
    gap: 50px;
  }
}

// Black - medium middle character
.black {
  left: 200px;
  width: 120px;
  height: 310px;
  background: linear-gradient(180deg, #3D3D3D 0%, #2D2D2D 50%, #1D1D1D 100%);
  border-radius: 10px 10px 0 0;
  z-index: 2;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  
  .eyes {
    gap: 30px;
  }
}

// Orange - semi-circle front left
.orange {
  left: 0;
  width: 220px;
  height: 200px;
  background: linear-gradient(180deg, #FFB088 0%, #FF9B6B 50%, #E8855B 100%);
  border-radius: 110px 110px 0 0;
  z-index: 3;
  box-shadow: 0 10px 30px rgba(255, 155, 107, 0.4);
  
  .eyes {
    gap: 40px;
  }
}

// Yellow - front right character
.yellow {
  left: 280px;
  width: 140px;
  height: 250px;
  background: linear-gradient(180deg, #F5E663 0%, #E8D754 50%, #D4C344 100%);
  border-radius: 70px 70px 0 0;
  z-index: 4;
  box-shadow: 0 10px 30px rgba(232, 215, 84, 0.4);
  
  .eyes {
    gap: 30px;
  }
  
  .mouth {
    position: absolute;
    width: 70px;
    height: 5px;
    background-color: #2D2D2D;
    border-radius: 3px;
    transition: all 0.3s ease-out;
  }
}
</style>
