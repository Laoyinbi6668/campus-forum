import request from '@/utils/request';
import type { ApiResponse, CommentForm, CommentItem, PageResponse, PostForm, PostItem } from '@/types';

export function getPosts(page = 0, size = 10): Promise<ApiResponse<PageResponse<PostItem>>> {
  return request.get('/api/posts', { params: { page, size } });
}

export function getPostDetail(id: number): Promise<ApiResponse<PostItem>> {
  return request.get(`/api/posts/${id}`);
}

export function createPost(data: PostForm): Promise<ApiResponse<PostItem>> {
  return request.post('/api/posts', data);
}

export function updatePost(id: number, data: PostForm): Promise<ApiResponse<PostItem>> {
  return request.put(`/api/posts/${id}`, data);
}

export function deletePost(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/posts/${id}`);
}

export function getComments(postId: number, page = 0, size = 10): Promise<ApiResponse<PageResponse<CommentItem>>> {
  return request.get(`/api/posts/${postId}/comments`, { params: { page, size } });
}

export function createComment(postId: number, data: CommentForm): Promise<ApiResponse<CommentItem>> {
  return request.post(`/api/posts/${postId}/comments`, data);
}

export function deleteComment(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/comments/${id}`);
}
