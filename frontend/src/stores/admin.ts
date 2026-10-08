import { ref } from 'vue';
import { defineStore } from 'pinia';
import {
  auditWarning,
  banUser,
  getAdminPosts,
  getAuditRecords,
  getStats,
  getUsers,
  getWarnings,
  removeAdminPost,
  unbanUser,
} from '@/api/admin';
import type { AuditForm, AuditRecordItem, PostItem, StatItem, UserProfile, WarningItem } from '@/types';

export const useAdminStore = defineStore('admin', () => {
  const stats = ref<StatItem | null>(null);
  const warnings = ref<WarningItem[]>([]);
  const posts = ref<PostItem[]>([]);
  const users = ref<UserProfile[]>([]);
  const auditRecords = ref<AuditRecordItem[]>([]);
  const total = ref(0);
  const loading = ref(false);

  async function fetchStats() {
    const response = await getStats();
    stats.value = response.data;
  }

  async function fetchWarnings(page = 1, size = 10) {
    loading.value = true;
    try {
      const response = await getWarnings(page - 1, size);
      warnings.value = response.data.content;
      total.value = response.data.totalElements;
    } finally {
      loading.value = false;
    }
  }

  async function fetchPosts(page = 1, size = 10) {
    loading.value = true;
    try {
      const response = await getAdminPosts(page - 1, size);
      posts.value = response.data.content;
      total.value = response.data.totalElements;
    } finally {
      loading.value = false;
    }
  }

  async function fetchUsers(page = 1, size = 10) {
    loading.value = true;
    try {
      const response = await getUsers(page - 1, size);
      users.value = response.data.content;
      total.value = response.data.totalElements;
    } finally {
      loading.value = false;
    }
  }

  async function fetchAuditRecords(page = 1, size = 10) {
    const response = await getAuditRecords(page - 1, size);
    auditRecords.value = response.data.content;
  }

  async function submitAudit(id: number, payload: AuditForm) {
    await auditWarning(id, payload);
  }

  async function submitBan(id: number) {
    await banUser(id);
  }

  async function submitUnban(id: number) {
    await unbanUser(id);
  }

  async function submitDeletePost(id: number) {
    await removeAdminPost(id);
  }

  return {
    stats,
    warnings,
    posts,
    users,
    auditRecords,
    total,
    loading,
    fetchStats,
    fetchWarnings,
    fetchPosts,
    fetchUsers,
    fetchAuditRecords,
    submitAudit,
    submitBan,
    submitUnban,
    submitDeletePost,
  };
});
