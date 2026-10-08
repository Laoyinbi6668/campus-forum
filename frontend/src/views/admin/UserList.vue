<script setup lang="ts">
import { onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useAdminStore } from '@/stores/admin';

const adminStore = useAdminStore();

onMounted(() => {
  adminStore.fetchUsers();
});

async function toggleBan(id: number, status: string) {
  if (status === 'BANNED') {
    await adminStore.submitUnban(id);
    ElMessage.success('用户已解封');
  } else {
    await adminStore.submitBan(id);
    ElMessage.success('用户已封禁');
  }
  await adminStore.fetchUsers();
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="font-display text-3xl font-semibold text-ink">用户管理</h1>
      <el-button plain round @click="adminStore.fetchUsers()">刷新列表</el-button>
    </div>

    <div class="panel-surface p-4">
      <el-table :data="adminStore.users" stripe>
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column prop="role" label="角色" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="110" />
        <el-table-column prop="violationCount" label="违规次数" min-width="110" />
        <el-table-column label="注册时间" min-width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="120">
          <template #default="{ row }">
            <el-button
              text
              :type="row.status === 'BANNED' ? 'success' : 'danger'"
              @click="toggleBan(row.id, row.status)"
            >
              {{ row.status === 'BANNED' ? '解封' : '封禁' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
