<script setup lang="ts">
import { onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAdminStore } from '@/stores/admin';

const adminStore = useAdminStore();

onMounted(() => {
  adminStore.fetchPosts();
});

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该帖子吗？该操作不可恢复。', '删除帖子', { type: 'warning' });
  await adminStore.submitDeletePost(id);
  ElMessage.success('帖子已删除');
  await adminStore.fetchPosts();
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="font-display text-3xl font-semibold text-ink">帖子管理</h1>
      <el-button plain round @click="adminStore.fetchPosts()">刷新列表</el-button>
    </div>

    <div class="panel-surface p-4">
      <el-table :data="adminStore.posts" stripe>
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="authorUsername" label="作者" min-width="120" />
        <el-table-column prop="status" label="状态" min-width="120" />
        <el-table-column label="情感" min-width="150">
          <template #default="{ row }">
            {{ row.negativeProb != null ? `负向 ${(row.negativeProb * 100).toFixed(1)}%` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" min-width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="120">
          <template #default="{ row }">
            <el-button text type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
