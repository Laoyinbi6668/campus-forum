<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { CanvasRenderer } from 'echarts/renderers';
import { LegendComponent, TooltipComponent } from 'echarts/components';
import { PieChart } from 'echarts/charts';
import { type ECharts, init, use } from 'echarts/core';
import { useAdminStore } from '@/stores/admin';

use([CanvasRenderer, TooltipComponent, LegendComponent, PieChart]);

const adminStore = useAdminStore();
const chartRef = ref<HTMLDivElement | null>(null);
let chart: ECharts | null = null;

function renderChart() {
  if (!chartRef.value || !adminStore.stats) return;
  chart ??= init(chartRef.value);
  chart.setOption({
    color: ['#3f6c51', '#e4b363', '#d65a31'],
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['42%', '72%'],
        label: { color: '#172033' },
        data: [
          { value: adminStore.stats.positivePostCount, name: '正向' },
          { value: adminStore.stats.neutralPostCount, name: '中性' },
          { value: adminStore.stats.negativePostCount, name: '负向' },
        ],
      },
    ],
  });
}

onMounted(async () => {
  await adminStore.fetchStats();
  await adminStore.fetchAuditRecords();
  await nextTick();
  renderChart();
  window.addEventListener('resize', renderChart);
});

watch(() => adminStore.stats, () => nextTick().then(renderChart));

onBeforeUnmount(() => {
  window.removeEventListener('resize', renderChart);
  chart?.dispose();
});
</script>

<template>
  <div class="space-y-8">
    <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-4" v-if="adminStore.stats">
      <div class="panel-surface p-6" v-for="item in [
        { label: '总帖子数', value: adminStore.stats.totalPostCount, tone: 'text-ink' },
        { label: '今日发帖', value: adminStore.stats.todayPostCount, tone: 'text-moss' },
        { label: '待审核帖子', value: adminStore.stats.pendingPostCount, tone: 'text-amber-600' },
        { label: '待处理预警', value: adminStore.stats.pendingWarningCount, tone: 'text-red-600' },
      ]" :key="item.label">
        <p class="text-sm text-slate">{{ item.label }}</p>
        <p class="mt-3 font-display text-4xl font-semibold" :class="item.tone">{{ item.value }}</p>
      </div>
    </section>

    <section class="grid gap-6 xl:grid-cols-[1fr_0.8fr]">
      <div class="panel-surface p-6">
        <div class="mb-4 flex items-center justify-between">
          <h2 class="section-title">情感分布</h2>
          <el-button plain round @click="adminStore.fetchStats">刷新统计</el-button>
        </div>
        <div ref="chartRef" class="h-[340px] w-full" />
      </div>

      <div class="panel-surface p-6">
        <h2 class="section-title">审核动态</h2>
        <div class="mt-5 space-y-4">
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-sm text-slate">用户总数</p>
            <p class="mt-2 text-2xl font-semibold text-ink">{{ adminStore.stats?.totalUserCount ?? 0 }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-sm text-slate">封禁用户</p>
            <p class="mt-2 text-2xl font-semibold text-red-600">{{ adminStore.stats?.bannedUserCount ?? 0 }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-sm text-slate">总审核记录</p>
            <p class="mt-2 text-2xl font-semibold text-ink">{{ adminStore.stats?.totalAuditCount ?? 0 }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="panel-surface p-6">
      <div class="mb-5 flex items-center justify-between">
        <h2 class="section-title">最近审核记录</h2>
        <el-button plain round @click="adminStore.fetchAuditRecords()">刷新</el-button>
      </div>
      <el-table :data="adminStore.auditRecords" stripe>
        <el-table-column prop="warningId" label="预警ID" min-width="100" />
        <el-table-column prop="contentType" label="内容类型" min-width="100" />
        <el-table-column prop="adminUsername" label="管理员" min-width="120" />
        <el-table-column prop="result" label="结果" min-width="100" />
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="时间" min-width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>
