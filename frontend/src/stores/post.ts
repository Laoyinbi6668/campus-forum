import { ref } from 'vue';
import { defineStore } from 'pinia';
import { createPost, deletePost, getPostDetail, getPosts } from '@/api/post';
import { createComment, deleteComment, getComments } from '@/api/comment';
import type { CommentForm, CommentItem, PageResponse, PostForm, PostItem } from '@/types';

export const usePostStore = defineStore('post', () => {
  const posts = ref<PostItem[]>([]);
  const total = ref(0);
  const currentPage = ref(1);
  const pageSize = ref(10);
  const detail = ref<PostItem | null>(null);
  const comments = ref<CommentItem[]>([]);
  const commentsTotal = ref(0);
  const loading = ref(false);

  async function fetchPosts(page = 1, size = pageSize.value) {
    loading.value = true;
    try {
      const response = await getPosts(page - 1, size);
      applyPageData(response.data, page, size);
    } finally {
      loading.value = false;
    }
  }

  async function fetchPostDetail(id: number) {
    loading.value = true;
    try {
      const response = await getPostDetail(id);
      detail.value = response.data;
      return response.data;
    } finally {
      loading.value = false;
    }
  }

  async function fetchComments(postId: number, page = 1, size = 10) {
    const response = await getComments(postId, page - 1, size);
    comments.value = response.data.content;
    commentsTotal.value = response.data.totalElements;
  }

  async function submitPost(payload: PostForm) {
    const response = await createPost(payload);
    return response.data;
  }

  async function submitComment(postId: number, payload: CommentForm) {
    const response = await createComment(postId, payload);
    return response.data;
  }

  async function removePost(id: number) {
    await deletePost(id);
  }

  async function removeComment(id: number) {
    await deleteComment(id);
  }

  function applyPageData(pageData: PageResponse<PostItem>, page: number, size: number) {
    posts.value = pageData.content;
    total.value = pageData.totalElements;
    currentPage.value = page;
    pageSize.value = size;
  }

  return {
    posts,
    total,
    currentPage,
    pageSize,
    detail,
    comments,
    commentsTotal,
    loading,
    fetchPosts,
    fetchPostDetail,
    fetchComments,
    submitPost,
    submitComment,
    removePost,
    removeComment,
  };
});
