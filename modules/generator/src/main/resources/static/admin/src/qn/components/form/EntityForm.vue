<script setup lang="ts">
import { StringField, NumberField } from '@/qn/components/form';
import { type Field, FieldType } from '@/qn/types/entities'
import type { PropType } from 'vue';
defineProps({
    fields: {
        type: Array as PropType<Field[]>,
        required: true
    }
})
</script>

<template>
    <v-container>
        <v-row v-for="field in fields" :key="field.key">
            <StringField v-if="field.type === FieldType.STRING" :field="field" />
            <NumberField v-else-if="field.type === FieldType.NUMBER" :field="field" />
            <v-col v-else>
                {{ field.label }}: Unsupported type: {{ FieldType[field.type] }}
            </v-col>
        </v-row>
    </v-container>
</template>