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
        mdi-account-plus
      </v-icon>
      
      <!-- Edit and Delete - admin only -->
      <template v-if="isAdmin">
        <v-icon @click="editCurricularUnit(item)" class="mr-2" tonal>mdi-pencil</v-icon>
        <v-icon @click="deleteCurricularUnit(item)" tonal>mdi-delete</v-icon>
      </template>
    </template>
  </v-data-table>

  <!-- Details Dialog -->
  <v-dialog v-model="showDetailsDialog" max-width="800">
    <v-card v-if="selectedCurricularUnit">
      <v-card-title class="d-flex justify-space-between align-center">
        <span class="text-h5">{{ selectedCurricularUnit.name }}</span>
        <v-btn 
          icon="mdi-close" 
          variant="text" 
          size="small"
          @click="showDetailsDialog = false"
        ></v-btn>
      </v-card-title>

      <v-card-text>
        <v-row>
          <v-col cols="6">
            <strong>Código:</strong> {{ selectedCurricularUnit.code }}
          </v-col>
          <v-col cols="6">
            <strong>ECTS:</strong> {{ selectedCurricularUnit.ects }}
          </v-col>
        </v-row>
        <v-row>
          <v-col cols="6">
            <strong>Semestre:</strong> {{ getSemesterText(selectedCurricularUnit.semester) }}
          </v-col>
          <v-col cols="6">
            <strong>Professor Regente:</strong> {{ selectedCurricularUnit.mainTeacher.name }}
          </v-col>
        </v-row>

        <v-divider class="my-4"></v-divider>

        <v-tabs v-model="activeTab" class="mt-6">
          <v-tab>Professores</v-tab>
          <v-tab>Alunos</v-tab>
        </v-tabs>

        <v-tabs-window v-model="activeTab">
          <!-- Teachers Tab -->
          <v-tabs-window-item>
            <v-data-table
              :headers="teacherHeaders"
              :items="allTeachers"
              class="mt-4"
              no-data-text="Sem professores a apresentar."
            >
              <template v-slot:[`item.type`]="{ item }">
                <v-chip 
                  :color="item.type === 'Regente' ? 'red' : 'blue'" 
                  size="small"
                >
                  {{ item.type }}
                </v-chip>
              </template>
            </v-data-table>
          </v-tabs-window-item>

          <!-- Students Tab -->
          <v-tabs-window-item>
            <v-data-table
              :headers="studentHeaders"
              :items="allStudents"
              class="mt-4"
              no-data-text="Sem alunos a apresentar."
            >
              <template v-slot:[`item.status`]="{ item }">
              </template>
              <!-- TODO: Status will be implemented later when backend supports it -->
            </v-data-table>
          </v-tabs-window-item>
        </v-tabs-window>
      </v-card-text>
    </v-card>
  </v-dialog>

  <!-- Edit Dialog -->
  <EditCurricularUnitDialog 
    v-if="isAdmin"
    v-model="showEditDialog"
    :curricular-unit="selectedCurricularUnit"
    @curricular-unit-updated="getCurricularUnits"
  />

  <!-- Delete Dialog -->
  <DeleteCurricularUnitDialog 
    v-if="isAdmin"
    v-model="showDeleteDialog"
    :curricular-unit="selectedCurricularUnit"
    @curricular-unit-deleted="getCurricularUnits"
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
import DeleteCurricularUnitDialog from './DeleteCurricularUnitsDialog.vue'
import AddPeopleDialog from './AddPeopleDialog.vue'

let search = ref('')
let loading = ref(true)
let activeTab = ref(0)

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

const teacherHeaders = [
  { title: 'Nome', key: 'name', value: 'name' },
  { title: 'IST ID', key: 'istId', value: 'istId' },
  { title: 'Email', key: 'email', value: 'email' },
  { title: 'Tipo', key: 'type', value: 'type' }
]

const studentHeaders = [
  { title: 'Nome', key: 'name', value: 'name' },
  { title: 'IST ID', key: 'istId', value: 'istId' },
  { title: 'Email', key: 'email', value: 'email' }
  // Status will be added later when backend supports it
  // { title: 'Estado', key: 'status', value: 'status' }
]

const curricularUnits: CurricularUnitDto[] = reactive([])

const allTeachers = computed(() => {
  if (!selectedCurricularUnit.value) return []
  
  const teachers = []
  
  // Add main teacher
  teachers.push({
    ...selectedCurricularUnit.value.mainTeacher,
    type: 'Regente'
  })
  
  // Add assistant teachers
  selectedCurricularUnit.value.assistantTeachers.forEach(teacher => {
    teachers.push({
      ...teacher,
      type: 'Assistente'
    })
  })
  
  return teachers
})

const allStudents = computed(() => {
  if (!selectedCurricularUnit.value) return []
  return selectedCurricularUnit.value.students || []
})

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
    'SECOND': '2º Semestre',
    'ANNUAL': 'Anual'
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

const getStatusText = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'inscrito': 'Inscrito',
    'aprovado': 'Aprovado',
    'reprovado': 'Reprovado'
  }
  return statusMap[status] || status
}

const getStatusColor = (status: string) => {
  const colorMap: { [key: string]: string } = {
    'inscrito': 'blue',
    'aprovado': 'green',
    'reprovado': 'red'
  }
  return colorMap[status] || 'grey'
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
  console.log('Deleting curricular unit:', curricularUnit)
  selectedCurricularUnit.value = curricularUnit
  showDeleteDialog.value = true
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
  console.log('Current students:', selectedCurricularUnit.value?.students?.length || 0)
  console.log('New students:', updatedCurricularUnit.students?.length || 0)
  
  // Update the selected curricular unit with the latest data
  selectedCurricularUnit.value = updatedCurricularUnit
  
  // Also update the curricular unit in the main list
  const index = curricularUnits.findIndex(cu => cu.id === updatedCurricularUnit.id)
  if (index !== -1) {
    Object.assign(curricularUnits[index], updatedCurricularUnit)
  }
  
  console.log('Updated selected curricular unit students:', selectedCurricularUnit.value?.students?.length || 0)
}

const fuzzySearch = (value: string, search: string) => {
  // Regex to match any character in between the search characters
  let searchRegex = new RegExp(search.split('').join('.*'), 'i')
  return searchRegex.test(value)
}
</script>