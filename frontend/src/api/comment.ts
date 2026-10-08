import request from '@/utils/request';
import type { ApiResponse, CommentForm, CommentItem, PageResponse } from '@/types';

export function getComments(postId: number, page = 0, size = 10): Promise<ApiResponse<PageResponse<CommentItem>>> {
  return request.get(`/api/posts/${postId}/comments`, { params: { page, size } });
}

export function createComment(postId: number, data: CommentForm): Promise<ApiResponse<CommentItem>> {
  return request.post(`/api/posts/${postId}/comments`, data);
}

export function deleteComment(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/comments/${id}`);
}
