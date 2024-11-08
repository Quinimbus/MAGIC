import { createApp, nextTick } from 'vue'
import { createPinia } from 'pinia'
import { PrimeVue } from '@primevue/core'
import Aura from '@primevue/themes/aura';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';

import App from './App.vue'
import router from './router'

import initialState from './initialState'

import '@/assets/styles.scss';
import '@/assets/tailwind.css';

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
    .use(router)
    .use(ConfirmationService)
    .use(ToastService)
    .use(PrimeVue, {
        theme: {
            preset: Aura,
            options: {
                darkModeSelector: '.app-dark'
            }
        }
    })

initialState(pinia)

if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
    document.documentElement.classList.add('app-dark')
}

app.mount('#app')
