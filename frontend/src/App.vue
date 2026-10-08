<script setup lang="ts">
import Header from '@/components/Header.vue';
</script>

<template>
  <div class="min-h-screen">
    <Header />
    <main class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>
