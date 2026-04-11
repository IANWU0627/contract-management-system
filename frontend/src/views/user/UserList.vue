<template>
  <div class="user-list">
    <div class="page-header">
      <h1 class="page-title">{{ $t('user.title') }}</h1>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        {{ $t('user.create') }}
      </el-button>
    </div>
    
    <!-- 筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="query.keyword"
            :placeholder="$t('user.placeholder.search')"
            clearable
            @keyup.enter="fetchData"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 160px;">
          <el-select v-model="query.role" clearable :placeholder="$t('user.role')" @change="fetchData">
            <el-option v-for="role in roles" :key="role.code" :label="locale === 'en' && role.nameEn ? role.nameEn : role.name" :value="role.code" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="fetchData" class="gradient-btn">
            <el-icon><Search /></el-icon>
            {{ $t('common.search') }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            {{ $t('common.reset') }}
          </el-button>
        </div>
      </div>
    </div>
    
    <el-card shadow="hover">
      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="username" :label="$t('user.username')" width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" :label="$t('user.nickname')" width="120" show-overflow-tooltip />
        <el-table-column prop="email" :label="$t('user.email')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="phone" :label="$t('user.phone')" width="120" show-overflow-tooltip />
        <el-table-column prop="role" :label="$t('user.role')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" size="small">{{ formatRole(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('user.status')" width="80" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? $t('user.statusEnabled') : $t('user.statusDisabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('contract.createdAt')" width="160" show-overflow-tooltip />
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
                  <el-dropdown-item command="toggleStatus">
                    {{ row.status === 'active' ? $t('user.statusDisabled') : $t('user.statusEnabled') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided type="danger">{{ $t('common.delete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </el-card>
    
    <!-- 用户表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? $t('user.edit') : $t('user.create')" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('user.username')" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('user.nickname')" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item :label="$t('user.email')" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item :label="$t('user.phone')" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item :label="$t('user.role')" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option v-for="role in roles" :key="role.code" :label="locale === 'en' && role.nameEn ? role.nameEn : role.name" :value="role.code" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('user.password')" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'
import { getActiveRoles } from '@/api/role'
import { Plus, Edit, Delete, Check, Close, ArrowDown, Search, Refresh } from '@element-plus/icons-vue'

const { t, locale } = useI18n()
const loading = ref(false)
const users = ref<any[]>([])
const total = ref(0)
const roles = ref<any[]>([])

const query = reactive({ page: 1, pageSize: 10, role: '', keyword: '' })

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  id: null as number | null,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  role: 'USER',
  password: ''
})

const rules = {
  username: [{ required: true, message: t('user.error.username'), trigger: 'blur' }, { min: 3, max: 20, message: t('user.error.usernameLength'), trigger: 'blur' }],
  nickname: [{ required: true, message: t('user.error.nickname'), trigger: 'blur' }],
  email: [{ required: false, type: 'email', message: t('user.error.email'), trigger: 'blur' }],
  phone: [{ required: false, pattern: /^1[3-9]\d{9}$/, message: t('user.error.phone'), trigger: 'blur' }],
  role: [{ required: true, message: t('user.error.role'), trigger: 'change' }],
  password: [{ required: true, message: t('user.error.password'), trigger: 'blur' }, { min: 6, message: t('user.error.passwordMin'), trigger: 'blur' }]
}

const formatRole = (role: string) => {
  const map: Record<string, string> = { 
    ADMIN: t('user.roles.admin'), 
    LEGAL: t('user.roles.legal'), 
    USER: t('user.roles.user') 
  }
  return map[role] || role
}

const getRoleTagType = (role: string) => {
  const map: Record<string, string> = { ADMIN: 'danger', LEGAL: 'warning', USER: '' }
  return map[role] || ''
}

const fetchRoles = async () => {
  try {
    const res = await getActiveRoles()
    roles.value = res.data || []
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList(query)
    if (res.data) {
      users.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      users.value = []
      total.value = 0
    }
  } catch (error) {
    users.value = []
    total.value = 0
  }
  finally { loading.value = false }
}

const handleReset = () => {
  query.keyword = ''
  query.role = ''
  query.page = 1
  fetchData()
}

const handleCreate = () => {
  isEdit.value = false
  form.id = null
  form.username = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  form.role = 'USER'
  form.password = ''
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.nickname = row.nickname
  form.email = row.email
  form.phone = row.phone
  form.role = row.role
  form.password = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      if (isEdit.value) {
        await updateUser(form.id!, {
          username: form.username,
          nickname: form.nickname,
          email: form.email,
          phone: form.phone,
          role: form.role
        })
        ElMessage.success(t('common.success'))
      } else {
        await createUser(form)
        ElMessage.success(t('common.success'))
      }
      dialogVisible.value = false
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.message || t('common.error'))
    } finally {
      loading.value = false
    }
  })
}

const handleAction = (command: string, row: any) => {
  switch (command) {
    case 'edit':
      handleEdit(row)
      break
    case 'toggleStatus':
      handleStatusChange(row)
      break
    case 'delete':
      handleDelete(row.id)
      break
  }
}

const handleStatusChange = async (row: any) => {
  try {
    const newStatus = row.status === 'active' ? 'inactive' : 'active'
    await updateUser(row.id, { status: newStatus })
    row.status = newStatus
    ElMessage.success(newStatus === 'active' ? t('user.statusEnabled') : t('user.statusDisabled'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(t('user.confirmDelete'), t('common.warning'), { type: 'warning' })
    await deleteUser(id)
    ElMessage.success(t('common.success'))
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || t('common.error'))
  }
}

onMounted(async () => {
  await fetchRoles()
  fetchData()
})

onUnmounted(() => {
  // 清理资源
  loading.value = false
  users.value = []
  total.value = 0
  dialogVisible.value = false
})
</script>

<style scoped lang="scss">
.gradient-btn {
  background: var(--primary-gradient) !important;
  border: none !important;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  }
  
  .el-icon {
    margin-right: 4px;
  }
}

/* 筛选区 */
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

.user-list {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
    .page-title { font-size: 24px; font-weight: 700; margin: 0; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
  }
  .pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
}
</style>
