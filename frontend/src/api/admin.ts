import request from '@/utils/request';
import type {
  ApiResponse,
  AuditForm,
  AuditRecordItem,
  PageResponse,
  PostItem,
  StatItem,
  UserProfile,
  WarningItem,
} from '@/types';

export function getAdminPosts(page = 0, size = 10): Promise<ApiResponse<PageResponse<PostItem>>> {
  return request.get('/api/admin/posts', { params: { page, size } });
}

export function removeAdminPost(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/admin/posts/${id}`);
}

export function getUsers(page = 0, size = 10): Promise<ApiResponse<PageResponse<UserProfile>>> {
  return request.get('/api/admin/users', { params: { page, size } });
}

export function banUser(id: number): Promise<ApiResponse<UserProfile>> {
  return request.put(`/api/admin/users/${id}/ban`);
}

export function unbanUser(id: number): Promise<ApiResponse<UserProfile>> {
  return request.put(`/api/admin/users/${id}/unban`);
}

export function getWarnings(page = 0, size = 10): Promise<ApiResponse<PageResponse<WarningItem>>> {
  return request.get('/api/admin/warnings', { params: { page, size } });
}

export function auditWarning(id: number, data: AuditForm): Promise<ApiResponse<WarningItem>> {
  return request.post(`/api/admin/warnings/${id}/audit`, data);
}

export function getStats(): Promise<ApiResponse<StatItem>> {
  return request.get('/api/admin/stats');
}

export function getAuditRecords(page = 0, size = 10): Promise<ApiResponse<PageResponse<AuditRecordItem>>> {
  return request.get('/api/admin/audit-records', { params: { page, size } });
}
