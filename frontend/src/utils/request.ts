import axios from 'axios';
import { ElMessage } from 'element-plus';
import { clearStoredUser, clearToken, getToken } from './auth';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 12000,
  headers: {
    'Content-Type': 'application/json',
  },
});

request.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload?.code !== 0) {
      ElMessage.error(payload?.message || '请求失败');
      return Promise.reject(new Error(payload?.message || 'Request failed'));
    }
    return payload;
  },
  (error) => {
    const status = error.response?.status;
    const message = error.response?.data?.message || error.message || '网络异常';
    if (status === 401 || status === 403) {
      clearToken();
      clearStoredUser();
    }
    ElMessage.error(message);
    return Promise.reject(error);
  },
);

export default request;
