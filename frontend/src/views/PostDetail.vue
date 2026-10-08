<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import CommentItem from '@/components/CommentItem.vue';
import { usePostStore } from '@/stores/post';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const postStore = usePostStore();
const userStore = useUserStore();
const commentForm = reactive({ content: '' });
const commenting = ref(false);

const postId = computed(() => Number(route.params.id));
const canModerate = computed(
  () =>
    userStore.isAdmin ||
    (userStore.profile?.id != null &&
      (userStore.profile.id === postStore.detail?.authorId || userStore.profile.role === 'ADMIN')),
);

async function loadData() {
  await postStore.fetchPostDetail(postId.value);
  await postStore.fetchComments(postId.value);
}

async function submitComment() {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  commenting.value = true;
  try {
    await postStore.submitComment(postId.value, { content: commentForm.content });
    ElMessage.success('评论成功');
    commentForm.content = '';
    await loadData();
  } finally {
    commenting.value = false;
  }
}

async function removeComment(id: number) {
  await postStore.removeComment(id);
  ElMessage.success('评论已删除');
  await loadData();
}

onMounted(loadData);
</script>

<template>
  <div v-if="postStore.detail" class="space-y-8">
    <section class="panel-surface p-8">
      <div class="mb-6 flex flex-wrap items-start justify-between gap-4">
        <div>
          <p class="text-sm uppercase tracking-[0.3em] text-moss">{{ postStore.detail.status }}</p>
          <h1 class="mt-3 font-display text-3xl font-semibold text-ink">{{ postStore.detail.title }}</h1>
        </div>
        <div class="rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate">
          <p>作者：{{ postStore.detail.authorUsername || '未知用户' }}</p>
          <p class="mt-1">发布时间：{{ new Date(postStore.detail.createdAt).toLocaleString() }}</p>
        </div>
      </div>

      <div class="whitespace-pre-wrap text-base leading-8 text-slate">
        {{ postStore.detail.content }}
      </div>

      <div class="mt-8 grid gap-4 md:grid-cols-4">
        <div class="rounded-2xl bg-slate-50 p-4">
          <p class="text-xs text-slate">浏览数</p>
          <p class="mt-2 text-2xl font-semibold text-ink">{{ postStore.detail.viewCount }}</p>
        </div>
        <div class="rounded-2xl bg-slate-50 p-4">
          <p class="text-xs text-slate">评论数</p>
          <p class="mt-2 text-2xl font-semibold text-ink">{{ postStore.detail.commentCount }}</p>
        </div>
        <div class="rounded-2xl bg-slate-50 p-4">
          <p class="text-xs text-slate">正向概率</p>
          <p class="mt-2 text-2xl font-semibold text-ink">
            {{ postStore.detail.positiveProb != null ? `${(postStore.detail.positiveProb * 100).toFixed(1)}%` : '-' }}
          </p>
        </div>
        <div class="rounded-2xl bg-slate-50 p-4">
          <p class="text-xs text-slate">负向概率</p>
          <p class="mt-2 text-2xl font-semibold text-ink">
            {{ postStore.detail.negativeProb != null ? `${(postStore.detail.negativeProb * 100).toFixed(1)}%` : '-' }}
          </p>
        </div>
      </div>
    </section>

    <section class="panel-surface p-8">
      <div class="mb-5 flex items-center justify-between">
        <h2 class="section-title">发表评论</h2>
        <span class="text-sm text-slate">评论也会经过情感分析与审核流程</span>
      </div>
      <template v-if="userStore.isLoggedIn">
        <el-input v-model="commentForm.content" type="textarea" :rows="4" resize="none" placeholder="说点什么吧" />
        <div class="mt-4 flex justify-end">
          <el-button type="primary" round :loading="commenting" @click="submitComment">提交评论</el-button>
        </div>
      </template>
      <el-empty v-else description="登录后才能发表评论" />
    </section>

    <section class="panel-surface p-8">
      <div class="mb-6 flex items-center justify-between">
        <h2 class="section-title">评论列表</h2>
        <el-button plain round @click="loadData">刷新</el-button>
      </div>
      <div v-if="postStore.comments.length" class="space-y-4">
        <CommentItem
          v-for="comment in postStore.comments"
          :key="comment.id"
          :comment="comment"
          :can-delete="canModerate"
          @delete="removeComment"
        />
      </div>
      <el-empty v-else description="暂无评论" />
    </section>
  </div>
</template>
