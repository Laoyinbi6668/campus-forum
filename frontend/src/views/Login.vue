<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useUserStore } from '@/stores/user';
import type { LoginForm } from '@/types';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();

const form = reactive<LoginForm>({
  username: '',
  password: '',
});

const rules: FormRules<LoginForm> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function submit() {
  await formRef.value?.validate();
  await userStore.loginAction(form);
  ElMessage.success('登录成功');
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/';
  router.push(redirect);
}
</script>

<template>
  <div class="mx-auto max-w-md">
    <div class="panel-surface p-8">
      <p class="text-sm uppercase tracking-[0.3em] text-ember">Welcome Back</p>
      <h1 class="mt-3 font-display text-3xl font-semibold text-ink">登录校园论坛</h1>
      <p class="mt-2 text-sm text-slate">使用已有账号进入论坛或管理后台。</p>

      <el-form ref="formRef" :model="form" :rules="rules" class="mt-8 space-y-4" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" class="w-full" :loading="userStore.loading" @click="submit">
          登录
        </el-button>
      </el-form>

      <p class="mt-6 text-center text-sm text-slate">
        还没有账号？
        <RouterLink to="/register" class="font-medium text-ember">立即注册</RouterLink>
      </p>
    </div>
  </div>
</template>
