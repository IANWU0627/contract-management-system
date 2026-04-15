<template>
  <div class="login-page">

    <!-- ===== LEFT: Characters Panel ===== -->
    <div class="left-panel">
      <!-- Language Switcher -->
      <div class="lang-switcher" role="navigation" aria-label="Language switch">
        <button 
          class="lang-btn" 
          :class="{ active: locale === 'zh' }"
          @click="switchLang('zh')"
          aria-label="Switch to Chinese"
        >
          中
        </button>
        <button 
          class="lang-btn" 
          :class="{ active: locale === 'en' }"
          @click="switchLang('en')"
          aria-label="Switch to English"
        >
          EN
        </button>
      </div>

      <!-- Logo -->
      <div class="brand">
        <div class="brand-icon">
          <el-icon :size="16"><Collection /></el-icon>
        </div>
        <span>Toy Contract</span>
      </div>

      <!-- Animated Characters Stage -->
      <div class="stage">
        <div class="characters-wrap">

          <!-- Purple tall rectangle — back layer -->
          <div
            ref="purpleRef"
            class="char char-purple"
            :style="{
              height: (isTyping || isHidingPwd) ? '440px' : '400px',
              transform: isShowingPwd
                ? 'skewX(0deg)'
                : (isTyping || isHidingPwd)
                  ? `skewX(${purplePos.bodySkew - 12}deg) translateX(40px)`
                  : `skewX(${purplePos.bodySkew}deg)`
            }"
          >
            <div
              class="eyes"
              :style="{
                left: isShowingPwd ? '20px' : isLookingAtEachOther ? '55px' : `${45 + purplePos.faceX}px`,
                top:  isShowingPwd ? '35px' : isLookingAtEachOther ? '65px' : `${40 + purplePos.faceY}px`,
                gap: '32px'
              }"
            >
              <EyeBall :size="18" :pupilSize="7" :maxDistance="5"
                eyeColor="white" pupilColor="#2D2D2D"
                :isBlinking="isPurpleBlinking"
                :forceLookX="isShowingPwd ? (isPurplePeeking ? 4 : -4) : isLookingAtEachOther ? 3 : undefined"
                :forceLookY="isShowingPwd ? (isPurplePeeking ? 5 : -4) : isLookingAtEachOther ? 4 : undefined"
              />
              <EyeBall :size="18" :pupilSize="7" :maxDistance="5"
                eyeColor="white" pupilColor="#2D2D2D"
                :isBlinking="isPurpleBlinking"
                :forceLookX="isShowingPwd ? (isPurplePeeking ? 4 : -4) : isLookingAtEachOther ? 3 : undefined"
                :forceLookY="isShowingPwd ? (isPurplePeeking ? 5 : -4) : isLookingAtEachOther ? 4 : undefined"
              />
            </div>
          </div>

          <!-- Black tall rectangle — middle layer -->
          <div
            ref="blackRef"
            class="char char-black"
            :style="{
              transform: isShowingPwd
                ? 'skewX(0deg)'
                : isLookingAtEachOther
                  ? `skewX(${blackPos.bodySkew * 1.5 + 10}deg) translateX(20px)`
                  : (isTyping || isHidingPwd)
                    ? `skewX(${blackPos.bodySkew * 1.5}deg)`
                    : `skewX(${blackPos.bodySkew}deg)`
            }"
          >
            <div
              class="eyes"
              :style="{
                left: isShowingPwd ? '10px' : isLookingAtEachOther ? '32px' : `${26 + blackPos.faceX}px`,
                top:  isShowingPwd ? '28px' : isLookingAtEachOther ? '12px' : `${32 + blackPos.faceY}px`,
                gap: '24px'
              }"
            >
              <EyeBall :size="16" :pupilSize="6" :maxDistance="4"
                eyeColor="white" pupilColor="#2D2D2D"
                :isBlinking="isBlackBlinking"
                :forceLookX="isShowingPwd ? -4 : isLookingAtEachOther ? 0 : undefined"
                :forceLookY="isShowingPwd ? -4 : isLookingAtEachOther ? -4 : undefined"
              />
              <EyeBall :size="16" :pupilSize="6" :maxDistance="4"
                eyeColor="white" pupilColor="#2D2D2D"
                :isBlinking="isBlackBlinking"
                :forceLookX="isShowingPwd ? -4 : isLookingAtEachOther ? 0 : undefined"
                :forceLookY="isShowingPwd ? -4 : isLookingAtEachOther ? -4 : undefined"
              />
            </div>
          </div>

          <!-- Orange semi-circle — front left -->
          <div
            ref="orangeRef"
            class="char char-orange"
            :style="{
              transform: isShowingPwd ? 'skewX(0deg)' : `skewX(${orangePos.bodySkew}deg)`
            }"
          >
            <div
              class="eyes"
              :style="{
                left: isShowingPwd ? '50px' : `${82 + orangePos.faceX}px`,
                top:  isShowingPwd ? '85px' : `${90 + orangePos.faceY}px`,
                gap: '32px'
              }"
            >
              <Pupil :size="12" :maxDistance="5" pupilColor="#2D2D2D"
                :forceLookX="isShowingPwd ? -5 : undefined"
                :forceLookY="isShowingPwd ? -4 : undefined"
              />
              <Pupil :size="12" :maxDistance="5" pupilColor="#2D2D2D"
                :forceLookX="isShowingPwd ? -5 : undefined"
                :forceLookY="isShowingPwd ? -4 : undefined"
              />
            </div>
          </div>

          <!-- Yellow rounded rectangle — front right -->
          <div
            ref="yellowRef"
            class="char char-yellow"
            :style="{
              transform: isShowingPwd ? 'skewX(0deg)' : `skewX(${yellowPos.bodySkew}deg)`
            }"
          >
            <div
              class="eyes"
              :style="{
                left: isShowingPwd ? '20px' : `${52 + yellowPos.faceX}px`,
                top:  isShowingPwd ? '35px' : `${40 + yellowPos.faceY}px`,
                gap: '24px'
              }"
            >
              <Pupil :size="12" :maxDistance="5" pupilColor="#2D2D2D"
                :forceLookX="isShowingPwd ? -5 : undefined"
                :forceLookY="isShowingPwd ? -4 : undefined"
              />
              <Pupil :size="12" :maxDistance="5" pupilColor="#2D2D2D"
                :forceLookX="isShowingPwd ? -5 : undefined"
                :forceLookY="isShowingPwd ? -4 : undefined"
              />
            </div>
            <!-- Mouth -->
            <div
              class="mouth"
              :style="{
                left: isShowingPwd ? '10px' : `${40 + yellowPos.faceX}px`,
                top:  isShowingPwd ? '88px' : `${88 + yellowPos.faceY}px`
              }"
            />
          </div>

        </div>
      </div>

      <!-- Footer links -->
      <div class="left-footer">
        <a href="#" @click.prevent="handlePrivacy">{{ t('auth.privacyPolicy') }}</a>
        <a href="#" @click.prevent="handleTerms">{{ t('auth.termsOfService') }}</a>
        <a href="#" @click.prevent="handleContact">{{ t('auth.contactUs') }}</a>
      </div>

      <!-- Decorative blobs -->
      <div class="blob blob-1" />
      <div class="blob blob-2" />
      <div class="grid-overlay" />
    </div>

    <!-- ===== RIGHT: Login Form ===== -->
    <div class="right-panel">
      <div class="form-box">
        <!-- Language Switcher -->
        <div class="lang-switcher-mobile" role="navigation" aria-label="Language switch">
          <button 
            class="lang-btn" 
            :class="{ active: locale === 'zh' }"
            @click="switchLang('zh')"
            aria-label="Switch to Chinese"
          >
            中
          </button>
          <button 
            class="lang-btn" 
            :class="{ active: locale === 'en' }"
            @click="switchLang('en')"
            aria-label="Switch to English"
          >
            EN
          </button>
        </div>

        <!-- Mobile brand -->
        <div class="mobile-brand">
          <div class="brand-icon">
            <el-icon :size="16"><Collection /></el-icon>
          </div>
          <span>Toy Contract</span>
        </div>

        <!-- Header -->
        <div class="form-header">
          <h1>{{ t('auth.welcomeBack') }}</h1>
          <p>{{ t('auth.loginInfo') }}</p>
        </div>

        <!-- Form -->
        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          class="the-form"
          @keyup.enter="handleLogin"
        >

          <el-form-item prop="username">
            <label class="field-label">{{ t('auth.username') }}</label>
            <el-input
              v-model="form.username"
              :placeholder="t('auth.username')"
              size="large"
              autocomplete="username"
              class="the-input"
              @focus="isTyping = true"
              @blur="isTyping = false"
              ref="usernameInput"
              aria-label="Username"
            />
          </el-form-item>

          <el-form-item prop="password" class="password-field">
            <label class="field-label">{{ t('auth.password') }}</label>
            <div class="password-input-wrapper">
              <el-input
                ref="passwordInput"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                :placeholder="t('auth.password')"
                size="large"
                class="the-input"
                @focus="isTyping = true"
                @blur="isTyping = false"
                aria-label="Password"
              />
              <span 
                class="eye-toggle" 
                @click="showPassword = !showPassword"
                role="button"
                :aria-label="showPassword ? 'Hide password' : 'Show password'"
                tabindex="0"
                @keydown.enter.prevent="showPassword = !showPassword"
                @keydown.space.prevent="showPassword = !showPassword"
              >
                <el-icon v-if="!showPassword" :size="18"><View /></el-icon>
                <el-icon v-else :size="18"><Hide /></el-icon>
              </span>
            </div>
          </el-form-item>

          <div class="row-options">
            <label class="remember-label">
              <input 
                type="checkbox" 
                v-model="form.remember" 
                class="remember-check"
                aria-label="Remember me for 30 days"
              />
              <span>{{ t('auth.rememberDays') }}</span>
            </label>
            <a 
              class="forgot-link" 
              @click="handleForgot"
              role="button"
              tabindex="0"
              @keydown.enter="handleForgot"
            >
              {{ t('auth.forgotPassword') }}
            </a>
          </div>

          <button
            type="button"
            class="btn-login"
            :class="{ loading }"
            :disabled="loading"
            @click="handleLogin"

          >
            <span v-if="!loading">{{ t('auth.login') }}</span>
            <span v-else class="spinner-wrap">
              <el-icon class="spinner" :size="16"><Loading /></el-icon>
              {{ t('auth.loggingIn') }}
            </span>
          </button>

        </el-form>

        <!-- Divider -->
        <div class="divider"><span>{{ t('auth.or') }}</span></div>

        <!-- Social Login Buttons -->
        <div class="social-login">
          <button type="button" class="btn-social btn-google" @click="handleGoogleLogin" aria-label="Sign in with Google">
            <el-icon :size="18"><ChromeFilled /></el-icon>
            <span class="social-text">{{ t('auth.googleLogin') }}</span>
          </button>

          <button type="button" class="btn-social btn-wecom" @click="handleWecomLogin" aria-label="Sign in with WeCom">
            <el-icon :size="18"><OfficeBuilding /></el-icon>
            <span class="social-text">{{ t('auth.wecomLogin') }}</span>
          </button>

          <button type="button" class="btn-social btn-dingtalk" @click="handleDingtalkLogin" aria-label="Sign in with DingTalk">
            <el-icon :size="18"><Promotion /></el-icon>
            <span class="social-text">{{ t('auth.dingtalkLogin') }}</span>
          </button>

          <button type="button" class="btn-social btn-feishu" @click="handleFeishuLogin" aria-label="Sign in with Feishu">
            <el-icon :size="18"><Connection /></el-icon>
            <span class="social-text">{{ t('auth.feishuLogin') }}</span>
          </button>
        </div>

        <!-- Sign up -->
        <p class="signup-hint">
          {{ t('auth.noAccount') }} <a @click="handleRegister" role="button" tabindex="0">{{ t('auth.registerNow') }}</a>
        </p>

      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import EyeBall from '@/components/EyeBall.vue'
import Pupil from '@/components/Pupil.vue'
import { Collection, View, Hide, Loading, ChromeFilled, OfficeBuilding, Promotion, Connection } from '@element-plus/icons-vue'

const { t, locale } = useI18n()

const router = useRouter()
const userStore = useUserStore()

// ── Form ──────────────────────────────────────────────
const formRef    = ref<FormInstance>()
const loading    = ref(false)
const showPassword = ref(false)
const isTyping   = ref(false)
const errorMsg   = ref('')
const usernameInput = ref<HTMLElement | null>(null)

// Load remembered credentials
const savedUsername = localStorage.getItem('rememberedUsername') || ''
const savedRemember = localStorage.getItem('rememberMe') === 'true'

const form = reactive({ 
  username: savedUsername, 
  password: '', 
  remember: savedRemember 
})

const rules = {
  username: [{ required: true, message: t('auth.error.username'), trigger: 'blur' }],
  password: [{ required: true, message: t('auth.error.password'),   trigger: 'blur' },
             { min: 6,         message: t('auth.error.passwordMin'),   trigger: 'blur' }]
}

// ── Language Switch ───────────────────────────────────
function switchLang(lang: 'zh' | 'en') {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

// ── Character refs ────────────────────────────────────
const purpleRef = ref<HTMLElement | null>(null)
const blackRef  = ref<HTMLElement | null>(null)
const yellowRef = ref<HTMLElement | null>(null)
const orangeRef = ref<HTMLElement | null>(null)

const mouseX = ref(0)
const mouseY = ref(0)

const isPurpleBlinking    = ref(false)
const isBlackBlinking     = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking     = ref(false)

// ── Computed helpers ─────────────────────────────────
const isHidingPwd  = computed(() => form.password.length > 0 && !showPassword.value)
const isShowingPwd = computed(() => form.password.length > 0 &&  showPassword.value)

function calcPos(el: HTMLElement | null) {
  if (!el) return { faceX: 0, faceY: 0, bodySkew: 0 }
  const r = el.getBoundingClientRect()
  const cx = r.left + r.width / 2
  const cy = r.top  + r.height / 3
  const dx = mouseX.value - cx
  const dy = mouseY.value - cy
  return {
    faceX:    Math.max(-15, Math.min(15, dx / 20)),
    faceY:    Math.max(-10, Math.min(10, dy / 30)),
    bodySkew: Math.max(-6,  Math.min(6, -dx / 120))
  }
}

const purplePos = computed(() => calcPos(purpleRef.value))
const blackPos  = computed(() => calcPos(blackRef.value))
const yellowPos = computed(() => calcPos(yellowRef.value))
const orangePos = computed(() => calcPos(orangeRef.value))

// ── Lifecycle ─────────────────────────────────────────
onMounted(() => {
  // Add keyboard listener for Enter key
  document.addEventListener('keydown', handleKeydown)
  window.addEventListener('mousemove', e => { mouseX.value = e.clientX; mouseY.value = e.clientY })

  // Auto-focus on mobile
  const isMobile = window.innerWidth <= 1024
  if (isMobile) {
    setTimeout(() => {
      usernameInput.value?.focus()
    }, 300)
  }

  // Random blink — purple
  const blinkPurple = () => setTimeout(() => {
    isPurpleBlinking.value = true
    setTimeout(() => { isPurpleBlinking.value = false; blinkPurple() }, 150)
  }, Math.random() * 4000 + 3000)
  blinkPurple()

  // Random blink — black
  const blinkBlack = () => setTimeout(() => {
    isBlackBlinking.value = true
    setTimeout(() => { isBlackBlinking.value = false; blinkBlack() }, 150)
  }, Math.random() * 4000 + 3000)
  blinkBlack()
})

// Look at each other when typing starts
watch(isTyping, v => {
  if (v) {
    isLookingAtEachOther.value = true
    setTimeout(() => { isLookingAtEachOther.value = false }, 800)
  } else {
    isLookingAtEachOther.value = false
  }
})

// Purple sneaky peek when password is visible
watch([isShowingPwd, isPurplePeeking], ([showing]) => {
  if (showing) {
    setTimeout(() => {
      isPurplePeeking.value = true
      setTimeout(() => { isPurplePeeking.value = false }, 800)
    }, Math.random() * 3000 + 2000)
  } else {
    isPurplePeeking.value = false
  }
})

// ── Keyboard Handler ──────────────────────────────────
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !loading.value) {
    const activeEl = document.activeElement
    if (activeEl?.tagName === 'INPUT') {
      if ((activeEl as HTMLInputElement).type === 'password') {
        handleLogin()
      }
    }
  }
}

// ── Handlers ──────────────────────────────────────────
const handleLogin = async () => {
  if (!formRef.value) return
  errorMsg.value = ''
  await formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    
    // Save remember state
    if (form.remember) {
      localStorage.setItem('rememberedUsername', form.username)
      localStorage.setItem('rememberMe', 'true')
    } else {
      localStorage.removeItem('rememberedUsername')
      localStorage.removeItem('rememberMe')
    }
    
    try {
      await userStore.login(form.username, form.password)
      ElMessage.success(t('common.success'))
      router.push('/dashboard')
    } catch (e: any) {
      errorMsg.value = e.message || t('auth.error.loginFailed')
    } finally {
      loading.value = false
    }
  })
}

const handleRegister   = () => ElMessage.info(locale.value === 'zh' ? '注册功能开发中' : 'Registration coming soon')
const handleForgot     = () => ElMessage.info(locale.value === 'zh' ? '密码重置功能开发中' : 'Password reset coming soon')
const handleGoogleLogin = () => ElMessage.info(locale.value === 'zh' ? 'Google 登录功能开发中' : 'Google login coming soon')
const handleWecomLogin = () => ElMessage.info(locale.value === 'zh' ? '企业微信登录功能开发中' : 'WeCom login coming soon')
const handleDingtalkLogin = () => ElMessage.info(locale.value === 'zh' ? '钉钉登录功能开发中' : 'DingTalk login coming soon')
const handleFeishuLogin = () => ElMessage.info(locale.value === 'zh' ? '飞书登录功能开发中' : 'Feishu login coming soon')
const handlePrivacy = () => ElMessage.info(locale.value === 'zh' ? '隐私政策页面开发中' : 'Privacy policy page coming soon')
const handleTerms = () => ElMessage.info(locale.value === 'zh' ? '服务条款页面开发中' : 'Terms of service page coming soon')
const handleContact = () => ElMessage.info(locale.value === 'zh' ? '联系我们页面开发中' : 'Contact us page coming soon')
</script>

<style scoped lang="scss">
/* ── Layout ─────────────────────────────────────────── */
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  @media (max-width: 1024px) { grid-template-columns: 1fr; }
}

/* ── Left Panel ─────────────────────────────────────── */
.left-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  overflow: hidden;
  background: linear-gradient(135deg,
    #667eea 0%,
    #764ba2 25%,
    #f093fb 50%,
    #4facfe 75%,
    #00f2fe 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  color: #fff;

  @media (max-width: 1024px) { display: none; }
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.lang-switcher {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  gap: 8px;
  z-index: 10;
  
  .lang-btn {
    padding: 6px 12px;
    border-radius: 6px;
    border: 1px solid rgba(255,255,255,0.3);
    background: rgba(255,255,255,0.1);
    color: rgba(255,255,255,0.7);
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    backdrop-filter: blur(10px);
    
    &:hover {
      background: rgba(255,255,255,0.2);
      color: #fff;
    }
    
    &.active {
      background: rgba(255,255,255,0.9);
      color: #667eea;
      border-color: transparent;
    }
  }
}

.lang-switcher-mobile {
  display: none;
  justify-content: flex-start;
  gap: 8px;
  margin-bottom: 24px;
  
  @media (max-width: 1024px) { display: flex; }
  
  .lang-btn {
    padding: 6px 14px;
    border-radius: 6px;
    border: 2px solid var(--border-color);
    background: var(--bg-card);
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      border-color: #667eea;
      color: #667eea;
    }
    
    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      border-color: transparent;
    }
  }
}

.grid-overlay {
  position: absolute; inset: 0; pointer-events: none;
  background-image:
    linear-gradient(rgba(255,255,255,.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,.03) 1px, transparent 1px);
  background-size: 30px 30px;
}

.blob {
  position: absolute; border-radius: 50%; filter: blur(80px); pointer-events: none;
  &-1 { top: 10%; right: 10%; width: 300px; height: 300px; background: rgba(255,255,255,.15); }
  &-2 { bottom: 10%; left: 10%; width: 400px; height: 400px; background: rgba(255,255,255,.08); }
}

.brand {
  position: relative; z-index: 2;
  display: flex; align-items: center; gap: 12px;
  font-size: 20px; font-weight: 700; letter-spacing: -0.5px;
}
.brand-icon {
  width: 40px; height: 40px; border-radius: 10px;
  background: rgba(255,255,255,.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,.3);
  display: flex; align-items: center; justify-content: center;
}

/* Stage */
.stage {
  position: relative; z-index: 2;
  flex: 1; display: flex; align-items: flex-end; justify-content: center;
  padding-bottom: 32px;
}
.characters-wrap {
  position: relative;
  width: 550px; height: 400px;
}

/* Characters */
.char {
  position: absolute; bottom: 0;
  transition: all .7s ease-in-out;
  transform-origin: bottom center;
  will-change: transform;

  .eyes {
    position: absolute;
    display: flex;
    transition: all .7s ease-in-out;
  }
  .mouth {
    position: absolute;
    width: 80px; height: 4px;
    background: #2D2D2D; border-radius: 2px;
    transition: all .2s ease-out;
  }
}

.char-purple {
  left: 70px; width: 180px; height: 400px;
  background: linear-gradient(180deg, #667eea 0%, #5568d3 100%);
  border-radius: 12px 12px 0 0; z-index: 1;
  box-shadow: 0 20px 60px rgba(102, 126, 234, 0.4);
}
.char-black {
  left: 240px; width: 120px; height: 310px;
  background: linear-gradient(180deg, #3d3d3d 0%, #1a1a1a 100%);
  border-radius: 10px 10px 0 0; z-index: 2;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
}
.char-orange {
  left: 0; width: 240px; height: 200px;
  background: linear-gradient(180deg, #ffa751 0%, #ff8c42 100%);
  border-radius: 120px 120px 0 0; z-index: 3;
  box-shadow: 0 20px 50px rgba(255, 167, 81, 0.4);
}
.char-yellow {
  left: 310px; width: 140px; height: 230px;
  background: linear-gradient(180deg, #ffd93d 0%, #ffb700 100%);
  border-radius: 70px 70px 0 0; z-index: 4;
  box-shadow: 0 20px 50px rgba(255, 217, 61, 0.4);
}

.left-footer {
  position: relative; z-index: 2;
  display: flex; gap: 32px; font-size: 13px;
  a {
    color: rgba(255,255,255,.6); text-decoration: none;
    transition: color .2s;
    &:hover { color: #fff; }
    &:focus-visible {
      outline: 2px solid rgba(255,255,255,0.8);
      outline-offset: 2px;
      border-radius: 2px;
    }
  }
}

/* ── Right Panel ────────────────────────────────────── */
.right-panel {
  display: flex; align-items: center; justify-content: center;
  padding: 48px 32px;
  background: var(--bg-page);
  position: relative;
}

.form-box {
  width: 100%; max-width: 420px;
}

.mobile-brand {
  display: none;
  align-items: center; justify-content: center;
  gap: 12px; font-size: 20px; font-weight: 700;
  margin-bottom: 40px;
  @media (max-width: 1024px) { display: flex; }
  .brand-icon { 
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }
}

.form-header {
  text-align: center; margin-bottom: 40px;
  h1 {
    font-size: 32px; font-weight: 700; letter-spacing: -1px;
    margin: 0 0 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  p { font-size: 14px; color: var(--text-secondary); margin: 0; }
}

/* Form */
.the-form {
  ::v-deep(.el-form-item) { margin-bottom: 24px; }
  ::v-deep(.el-form-item__content) { flex-direction: column; align-items: flex-start; }
  ::v-deep(.el-form-item__error) { display: none; }
}

.field-label {
  display: block; 
  font-size: 14px; 
  font-weight: 600;
  color: var(--text-primary); 
  margin-bottom: 8px;
}

.the-input {
  width: 100%;
  
  ::v-deep(.el-input__wrapper) {
    height: 52px;
    padding: 0 16px;
    border-radius: 12px;
    border: 2px solid var(--border-color);
    background: var(--bg-card);
    transition: all 0.3s ease;
    box-shadow: none !important;
    
    &:hover { 
      border-color: var(--primary);
    }
    
    &.is-focus,
    &:focus-within {
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) !important;
    }
  }
  
  ::v-deep(.el-input__inner) {
    font-size: 15px; 
    color: var(--text-primary);
    height: 100%;
    line-height: normal;
    text-shadow: none !important;
    -webkit-text-fill-color: var(--text-primary);
    
    &::placeholder { 
      color: var(--text-placeholder); 
      text-shadow: none !important;
      -webkit-text-fill-color: var(--text-placeholder);
    }
  }
  
  ::v-deep(.el-input__prefix) {
    color: var(--text-secondary);
  }
}

.password-field {
  .password-input-wrapper {
    position: relative;
    width: 100%;
    
    .the-input {
      width: 100%;
      
      ::v-deep(.el-input__wrapper) {
        width: 100%;
        padding-right: 48px;
      }
    }
  }
}

.eye-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer; 
  color: var(--text-secondary); 
  display: flex; 
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  transition: all 0.2s;
  z-index: 10;
  
  &:hover { 
    color: var(--primary); 
    background: var(--info-bg);
  }
  
  &:focus-visible {
    outline: 2px solid var(--primary);
    outline-offset: 2px;
  }
}

.row-options {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px;
}
.remember-label {
  display: flex; align-items: center; gap: 8px;
  font-size: 14px; color: var(--text-regular); cursor: pointer;
  .remember-check { accent-color: var(--primary); width: 15px; height: 15px; cursor: pointer; }
}
.forgot-link {
  font-size: 14px; font-weight: 500; color: var(--primary); cursor: pointer;
  transition: color 0.2s;
  
  &:hover { opacity: 0.8; text-decoration: underline; }
  
  &:focus-visible {
    outline: 2px solid var(--primary);
    outline-offset: 2px;
    border-radius: 2px;
  }
}

/* Login button — modern gradient */
.btn-login {
  width: 100%; height: 48px; border-radius: 10px; border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 16px; font-weight: 600; letter-spacing: .3px;
  cursor: pointer; transition: all .3s;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;

  &:hover:not(:disabled) { 
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(102, 126, 234, 0.4);
  }
  &:active:not(:disabled) { transform: scale(.98); }
  &:disabled { opacity: .6; cursor: not-allowed; }
  
  &:focus-visible {
    outline: 2px solid #667eea;
    outline-offset: 2px;
  }
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
    transition: left 0.5s;
  }
  
  &:hover::before {
    left: 100%;
  }
}

.spinner-wrap {
  display: flex; align-items: center; gap: 8px;
}
.spinner {
  width: 16px; height: 16px;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Divider */
.divider {
  display: flex; align-items: center; gap: 12px;
  margin: 24px 0; color: var(--text-secondary); font-size: 13px;
  &::before, &::after {
    content: ''; flex: 1; height: 1px; background: var(--border-color);
  }
}

/* Social Login */
.social-login {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.btn-social {
  height: 44px; border-radius: 10px;
  border: 2px solid var(--border-color); background: var(--bg-card);
  color: var(--text-regular); font-size: 14px; font-weight: 500;
  cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px;
  transition: all .3s;
  box-shadow: var(--shadow-sm);
  
  .social-text {
    @media (max-width: 480px) { display: none; }
  }
  
  &:hover { 
    background: var(--bg-hover);
    border-color: var(--primary);
    box-shadow: var(--shadow);
    transform: translateY(-1px);
  }
  
  &:focus-visible {
    outline: 2px solid var(--primary);
    outline-offset: 2px;
  }
  
  &:active {
    transform: scale(0.98);
  }
}

.btn-social .el-icon {
  font-size: 19px;
}

.btn-google {
  &:hover { border-color: #4285f4; }
}

.btn-wecom,
.btn-dingtalk,
.btn-feishu {
  &:hover { border-color: var(--primary); }
}

.signup-hint {
  text-align: center; margin-top: 24px;
  font-size: 14px; color: var(--text-secondary);
  
  a {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    font-weight: 600; cursor: pointer;
    
    &:hover { text-decoration: underline; }
    
    &:focus-visible {
      outline: 2px solid #667eea;
      outline-offset: 2px;
      border-radius: 2px;
    }
  }
}
</style>
