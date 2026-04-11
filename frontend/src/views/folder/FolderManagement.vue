<template>
  <div class="folders-page">
    <div class="page-header">
      <h1 class="page-title">{{ $t('folder.title') }}</h1>
      <el-button type="primary" class="gradient-btn" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        {{ $t('folder.create') }}
      </el-button>
    </div>
    
    <div class="folders-section" v-loading="loading">
      <div class="section-title">
        <el-icon><FolderOpened /></el-icon>
        <span>{{ $t('folder.myFolders') }}</span>
        <el-tag size="small" type="info">{{ folders.length }}</el-tag>
      </div>
      <div class="folders-grid" v-if="folders.length > 0">
        <div v-for="folder in folders" :key="folder.id" class="folder-card" :style="{ '--folder-color': folder.color || '#667eea', '--folder-color-light': lightenColor(folder.color || '#667eea', 15) }">
          <div class="folder-header">
            <div class="folder-icon" :style="{ background: `linear-gradient(135deg, ${folder.color || '#667eea'}, ${lightenColor(folder.color || '#667eea', 12)})` }">
              <el-icon :size="24" :style="{ color: '#fff' }"><FolderOpened /></el-icon>
            </div>
            <span class="folder-name">{{ folder.name }}</span>
            <el-dropdown trigger="click">
              <el-button text><el-icon><MoreFilled /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="openCreateSubFolder(folder)">
                    <el-icon><Plus /></el-icon> {{ $t('folder.addSubFolder') }}
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleEdit(folder)">
                    <el-icon><Edit /></el-icon> {{ $t('common.edit') }}
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(folder.id)" divided type="danger">
                    <el-icon><Delete /></el-icon> {{ $t('common.delete') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="folder-description" v-if="folder.description">
            {{ folder.description }}
          </div>
          <div class="folder-footer">
            <span class="folder-meta">{{ folder.createdBy || 'admin' }}</span>
            <span class="folder-count" v-if="folder.children && folder.children.length > 0">
              {{ folder.children.length }} {{ $t('folder.subFolders') }}
            </span>
          </div>
          
          <!-- 子文件夹 -->
          <div class="sub-folders" v-if="folder.children && folder.children.length > 0">
            <div v-for="child in folder.children" :key="child.id" class="sub-folder-item" :style="{ '--child-color': child.color || '#667eea' }">
              <div class="sub-folder-icon" :style="{ background: `linear-gradient(135deg, ${child.color || '#667eea'}, ${lightenColor(child.color || '#667eea', 12)})` }">
                <el-icon :size="16" :style="{ color: '#fff' }"><FolderOpened /></el-icon>
              </div>
              <span class="sub-folder-name">{{ child.name }}</span>
              <el-dropdown trigger="click" size="small">
                <el-button text size="small"><el-icon size="14"><MoreFilled /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleEdit(child)" type="primary" size="small">
                      <el-icon size="14"><Edit /></el-icon> {{ $t('common.edit') }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleDelete(child.id)" type="danger" size="small">
                      <el-icon size="14"><Delete /></el-icon> {{ $t('common.delete') }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else :description="$t('folder.empty')" />
    </div>
    
    <el-dialog v-model="showCreateDialog" :title="$t('folder.create')" width="400px">
      <el-form :model="folderForm" label-width="100px">
        <el-form-item :label="$t('folder.name')">
          <el-input v-model="folderForm.name" :placeholder="$t('folder.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('folder.parent')" v-if="parentId">
          <el-input :value="parentName" disabled />
        </el-form-item>
        <el-form-item :label="$t('folder.description')">
          <el-input v-model="folderForm.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item :label="$t('folder.color')">
          <div class="color-picker">
            <div 
              v-for="color in colorOptions" 
              :key="color"
              class="color-option"
              :class="{ active: folderForm.color === color }"
              :style="{ background: color }"
              @click="folderForm.color = color"
            ></div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeCreateDialog">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreate">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="showEditDialog" :title="$t('folder.edit')" width="400px">
      <el-form :model="folderForm" label-width="100px">
        <el-form-item :label="$t('folder.name')">
          <el-input v-model="folderForm.name" />
        </el-form-item>
        <el-form-item :label="$t('folder.description')">
          <el-input v-model="folderForm.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item :label="$t('folder.color')">
          <div class="color-picker">
            <div 
              v-for="color in colorOptions" 
              :key="color"
              class="color-option"
              :class="{ active: folderForm.color === color }"
              :style="{ background: color }"
              @click="folderForm.color = color"
            ></div>
          </div>
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
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFolderTree, createFolder, updateFolder, deleteFolder } from '@/api/folder'
import { Plus, Edit, Delete, MoreFilled, FolderOpened } from '@element-plus/icons-vue'

const { t } = useI18n()

interface FolderNode {
  id: number
  name: string
  parentId: number | null
  description?: string
  color?: string
  createdBy?: string
  children?: FolderNode[]
}

const loading = ref(false)
const folders = ref<FolderNode[]>([])
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingId = ref<number | null>(null)
const parentId = ref<number | null>(null)
const parentName = ref('')

const colorOptions = [
  '#667eea', '#764ba2', '#f093fb', '#f5576c',
  '#11998e', '#38ef7d', '#ff6b6b', '#ee5a24',
  '#fbbf24', '#f59e0b', '#3b82f6', '#10b981'
]

// 颜色转带透明度（用于 CSS v-bind）
const colorWithAlpha = (color: string, alpha: number): string => {
  const hex = color.replace('#', '')
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

// 颜色变亮
const lightenColor = (color: string, percent: number): string => {
  const hex = color.replace('#', '')
  const r = Math.min(255, Math.round(parseInt(hex.substring(0, 2), 16) + (255 - parseInt(hex.substring(0, 2), 16)) * (percent / 100)))
  const g = Math.min(255, Math.round(parseInt(hex.substring(2, 4), 16) + (255 - parseInt(hex.substring(2, 4), 16)) * (percent / 100)))
  const b = Math.min(255, Math.round(parseInt(hex.substring(4, 6), 16) + (255 - parseInt(hex.substring(4, 6), 16)) * (percent / 100)))
  return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`
}

const folderForm = reactive({
  name: '',
  description: '',
  color: '#667eea'
})

const loadFolders = async () => {
  loading.value = true
  try {
    const res = await getFolderTree()
    folders.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  } finally {
    loading.value = false
  }
}

const closeCreateDialog = () => {
  showCreateDialog.value = false
  parentId.value = null
  parentName.value = ''
  folderForm.name = ''
  folderForm.description = ''
  folderForm.color = '#667eea'
}

const openCreateSubFolder = (parent: FolderNode) => {
  parentId.value = parent.id
  parentName.value = parent.name
  folderForm.name = ''
  folderForm.description = ''
  folderForm.color = parent.color || '#667eea'
  showCreateDialog.value = true
}

const handleCreate = async () => {
  if (!folderForm.name.trim()) {
    ElMessage.warning(t('folder.nameRequired'))
    return
  }
  
  try {
    await createFolder({
      name: folderForm.name.trim(),
      parentId: parentId.value,
      description: folderForm.description.trim(),
      color: folderForm.color
    })
    ElMessage.success(t('common.success'))
    closeCreateDialog()
    loadFolders()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  }
}

const handleEdit = (folder: FolderNode) => {
  editingId.value = folder.id
  folderForm.name = folder.name
  folderForm.description = folder.description || ''
  folderForm.color = folder.color || '#667eea'
  showEditDialog.value = true
}

const handleUpdate = async () => {
  if (!editingId.value) return
  if (!folderForm.name.trim()) {
    ElMessage.warning(t('folder.nameRequired'))
    return
  }
  
  try {
    await updateFolder(editingId.value, {
      name: folderForm.name.trim(),
      description: folderForm.description.trim(),
      color: folderForm.color
    })
    ElMessage.success(t('common.success'))
    showEditDialog.value = false
    loadFolders()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      t('folder.confirmDelete'),
      t('common.warning'),
      { type: 'warning' }
    )
    await deleteFolder(id)
    ElMessage.success(t('common.success'))
    loadFolders()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  }
}

onMounted(() => {
  loadFolders()
})
</script>

<style scoped lang="scss">
.folders-page {
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

.folders-section {
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

.folders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.folder-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 20px;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;

  /* 彩色顶部装饰条 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, var(--folder-color), var(--folder-color-light));
    border-radius: 16px 16px 0 0;
  }

  &:hover {
    border-color: transparent;
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(102, 110, 234, 0.18);

    .folder-icon {
      transform: scale(1.08) rotate(-3deg);
    }
  }

  .folder-header {
    display: flex;
    align-items: center;
    gap: 12px;

    .folder-icon {
      display: flex;
      width: 48px;
      height: 48px;
      border-radius: 14px;
      align-items: center;
      justify-content: center;
      color: #fff;
      transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
      flex-shrink: 0;
    }
    
    .folder-name {
      flex: 1;
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }

  .folder-description {
    margin-top: 10px;
    font-size: 13px;
    color: var(--text-secondary);
    line-height: 1.5;
    padding-left: 60px;
  }

  .folder-footer {
    margin-top: 14px;
    padding-top: 14px;
    border-top: 1px solid var(--border-color);
    display: flex;
    justify-content: space-between;
    align-items: center;

    .folder-meta {
      font-size: 12px;
      color: var(--text-secondary);
    }

    .folder-count {
      font-size: 12px;
      font-weight: 600;
      padding: 2px 10px;
      border-radius: 10px;
      background: rgba(102, 110, 234, 0.1);
      color: var(--folder-color);
    }
  }
  
  .sub-folders {
    margin-top: 14px;
    padding-top: 14px;
    border-top: 1px dashed var(--border-color);

    .sub-folder-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px 0;

      .sub-folder-icon {
        display: flex;
        width: 32px;
        height: 32px;
        border-radius: 9px;
        align-items: center;
        justify-content: center;
        color: #fff;
        transition: transform 0.3s ease;
      }

      .sub-folder-name {
        flex: 1;
        font-size: 13px;
        font-weight: 500;
        color: var(--text-primary);
      }

      &:hover {
        background: var(--bg-hover);
        margin: 0 -10px;
        padding: 8px 10px;
        border-radius: 8px;

        .sub-folder-icon {
          transform: scale(1.08);
        }
      }
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
