<script setup lang="ts">
import { computed, ref, type PropType } from 'vue';
import { VDataTable } from 'vuetify/labs/VDataTable';
import { EntityEditDialog } from '@/qn/components/dialog'
import { type TypeDefinition } from '@/qn/types/entities'
import { toTableHeader } from '@/qn/ui/UI';
const props = defineProps({
    type: {
        type: Object as PropType<TypeDefinition>,
        required: true
    },
    items: {
        type: Array,
        required: true
    }
})
let search = ref("")
const headers = computed(() => {
    const h = props.type.fields
        .map(toTableHeader)
    h.push({
        key: 'actions',
        title: 'Aktionen',
        sortable: false
    })
    return h
})
const editItem = (item: Object) => {
    console.log(item)
}
const deleteItem = (item: Object) => {
    console.log(item)
}
</script>

<template>
    <v-data-table :headers="headers" :items="items" :search="search">
        <template v-slot:[`item.actions`]="{ item }">
            <EntityEditDialog :type="type">
                <template #activator="{ props }">
                    <v-icon v-bind="props" icon="mdi-pencil" size="small" class="me-2" @click="editItem(item.raw)" />
                </template>
            </EntityEditDialog>
            <v-icon icon="mdi-delete" size="small" @click="deleteItem(item.raw)" />
        </template>
    </v-data-table>
</template>