<template>
  <div class="role-management">
    <div class="page-header">
      <h1 class="page-title">{{ $t('menu.roleManagement') }}</h1>
      <el-button type="primary" class="gradient-btn" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        {{ $t('role.create') }}
      </el-button>
    </div>
    
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="searchKeyword"
            :placeholder="$t('common.search') + '...'"
            clearable
            @input="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch" class="gradient-btn">
            <el-icon><Search /></el-icon>
            {{ t('common.search') }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            {{ t('common.reset') }}
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="table-section" v-loading="loading">
      <el-table :data="paginatedRoles" style="width: 100%">
        <el-table-column prop="name" :label="$t('role.name')" width="120" />
        <el-table-column prop="code" :label="$t('role.code')" width="100" />
        <el-table-column prop="description" :label="$t('role.description')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('role.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? $t('role.active') : $t('role.inactive') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.action')" width="100" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleAction(command, row)">
              <el-button size="small">
                {{ $t('common.action') }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">{{ $t('common.edit') }}</el-dropdown-item>
                  <el-dropdown-item command="toggle" :disabled="row.code === 'ADMIN'">
                    {{ row.status === 1 ? $t('role.inactive') : $t('role.active') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided type="danger">{{ $t('common.delete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="filteredRoles.length === 0 && !loading" class="empty-state">
        <el-empty :description="$t('role.noRoles')" />
      </div>
      
      <div class="pagination-wrap" v-if="filteredRoles.length > 0">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="filteredRoles.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
    
    <el-dialog v-model="showDialog" :title="dialogTitle" width="900px" class="permission-dialog">
      <el-form :model="roleForm" label-width="100px">
        <el-form-item :label="$t('role.name')" required>
          <el-input v-model="roleForm.name" :placeholder="$t('role.enterName')" />
        </el-form-item>
        <el-form-item :label="$t('role.code')" required>
          <el-input v-model="roleForm.code" :placeholder="$t('role.enterCode')" />
        </el-form-item>
        <el-form-item :label="$t('role.description')">
          <el-input v-model="roleForm.description" type="textarea" :rows="2" :placeholder="$t('role.enterDescription')" />
        </el-form-item>
        <el-form-item :label="$t('role.status')">
          <el-switch v-model="roleForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        
        <el-form-item :label="$t('role.permissions')">
          <div class="permission-section">
            <!-- 快捷操作栏 -->
            <div class="permission-toolbar">
              <div class="toolbar-left">
                <el-select v-model="selectedPreset" size="small" :placeholder="t('role.selectPreset')" clearable @change="applyPreset">
                  <el-option :label="t('role.presetAdmin')" value="admin" />
                  <el-option :label="t('role.presetUser')" value="user" />
                  <el-option :label="t('role.presetReadonly')" value="readonly" />
                </el-select>
              </div>
              <div class="toolbar-right">
                <el-button size="small" @click="selectAllPermissions">
                  <el-icon><CircleCheck /></el-icon>
                  {{ t('role.selectAll') }}
                </el-button>
                <el-button size="small" @click="invertSelection">
                  <el-icon><RefreshRight /></el-icon>
                  {{ t('role.invertSelection') }}
                </el-button>
                <el-button size="small" @click="clearSelection">
                  <el-icon><Delete /></el-icon>
                  {{ t('role.clearSelection') }}
                </el-button>
                <el-input
                  v-model="permissionSearch"
                  size="small"
                  :placeholder="t('role.searchPermissions')"
                  style="width: 200px"
                  clearable
                >
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            
            <!-- 已选权限标签 -->
            <div class="selected-permissions" v-if="selectedPermissions.length > 0">
              <span class="selected-label">{{ t('role.selected') }} ({{ selectedPermissions.length }}):</span>
              <div class="selected-tags">
                <el-tag
                  v-for="perm in selectedPermissions"
                  :key="perm.id"
                  size="small"
                  closable
                  @close="togglePermission(perm.id, false)"
                  class="selected-tag"
                >
                  <span class="tag-name">{{ perm.name }}</span>
                  <span class="tag-code">{{ perm.code }}</span>
                </el-tag>
              </div>
            </div>
            
            <!-- 权限树形结构 -->
            <div class="permission-tree-wrapper">
              <el-tree
                ref="permissionTreeRef"
                :data="filteredPermissionTree"
                :props="treeProps"
                show-checkbox
                node-key="id"
                :default-expand-all="true"
                :expand-on-click-node="false"
                @check="handleTreeCheck"
                class="permission-tree"
              >
                <template #default="{ node, data }">
                  <div class="tree-node-content" :class="{ 'is-selected': data.id && roleForm.permissionIds.includes(data.id) }">
                    <el-icon v-if="data.isGroup" class="group-icon"><Folder /></el-icon>
                    <el-icon v-else class="perm-icon" :class="'icon-' + data.code.toLowerCase()"><Key /></el-icon>
                    <span class="node-label">{{ node.label }}</span>
                    <el-tooltip v-if="data.description" :content="data.description" placement="top">
                      <el-icon class="info-icon"><InfoFilled /></el-icon>
                    </el-tooltip>
                    <span v-if="!data.isGroup" class="node-code">{{ data.code }}</span>
                    <span v-if="data.isGroup" class="node-count">({{ data.permissionCount }})</span>
                  </div>
                </template>
              </el-tree>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, createRole, updateRole, deleteRole, toggleRole, getActivePermissions } from '@/api/role'
import { 
  Plus, Search, Refresh, Edit, Delete, TurnOff, 
  CircleCheck, RefreshRight, Folder, Key, InfoFilled, ArrowDown
} from '@element-plus/icons-vue'

const { t } = useI18n()

interface Permission {
  id: number
  name: string
  code: string
  description: string
  groupName?: string
}

interface PermissionGroup {
  id: string
  name: string
  children: Permission[]
  isGroup: boolean
  permissionCount: number
}

const loading = ref(false)
const roles = ref<any[]>([])
const permissions = ref<Permission[]>([])
const showDialog = ref(false)
const dialogTitle = ref('')
const currentRoleId = ref<number | null>(null)
const searchKeyword = ref('')
const permissionSearch = ref('')
const selectedPreset = ref('')
const permissionTreeRef = ref()

const treeProps = {
  children: 'children',
  label: 'name'
}

const query = reactive({
  page: 1,
  pageSize: 10
})

const filteredRoles = computed(() => {
  if (!searchKeyword.value) return roles.value
  const kw = searchKeyword.value.toLowerCase()
  return roles.value.filter(r => 
    r.name.toLowerCase().includes(kw) || 
    r.code.toLowerCase().includes(kw) ||
    (r.description && r.description.toLowerCase().includes(kw))
  )
})

const paginatedRoles = computed(() => {
  const start = (query.page - 1) * query.pageSize
  return filteredRoles.value.slice(start, start + query.pageSize)
})

const roleForm = reactive({
  name: '',
  code: '',
  description: '',
  status: 1,
  permissionIds: [] as number[]
})

const selectedPermissions = computed(() => {
  return permissions.value.filter(p => roleForm.permissionIds.includes(p.id))
})

const permissionTree = computed<PermissionGroup[]>(() => {
  const groups: Record<string, Permission[]> = {
    [t('role.groupSystem')]: [],
    [t('role.groupContract')]: []
  }
  
  const groupMap: Record<string, string> = {
    'USER_MANAGE': t('role.groupSystem'),
    'ROLE_MANAGE': t('role.groupSystem'),
    'SETTING_MANAGE': t('role.groupSystem'),
    'CONTRACT_TYPE_MANAGE': t('role.groupSystem'),
    'VARIABLE_MANAGE': t('role.groupSystem'),
    'QUICK_CODE_MANAGE': t('role.groupSystem'),
    'REPORT_VIEW': t('role.groupSystem'),
    'CONTRACT_MANAGE': t('role.groupContract'),
    'TEMPLATE_MANAGE': t('role.groupContract'),
    'FOLDER_MANAGE': t('role.groupContract'),
    'TAG_MANAGE': t('role.groupContract'),
    'FAVORITE_MANAGE': t('role.groupContract'),
    'CONTRACT_APPROVE': t('role.groupContract'),
    'RENEWAL_MANAGE': t('role.groupContract'),
    'REMINDER_MANAGE': t('role.groupContract'),
    'REMINDER_RULE_MANAGE': t('role.groupContract')
  }
  
  permissions.value.forEach(p => {
    const group = groupMap[p.code] || t('role.groupSystem')
    p.groupName = group
    const targetGroup = groups[group]
    if (!targetGroup) {
      groups[group] = []
      groups[group].push(p)
    } else {
      targetGroup.push(p)
    }
  })
  
  return Object.entries(groups)
    .filter(([_, perms]) => perms.length > 0)
    .map(([name, perms]) => ({
      id: 'group-' + name,
      name,
      children: perms as Permission[],
      isGroup: true,
      permissionCount: perms.length
    }))
})

const filteredPermissionTree = computed(() => {
  if (!permissionSearch.value) return permissionTree.value
  
  const kw = permissionSearch.value.toLowerCase()
  return permissionTree.value.map(group => {
    const filteredChildren = group.children.filter(p => 
      p.name.toLowerCase().includes(kw) || 
      p.code.toLowerCase().includes(kw)
    )
    return {
      ...group,
      children: filteredChildren,
      permissionCount: filteredChildren.length
    }
  }).filter(group => group.children.length > 0)
})

const handleSearch = () => {
  query.page = 1
}

const handleReset = () => {
  searchKeyword.value = ''
  query.page = 1
}

const handleSizeChange = () => {
  query.page = 1
}

const handlePageChange = () => {}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(t('role.confirmDelete'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteRole(row.id)
    ElMessage.success(t('common.success'))
    fetchRoles()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  }
}

const handleAction = (command: string, row: any) => {
  switch (command) {
    case 'edit':
      handleEdit(row)
      break
    case 'toggle':
      handleToggle(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

const handleToggle = async (row: any) => {
  try {
    await toggleRole(row.id)
    ElMessage.success(t('common.success'))
    fetchRoles()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  }
}

const togglePermission = (permissionId: number, selected: boolean) => {
  if (selected) {
    if (!roleForm.permissionIds.includes(permissionId)) {
      roleForm.permissionIds.push(permissionId)
    }
  } else {
    roleForm.permissionIds = roleForm.permissionIds.filter(id => id !== permissionId)
  }
  nextTick(() => {
    updateTreeCheck()
  })
}

const handleTreeCheck = () => {
  const checkedKeys = permissionTreeRef.value?.getCheckedKeys(false) || []
  const halfCheckedKeys = permissionTreeRef.value?.getHalfCheckedKeys() || []
  roleForm.permissionIds = [...checkedKeys, ...halfCheckedKeys].filter(id => typeof id === 'number')
}

const updateTreeCheck = () => {
  if (permissionTreeRef.value) {
    permissionTreeRef.value.setCheckedKeys(roleForm.permissionIds)
  }
}

const selectAllPermissions = () => {
  roleForm.permissionIds = permissions.value.map(p => p.id)
  nextTick(() => {
    updateTreeCheck()
  })
}

const invertSelection = () => {
  const allIds = permissions.value.map(p => p.id)
  roleForm.permissionIds = allIds.filter(id => !roleForm.permissionIds.includes(id))
  nextTick(() => {
    updateTreeCheck()
  })
}

const clearSelection = () => {
  roleForm.permissionIds = []
  nextTick(() => {
    updateTreeCheck()
  })
}

const applyPreset = (preset: string) => {
  if (!preset) return
  
  let presetPermissionCodes: string[] = []
  
  switch (preset) {
    case 'admin':
      presetPermissionCodes = permissions.value.map(p => p.code)
      break
    case 'user':
      presetPermissionCodes = [
        'CONTRACT_MANAGE', 'FAVORITE_MANAGE', 'REMINDER_MANAGE', 'REPORT_VIEW'
      ]
      break
    case 'readonly':
      presetPermissionCodes = ['REPORT_VIEW']
      break
  }
  
  roleForm.permissionIds = permissions.value
    .filter(p => presetPermissionCodes.includes(p.code))
    .map(p => p.id)
  
  nextTick(() => {
    updateTreeCheck()
  })
  
  ElMessage.success(t('role.presetApplied', { preset: preset === 'admin' ? t('role.presetAdmin') : preset === 'user' ? t('role.presetUser') : t('role.presetReadonly') }))
}

const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await getRoles()
    roles.value = res.data?.list || []
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

const fetchPermissions = async () => {
  try {
    const res = await getActivePermissions()
    permissions.value = res.data?.list || res.data || []
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const openCreateDialog = () => {
  currentRoleId.value = null
  resetForm()
  dialogTitle.value = t('role.create')
  showDialog.value = true
}

const handleEdit = async (row: any) => {
  currentRoleId.value = row.id
  dialogTitle.value = t('role.edit')
  
  try {
    const res = await import('@/api/role').then(m => m.getRoleDetail(row.id))
    const roleDetail = res.data
    
    roleForm.name = roleDetail.role.name
    roleForm.code = roleDetail.role.code
    roleForm.description = roleDetail.role.description
    roleForm.status = roleDetail.role.status
    roleForm.permissionIds = roleDetail.permissions.map((p: any) => p.id)
    
    showDialog.value = true
    
    nextTick(() => {
      updateTreeCheck()
    })
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleSubmit = async () => {
  if (!roleForm.name) {
    ElMessage.warning(t('role.enterName'))
    return
  }
  if (!roleForm.code) {
    ElMessage.warning(t('role.enterCode'))
    return
  }
  
  try {
    const data = {
      name: roleForm.name,
      code: roleForm.code,
      description: roleForm.description,
      status: Number(roleForm.status),
      permissionIds: roleForm.permissionIds
    }
    
    if (currentRoleId.value) {
      await updateRole(currentRoleId.value, data)
    } else {
      await createRole(data)
    }
    ElMessage.success(t('common.success'))
    showDialog.value = false
    resetForm()
    fetchRoles()
  } catch (error: any) {
    ElMessage.error(t('common.error'))
  }
}

const resetForm = () => {
  currentRoleId.value = null
  roleForm.name = ''
  roleForm.code = ''
  roleForm.description = ''
  roleForm.status = 1
  roleForm.permissionIds = []
  permissionSearch.value = ''
  selectedPreset.value = ''
  dialogTitle.value = t('role.create')
}

onMounted(() => {
  fetchRoles()
  fetchPermissions()
})
</script>

<style scoped lang="scss">
.role-management {
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
  
  .gradient-btn {
    background: var(--primary-gradient) !important;
    border: none !important;
  }
  
  .filter-section {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    padding: 16px 20px;
    margin-bottom: 24px;
  }
  
  .filter-row {
    display: flex;
    gap: 12px;
    align-items: center;
  }
  
  .filter-item-wide {
    flex: 1;
    
    :deep(.el-input__wrapper) {
      border-radius: 8px;
    }
  }
  
  .filter-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
  
  .table-section {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    overflow: hidden;
    
    .el-table {
      --el-table-border-color: #f0f0f0;
      --el-table-header-bg-color: #fafafa;
      --el-table-header-text-color: #1a1a1a;
      --el-table-row-hover-bg-color: #f9f9f9;
      --el-table-cell-padding: 14px 12px;
      
      th {
        font-weight: 600;
        font-size: 13px;
      }
      
      td {
        font-size: 14px;
        color: #595959;
      }
    }
    
    .empty-state {
      padding: 40px 0;
    }
    
    .pagination-wrap {
      display: flex;
      justify-content: flex-end;
      padding: 16px 24px;
      border-top: 1px solid #f0f0f0;
    }
  }
}

.permission-dialog {
  :deep(.el-dialog__body) {
    max-height: 70vh;
    overflow-y: auto;
  }
}

.permission-section {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  
  .permission-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: var(--bg-hover);
    border-bottom: 1px solid var(--border-color);
    flex-wrap: wrap;
    gap: 12px;
    
    .toolbar-left,
    .toolbar-right {
      display: flex;
      gap: 8px;
      align-items: center;
    }
  }
  
  .selected-permissions {
    padding: 12px 16px;
    background: #f0f9ff;
    border-bottom: 1px solid var(--border-color);
    
    .selected-label {
      font-size: 13px;
      color: var(--text-secondary);
      margin-right: 8px;
    }
    
    .selected-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 8px;
      
      .selected-tag {
        .tag-name {
          font-weight: 500;
        }
        
        .tag-code {
          font-size: 10px;
          color: var(--text-secondary);
          margin-left: 4px;
          opacity: 0.7;
        }
      }
    }
  }
  
  .permission-tree-wrapper {
    padding: 16px;
    max-height: 400px;
    overflow-y: auto;
  }
  
  .permission-tree {
    :deep(.el-tree-node__content) {
      height: 36px;
      border-radius: 6px;
      transition: background 0.2s;
      
      &:hover {
        background: var(--bg-hover);
      }
    }
    
    :deep(.el-tree-node.is-expanded > .el-tree-node__children) {
      padding-left: 12px;
    }
  }
  
  .tree-node-content {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 8px;
    border-radius: 4px;
    transition: all 0.2s;
    
    &:hover {
      background: var(--bg-hover);
    }
    
    &.is-selected {
      background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%);
    }
    
    .group-icon,
    .perm-icon {
      font-size: 16px;
      color: var(--text-secondary);
    }
    
    .group-icon {
      color: #f59e0b;
    }
    
    .node-label {
      font-size: 14px;
      color: var(--text-primary);
      font-weight: 500;
    }
    
    .info-icon {
      font-size: 14px;
      color: var(--text-secondary);
      cursor: help;
    }
    
    .node-code {
      font-size: 11px;
      color: var(--text-secondary);
      background: var(--bg-hover);
      padding: 1px 6px;
      border-radius: 4px;
      margin-left: 6px;
    }
    
    .node-count {
      font-size: 12px;
      color: var(--text-secondary);
      margin-left: 4px;
    }
  }
}
</style>
