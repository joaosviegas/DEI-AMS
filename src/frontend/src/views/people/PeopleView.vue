<template>
  <v-row align="center">
    <v-col>
      <h2 class="text-left ml-1">Listagem de Pessoas</h2>
    </v-col>
    <v-col cols="auto" v-if="isAdmin">
      <CreatePersonDialog @person-created="getPeople" />
    </v-col>
  </v-row>


  <v-text-field
    v-model="search"
    label="Search"
    prepend-inner-icon="mdi-magnify"
    variant="outlined"
    hide-details
    single-line
  ></v-text-field>

  <v-data-table
    :headers="headers"
    :items="people"
    :search="search"
    :loading="loading"
    :custom-filter="fuzzySearch"
    item-key="id"
    class="text-left"
    no-data-text="Sem pessoas a apresentar."
  >
    <template v-slot:[`item.type`]="{ item }">
      <v-chip v-if="item.type === 'ADMINISTRATOR'" color="purple" text-color="white">
        Administrador
      </v-chip>
      <v-chip v-else-if="item.type === 'MAIN_TEACHER'" color="red" text-color="white">
        Professor Regente
      </v-chip>
      <v-chip v-else-if="item.type === 'TEACHING_ASSISTANT'" color="blue" text-color="white">
        Professor Assistente
      </v-chip>
      <v-chip v-else color="green" text-color="white">
        Aluno
      </v-chip>
    </template>
    <template v-slot:[`item.actions`]="{ item }" v-if="isAdmin">
      <v-icon @click="editPerson(item)" class="mr-2" title="Editar Pessoa">mdi-pencil</v-icon>
      <v-icon @click="deletePerson(item)" title="Remover Pessoa">mdi-delete</v-icon>
    </template>

  </v-data-table>

  <EditPersonDialog
    v-if="isAdmin"
    v-model="showEditDialog"
    :person="selectedPerson"
    @person-updated="getPeople"
  />

  <ConfirmDeleteDialog
    v-if="isAdmin"
    v-model="showDeleteDialog"
    title="Remover Pessoa"
    :message="`Tem a certeza de que deseja remover a pessoa '${selectedPerson?.name}'?`"
    item-type="pessoa"
    :item-name="selectedPerson?.name"
    :item-subtitle="`IST ID: ${selectedPerson?.istId}`"
    icon="mdi-account-remove"
    icon-color="red"
    confirm-text="Remover Pessoa"
    warning-message="Esta ação não pode ser desfeita e eliminará permanentemente todos os dados associados a esta pessoa."
    @confirm="executeDeletePerson"
    @update:modelValue="val => { if (!val) selectedPerson = undefined }"
  />

</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRoleStore } from '../../stores/role'
import type PersonDto from '../../models/PersonDto'
import RemoteService from '../../services/RemoteService'
import CreatePersonDialog from './CreatePersonDialog.vue'
import EditPersonDialog from './EditPersonDialog.vue'
import ConfirmDeleteDialog from '../../components/ConfirmDeleteDialog.vue'

let search = ref('')
let loading = ref(true)

const roleStore = useRoleStore()
const isAdmin = computed(() => roleStore.isAdministrator)

const showDeleteDialog = ref(false)
const showEditDialog = ref(false)
const selectedPerson = ref<PersonDto>()

const headers = computed(() => {
  const baseHeaders = [
    { title: 'ID', key: 'id', value: 'id', sortable: true, filterable: false },
    {
      title: 'Nome',
      key: 'name',
      value: 'name',
      sortable: true,
      filterable: true
    },
    {
      title: 'IST ID',
      key: 'istId',
      value: 'istId',
      sortable: true,
      filterable: true
    },
    {
      title: 'E-mail',
      key: 'email',
      value: 'email',
      sortable: true,
      filterable: true
    },
    {
      title: 'Tipo',
      key: 'type',
      value: 'type',
      sortable: true,
      filterable: true
    }
  ]
  
  // Only add actions column for admins
  if (isAdmin.value) {
    baseHeaders.push({
      title: 'Ações',
      key: 'actions',
      value: 'actions',
      sortable: false,
      filterable: false
    })
  }
  
  return baseHeaders
})

const people: PersonDto[] = reactive([])

getPeople()
async function getPeople() {
  people.splice(0, people.length)
  people.push(...(await RemoteService.getPeople()))
  loading.value = false
}

// Open the edit dialog
const editPerson = (person: PersonDto) => {
  selectedPerson.value = person
  showEditDialog.value = true
}

// Open the delete dialog
const deletePerson = (person: PersonDto) => {
  selectedPerson.value = person
  showDeleteDialog.value = true
}

// Actually execute the deletion
const executeDeletePerson = async () => {
  if (!selectedPerson.value?.id) return
  
  try {
    await RemoteService.deletePerson(selectedPerson.value.id)
    
    // Refresh the people list
    await getPeople()
    
    // Close dialog and reset
    showDeleteDialog.value = false
    selectedPerson.value = undefined
  } catch (error) {
    console.error('Error deleting person:', error)
    alert('Erro ao eliminar pessoa. Por favor, tente novamente.')
  }
}


const fuzzySearch = (value: string, search: string) => {
  // Regex to match any character in between the search characters
  let searchRegex = new RegExp(search.split('').join('.*'), 'i')
  return searchRegex.test(value)
}

</script>
