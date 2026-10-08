import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { getProfile, login, register } from '@/api/user';
import { clearStoredUser, clearToken, getStoredUser, getToken, setStoredUser, setToken } from '@/utils/auth';
import type { LoginForm, RegisterForm, UserProfile } from '@/types';

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken());
  const profile = ref<UserProfile | null>(getStoredUser());
  const loading = ref(false);

  const isLoggedIn = computed(() => Boolean(token.value));
  const isAdmin = computed(() => profile.value?.role === 'ADMIN');

  async function loginAction(form: LoginForm) {
    loading.value = true;
    try {
      const response = await login(form);
      token.value = response.data.token;
      setToken(response.data.token);
      profile.value = {
        id: response.data.userId,
        username: response.data.username,
        email: response.data.email,
        role: response.data.role,
        status: 'ACTIVE',
        violationCount: 0,
        createdAt: '',
      };
      setStoredUser(profile.value);
      await fetchProfile();
    } finally {
      loading.value = false;
    }
  }

  async function registerAction(form: RegisterForm) {
    loading.value = true;
    try {
      await register(form);
    } finally {
      loading.value = false;
    }
  }

  async function fetchProfile() {
    if (!token.value) return null;
    const response = await getProfile();
    profile.value = response.data;
    setStoredUser(response.data);
    return response.data;
  }

  function logout() {
    token.value = '';
    profile.value = null;
    clearToken();
    clearStoredUser();
  }

  return {
    token,
    profile,
    loading,
    isLoggedIn,
    isAdmin,
    loginAction,
    registerAction,
    fetchProfile,
    logout,
  };
});
