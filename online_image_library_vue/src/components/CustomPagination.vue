<template>
  <a-pagination
    v-model:current="currentPage"
    v-model:pageSize="pageSize"
    :total="total"
    :pageSizeOptions="pageSizeOptions"
    show-size-changer
    @change="handlePageChange"
  />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps({
  current: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
  pageSizeOptions: { type: Array<string>, default: () => ['10', '20', '30', '40'] },
})

const emit = defineEmits<{ (e: 'change', current: number, pageSize: number): void }>()

const currentPage = ref(props.current)
const pageSize = ref(props.pageSize)

watch(
  () => props.current,
  (newVal) => {
    currentPage.value = newVal
  },
)

watch(
  () => props.pageSize,
  (newVal) => {
    pageSize.value = newVal
  },
)

const handlePageChange = (current: number, size: number) => {
  emit('change', current, size)
}
</script>

<style scoped>
:deep(.ant-pagination .ant-pagination-item-link),
:deep(.ant-pagination .ant-pagination-item) {
  background: #ffffff !important;
  border: 1px solid #000000 !important;
  color: #000000 !important;
}

:deep(.ant-pagination .ant-pagination-item-link-icon) {
  color: #000000 !important;
}

:deep(.ant-pagination .ant-pagination-item:hover),
:deep(.ant-pagination .ant-pagination-item-link:hover) {
  background: #f0f0f0 !important;
  border-color: var(--primary-color) !important;
  color: var(--primary-color) !important;
}

:deep(.ant-pagination .ant-pagination-item-active) {
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color)) !important;
  border-color: transparent !important;
  color: #fff !important;
}

:deep(.ant-pagination-options .ant-select-selector) {
  background: #ffffff !important;
  border-color: #000000 !important;
  color: #000000 !important;
}

:deep(.ant-pagination-total-text) {
  color: #fff;
}
</style>
