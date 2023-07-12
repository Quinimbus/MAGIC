<script setup lang="ts">
import { type Field } from '@/qn/types/entities'
import type { PropType } from 'vue';
defineProps({
    field: {
        type: Object as PropType<Field>,
        required: true
    }
})
</script>

<template>
    <v-col>
        <v-text-field clearable :label="field.label" />
    </v-col>
</template>