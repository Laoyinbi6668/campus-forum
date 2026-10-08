import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue'),
      meta: { title: '论坛首页' },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { guestOnly: true, title: '用户登录' },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
      meta: { guestOnly: true, title: '用户注册' },
    },
    {
      path: '/posts/create',
      name: 'post-create',
      component: () => import('@/views/PostCreate.vue'),
      meta: { requiresAuth: true, title: '发布帖子' },
    },
    {
      path: '/posts/:id',
      name: 'post-detail',
      component: () => import('@/views/PostDetail.vue'),
      meta: { title: '帖子详情' },
    },
    {
      path: '/admin',
      redirect: '/admin/dashboard',
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: () => import('@/views/admin/Dashboard.vue'),
      meta: { requiresAdmin: true, title: '管理后台概览' },
    },
    {
      path: '/admin/warnings',
      name: 'admin-warnings',
      component: () => import('@/views/admin/WarningList.vue'),
      meta: { requiresAdmin: true, title: '预警审核' },
    },
    {
      path: '/admin/posts',
      name: 'admin-posts',
      component: () => import('@/views/admin/PostList.vue'),
      meta: { requiresAdmin: true, title: '帖子管理' },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/views/admin/UserList.vue'),
      meta: { requiresAdmin: true, title: '用户管理' },
    },
  ],
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach(async (to) => {
  const userStore = useUserStore();

  if (userStore.token && !userStore.profile) {
    try {
      await userStore.fetchProfile();
    } catch (error) {
      userStore.logout();
    }
  }

  if (to.meta.guestOnly && userStore.isLoggedIn) {
    return '/';
  }

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`;
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return '/';
  }

  if (to.meta.title) {
    document.title = `${to.meta.title as string} - 校园论坛智能管理系统`;
  }

  return true;
});

export default router;
