<script setup lang="ts">
import { type Field } from '@/qn/types/entities'
import type { PropType } from 'vue';
defineProps({
    field: {
        type: Object as PropType<Field>,
        required: true
    },
    modelValue: String
})
defineEmits<{
    (e: "update:model-value", modelValue: string): void
}>()
const rules = {
    number: (value: string) => /^\d+$/.test(value) || 'invalid number'
}
</script>

<template>
    <v-col>
        <v-text-field
            clearable
            :rules="[rules.number]"
            :label="field.label"
            :model-value="modelValue"
            @update:model-value="['onUpdate:model-value']" />
    </v-col>
</template>