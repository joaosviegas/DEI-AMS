<template>
  <v-dialog v-model="localDialog" max-width="1500">
    <v-card v-if="curricularUnit">
      <v-card-title class="d-flex justify-space-between align-center">
        <span class="text-h5">{{ curricularUnit.name }}</span>
        <v-btn 
          icon="mdi-close" 
          variant="text" 
          size="small"
          @click="localDialog = false"
        ></v-btn>
      </v-card-title>

      <v-card-text>
        <v-row>
          <v-col cols="6">
            <strong>Código:</strong> {{ curricularUnit.code }}
          </v-col>
          <v-col cols="6">
            <strong>ECTS:</strong> {{ curricularUnit.ects }}
          </v-col>
        </v-row>
        <v-row>
          <v-col cols="6">
            <strong>Semestre:</strong> {{ getSemesterText(curricularUnit.semester) }}
          </v-col>
          <v-col cols="6">
            <strong>Professor Regente:</strong> {{ curricularUnit.mainTeacher.name }}
          </v-col>
        </v-row>
        <v-row>
          <v-col cols="6">
            <strong>Cursos:</strong>
            <div class="mt-2">
              <v-chip 
                v-for="course in curricularUnit.courses" 
                :key="course.id" 
                size="small" 
                class="mr-1 mb-1"
              >
                {{ course.code }}
              </v-chip>
            </div>
          </v-col>
        </v-row>

        <v-divider class="my-4"></v-divider>

        <v-tabs v-model="activeTab" class="mt-6">
          <v-tab>Professores</v-tab>
          <v-tab>Alunos</v-tab>
          <v-tab>Avaliações</v-tab>
        </v-tabs>

        <v-tabs-window v-model="activeTab">
          <!-- Teachers Tab -->
          <v-tabs-window-item>
            <!-- Add Teachers Button (Only for Main Teachers) -->
            <div v-if="canManageEvaluations" class="mb-4 d-flex justify-end">
              <v-btn 
                color="primary" 
                prepend-icon="mdi-account-edit"
                @click="openAddTeachersDialog"
              >
                Gerir Professores
              </v-btn>
            </div>

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
            <!-- Add Students Button (Only for Main Teachers) -->
            <div v-if="canManageEvaluations" class="mb-4 d-flex justify-end">
              <v-btn 
                color="primary" 
                prepend-icon="mdi-account-edit"
                @click="openAddStudentsDialog"
              >
                Gerir Alunos
              </v-btn>
            </div>

            <v-data-table
              :headers="studentHeaders"
              :items="allStudents"
              class="mt-4"
              no-data-text="Sem alunos a apresentar."
            >
              <template v-slot:[`item.status`]="{ item }">
                <v-chip 
                  :color="getStatusColor(item.status)" 
                  size="small"
                >
                  {{ getStatusText(item.status) }}
                </v-chip>
              </template>
            </v-data-table>
          </v-tabs-window-item>

          <!-- Evaluations Tab -->
          <v-tabs-window-item>
            <!-- Create Test Button (Only for Main Teachers) -->
            <div v-if="canManageEvaluations" class="mb-4 d-flex justify-end">
              <v-btn 
                color="primary" 
                prepend-icon="mdi-plus"
                @click="showCreateTestDialog = true"
              >
                Criar Teste
              </v-btn>
            </div>

            <v-data-table
              :headers="evaluationHeaders"
              :items="allEvaluations"
              class="mt-4"
              no-data-text="Sem avaliações a apresentar."
              :loading="loadingEvaluations"
            >
              <template v-slot:[`item.type`]="{ item }">
                <v-chip 
                  :color="item.type === 'TEST' ? 'green' : 'blue'" 
                  size="small"
                >
                  {{ item.type === 'TEST' ? 'Teste' : 'Projeto' }}
                </v-chip>
              </template>
              <template v-slot:[`item.date`]="{ item }">
                {{ formatDate(item.date) }}
              </template>
              <template v-slot:[`item.weight`]="{ item }">
                {{ item.weight*100 }}% <!-- Show weight as percentage -->
              </template>
              <template v-slot:[`item.actions`]="{ item }">
                <v-btn
                v-if="roleStore.isStudent"
                  icon="mdi-eye"
                  variant="text"
                  size="small"
                  @click="openEvaluationDetails(item)"
                  title="Ver Notas"
                ></v-btn>
                <v-btn 
                  v-if="canGradeEvaluations"
                  icon="mdi-clipboard-edit"
                  variant="text"
                  size="small"
                  @click="openEvaluationDetails(item)"
                  title="Atribuir Notas"
                ></v-btn>
                <v-btn 
                  v-if="canManageEvaluations"
                  icon="mdi-delete"
                  variant="text"
                  color="red"
                  size="small"
                  @click="confirmDeleteTest(item)"
                  title="Eliminar"
                ></v-btn>
              </template>
            </v-data-table>
          </v-tabs-window-item>
        </v-tabs-window>
      </v-card-text>
    </v-card>
  </v-dialog>

  <!-- AddPeople Dialog -->
  <AddPeopleDialog 
    v-model="showAddPeopleDialog"
    :curricular-unit="curricularUnit"
    :initial-tab="addPeopleInitialTab"
    @people-updated="handlePeopleUpdated"
    @curricular-unit-updated="handleCurricularUnitUpdated"
  />

  <!-- Create Test Dialog -->
  <CreateTestDialog
    v-model="showCreateTestDialog"
    :curricular-unit-id="curricularUnit?.id || 0"
    @test-created="loadEvaluations"
  />
  
  <!-- Evaluation Details Dialog -->
  <EvaluationDetailsDialog
    v-model="showEvaluationDetailsDialog"
    :evaluation="selectedEvaluation"
  />
  
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import CurricularUnitDto from '../../models/CurricularUnitDto'
import TestDto from '../../models/TestDto'
import RemoteService from '../../services/RemoteService'
import { useRoleStore } from '../../stores/role'
import AddPeopleDialog from './ManagePeopleDialog.vue'
import CreateTestDialog from './evaluations/CreateTestDialog.vue'
import EvaluationDetailsDialog from './evaluations/EvaluationDetailsDialog.vue'

const emit = defineEmits(['update:modelValue', 'curricular-unit-updated'])

const props = defineProps({
  curricularUnit: {
    type: Object as () => CurricularUnitDto | undefined,
    default: undefined
  },
  modelValue: {
    type: Boolean,
    default: false
  }
})

const showAddPeopleDialog = ref(false)
const addPeopleInitialTab = ref(0) // 0 for Teachers, 1 for Students
const selectedCurricularUnit = ref<CurricularUnitDto>()

const activeTab = ref(0)
const roleStore = useRoleStore()

// Evaluations data
const allEvaluations = ref<TestDto[]>([])
const loadingEvaluations = ref(false)
const showCreateTestDialog = ref(false)
const showEvaluationDetailsDialog = ref(false)
const selectedEvaluation = ref<TestDto | undefined>()

// Computed property that always returns the most up-to-date curricular unit
const curricularUnit = computed(() => {
  return selectedCurricularUnit.value || props.curricularUnit
})

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
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
  { title: 'Email', key: 'email', value: 'email' },
  { title: 'Estado', key: 'status', value: 'status' }
]

const evaluationHeaders = [
  { title: 'Título', key: 'title', value: 'title' },
  { title: 'Tipo', key: 'type', value: 'type' },
  { title: 'Data', key: 'date', value: 'date' },
  { title: 'Peso', key: 'weight', value: 'weight' },
  { title: 'Ações', key: 'actions', value: 'actions', sortable: false }
]

const allTeachers = computed(() => {
  if (!curricularUnit.value) return []
  
  const teachers = []
  
  // Add main teacher
  teachers.push({
    ...curricularUnit.value.mainTeacher,
    type: 'Regente'
  })
  
  // Add assistant teachers
  curricularUnit.value.assistantTeachers.forEach(teacher => {
    teachers.push({
      ...teacher,
      type: 'Assistente'
    })
  })
  
  return teachers
})

// Permission-based computed properties
const canManageEvaluations = computed(() => {
  return roleStore.isMainTeacher
})

const canGradeEvaluations = computed(() => {
  return roleStore.isMainTeacher || roleStore.isTeachingAssistant
})

const allStudents = computed(() => {
  if (!curricularUnit.value?.studentEnrollments) return []
  
  // Transform enrollments to show student data with status
  return curricularUnit.value.studentEnrollments.map(enrollment => ({
    ...enrollment.student,
    status: enrollment.status
  }))
})

// Helper function to get status color
const getStatusColor = (status: string) => {
  switch (status) {
    case 'ENROLLED': return 'blue'
    case 'APPROVED': return 'green'
    case 'FAILED': return 'red'
    default: return 'grey'
  }
}

// Helper function for status display text
const getStatusText = (status: string) => {
  switch (status) {
    case 'ENROLLED': return 'Inscrito';
    case 'APPROVED': return 'Aprovado';
    case 'FAILED': return 'Reprovado';
    default: return 'Desconhecido';
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

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('pt-PT')
}

// Evaluation management functions
const loadEvaluations = async () => {
  if (!curricularUnit.value?.id) return
  
  loadingEvaluations.value = true
  try {
    const evaluations = await RemoteService.getTestsByCurricularUnit(curricularUnit.value.id)
    allEvaluations.value = evaluations.map(TestDto.fromBackend)
  } catch (error) {
    console.error('Error loading evaluations:', error)
  } finally {
    loadingEvaluations.value = false
  }
}

const openEvaluationDetails = (evaluation: TestDto) => {
  selectedEvaluation.value = evaluation
  showEvaluationDetailsDialog.value = true
}

const confirmDeleteTest = (evaluation: TestDto) => {
  // TODO: Implement delete confirmation
  console.log('Confirming delete for:', evaluation)
}

// Methods for opening AddPeopleDialog with correct tab
const openAddTeachersDialog = () => {
  addPeopleInitialTab.value = 0 // Teachers tab
  showAddPeopleDialog.value = true
}

const openAddStudentsDialog = () => {
  addPeopleInitialTab.value = 1 // Students tab
  showAddPeopleDialog.value = true
}

// Event handlers for AddPeopleDialog
const handlePeopleUpdated = () => {
  // This would typically refresh the curricular unit data
  // For now, we'll emit to the parent to handle the refresh
  emit('curricular-unit-updated')
}

const handleCurricularUnitUpdated = (updatedCU: CurricularUnitDto) => {
  // Update local data to trigger reactivity
  selectedCurricularUnit.value = updatedCU
  // Force update of allStudents and allTeachers computeds by triggering a re-render
  nextTick(() => {
    emit('curricular-unit-updated', updatedCU)
  })
}

// Watch for curricular unit changes to load evaluations
watch(() => curricularUnit.value, async (newCU) => {
  if (newCU) {
    await loadEvaluations()
  }
}, { immediate: true })

// Also watch props.curricularUnit for initial load
watch(() => props.curricularUnit, async (newCU) => {
  if (newCU && !selectedCurricularUnit.value) {
    await loadEvaluations()
  }
}, { immediate: true })
</script>
