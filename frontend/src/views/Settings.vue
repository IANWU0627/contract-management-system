<template>
  <div class="settings">
    <h1 class="page-title">{{ $t('menu.settings') }}</h1>
    
    <el-card shadow="hover">
      <el-form label-width="120px">
        <el-divider content-position="left">{{ t('settings.appearance') }}</el-divider>
        
        <el-form-item :label="t('settings.theme')">
          <el-radio-group v-model="theme" @change="handleThemeChange">
            <el-radio label="light">{{ t('settings.themeLight') }}</el-radio>
            <el-radio label="dark">{{ t('settings.themeDark') }}</el-radio>
            <el-radio label="system">{{ t('settings.themeSystem') }}</el-radio>
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
        
        <el-divider content-position="left">{{ t('settings.aiConfig') }}</el-divider>
        
        <el-form-item :label="t('settings.aiProvider')">
          <el-select v-model="aiConfig.provider" style="width: 280px" @change="handleAiProviderChange">
            <el-option :label="t('settings.aiNone')" value="none" />
            <el-option-group :label="t('settings.aiGroupOpenAI')">
              <el-option :label="t('settings.aiOpenAI')" value="openai" />
              <el-option :label="t('settings.aiAzure')" value="azure" />
            </el-option-group>
            <el-option-group :label="t('settings.aiGroupChinese')">
              <el-option :label="t('settings.aiZhipu')" value="zhipu" />
              <el-option :label="t('settings.aiBaichuan')" value="baichuan" />
              <el-option :label="t('settings.aiTongyi')" value="tongyi" />
              <el-option :label="t('settings.aiYi')" value="yi" />
              <el-option :label="t('settings.aiDeepseek')" value="deepseek" />
            </el-option-group>
            <el-option-group :label="t('settings.aiGroupOther')">
              <el-option :label="t('settings.aiSiliconFlow')" value="siliconflow" />
              <el-option :label="t('settings.aiAnthropic')" value="anthropic" />
              <el-option :label="t('settings.aiGemini')" value="gemini" />
              <el-option :label="t('settings.aiOllama')" value="ollama" />
            </el-option-group>
          </el-select>
        </el-form-item>
        
        <template v-if="aiConfig.provider !== 'none'">
          <el-form-item :label="t('settings.aiApiUrl')">
            <el-input 
              v-model="aiConfig.apiUrl" 
              :placeholder="t('settings.aiApiUrlPlaceholder')"
              style="width: 400px" 
            />
          </el-form-item>
          
          <el-form-item :label="t('settings.aiApiKey')">
            <el-input 
              v-model="aiConfig.apiKey" 
              type="password"
              :placeholder="t('settings.aiApiKeyPlaceholder')"
              style="width: 400px" 
              show-password
            />
          </el-form-item>
          
          <el-form-item :label="t('settings.aiModel')">
            <el-select 
              v-model="aiConfig.model" 
              style="width: 280px"
              filterable
              allow-create
              :allow-create-fn="(input: string) => ({ value: input, label: input })"
              :placeholder="t('settings.aiModelPlaceholder')"
            >
              <el-option
                v-for="model in availableModels"
                :key="model.value"
                :label="model.label"
                :value="model.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item :label="t('settings.aiTestConnection')">
            <el-button @click="handleTestAiConnection" :loading="aiTesting">
              {{ t('settings.aiTest') }}
            </el-button>
            <span v-if="aiTestResult" :class="aiTestResult === 'success' ? 'test-success' : 'test-error'">
              {{ aiTestResult === 'success' ? t('settings.aiTestSuccess') : t('settings.aiTestError') }}
            </span>
          </el-form-item>
        </template>
        
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
import { ref, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { UploadFilled } from '@element-plus/icons-vue'
import { testAiConnection } from '@/api/ai'

const { locale, t } = useI18n()
const appStore = useAppStore()

const theme = computed({
  get: () => appStore.theme,
  set: (val: string) => appStore.setTheme(val)
})
const systemName = ref(appStore.systemName)
const showImportDialog = ref(false)

interface AiConfig {
  provider: string
  apiUrl: string
  apiKey: string
  model: string
}

const aiConfig = reactive<AiConfig>({
  provider: localStorage.getItem('ai_provider') || 'none',
  apiUrl: localStorage.getItem('ai_api_url') || '',
  apiKey: localStorage.getItem('ai_api_key') || '',
  model: localStorage.getItem('ai_model') || ''
})

interface AiProvider {
  apiUrl: string
  models: { value: string; label: string }[]
}

const providerPresets: Record<string, AiProvider> = {
  openai: {
    apiUrl: 'https://api.openai.com/v1/chat/completions',
    models: [
      { value: 'gpt-4o', label: 'GPT-4o (最新)' },
      { value: 'gpt-4o-mini', label: 'GPT-4o Mini' },
      { value: 'gpt-4-turbo', label: 'GPT-4 Turbo' },
      { value: 'gpt-4', label: 'GPT-4' },
      { value: 'gpt-3.5-turbo', label: 'GPT-3.5 Turbo' }
    ]
  },
  azure: {
    apiUrl: 'https://your-resource.openai.azure.com',
    models: [
      { value: 'gpt-4o', label: 'GPT-4o' },
      { value: 'gpt-4', label: 'GPT-4' },
      { value: 'gpt-35-turbo', label: 'GPT-3.5 Turbo' }
    ]
  },
  anthropic: {
    apiUrl: 'https://api.anthropic.com/v1/messages',
    models: [
      { value: 'claude-sonnet-4-20250514', label: 'Claude Sonnet 4' },
      { value: 'claude-3-5-sonnet-latest', label: 'Claude 3.5 Sonnet' },
      { value: 'claude-3-opus-latest', label: 'Claude 3 Opus' },
      { value: 'claude-3-haiku-latest', label: 'Claude 3 Haiku' }
    ]
  },
  gemini: {
    apiUrl: 'https://generativelanguage.googleapis.com/v1beta/models',
    models: [
      { value: 'gemini-2.0-flash-exp', label: 'Gemini 2.0 Flash' },
      { value: 'gemini-1.5-pro', label: 'Gemini 1.5 Pro' },
      { value: 'gemini-1.5-flash', label: 'Gemini 1.5 Flash' },
      { value: 'gemini-pro', label: 'Gemini Pro' }
    ]
  },
  zhipu: {
    apiUrl: 'https://open.bigmodel.cn/api/paas/v4/chat/completions',
    models: [
      { value: 'glm-4-plus', label: 'GLM-4 Plus (最新)' },
      { value: 'glm-4', label: 'GLM-4' },
      { value: 'glm-4-flash', label: 'GLM-4 Flash' },
      { value: 'glm-3-turbo', label: 'GLM-3 Turbo' }
    ]
  },
  baichuan: {
    apiUrl: 'https://api.baichuan-ai.com/v1/chat/completions',
    models: [
      { value: 'Baichuan4', label: '百川4' },
      { value: 'Baichuan3-Turbo', label: '百川3-Turbo' },
      { value: 'Baichuan2-Turbo', label: '百川2-Turbo' }
    ]
  },
  tongyi: {
    apiUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
    models: [
      { value: 'qwen-plus', label: '通义千问 Plus' },
      { value: 'qwen-turbo', label: '通义千问 Turbo' },
      { value: 'qwen-max', label: '通义千问 Max' },
      { value: 'qwen-long', label: '通义千问 Long' }
    ]
  },
  yi: {
    apiUrl: 'https://api.lingyiwanwu.com/v1/chat/completions',
    models: [
      { value: 'yi-large', label: 'Yi Large (最新)' },
      { value: 'yi-medium', label: 'Yi Medium' },
      { value: 'yi-small', label: 'Yi Small' }
    ]
  },
  deepseek: {
    apiUrl: 'https://api.deepseek.com/v1/chat/completions',
    models: [
      { value: 'deepseek-chat', label: 'DeepSeek Chat' },
      { value: 'deepseek-coder', label: 'DeepSeek Coder' }
    ]
  },
  siliconflow: {
    apiUrl: 'https://api.siliconflow.cn/v1/chat/completions',
    models: [
      { value: 'deepseek-ai/DeepSeek-V2.5', label: 'DeepSeek V2.5' },
      { value: 'Qwen/Qwen2.5-72B-Instruct', label: 'Qwen2.5-72B' },
      { value: 'Qwen/Qwen2.5-7B-Instruct', label: 'Qwen2.5-7B' },
      { value: 'THUDM/glm-4-9b-chat', label: 'GLM-4-9B' },
      { value: '01-ai/Yi-1.5-34B-Chat', label: 'Yi-1.5-34B' }
    ]
  },
  ollama: {
    apiUrl: 'http://localhost:11434/v1/chat/completions',
    models: [
      { value: 'gpt-oss:20b', label: 'GPT-OSS 20B (您当前使用)' },
      { value: 'llama3.2', label: 'Llama 3.2 (最新)' },
      { value: 'llama3.1', label: 'Llama 3.1' },
      { value: 'llama3', label: 'Llama 3' },
      { value: 'llama3.2-vision', label: 'Llama 3.2 Vision (多模态)' },
      { value: 'mistral', label: 'Mistral' },
      { value: 'mistral-nemo', label: 'Mistral Nemo' },
      { value: 'mixtral', label: 'Mixtral 8x7B' },
      { value: 'qwen2.5', label: 'Qwen 2.5' },
      { value: 'qwen2.5-coder', label: 'Qwen 2.5 Coder' },
      { value: 'deepseek-r1', label: 'DeepSeek R1' },
      { value: 'deepseek-r1-distill-qwen', label: 'DeepSeek R1 (Qwen蒸馏)' },
      { value: 'deepseek-r1-distill-llama', label: 'DeepSeek R1 (Llama蒸馏)' },
      { value: 'codellama', label: 'Code Llama' },
      { value: 'codellama:70b', label: 'Code Llama 70B' },
      { value: 'gemma2', label: 'Gemma 2' },
      { value: 'gemma2:27b', label: 'Gemma 2 27B' },
      { value: 'phi3', label: 'Phi-3' },
      { value: 'phi3.5', label: 'Phi-3.5' },
      { value: 'nomic-embed-text', label: 'Nomic Embed Text (嵌入)' },
      { value: 'mxbai-embed-large', label: 'MxBai Embed Large (嵌入)' },
      { value: 'granite3.3', label: 'Granite 3.3' },
      { value: 'command-r7b', label: 'Command R7B' },
      { value: 'stablelm2', label: 'Stable LM 2' },
      { value: 'aya', label: 'Aya (多语言)' },
      { value: 'wizardlm2', label: 'WizardLM 2' },
      { value: 'neural-chat', label: 'Neural Chat' }
    ]
  }
}

const availableModels = computed(() => {
  if (!aiConfig.provider || aiConfig.provider === 'none') return []
  const preset = providerPresets[aiConfig.provider]
  return preset?.models || []
})

const aiTesting = ref(false)
const aiTestResult = ref<'success' | 'error' | ''>('')

const handleAiProviderChange = (provider: string) => {
  if (provider === 'none') {
    aiConfig.apiUrl = ''
    aiConfig.model = ''
    return
  }
  
  const preset = providerPresets[provider]
  if (preset) {
    aiConfig.apiUrl = preset.apiUrl
    aiConfig.model = preset.models[0]?.value || ''
  }
}

const handleTestAiConnection = async () => {
  if (!aiConfig.apiKey) {
    ElMessage.warning(t('settings.aiApiKeyRequired'))
    return
  }
  
  aiTesting.value = true
  aiTestResult.value = ''
  
  try {
    await testAiConnection({
      provider: aiConfig.provider,
      apiUrl: aiConfig.apiUrl,
      apiKey: aiConfig.apiKey,
      model: aiConfig.model
    })
    aiTestResult.value = 'success'
    ElMessage.success(t('settings.aiTestSuccess'))
  } catch (error) {
    aiTestResult.value = 'error'
    ElMessage.error(t('settings.aiTestError'))
  } finally {
    aiTesting.value = false
  }
}

const handleThemeChange = (val: string) => { appStore.setTheme(val) }
const handleLocaleChange = (val: string) => { appStore.setLocale(val); locale.value = val }
const handleSave = () => {
  appStore.setSystemName(systemName.value)
  
  localStorage.setItem('ai_provider', aiConfig.provider)
  localStorage.setItem('ai_api_url', aiConfig.apiUrl)
  localStorage.setItem('ai_api_key', aiConfig.apiKey)
  localStorage.setItem('ai_model', aiConfig.model)
  
  ElMessage.success(t('common.success'))
}
const handleBackup = () => { ElMessage.success(t('common.success')) }
const handleImport = () => { showImportDialog.value = false; ElMessage.success(t('common.success')) }
</script>

<style scoped lang="scss">
.settings {
  .page-title { font-size: 24px; font-weight: 700; margin-bottom: 24px; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
  
  .test-success {
    color: #67c23a;
    margin-left: 12px;
  }
  
  .test-error {
    color: #f56c6c;
    margin-left: 12px;
  }
}
</style>
