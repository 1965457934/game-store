<template>
  <div class="admin-page order-manage">
    <div class="table-wrapper">
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : (row.status === 1 ? 'success' : 'info')">
              {{ row.status === 0 ? '待支付' : (row.status === 1 ? '已支付' : '已取消') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="150" />
      </el-table>
      
      <!-- 分页 -->
      <el-pagination 
        v-if="total > 0"
        v-model:current-page="page" 
        v-model:page-size="size" 
        :total="total" 
        layout="prev, pager, next" 
        @current-change="loadOrders"
        class="pagination"
      />
      
      <!-- 空状态 -->
      <div v-if="orders.length === 0" class="empty-state">
        <el-icon :size="48" color="#606266"><ShoppingCart /></el-icon>
        <p>暂无订单数据</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ShoppingCart } from '@element-plus/icons-vue'
import { getOrderList } from '../../api/order'

export default {
  name: 'OrderManage',
  components: { ShoppingCart },
  setup() {
    const orders = ref([])
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)

    onMounted(() => {
      loadOrders()
    })

    const loadOrders = async () => {
      const res = await getOrderList({ page: page.value, size: size.value })
      orders.value = res.records
      total.value = res.total
    }

    return {
      orders,
      page,
      size,
      total,
      loadOrders
    }
  }
}
</script>

<style scoped>
.order-manage {
  padding: 20px;
  box-sizing: border-box;
}

.table-wrapper {
  background: #2a2f38;
  border: 1px solid #3d444d;
  border-radius: 8px;
  padding: 16px;
}

/* 表格深色主题 */
.table-wrapper :deep(.el-table) {
  background: #2a2f38 !important;
}

.table-wrapper :deep(.el-table__header-wrapper),
.table-wrapper :deep(.el-table__header) {
  background: #1e2329 !important;
}

.table-wrapper :deep(.el-table__header th) {
  background: #1e2329 !important;
  color: #fff !important;
  border-bottom: 1px solid #3d444d !important;
}

.table-wrapper :deep(.el-table__body-wrapper) {
  background: #2a2f38 !important;
}

.table-wrapper :deep(.el-table__body tr) {
  background: #2a2f38 !important;
}

.table-wrapper :deep(.el-table__body td) {
  background: #2a2f38 !important;
  color: #c0c0c0 !important;
  border-bottom: 1px solid #3d444d !important;
}

.table-wrapper :deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background: #353b45 !important;
}

.table-wrapper :deep(.el-table__empty-block) {
  background: #2a2f38 !important;
}

.table-wrapper :deep(.el-table__empty-text) {
  color: #808080 !important;
}

/* 分页深色主题 */
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.pagination :deep(.el-pager li) {
  background: #1e2329 !important;
  color: #c0c0c0 !important;
  border: 1px solid #3d444d;
}

.pagination :deep(.el-pager li.active) {
  background: #1a9fff !important;
  color: #fff !important;
  border-color: #1a9fff;
}

.pagination :deep(.el-pagination button) {
  background: #1e2329 !important;
  color: #c0c0c0 !important;
  border: 1px solid #3d444d;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-state p {
  margin-top: 16px;
  font-size: 14px;
}
</style>
