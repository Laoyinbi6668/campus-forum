<script setup lang="ts">
import { computed } from 'vue';
import type { PostItem } from '@/types';

const props = defineProps<{
  post: PostItem;
}>();

const sentimentLabel = computed(() => {
  if (props.post.negativeProb && props.post.negativeProb > 0.7) return '高风险';
  if (props.post.sentiment === 2) return '正向';
  if (props.post.sentiment === 1) return '中性';
  return '待审核';
});
</script>

<template>
  <RouterLink :to="`/posts/${post.id}`" class="panel-surface block p-6 transition duration-200 hover:-translate-y-1 hover:shadow-xl">
    <div class="mb-4 flex items-start justify-between gap-4">
      <div>
        <h3 class="font-display text-xl font-semibold text-ink">{{ post.title }}</h3>
        <p class="mt-2 text-sm leading-6 text-slate">
          {{ post.content }}
        </p>
      </div>
      <span
        class="rounded-full px-3 py-1 text-xs font-semibold"
        :class="post.status === 'PUBLISHED' ? 'bg-moss/15 text-moss' : post.status === 'REJECTED' ? 'bg-red-100 text-red-600' : 'bg-gold/20 text-amber-700'"
      >
        {{ post.status }}
      </span>
    </div>
    <div class="flex flex-wrap items-center gap-3 text-sm text-slate">
      <span>{{ post.authorUsername || '匿名' }}</span>
      <span>{{ new Date(post.createdAt).toLocaleString() }}</span>
      <span>浏览 {{ post.viewCount }}</span>
      <span>评论 {{ post.commentCount }}</span>
      <span class="rounded-full bg-ink/5 px-2 py-1 text-xs">{{ sentimentLabel }}</span>
    </div>
  </RouterLink>
</template>
