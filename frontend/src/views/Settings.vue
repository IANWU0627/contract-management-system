<template>
  <div class="settings">
    <h1 class="page-title">{{ $t('menu.settings') }}</h1>
    
    <el-card shadow="hover">
      <el-form label-width="120px">
        <el-divider content-position="left">{{ t('settings.appearance') }}</el-divider>
        
        <el-form-item :label="t('settings.theme')">
          <el-radio-group v-model="theme" @change="handleThemeChange">
            <el-radio value="light">{{ t('settings.themeLight') }}</el-radio>
            <el-radio value="dark">{{ t('settings.themeDark') }}</el-radio>
            <el-radio value="system">{{ t('settings.themeSystem') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item :label="t('settings.language')">
          <el-select v-model="locale" @change="handleLocaleChange" style="width: 200px">
            <el-option label="中文" value="zh" />
            <el-option label="English" value="en" />
          </el-select>
        </el-form-item>
        
        <el-divider content-position="left">{{ t('settings.system') }}</el-divider>
        
        <el-form-item :label="t('settings.systemName')">
          <el-input v-model="systemName" style="width: 300px" />
        </el-form-item>
        
        <el-form-item :label="t('settings.dataBackup')">
          <el-button @click="handleBackup">{{ t('settings.exportData') }}</el-button>
          <el-button @click="showImportDialog = true">{{ t('settings.importData') }}</el-button>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave">{{ t('settings.saveSettings') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-dialog v-model="showImportDialog" :title="t('settings.importData')" width="400px">
      <el-upload :auto-upload="false" :on-change="handleImport" drag>
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">{{ t('contract.placeholder.dragUpload') }}</div>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { UploadFilled } from '@element-plus/icons-vue'

const { locale, t } = useI18n()
const appStore = useAppStore()

const theme = computed({
  get: () => appStore.theme,
  set: (val: string) => appStore.setTheme(val)
})
const systemName = ref(appStore.systemName)
const showImportDialog = ref(false)

const handleThemeChange = (val: string) => { appStore.setTheme(val) }
const handleLocaleChange = (val: string) => { appStore.setLocale(val); locale.value = val }
const handleSave = () => {
  appStore.setSystemName(systemName.value)
  ElMessage.success(t('common.success'))
}
const handleBackup = () => { ElMessage.success(t('common.success')) }
const handleImport = () => { showImportDialog.value = false; ElMessage.success(t('common.success')) }
</script>

<style scoped lang="scss">
.settings {
  .page-title { font-size: 24px; font-weight: 700; margin-bottom: 24px; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
}
</style>
