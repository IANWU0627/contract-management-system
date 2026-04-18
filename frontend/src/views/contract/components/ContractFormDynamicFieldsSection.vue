<template>
  <div v-if="dynamicFields.length > 0" class="dynamic-fields-section">
    <div class="section-title-row">
      <div class="section-title">
        <el-icon><Tickets /></el-icon>
        <span>{{ $t('typeFieldConfig.dynamicFields') }}</span>
        <el-tag size="small" type="info">{{ dynamicFields.length }}</el-tag>
      </div>
    </div>
    <el-row :gutter="20">
      <el-col v-for="field in dynamicFields" :key="field.id" :span="8" :xs="24" :sm="12" :md="8">
        <el-form-item :label="locale === 'zh' ? field.fieldLabel : (field.fieldLabelEn || field.fieldLabel)" :required="field.required">
          <el-input v-if="field.fieldType === 'text'" v-model="dynamicFieldValues[field.fieldKey]" :placeholder="field.placeholder" clearable />
          <el-input-number v-else-if="field.fieldType === 'number'" v-model="dynamicFieldValues[field.fieldKey]" :min="0" style="width: 100%" />
          <el-date-picker v-else-if="field.fieldType === 'date'" v-model="dynamicFieldValues[field.fieldKey]" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          <el-select v-else-if="field.fieldType === 'select'" v-model="dynamicFieldValues[field.fieldKey]" style="width: 100%" clearable>
            <el-option v-for="opt in getSelectOptions(field)" :key="opt.code" :label="opt.displayLabel" :value="opt.code" />
          </el-select>
          <el-select v-else-if="field.fieldType === 'multiselect'" v-model="dynamicFieldValues[field.fieldKey]" multiple style="width: 100%" clearable collapse-tags collapse-tags-tooltip>
            <el-option v-for="opt in getSelectOptions(field)" :key="opt.code" :label="opt.displayLabel" :value="opt.code" />
          </el-select>
          <el-input v-else v-model="dynamicFieldValues[field.fieldKey]" />
        </el-form-item>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { Tickets } from '@element-plus/icons-vue'
import type { ContractTypeField } from '@/api/contractTypeField'

type DynamicFieldValue = string | number | string[] | null

type SelectOption = {
  code: string
  meaning: string
  meaningEn?: string
  enabled?: boolean
  displayLabel: string
}

defineProps<{
  dynamicFields: ContractTypeField[]
  dynamicFieldValues: Record<string, DynamicFieldValue>
  locale: string
  getSelectOptions: (field: ContractTypeField) => SelectOption[]
}>()
</script>

<style scoped lang="scss">
.dynamic-fields-section {
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
  margin: 20px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}
</style>
