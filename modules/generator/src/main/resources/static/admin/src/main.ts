import './assets/main.css'

import { createApp, nextTick } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { md3 } from 'vuetify/blueprints'
import { aliases, mdi } from 'vuetify/iconsets/mdi'
import initialState from './initialState'

const vuetify = createVuetify({
    components,
    directives,
    blueprint: md3,
    icons: {
        defaultSet: 'mdi',
        aliases,
        sets: {
            mdi
        }
    },
    theme: {
        defaultTheme: 'dark'
    }
})

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
    .use(router)
    .use(vuetify)

initialState(pinia)

app.mount('#app')