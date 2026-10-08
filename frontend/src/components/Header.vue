<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const adminLinks = [
  { label: '概览', path: '/admin/dashboard' },
  { label: '预警审核', path: '/admin/warnings' },
  { label: '帖子管理', path: '/admin/posts' },
  { label: '用户管理', path: '/admin/users' },
];

const navLinks = computed(() => [
  { label: '首页', path: '/' },
  { label: '发布帖子', path: '/posts/create', auth: true },
  ...(userStore.isAdmin ? [{ label: '管理后台', path: '/admin/dashboard' }] : []),
]);

function logout() {
  userStore.logout();
  ElMessage.success('已退出登录');
  router.push('/');
}
</script>

<template>
  <header class="sticky top-0 z-30 border-b border-white/60 bg-mist/75 backdrop-blur">
    <div class="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
      <RouterLink to="/" class="flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-ink text-lg font-bold text-white">
          论
        </div>
        <div>
          <p class="font-display text-lg font-semibold text-ink">校园论坛智能管理系统</p>
          <p class="text-xs text-slate">情感分析驱动的内容治理平台</p>
        </div>
      </RouterLink>

      <nav class="hidden items-center gap-2 md:flex">
        <RouterLink
          v-for="item in navLinks"
          :key="item.path"
          :to="item.path"
          class="rounded-full px-4 py-2 text-sm font-medium transition hover:bg-white/80"
          :class="route.path === item.path ? 'bg-ink text-white' : 'text-slate'"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="flex items-center gap-2">
        <template v-if="userStore.isLoggedIn">
          <div class="hidden text-right sm:block">
            <p class="text-sm font-semibold text-ink">{{ userStore.profile?.username }}</p>
            <p class="text-xs text-slate">{{ userStore.profile?.role }}</p>
          </div>
          <el-button plain round @click="logout">退出</el-button>
        </template>
        <template v-else>
          <el-button round @click="router.push('/login')">登录</el-button>
          <el-button type="primary" round @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>

    <div v-if="route.path.startsWith('/admin')" class="border-t border-white/60 bg-white/60">
      <div class="mx-auto flex max-w-7xl gap-2 overflow-x-auto px-4 py-3 sm:px-6 lg:px-8">
        <RouterLink
          v-for="item in adminLinks"
          :key="item.path"
          :to="item.path"
          class="whitespace-nowrap rounded-full px-4 py-2 text-sm font-medium transition hover:bg-white"
          :class="route.path === item.path ? 'bg-ember text-white' : 'bg-white/70 text-slate'"
        >
          {{ item.label }}
        </RouterLink>
      </div>
    </div>
  </header>
</template>
