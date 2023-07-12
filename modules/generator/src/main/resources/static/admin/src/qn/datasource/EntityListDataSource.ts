import type { TypeDefinition } from "../types/entities";

export interface EntityListDataSource<E> {
    loadAll(): Promise<E[]>;
}

export class RestBasedEntityListDataSource<E> implements EntityListDataSource<E> {

    readAllPath: string

    constructor(basepath: String, type: TypeDefinition) {
        this.readAllPath = basepath + "/" + type.keyPlural
    }

    async loadAll(): Promise<E[]> {
        const response = await fetch(this.readAllPath)
        const body = await response.json()
        return body
    }
}