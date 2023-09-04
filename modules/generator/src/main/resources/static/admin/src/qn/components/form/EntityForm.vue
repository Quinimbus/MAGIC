<script setup lang="ts">
import { DateField, DateTimeField, NumberField, StringField } from '@/qn/components/form';
import { type Field, FieldType } from '@/qn/types/entities'
import type { PropType } from 'vue';
defineProps({
    fields: {
        type: Array as PropType<Field[]>,
        required: true
    },
    modelValue: {
        type: Object,
        required: true
    }
})
defineEmits<{
    (e: "update:model-value", modelValue: string): void
}>()
</script>

<template>
    <v-container>
        <v-row v-for="field in fields" :key="field.key">
            <StringField
                v-if="field.type === FieldType.STRING"
                :field="field"
                :model-value="modelValue[field.key]"
                @update:model-value="['onUpdate:model-value']" />
            <NumberField
                v-else-if="field.type === FieldType.NUMBER"
                :field="field"
                :model-value="modelValue[field.key]"
                @update:model-value="['onUpdate:model-value']" />
            <DateField
                v-else-if="field.type === FieldType.LOCALDATE"
                :field="field"
                :model-value="modelValue[field.key]"
                @update:model-value="['onUpdate:model-value']" />
            <DateTimeField
                v-else-if="field.type === FieldType.LOCALDATETIME"
                :field="field"
                :model-value="modelValue[field.key]"
                @update:model-value="['onUpdate:model-value']" />
            <v-col v-else>
                {{ field.label }}: Unsupported type: {{ FieldType[field.type] }}
            </v-col>
        </v-row>
    </v-container>
</template>