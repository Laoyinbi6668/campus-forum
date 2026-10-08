<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import WarningItem from '@/components/WarningItem.vue';
import { useAdminStore } from '@/stores/admin';
import type { AuditForm } from '@/types';

const adminStore = useAdminStore();
const auditDialogVisible = ref(false);
const currentId = ref<number | null>(null);
const auditForm = reactive<AuditForm>({
  result: 'PASS',
  remark: '',
});

onMounted(() => {
  adminStore.fetchWarnings();
});

function openAudit(id: number, result: 'PASS' | 'REJECT') {
  currentId.value = id;
  auditForm.result = result;
  auditForm.remark = '';
  auditDialogVisible.value = true;
}

async function submitAudit() {
  if (currentId.value == null) return;
  await adminStore.submitAudit(currentId.value, auditForm);
  ElMessage.success('审核已提交');
  auditDialogVisible.value = false;
  await adminStore.fetchWarnings();
  await adminStore.fetchAuditRecords();
}

async function quickReject(id: number) {
  await ElMessageBox.confirm('确认将该预警内容判定为拒绝吗？', '提示', { type: 'warning' });
  await adminStore.submitAudit(id, { result: 'REJECT', remark: '管理员快速拒绝' });
  ElMessage.success('已拒绝该内容');
  await adminStore.fetchWarnings();
}
</script>

<template>
  <div class="space-y-8">
    <section class="flex items-center justify-between">
      <div>
        <p class="text-sm uppercase tracking-[0.3em] text-red-500">Moderation Queue</p>
        <h1 class="mt-2 font-display text-3xl font-semibold text-ink">预警审核列表</h1>
      </div>
      <el-button plain round @click="adminStore.fetchWarnings()">刷新队列</el-button>
    </section>

    <div v-if="adminStore.warnings.length" class="grid gap-5 lg:grid-cols-2">
      <div v-for="warning in adminStore.warnings" :key="warning.id" class="panel-surface p-6">
        <WarningItem :warning="warning" />
        <div class="mt-5 flex flex-wrap justify-end gap-3">
          <el-button round @click="openAudit(warning.id, 'PASS')">通过并发布</el-button>
          <el-button type="danger" round @click="openAudit(warning.id, 'REJECT')">打开拒绝面板</el-button>
          <el-button text type="danger" @click="quickReject(warning.id)">快速拒绝</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="当前没有待处理预警" class="panel-surface py-12" />

    <el-dialog v-model="auditDialogVisible" title="提交审核结果" width="520px">
      <el-form label-position="top">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.result">
            <el-radio-button label="PASS">PASS</el-radio-button>
            <el-radio-button label="REJECT">REJECT</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.remark" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>
