<script setup lang="ts">
import type { WarningItem as Warning } from '@/types';

defineProps<{
  warning: Warning;
}>();
</script>

<template>
  <div class="rounded-3xl border border-red-100 bg-red-50/80 p-5">
    <div class="mb-3 flex items-center justify-between gap-4">
      <div>
        <p class="font-semibold text-red-700">{{ warning.contentType }} #{{ warning.contentId }}</p>
        <p class="text-xs text-red-500">{{ new Date(warning.createdAt).toLocaleString() }}</p>
      </div>
      <span class="rounded-full bg-white px-3 py-1 text-xs font-semibold text-red-600">
        负向 {{ (warning.negativeProb * 100).toFixed(1) }}%
      </span>
    </div>
    <p class="text-sm leading-6 text-slate">{{ warning.content }}</p>
  </div>
</template>
