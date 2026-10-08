<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import PostCard from '@/components/PostCard.vue';
import { usePostStore } from '@/stores/post';
import { useUserStore } from '@/stores/user';

const postStore = usePostStore();
const userStore = useUserStore();
const router = useRouter();

const visiblePosts = computed(() =>
  userStore.isAdmin ? postStore.posts : postStore.posts.filter((item) => item.status === 'PUBLISHED'),
);

onMounted(() => {
  postStore.fetchPosts();
});

function handlePageChange(page: number) {
  postStore.fetchPosts(page);
}
</script>

<template>
  <div class="space-y-8">
    <section class="grid gap-6 lg:grid-cols-[1.35fr_0.65fr]">
      <div class="panel-surface overflow-hidden p-8">
        <p class="text-sm uppercase tracking-[0.35em] text-ember">Stage 7 Frontend</p>
        <h1 class="mt-4 max-w-2xl font-display text-4xl font-semibold leading-tight text-ink">
          用一个清晰的前端入口，把论坛互动、情感预警和管理员处置连成完整工作流。
        </h1>
        <p class="mt-5 max-w-2xl text-base leading-7 text-slate">
          普通用户可以注册、登录、发帖和评论；管理员可以查看统计、审核预警、管理帖子与用户。
        </p>
        <div class="mt-8 flex flex-wrap gap-3">
          <el-button type="primary" size="large" round @click="router.push('/posts/create')">发布帖子</el-button>
          <el-button size="large" round plain @click="router.push('/admin/dashboard')">查看管理后台</el-button>
        </div>
      </div>

      <div class="grid gap-4">
        <div class="panel-surface p-6">
          <p class="text-sm text-slate">当前展示</p>
          <p class="mt-3 font-display text-3xl font-semibold text-ink">{{ visiblePosts.length }}</p>
          <p class="mt-2 text-sm text-slate">本页帖子</p>
        </div>
        <div class="panel-surface p-6">
          <p class="text-sm text-slate">访问建议</p>
          <ul class="mt-3 space-y-2 text-sm leading-6 text-slate">
            <li>1. 新用户先注册并登录。</li>
            <li>2. 普通用户从首页进入帖子详情并评论。</li>
            <li>3. 管理员到后台完成预警审核与用户治理。</li>
          </ul>
        </div>
      </div>
    </section>

    <section class="space-y-5">
      <div class="flex items-center justify-between">
        <h2 class="section-title">最新帖子</h2>
        <el-button plain round @click="postStore.fetchPosts(postStore.currentPage)">刷新</el-button>
      </div>

      <el-empty v-if="!visiblePosts.length && !postStore.loading" description="暂无帖子数据" class="panel-surface py-12" />

      <div v-else class="grid gap-5">
        <PostCard v-for="post in visiblePosts" :key="post.id" :post="post" />
      </div>

      <div class="flex justify-end">
        <el-pagination
          layout="prev, pager, next, total"
          :current-page="postStore.currentPage"
          :page-size="postStore.pageSize"
          :total="postStore.total"
          background
          @current-change="handlePageChange"
        />
      </div>
    </section>
  </div>
</template>
