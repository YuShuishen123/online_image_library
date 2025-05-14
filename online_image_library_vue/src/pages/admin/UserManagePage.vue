<template>
  <div class="table-container">
    <a-form ref="formRef" layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button type="primary" html-type="submit" @click="resetSearchParams()">重置搜索参数</a-button>
        </a-space>
      </a-form-item>
    </a-form>
    <a-table :columns="columns" :data-source="dataList" :pagination="pagination" @loading="true"
      @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" width="32" height="32" />
          <a>
            {{ record.name }}
          </a>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <span v-if="record.userRole === 'admin'">
            <a-tag color="blue">管理员</a-tag>
          </span>
          <span v-else-if="record.userRole === 'user'">
            <a-tag color="gold">普通用户</a-tag>
          </span>
        </template>
        <template v-else-if="column.dataIndex === 'updateTime'">
          {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button danger @click="doDelete(record.id)">删除</a-button>
            <a-button type="primary">编辑</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>

</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { deleteUserUsingPost, getUserVobyIdUsingPost } from '@/api/userController'
import dayjs from 'dayjs'


const columns = [
  {
    title: 'Id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
// 数据
const dataList = ref<API.UserVO[]>([])
const total = ref(0)
// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 5,
  sortField: "updateTime",
  sortOrder: "desc",
})

// 删除数据
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingPost({ id })
  if (res.data.code === 200) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败,' + res.data.message)
  }
}

// 分页参数
const pagination = reactive({
  current: searchParams.current,
  pageSize: searchParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showQuickJumper: true,
  responsive: false,
  showTotal: (total: number) => `共 ${total} 条`,
})
// 获取数据
const fetchData = async () => {
  const res = await getUserVobyIdUsingPost({
    ...searchParams
  })
  if (res.data.data) {
    dataList.value = res.data.data.records as API.UserVO[] ?? []
    total.value = Number(res.data.data.total) || 0
    // 更新分页信息
    pagination.total = total.value
    pagination.current = Number(res.data.data.current) || searchParams.current
    pagination.pageSize = Number(res.data.data.size) || searchParams.pageSize
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}


// 添加表单ref
const formRef = ref()

// 搜索
const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

const resetSearchParams = () => {
  searchParams.userAccount = undefined
  searchParams.userName = undefined
  formRef.value.resetFields()
}


// 分页改变
const handleTableChange = (pag: { current: number, pageSize: number }) => {
  // 更新搜索参数
  searchParams.current = pag.current
  searchParams.pageSize = pag.pageSize
  // 更新分页参数
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})


</script>

<style scoped>
.table-container {
  width: 70%;
  margin: 0 auto;
  /* 居中显示 */
}
</style>
