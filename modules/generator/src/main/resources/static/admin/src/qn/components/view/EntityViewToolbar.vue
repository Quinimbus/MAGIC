<script setup lang="ts">
import { ref, type PropType } from 'vue';
import { EntityAddDialog } from '@/qn/components/dialog';
import { type TypeDefinition } from '@/qn/types/entities'
let search = ref("")
defineProps({
    type: {
        type: Object as PropType<TypeDefinition>,
        required: true
    }
})
</script>

<template>
    <v-toolbar density="compact">
        <v-text-field v-model="search" prepend-inner-icon="mdi-magnify" label="Suchen" single-line hide-details />
        <v-spacer />
        <EntityAddDialog :type="type">
            <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-plus" />
            </template>
        </EntityAddDialog>
    </v-toolbar>
</template>