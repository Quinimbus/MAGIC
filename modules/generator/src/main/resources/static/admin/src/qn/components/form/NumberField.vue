<script setup lang="ts">
import { type Field } from '@/qn/types/entities'
import type { PropType } from 'vue';
defineProps({
    field: {
        type: Object as PropType<Field>,
        required: true
    }
})
const rules = {
    number: (value: string) => /^\d+$/.test(value) || 'invalid number'
}
</script>

<template>
    <v-col>
        <v-text-field clearable :rules="[rules.number]" :label="field.label" />
    </v-col>
</template>