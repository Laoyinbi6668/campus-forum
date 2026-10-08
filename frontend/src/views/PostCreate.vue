<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { usePostStore } from '@/stores/post';
import type { PostForm } from '@/types';

const router = useRouter();
const postStore = usePostStore();
const formRef = ref<FormInstance>();
const submitting = ref(false);

const form = reactive<PostForm>({
  title: '',
  content: '',
});

const rules: FormRules<PostForm> = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { max: 200, message: '标题不能超过 200 个字符', trigger: 'blur' },
  ],
  content: [{ required: true, message: '请输入帖子内容', trigger: 'blur' }],
};

async function submit() {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    const created = await postStore.submitPost(form);
    ElMessage.success('发帖成功');
    router.push(`/posts/${created.id}`);
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="mx-auto max-w-4xl">
    <div class="panel-surface p-8">
      <div class="mb-8 flex items-start justify-between gap-4">
        <div>
          <p class="text-sm uppercase tracking-[0.3em] text-ember">Create Post</p>
          <h1 class="mt-3 font-display text-3xl font-semibold text-ink">发布新帖子</h1>
        </div>
        <div class="rounded-2xl bg-gold/15 px-4 py-3 text-sm text-slate">
          系统会在提交后自动进行情感分析与内容预警判断。
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" size="large" placeholder="输入一个清晰的标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            resize="none"
            placeholder="分享你的问题、经验或校园见闻"
          />
        </el-form-item>
        <div class="flex justify-end gap-3">
          <el-button size="large" round @click="router.back()">取消</el-button>
          <el-button type="primary" size="large" round :loading="submitting" @click="submit">提交帖子</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>
