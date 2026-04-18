<template>
  <div class="info-section">
    <div class="section-title-row">
      <div class="section-title">
        <el-icon><Collection /></el-icon>
        <span>{{ $t('common.basicInfo') }}</span>
      </div>
      <div class="meta-info-bar">
        <span class="meta-item">
          <span class="meta-label">{{ $t('contract.no') }}:</span>
          <span class="meta-value contract-no">{{ form.contractNo }}</span>
        </span>
        <span class="meta-separator">|</span>
        <span class="meta-item">
          <span class="meta-label">{{ $t('contract.createdBy') }}:</span>
          <span class="meta-value">{{ form.createdBy || $t('contract.placeholder.currentUser') }}</span>
        </span>
        <span class="meta-separator">|</span>
        <span class="meta-item">
          <span class="meta-label">{{ $t('contract.timezone') }}:</span>
          <span class="meta-value">{{ form.timezone }}</span>
        </span>
        <span class="meta-separator">|</span>
        <span class="meta-item">
          <span class="meta-label">{{ $t('contract.createdAt') }}:</span>
          <span class="meta-value">{{ form.createdAt }}</span>
        </span>
      </div>
    </div>
    <el-row :gutter="20">
      <el-col :span="24">
        <el-form-item :label="$t('contract.name')" prop="title">
          <el-input v-model="form.title" :placeholder="$t('contract.placeholder.name')" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="8" :xs="24" :sm="12" :md="8">
        <el-form-item :label="$t('contract.type')" prop="type">
          <el-select v-model="form.type" :placeholder="$t('common.placeholder.select')" style="width: 100%" @change="$emit('type-change')">
            <el-option v-for="cat in categories" :key="cat.code" :label="locale === 'en' && cat.nameEn ? cat.nameEn : cat.name" :value="cat.code" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="8" :xs="24" :sm="12" :md="8">
        <el-form-item :label="$t('contract.amount')" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="8" :xs="24" :sm="12" :md="8">
        <el-form-item :label="$t('contract.currency')" prop="currency">
          <el-select v-model="form.currency" style="width: 100%">
            <el-option v-for="item in currencyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="8" :xs="24" :sm="12" :md="8">
        <el-form-item :label="$t('contract.startDate')" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" :placeholder="$t('contract.placeholder.startDate')" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="8" :xs="24" :sm="12" :md="8">
        <el-form-item :label="$t('contract.endDate')" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" :placeholder="$t('contract.placeholder.endDate')" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="8" :xs="24" :sm="12" :md="8">
        <el-form-item :label="$t('contract.folder')">
          <el-select v-model="form.folderId" clearable :placeholder="$t('contract.placeholder.folder')" style="width: 100%">
            <el-option v-for="folder in folders" :key="folder.id" :label="folder.name" :value="folder.id">
              <span :style="{ color: folder.color }">●</span> {{ folder.name }}
            </el-option>
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="8" :xs="24" :sm="12" :md="8">
        <el-form-item :label="$t('contract.relationType')">
          <el-select v-model="form.relationType" style="width: 100%">
            <el-option :label="$t('contract.relationTypes.main')" value="MAIN" />
            <el-option :label="$t('contract.relationTypes.supplement')" value="SUPPLEMENT" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="16" :xs="24" :sm="12" :md="16" v-if="form.relationType === 'SUPPLEMENT'">
        <el-form-item :label="$t('contract.parentContract')">
          <el-select v-model="form.parentContractId" filterable clearable :placeholder="$t('contract.placeholder.parentContract')" style="width: 100%">
            <el-option
              v-for="item in parentContractOptions"
              :key="item.id"
              :label="`${item.contractNo} - ${item.title}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <div class="remark-section">
      <el-form-item :label="$t('contract.remark')">
        <el-input v-model="form.remark" type="textarea" :rows="2" :placeholder="$t('contract.placeholder.remark')" />
      </el-form-item>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Collection } from '@element-plus/icons-vue'
import type { ContractCategory } from '@/api/contractCategory'
import type { Contract } from '@/api/contract'

export interface ContractFolderRow {
  id: number
  name: string
  color?: string
}

export interface BasicFormFields {
  title: string
  type: string
  amount: number
  currency: string
  startDate: string
  endDate: string
  remark: string
  contractNo: string
  createdBy: string
  timezone: string
  createdAt: string
  folderId: number | null
  relationType: 'MAIN' | 'SUPPLEMENT'
  parentContractId: number | null
}

defineProps<{
  form: BasicFormFields
  categories: ContractCategory[]
  folders: ContractFolderRow[]
  parentContractOptions: Contract[]
  locale: string
  currencyOptions: { value: string; label: string }[]
}>()

defineEmits<{
  'type-change': []
}>()
</script>

<style scoped lang="scss">
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  flex-shrink: 0;
}

.section-title-row {
  display: flex;
  align-items: center;
  margin: 20px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.meta-info-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  padding: 0;
  background: transparent;
  margin-left: 20px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  white-space: nowrap;
}

.meta-separator {
  margin: 0 12px;
  color: var(--border-color);
}

.meta-label {
  color: var(--text-secondary);
}

.contract-no {
  font-family: monospace;
  font-weight: 600;
  color: var(--primary);
}

.remark-section {
  margin: 20px 0;
}
</style>
