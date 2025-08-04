<script setup lang="ts">
import { RouterView } from 'vue-router'
import '@mdi/font/css/materialdesignicons.css'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Toolbar from 'primevue/toolbar'
import Chip from 'primevue/chip'
import Dialog from 'primevue/dialog'
import FloatLabel from 'primevue/floatlabel'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import DatePicker from 'primevue/datepicker'
import RadioButton from 'primevue/radiobutton'
import Checkbox from 'primevue/checkbox'
import AutoComplete from 'primevue/autocomplete'
import ConfirmPopup from 'primevue/confirmpopup'
import Textarea from 'primevue/textarea';
import { Tab, TabList, TabPanel, TabPanels, Tabs } from 'primevue'
</script>

<template>
  <Card v-show="false" />
  <DataTable v-show="false">
    <Column v-show="false" />
  </DataTable>
  <Button v-show="false" />
  <Toolbar v-show="false" />
  <Chip v-show="false" />
  <Dialog v-show="false" />
  <FloatLabel v-show="false" />
  <InputText v-show="false" />
  <Select v-show="false" />
  <DatePicker v-show="false" />
  <RadioButton v-show="false" />
  <Checkbox v-show="false" />
  <AutoComplete v-show="false" />
  <Textarea v-show="false" />
  <Tabs value="0" v-show="false">
    <TabList>
      <Tab value="0" />
    </TabList>
    <TabPanels>
      <TabPanel value="0" />
    </TabPanels>
  </Tabs>
  <ConfirmPopup />
  <router-view />
</template>

<style scoped>
</style>
