<script setup lang="ts">
import { type PropType, ref } from 'vue';
import { type TypeDefinition } from '@/qn/types/entities'
import { EntityForm } from '@/qn/components/form';

defineProps({
    type: {
        type: Object as PropType<TypeDefinition>,
        required: true
    }
})

defineSlots<{
    activator(props: Record<string, any>): any
}>()
const open = ref(false)
</script>

<template>
    <v-dialog v-model="open" width="auto">
        <template #activator="{ props }">
            <slot name="activator" :props="props"></slot>
        </template>
        <v-card>
            <v-card-title>{{ type.labelSingular }} bearbeiten</v-card-title>
            <v-card-text>
                <EntityForm :fields="type.fields" />
            </v-card-text>
            <v-card-actions>
                <v-spacer />
                <v-btn>Speichern</v-btn>
                <v-btn>Abbrechen</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>