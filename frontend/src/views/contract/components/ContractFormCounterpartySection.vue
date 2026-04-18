<template>
  <div class="counterparty-section">
    <div class="section-title-row">
      <div class="section-title">
        <el-icon><User /></el-icon>
        <span>{{ $t('contract.counterparts') }}</span>
        <el-tag size="small" type="info">{{ counterparties.length }}</el-tag>
      </div>
      <el-button type="primary" size="small" @click="$emit('add')" class="add-btn">
        <el-icon><Plus /></el-icon>
        {{ $t('common.add') }}
      </el-button>
    </div>
    <el-table :data="counterparties" border class="counterparty-table">
      <el-table-column :label="$t('contract.counterpartyLabel')" min-width="140">
        <template #default="{ row, $index }">
          <div class="cp-type-cell">
            <el-tag :type="getPartyTagType(row.type)" effect="dark" size="small" round>
              {{ getPartyLabel(row.type) }}
            </el-tag>
            <el-button type="danger" text size="small" @click="$emit('remove', $index)" v-if="counterparties.length > 1">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="$t('contract.placeholder.companyName')" min-width="200">
        <template #default="{ row }">
          <el-input v-model="row.name" :placeholder="$t('contract.placeholder.companyName')" />
        </template>
      </el-table-column>
      <el-table-column :label="$t('contract.counterparty.contact')" min-width="120">
        <template #default="{ row }">
          <el-input v-model="row.contact" :placeholder="$t('contract.placeholder.contact')" />
        </template>
      </el-table-column>
      <el-table-column :label="$t('contract.counterparty.phone')" min-width="140">
        <template #default="{ row }">
          <el-input v-model="row.phone" :placeholder="$t('contract.placeholder.phone')" />
        </template>
      </el-table-column>
      <el-table-column :label="$t('contract.counterparty.email')" min-width="180">
        <template #default="{ row }">
          <el-input v-model="row.email" :placeholder="$t('contract.placeholder.email')" />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { User, Plus, Delete } from '@element-plus/icons-vue'
import { useContractPartyLabels } from '@/views/contract/composables/useContractPartyLabels'

export interface CounterpartyRow {
  type: string
  name: string
  contact: string
  phone: string
  email: string
}

defineProps<{
  counterparties: CounterpartyRow[]
}>()

defineEmits<{
  add: []
  remove: [index: number]
}>()

const { getPartyLabel, getPartyTagType } = useContractPartyLabels()
</script>

<style scoped lang="scss">
.counterparty-section {
  margin: 0;
}

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
  justify-content: space-between;
  margin: 20px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.counterparty-table {
  margin-top: 16px;

  :deep(.el-input__wrapper) {
    box-shadow: none !important;
    background: transparent;
    padding: 0;
  }

  :deep(.el-input) {
    --el-input-border-color: transparent;
  }

  :deep(.el-input:hover .el-input__wrapper) {
    box-shadow: 0 0 0 1px var(--el-border-color) !important;
  }

  :deep(.el-input.is-focus .el-input__wrapper) {
    box-shadow: 0 0 0 1px var(--el-color-primary) !important;
  }
}

.cp-type-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-btn {
  margin-left: 8px;
}
</style>
