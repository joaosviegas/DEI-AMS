<template>
  <v-row align="center">
    <v-col>
      <h2 class="text-left ml-1">Listagem de Unidades Curriculares</h2>
    </v-col>
    <v-col cols="auto" v-if="isAdmin">
      <CreateCurricularUnitDialog @curricular-unit-created="getCurricularUnits" />
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
    :items="curricularUnits"
    :search="search"
    :loading="loading"
    :custom-filter="fuzzySearch"
    item-key="id"
    class="text-left"
    no-data-text="Sem unidades curriculares a apresentar."
  >
    <template v-slot:[`item.semester`]="{ item }">
      <v-chip :color="getSemesterColor(item.semester)" size="small">
        {{ getSemesterText(item.semester) }}
      </v-chip>
    </template>

    <template v-slot:[`item.courses`]="{ item }">
      <v-chip 
        v-for="course in item.courses" 
        :key="course.id" 
        size="small" 
        class="mr-1"
      >
        {{ course.code }}
      </v-chip>
    </template>

    <template v-slot:[`item.mainTeacher`]="{ item }">
      {{ item.mainTeacher.name }}
    </template>

    <template v-slot:[`item.actions`]="{ item }">
      <!-- View Details - accessible to everyone -->
      <v-icon @click="viewDetails(item)" class="mr-2" tonal>mdi-eye</v-icon>
      
      <!-- Add People - only for main teacher of this UC -->
      <v-icon 
        v-if="canAddPeople(item)" 
        @click="addPeople(item)" 
        class="mr-2" 
        tonal
      >
        mdi-account-edit
      </v-icon>
      
      <!-- Edit and Delete - admin only -->
      <template v-if="isAdmin">
        <v-icon @click="editCurricularUnit(item)" class="mr-2" tonal>mdi-pencil</v-icon>
        <v-icon @click="deleteCurricularUnit(item)" tonal>mdi-delete</v-icon>
      </template>
    </template>
  </v-data-table>

  <!-- Details Dialog -->
  <CurricularUnitDetailsDialog 
    v-model="showDetailsDialog"
    :curricular-unit="selectedCurricularUnit"
  />

  <!-- Edit Dialog -->
  <EditCurricularUnitDialog 
    v-if="isAdmin"
    v-model="showEditDialog"
    :curricular-unit="selectedCurricularUnit"
    @curricular-unit-updated="getCurricularUnits"
  />

  <!-- Delete Dialog -->
  <ConfirmDeleteDialog 
    v-if="isAdmin"
    v-model="showDeleteDialog"
    title="Remover Unidade Curricular"
    :message="`Tem a certeza de que deseja remover permanentemente a unidade curricular '${selectedCurricularUnit?.name}'?`"
    item-type="unidade curricular"
    :item-name="selectedCurricularUnit?.name"
    :item-subtitle="`Código: ${selectedCurricularUnit?.code} • ${selectedCurricularUnit?.ects} ECTS`"
    icon="mdi-book-education-outline"
    icon-color="purple"
    confirm-text="Remover Unidade Curricular"
    warning-message="Esta ação eliminará permanentemente a unidade curricular, todas as inscrições de alunos e avaliações associadas."
    @confirm="executeDeleteCurricularUnit"
  />

  <!-- Add People Dialog -->
  <AddPeopleDialog 
    v-model="showAddPeopleDialog"
    :curricular-unit="selectedCurricularUnit"
    @people-updated="getCurricularUnits"
    @curricular-unit-updated="updateSelectedCurricularUnit"
  />
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRoleStore } from '../../stores/role'
import RemoteService from '../../services/RemoteService'
import CurricularUnitDto from '../../models/CurricularUnitDto'
import CreateCurricularUnitDialog from './CreateCurricularUnitsDialog.vue'
import EditCurricularUnitDialog from './EditCurricularUnitsDialog.vue'
import ConfirmDeleteDialog from '../../components/ConfirmDeleteDialog.vue'
import AddPeopleDialog from './ManagePeopleDialog.vue'
import CurricularUnitDetailsDialog from './CurricularUnitDetailsDialog.vue'

let search = ref('')
let loading = ref(true)

const showDetailsDialog = ref(false)
const showDeleteDialog = ref(false)
const showEditDialog = ref(false)
const showAddPeopleDialog = ref(false)
const selectedCurricularUnit = ref<CurricularUnitDto>()

const roleStore = useRoleStore()
const isAdmin = computed(() => roleStore.isAdministrator)

const headers = computed(() => {
  const baseHeaders = [
    { title: 'ID', key: 'id', value: 'id', sortable: true, filterable: false },
    {
      title: 'Código',
      key: 'code',
      value: 'code',
      sortable: true,
      filterable: true
    },
    {
      title: 'Nome',
      key: 'name',
      value: 'name',
      sortable: true,
      filterable: true
    },
    {
      title: 'Semestre',
      key: 'semester',
      value: 'semester',
      sortable: true,
      filterable: false
    },
    {
      title: 'ECTS',
      key: 'ects',
      value: 'ects',
      sortable: true,
      filterable: false
    },
    {
      title: 'Cursos',
      key: 'courses',
      value: 'courses',
      sortable: false,
      filterable: false
    },
    {
      title: 'Professor Regente',
      key: 'mainTeacher',
      value: 'mainTeacher',
      sortable: false,
      filterable: false
    }
  ]
  
  baseHeaders.push({
    title: 'Ações',
    key: 'actions',
    value: 'actions',
    sortable: false,
    filterable: false
  })
  
  return baseHeaders
})

const curricularUnits: CurricularUnitDto[] = reactive([])

getCurricularUnits()
async function getCurricularUnits() {
  try {
    curricularUnits.splice(0, curricularUnits.length)
    curricularUnits.push(...(await RemoteService.getCurricularUnits()))
    loading.value = false
    console.log('Curricular Units loaded:', curricularUnits)
  } catch (error) {
    console.error('Error loading curricular units:', error)
    loading.value = false
  }
}

// Helper functions for display
const getSemesterText = (semester: string) => {
  const semesterMap: { [key: string]: string } = {
    'FIRST': '1º Semestre',
    'SECOND': '2º Semestre'
  }
  return semesterMap[semester] || semester
}

const getSemesterColor = (semester: string) => {
  const colorMap: { [key: string]: string } = {
    'FIRST': 'blue',
    'SECOND': 'green',
    'ANNUAL': 'orange'
  }
  return colorMap[semester] || 'grey'
}

// Action handlers
const viewDetails = (curricularUnit: CurricularUnitDto) => {
  console.log('Viewing details for:', curricularUnit)
  selectedCurricularUnit.value = curricularUnit
  showDetailsDialog.value = true
}

const editCurricularUnit = (curricularUnit: CurricularUnitDto) => {
  console.log('Editing curricular unit:', curricularUnit)
  selectedCurricularUnit.value = curricularUnit
  showEditDialog.value = true
}

const deleteCurricularUnit = (curricularUnit: CurricularUnitDto) => {
  console.log('Preparing to delete curricular unit:', curricularUnit)
  selectedCurricularUnit.value = curricularUnit
  showDeleteDialog.value = true
}

// Actually execute the deletion
const executeDeleteCurricularUnit = async () => {
  if (!selectedCurricularUnit.value?.id) return
  
  try {
    await RemoteService.deleteCurricularUnit(selectedCurricularUnit.value.id)
    
    // Refresh the curricular units list
    await getCurricularUnits()
    
    // Close dialog and reset
    showDeleteDialog.value = false
    selectedCurricularUnit.value = undefined
  } catch (error) {
    console.error('Error deleting curricular unit:', error)
    // TODO: Show error message to user
  }
}

// Permission check - only main teacher can add people to UC
const canAddPeople = (curricularUnit: CurricularUnitDto) => {
  return roleStore.isMainTeacher
}

const addPeople = (curricularUnit: CurricularUnitDto) => {
  console.log('Adding people to curricular unit:', curricularUnit)
  selectedCurricularUnit.value = curricularUnit
  showAddPeopleDialog.value = true
}

const updateSelectedCurricularUnit = (updatedCurricularUnit: CurricularUnitDto) => {
  console.log('Updating selected curricular unit:', updatedCurricularUnit)
  console.log('Current students:', selectedCurricularUnit.value?.studentEnrollments?.length || 0)
  console.log('New students:', updatedCurricularUnit.studentEnrollments?.length || 0)

  // Update the selected curricular unit with the latest data
  selectedCurricularUnit.value = updatedCurricularUnit
  
  // Also update the curricular unit in the main list
  const index = curricularUnits.findIndex(cu => cu.id === updatedCurricularUnit.id)
  if (index !== -1) {
    Object.assign(curricularUnits[index], updatedCurricularUnit)
  }
  
  console.log('Updated selected curricular unit students:', selectedCurricularUnit.value?.studentEnrollments?.length || 0)
}

const fuzzySearch = (value: string, search: string) => {
  // Regex to match any character in between the search characters
  let searchRegex = new RegExp(search.split('').join('.*'), 'i')
  return searchRegex.test(value)
}
</script>