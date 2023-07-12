<script setup lang="ts">
import { type PropType, ref } from 'vue';
import { EntityViewDataTable, EntityViewToolbar } from '.';
import { type EntityListDataSource } from '@/qn/datasource/EntityListDataSource'
import { type TypeDefinition } from '@/qn/types/entities'
const props = defineProps({
    type: {
        type: Object as PropType<TypeDefinition>,
        required: true
    },
    datasource: {
        type: Object as PropType<EntityListDataSource<any>>,
        required: true
    }
})
let state = ref({entries: <any[]>[]})
props.datasource.loadAll().then(e => state.value.entries = e)
</script>

<template>
    <v-card>
        <v-card-title>
            {{ type.labelPlural }}
            <v-spacer></v-spacer>
            <EntityViewToolbar :type="type" />
        </v-card-title>
        <EntityViewDataTable :items="state.entries" :type="type" />
    </v-card>
</template>