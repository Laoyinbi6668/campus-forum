<script setup lang="ts">
import type { CommentItem as Comment } from '@/types';

defineProps<{
  comment: Comment;
  canDelete?: boolean;
}>();

defineEmits<{
  delete: [id: number];
}>();
</script>

<template>
  <div class="rounded-2xl border border-slate-100 bg-slate-50/80 p-4">
    <div class="flex items-start justify-between gap-4">
      <div>
        <p class="font-medium text-ink">{{ comment.authorUsername || '匿名用户' }}</p>
        <p class="mt-2 whitespace-pre-wrap text-sm leading-6 text-slate">{{ comment.content }}</p>
      </div>
      <el-button v-if="canDelete" text type="danger" @click="$emit('delete', comment.id)">删除</el-button>
    </div>
    <div class="mt-3 flex flex-wrap gap-3 text-xs text-slate">
      <span>{{ comment.status }}</span>
      <span>{{ new Date(comment.createdAt).toLocaleString() }}</span>
      <span v-if="comment.negativeProb != null">负向 {{ (comment.negativeProb * 100).toFixed(1) }}%</span>
    </div>
  </div>
</template>
