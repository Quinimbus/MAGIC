<script setup lang="ts">
import { RouterView } from 'vue-router'
import '@mdi/font/css/materialdesignicons.css'
import entityTypeDefinitions from '@/domain'
import { useAppConfigStore } from '@quinimbus/admin-ui'
</script>

<template>
  <v-card>
    <v-app>
      <v-app-bar :title="useAppConfigStore().name" />

      <v-navigation-drawer expand-on-hover rail>
        <v-list-item
          v-for="type in entityTypeDefinitions"
          :key="type.key"
          :title="type.labelPlural"
          :prepend-icon="'mdi-' + type.icon"
          :to="'/' + type.keyPlural" />
      </v-navigation-drawer>

      <v-main style="min-height: 300px;">
        <RouterView />
        <v-footer>{{ useAppConfigStore().name }} {{ useAppConfigStore().version }}</v-footer>
      </v-main>
    </v-app>
  </v-card>
</template>

<style scoped>
</style>
