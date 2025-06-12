<template>
  <div class="custom-pagination">
    <div class="pagination-info">共 {{ total }} 条</div>
    <div class="pagination-controls">
      <button
        class="pagination-btn"
        :disabled="currentPage === 1"
        @click="changePage(currentPage - 1)"
      >
        &lt;
      </button>

      <template v-for="page in visiblePages" :key="page">
        <button v-if="page === '...'" class="pagination-ellipsis" disabled>...</button>
        <button
          v-else
          class="pagination-btn"
          :class="{ active: page === currentPage }"
          @click="changePage(page)"
        >
          {{ page }}
        </button>
      </template>

      <button
        class="pagination-btn"
        :disabled="currentPage === totalPages"
        @click="changePage(currentPage + 1)"
      >
        >
      </button>

      <select class="page-size-select" v-model="localPageSize" @change="handlePageSizeChange">
        <option v-for="option in pageSizeOptions" :value="option" :key="option">
          {{ option }}条/页
        </option>
      </select>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

const props = defineProps({
  current: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
  pageSizeOptions: { type: Array<string>, default: () => ['10', '20', '30', '40'] },
})

const emit = defineEmits<{ (e: 'change', current: number, pageSize: number): void }>()

const currentPage = ref(props.current)
const localPageSize = ref(props.pageSize)

const totalPages = computed(() => {
  return Math.ceil(props.total / localPageSize.value)
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let startPage = 1
  let endPage = totalPages.value

  if (totalPages.value > maxVisible) {
    startPage = Math.max(currentPage.value - 2, 1)
    endPage = Math.min(currentPage.value + 2, totalPages.value)

    if (startPage > 1) {
      pages.push(1)
      if (startPage > 2) {
        pages.push('...')
      }
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i)
    }

    if (endPage < totalPages.value) {
      if (endPage < totalPages.value - 1) {
        pages.push('...')
      }
      pages.push(totalPages.value)
    }
  } else {
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push(i)
    }
  }

  return pages
})

watch(
  () => props.current,
  (newVal) => {
    currentPage.value = newVal
  },
)

watch(
  () => props.pageSize,
  (newVal) => {
    localPageSize.value = newVal
  },
)

const changePage = (page: number | string) => {
  if (typeof page === 'string' || page < 1 || page > totalPages.value || page === currentPage.value)
    return
  currentPage.value = page
  emit('change', currentPage.value, localPageSize.value)
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  emit('change', currentPage.value, localPageSize.value)
}
</script>

<style scoped>
.custom-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
  padding: 10px;
  background: #181818;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.pagination-info {
  font-size: 14px;
  color: #a3a0a0;
  font-weight: bold;
  margin-right: 10px;
}

.pagination-controls {
  display: flex;
  gap: 5px;
}

/**
按钮标准
*/
.pagination-btn {
  min-width: 40px;
  height: 40px;
  padding: 0 10px;
  border: 2px solid #333;
  background: #565555;
  color: #333;
  font-size: 16px;
  font-weight: bold;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  background: #f0f0f0;
  border-color: #666;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn.active {
  background: #ac0cd0;
  color: #fff;
  border-color: #ac0cd0;
  box-shadow: 0 0 10px rgba(24, 144, 255, 0.5);
  transform: scale(1.05);
}

.pagination-ellipsis {
  min-width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #565555;
  color: #333;
  font-size: 16px;
  font-weight: bold;
  border-radius: 4px;
}

.page-size-select {
  height: 40px;
  padding: 0 10px;
  border: 2px solid #333;
  background: #565555;
  color: #333;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  margin-left: 10px;
  cursor: pointer;
}

@media (max-width: 768px) {
  .custom-pagination {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
