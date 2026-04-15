<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const appStore = useAppStore()
const OVERFLOW_TOOLTIP_DELAY_KEY = 'overflowTooltipDelayMs'
const OVERFLOW_TOOLTIP_DELAY_OPTIONS = [150, 200, 300] as const

const syncThemeByRoute = () => {
  const isLoginPage = route.path === '/login' || route.path === '/'
  if (!isLoginPage) {
    appStore.initTheme()
  } else {
    document.body.classList.remove('dark')
    document.documentElement.classList.remove('dark')
    document.body.removeAttribute('data-theme')
  }
}

const overflowTooltipSelector = [
  '.el-tabs__item',
  '.el-form-item__label',
  '.el-descriptions__label',
  '.el-table th .cell',
  '.el-breadcrumb__inner',
  '.el-dialog__title',
  '.el-card__header',
  '.el-card__header .card-header',
  '.el-pagination__total',
  '.el-pagination__jump',
  '.menu-title',
  '.page-title'
].join(', ')

let activeOverflowTarget: HTMLElement | null = null
let overflowTooltipEl: HTMLDivElement | null = null
let showTooltipTimer: number | null = null

const getOverflowTooltipDelay = () => {
  const raw = Number(localStorage.getItem(OVERFLOW_TOOLTIP_DELAY_KEY) || 200)
  return OVERFLOW_TOOLTIP_DELAY_OPTIONS.includes(raw as 150 | 200 | 300) ? raw : 200
}

const isTextOverflow = (el: HTMLElement) => {
  return el.scrollWidth - el.clientWidth > 1 || el.scrollHeight - el.clientHeight > 1
}

const ensureOverflowTooltip = () => {
  if (overflowTooltipEl) return overflowTooltipEl
  const el = document.createElement('div')
  el.className = 'app-overflow-tooltip'
  el.style.display = 'none'
  document.body.appendChild(el)
  overflowTooltipEl = el
  return el
}

const hideOverflowTooltip = () => {
  if (showTooltipTimer) {
    window.clearTimeout(showTooltipTimer)
    showTooltipTimer = null
  }
  if (overflowTooltipEl) {
    overflowTooltipEl.style.display = 'none'
  }
  activeOverflowTarget = null
}

const updateTooltipPosition = (x: number, y: number) => {
  if (!overflowTooltipEl) return
  const offset = 14
  const maxLeft = window.innerWidth - overflowTooltipEl.offsetWidth - 12
  const maxTop = window.innerHeight - overflowTooltipEl.offsetHeight - 12
  overflowTooltipEl.style.left = `${Math.min(x + offset, Math.max(8, maxLeft))}px`
  overflowTooltipEl.style.top = `${Math.min(y + offset, Math.max(8, maxTop))}px`
}

const showOverflowTooltip = (target: HTMLElement, x: number, y: number) => {
  const text = target.textContent?.trim()
  if (!text) return
  const tooltip = ensureOverflowTooltip()
  tooltip.textContent = text
  tooltip.classList.remove('is-visible')
  tooltip.style.display = 'block'
  updateTooltipPosition(x, y)
  requestAnimationFrame(() => {
    tooltip.classList.add('is-visible')
  })
}

const handleGlobalMouseOver = (event: MouseEvent) => {
  const target = event.target as HTMLElement | null
  if (!target) {
    hideOverflowTooltip()
    return
  }
  const matched = target.closest(overflowTooltipSelector) as HTMLElement | null
  if (!matched || !isTextOverflow(matched)) {
    hideOverflowTooltip()
    return
  }
  matched.removeAttribute('title')
  if (activeOverflowTarget === matched && overflowTooltipEl?.style.display === 'block') {
    updateTooltipPosition(event.clientX, event.clientY)
    return
  }
  if (showTooltipTimer) {
    window.clearTimeout(showTooltipTimer)
    showTooltipTimer = null
  }
  activeOverflowTarget = matched
  showTooltipTimer = window.setTimeout(() => {
    if (activeOverflowTarget === matched) {
      showOverflowTooltip(matched, event.clientX, event.clientY)
    }
    showTooltipTimer = null
  }, getOverflowTooltipDelay())
}

const handleGlobalMouseMove = (event: MouseEvent) => {
  if (!activeOverflowTarget || !overflowTooltipEl || overflowTooltipEl.style.display === 'none') return
  updateTooltipPosition(event.clientX, event.clientY)
}

const handleGlobalMouseOut = (event: MouseEvent) => {
  if (!activeOverflowTarget) return
  const related = event.relatedTarget as Node | null
  if (!related || !activeOverflowTarget.contains(related)) {
    hideOverflowTooltip()
  }
}

onMounted(() => {
  syncThemeByRoute()
  window.addEventListener('mouseover', handleGlobalMouseOver, true)
  window.addEventListener('mousemove', handleGlobalMouseMove, true)
  window.addEventListener('mouseout', handleGlobalMouseOut, true)
  window.addEventListener('scroll', hideOverflowTooltip, true)
  window.addEventListener('resize', hideOverflowTooltip)
})

onUnmounted(() => {
  window.removeEventListener('mouseover', handleGlobalMouseOver, true)
  window.removeEventListener('mousemove', handleGlobalMouseMove, true)
  window.removeEventListener('mouseout', handleGlobalMouseOut, true)
  window.removeEventListener('scroll', hideOverflowTooltip, true)
  window.removeEventListener('resize', hideOverflowTooltip)
  overflowTooltipEl?.remove()
  overflowTooltipEl = null
})

watch(() => route.path, syncThemeByRoute)
</script>

<style>
@import '@/assets/styles/variables.scss';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

body {
  background-color: var(--bg-color);
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* 暗色主题 - 仅在非登录页生效 */
body.dark,
html.dark {
  --el-bg-color: var(--bg-card);
  --el-bg-color-overlay: var(--bg-hover);
  --el-text-color-primary: var(--text-primary);
  --el-text-color-regular: var(--text-regular);
  --el-border-color: var(--border-color);
  --el-border-color-light: var(--border-light);
}

/* Element Plus 暗色主题适配 */
html.dark,
html[data-theme="dark"] {
  --el-bg-color: var(--bg-card);
  --el-bg-color-page: var(--bg-page);
  --el-text-color-primary: var(--text-primary);
  --el-text-color-regular: var(--text-regular);
  --el-border-color: var(--border-color);
  --el-border-color-light: var(--border-light);
}

/* 全局表格优化 - 解决英文过长导致的变形问题 */
.el-table {
  .el-table__header-wrapper {
    th .cell {
      word-break: normal;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      line-height: 1.4;
      font-weight: 600;
    }
  }
  
  .el-table__body-wrapper {
    td .cell {
      word-break: break-word;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

/* 通用表格标题链接样式 */
.table-title-link {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 英文长文案场景下的通用防溢出处理 */
.page-header {
  min-width: 0;
  gap: 12px;
}

.page-header .page-title {
  min-width: 0;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.el-tabs__item {
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: default;
}

.el-form-item__label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: default;
}

.el-descriptions__label {
  cursor: default;
}

.el-breadcrumb {
  min-width: 0;
}

.el-breadcrumb__item {
  min-width: 0;
}

.el-breadcrumb__inner {
  display: inline-block;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
  cursor: default;
}

.el-dialog__header {
  min-width: 0;
}

.el-dialog__title {
  display: inline-block;
  max-width: calc(100% - 32px);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
  cursor: default;
}

.el-card__header {
  min-width: 0;
}

.el-card__header .card-header,
.el-card__header > span,
.el-card__header .el-divider__text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
  cursor: default;
}

.el-pagination {
  min-width: 0;
  flex-wrap: wrap;
  row-gap: 6px;
}

.el-pagination__total,
.el-pagination__jump {
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: default;
}

.el-select-dropdown__item {
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-overflow-tooltip {
  position: fixed;
  z-index: 10000;
  max-width: 420px;
  padding: 8px 10px;
  border-radius: 6px;
  background: rgba(32, 32, 32, 0.95);
  color: #fff;
  font-size: 12px;
  line-height: 1.45;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.16);
  pointer-events: none;
  white-space: normal;
  word-break: break-word;
  opacity: 0;
  transform: translateY(2px);
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.app-overflow-tooltip.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.app-overflow-tooltip::after {
  content: '';
  position: absolute;
  left: 12px;
  top: -6px;
  width: 10px;
  height: 10px;
  background: rgba(32, 32, 32, 0.95);
  border-left: 1px solid rgba(255, 255, 255, 0.16);
  border-top: 1px solid rgba(255, 255, 255, 0.16);
  transform: rotate(45deg);
}

/* Light theme: brighter tooltip with subtle border */
html:not(.dark) .app-overflow-tooltip,
body:not(.dark) .app-overflow-tooltip {
  background: #ffffff;
  color: #303133;
  border-color: #dcdfe6;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

html:not(.dark) .app-overflow-tooltip::after,
body:not(.dark) .app-overflow-tooltip::after {
  background: #ffffff;
  border-left-color: #dcdfe6;
  border-top-color: #dcdfe6;
}

/* Dark theme: increase border/arrow contrast */
html.dark .app-overflow-tooltip,
body.dark .app-overflow-tooltip,
html[data-theme='dark'] .app-overflow-tooltip,
body[data-theme='dark'] .app-overflow-tooltip {
  background: #1f2430;
  color: #e8eef8;
  border-color: rgba(167, 190, 255, 0.38);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.45);
}

html.dark .app-overflow-tooltip::after,
body.dark .app-overflow-tooltip::after,
html[data-theme='dark'] .app-overflow-tooltip::after,
body[data-theme='dark'] .app-overflow-tooltip::after {
  background: #1f2430;
  border-left-color: rgba(167, 190, 255, 0.38);
  border-top-color: rgba(167, 190, 255, 0.38);
}

.el-button {
  max-width: 100%;
}

.el-button > span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .page-header {
    flex-wrap: wrap;
  }
}
</style>
