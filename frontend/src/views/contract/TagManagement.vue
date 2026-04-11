<template>
  <div class="tags-page">
    <div class="page-header">
      <h1 class="page-title">{{ $t('contract.tagManagement') }}</h1>
      <el-button type="primary" class="gradient-btn" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        {{ $t('contract.createTag') }}
      </el-button>
    </div>
    
    <div class="tags-section" v-loading="loading">
      <div class="section-title">
        <el-icon><User /></el-icon>
        <span>{{ $t('contract.myTags') }}</span>
        <el-tag size="small" type="info">{{ myTags.length }}</el-tag>
      </div>
      <div class="tags-grid" v-if="myTags.length > 0">
        <div v-for="tag in myTags" :key="tag.id" class="tag-card my-tag">
          <div class="tag-header">
            <div class="tag-color" :style="{ background: tag.color }"></div>
            <span class="tag-name">{{ tag.name }}</span>
            <el-dropdown trigger="click">
              <el-button text><el-icon><MoreFilled /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleEdit(tag)">
                    <el-icon><Edit /></el-icon> {{ $t('common.edit') }}
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(tag.id)" divided type="danger">
                    <el-icon><Delete /></el-icon> {{ $t('common.delete') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="tag-description" v-if="tag.description">
            {{ tag.description }}
          </div>
          <div class="tag-footer">
            <span class="tag-badge" :class="tag.isPublic ? 'team' : 'private'">
              {{ tag.isPublic ? $t('contract.teamVisible') : $t('contract.onlyMe') }}
            </span>
            <span class="usage-count">{{ $t('contract.usageCount', { count: tag.usageCount || 0 }) }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else :description="$t('contract.noTags')" />
    </div>
    
    <div class="tags-section" v-loading="loading">
      <div class="section-title">
        <el-icon><Connection /></el-icon>
        <span>{{ $t('contract.teamTags') }}</span>
        <el-tag size="small" type="info">{{ teamTags.length }}</el-tag>
      </div>
      <div class="tags-grid" v-if="teamTags.length > 0">
        <div v-for="tag in teamTags" :key="tag.id" class="tag-card">
          <div class="tag-header">
            <div class="tag-color" :style="{ background: tag.color }"></div>
            <span class="tag-name">{{ tag.name }}</span>
          </div>
          <div class="tag-description" v-if="tag.description">
            {{ tag.description }}
          </div>
          <div class="tag-footer">
            <span class="usage-count">{{ $t('contract.usageCount', { count: tag.usageCount || 0 }) }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else :description="$t('contract.noTags')" />
    </div>
    
    <el-dialog v-model="showCreateDialog" :title="$t('contract.createTag')" width="400px">
      <el-form :model="tagForm" label-width="80px">
        <el-form-item :label="$t('contract.tagName')">
          <el-input v-model="tagForm.name" :placeholder="$t('contract.tagNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('contract.tagColor')">
          <div class="color-picker">
            <div 
              v-for="color in colorOptions" 
              :key="color"
              class="color-option"
              :class="{ active: tagForm.color === color }"
              :style="{ background: color }"
              @click="tagForm.color = color"
            ></div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('contract.tagDescription')">
          <el-input v-model="tagForm.description" :placeholder="$t('contract.tagDescriptionPlaceholder')" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item :label="$t('contract.visibility')">
          <el-radio-group v-model="tagForm.isPublic">
            <el-radio :value="true">{{ $t('contract.teamVisible') }}</el-radio>
            <el-radio :value="false">{{ $t('contract.onlyMe') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreate">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="showEditDialog" :title="$t('common.edit')" width="400px">
      <el-form :model="tagForm" label-width="80px">
        <el-form-item :label="$t('contract.tagName')">
          <el-input v-model="tagForm.name" />
        </el-form-item>
        <el-form-item :label="$t('contract.tagColor')">
          <div class="color-picker">
            <div 
              v-for="color in colorOptions" 
              :key="color"
              class="color-option"
              :class="{ active: tagForm.color === color }"
              :style="{ background: color }"
              @click="tagForm.color = color"
            ></div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('contract.tagDescription')">
          <el-input v-model="tagForm.description" :placeholder="$t('contract.tagDescriptionPlaceholder')" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleUpdate">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyTags, createTag, updateTag, deleteTag } from '@/api/tag'
import { Plus, Edit, Delete, MoreFilled, User, Connection } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
const userStore = useUserStore()

const loading = ref(false)
const allTags = ref<any[]>([])
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingTagId = ref<number | null>(null)

const myTags = computed(() => {
  return allTags.value.filter(tag => tag.creatorId === userStore.userInfo?.id)
})

const teamTags = computed(() => {
  return allTags.value.filter(tag => tag.isPublic)
})

const colorOptions = [
  '#667eea', '#764ba2', '#f093fb', '#f5576c',
  '#11998e', '#38ef7d', '#ff6b6b', '#ee5a24',
  '#fbbf24', '#f59e0b', '#3b82f6', '#10b981'
]

const tagForm = reactive({
  name: '',
  color: '#667eea',
  description: '',
  isPublic: true
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyTags()
    allTags.value = res.data?.list || []
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!tagForm.name.trim()) {
    ElMessage.warning(t('contract.tagNameRequired'))
    return
  }
  try {
    const data = {
      name: tagForm.name.trim(),
      color: tagForm.color,
      description: tagForm.description.trim(),
      isPublic: tagForm.isPublic
    }
    await createTag(data)
    ElMessage.success(t('common.success'))
    showCreateDialog.value = false
    tagForm.name = ''
    tagForm.color = '#667eea'
    tagForm.description = ''
    tagForm.isPublic = true
    fetchData()
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleEdit = (tag: any) => {
  editingTagId.value = tag.id
  tagForm.name = JSON.parse(JSON.stringify(tag.name))
  tagForm.color = JSON.parse(JSON.stringify(tag.color))
  tagForm.description = JSON.parse(JSON.stringify(tag.description || ''))
  showEditDialog.value = true
}

const handleUpdate = async () => {
  if (!editingTagId.value) {
    ElMessage.warning(t('tag.tagIdMissing'))
    return
  }
  if (!tagForm.name.trim()) {
    ElMessage.warning(t('contract.tagNameRequired'))
    return
  }
  try {
    const data = {
      name: tagForm.name.trim(),
      color: tagForm.color,
      description: tagForm.description.trim()
    }
    await updateTag(editingTagId.value, data)
    ElMessage.success(t('common.success'))
    showEditDialog.value = false
    editingTagId.value = null
    tagForm.name = ''
    tagForm.color = '#667eea'
    tagForm.description = ''
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(t('contract.confirmDeleteTag'), t('common.warning'), { type: 'warning' })
    await deleteTag(id)
    ElMessage.success(t('common.success'))
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.tags-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin: 0;
    }
  }
}

.gradient-btn {
  background: var(--primary-gradient) !important;
  border: none !important;
}

.tags-section {
  margin-bottom: 32px;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    
    .el-icon {
      color: var(--primary);
    }
  }
}

.tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.tag-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: var(--primary);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
  }
  
  &.my-tag {
    border-left: 3px solid var(--primary);
  }
  
  .tag-header {
    display: flex;
    align-items: center;
    gap: 10px;
    
    .tag-color {
      width: 16px;
      height: 16px;
      border-radius: 50%;
    }
    
    .tag-name {
      flex: 1;
      font-size: 15px;
      font-weight: 500;
      color: var(--text-primary);
    }
  }
  
  .tag-description {
    margin-top: 8px;
    font-size: 13px;
    color: var(--text-secondary);
    line-height: 1.4;
    word-break: break-word;
  }
  
  .tag-footer {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid var(--border-color);
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .tag-badge {
      font-size: 11px;
      padding: 2px 8px;
      border-radius: 10px;
      
      &.team {
        background: rgba(102, 126, 234, 0.1);
        color: var(--primary);
      }
      
      &.private {
        background: rgba(245, 158, 11, 0.1);
        color: #f59e0b;
      }
    }
    
    .usage-count {
      font-size: 12px;
      color: var(--text-secondary);
    }
  }
}

.color-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  
  .color-option {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      transform: scale(1.1);
    }
    
    &.active {
      box-shadow: 0 0 0 3px white, 0 0 0 5px var(--primary);
    }
  }
}
</style>
