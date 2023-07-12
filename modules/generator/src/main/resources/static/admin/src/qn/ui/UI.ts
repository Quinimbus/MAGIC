import { VDataTable } from 'vuetify/labs/VDataTable';
import type { Field, TypeDefinition } from '@/qn/types/entities';
import type { RouteRecordRaw } from 'vue-router';
type UnwrapReadonlyArrayType<A> = A extends Readonly<Array<infer I>> ? UnwrapReadonlyArrayType<I> : A
type DT = InstanceType<typeof VDataTable>;
type ReadonlyDataTableHeader = UnwrapReadonlyArrayType<DT['headers']>;

export const toTableHeader = (f: Field): ReadonlyDataTableHeader => {
    return {
        title: f.label,
        align: "start",
        sortable: false,
        key: f.key
    }
}

export const toRoute = (type: TypeDefinition): RouteRecordRaw => {
    return {
        path: '/' + type.keyPlural,
        name: type.labelPlural,
        component: type.listView
    }
}