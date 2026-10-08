<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useUserStore } from '@/stores/user';
import type { RegisterForm } from '@/types';

const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();

const form = reactive<RegisterForm>({
  username: '',
  email: '',
  password: '',
});

const rules: FormRules<RegisterForm> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度需在 3-50 个字符之间', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
};

async function submit() {
  await formRef.value?.validate();
  await userStore.registerAction(form);
  ElMessage.success('注册成功，请登录');
  router.push('/login');
}
</script>

<template>
  <div class="mx-auto max-w-md">
    <div class="panel-surface p-8">
      <p class="text-sm uppercase tracking-[0.3em] text-moss">Join Us</p>
      <h1 class="mt-3 font-display text-3xl font-semibold text-ink">创建论坛账号</h1>
      <p class="mt-2 text-sm text-slate">注册后即可发布帖子、发表评论并查看个人信息。</p>

      <el-form ref="formRef" :model="form" :rules="rules" class="mt-8 space-y-4" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="例如 user1" size="large" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱地址" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" class="w-full" :loading="userStore.loading" @click="submit">
          注册
        </el-button>
      </el-form>
    </div>
  </div>
</template>
