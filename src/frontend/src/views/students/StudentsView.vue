<template>
  <div class="students-view">
    <!-- Header -->
    <div class="d-flex justify-space-between align-center mb-6">
      <div>
        <h1 class="text-h4 font-weight-bold mb-2">Gestão de Estudantes</h1>
        <p class="text-subtitle-1 ma-0">
          Consulte todos os estudantes inscritos nas unidades curriculares
        </p>
      </div>
      <v-chip
        color="primary"
        size="large"
        variant="tonal"
        prepend-icon="mdi-account-group"
      >
        {{ totalStudents }} estudantes
      </v-chip>
    </div>

    <!-- Access Control Check -->
    <div v-if="!hasAccess" class="text-center py-12">
      <v-icon size="64" color="error">mdi-lock</v-icon>
      <h3 class="text-h5 mt-4 mb-2 text-error">Acesso Negado</h3>
      <p class="text-body-1 ">
        Esta página é apenas acessível a professores e assistentes de ensino.
      </p>
    </div>

    <!-- Loading State -->
    <div v-else-if="loading" class="text-center py-12">
      <v-progress-circular
        size="64"
        color="primary"
        indeterminate
      ></v-progress-circular>
      <p class="text-h6 mt-4">A carregar estudantes...</p>
    </div>

    <!-- Content -->
    <div v-else>
      <!-- Search and Filters -->
      <v-card flat class="mb-6">
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="searchQuery"
                label="Pesquisar estudantes"
                prepend-inner-icon="mdi-magnify"
                clearable
                variant="outlined"
                density="compact"
                hide-details
                placeholder="Nome, IST ID ou email..."
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="3">
              <v-select
                v-model="statusFilter"
                :items="statusOptions"
                label="Filtrar por estado"
                variant="outlined"
                density="compact"
                hide-details
              ></v-select>
            </v-col>
            <v-col cols="12" md="3">
              <v-select
                v-model="ucFilter"
                :items="ucOptions"
                label="Filtrar por UC"
                variant="outlined"
                density="compact"
                hide-details
              ></v-select>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Curricular Units List -->
      <div v-if="filteredCurricularUnits.length > 0">
        <v-expansion-panels
          v-model="expandedPanels"
          multiple
        >
          <v-expansion-panel
            v-for="unit in filteredCurricularUnits"
            :key="unit.id"
            :value="unit.id"
          >
            <v-expansion-panel-title>
              <div class="d-flex justify-space-between align-center w-100">
                <div>
                  <h3 class="text-h6">{{ unit.name }}</h3>
                  <p class="text-subtitle-2  mb-0">
                    {{ unit.code }} • {{ unit.semester == 'FIRST' ? '1º Semestre' : '2º Semestre' }} • {{ unit.ects }} ECTS
                  </p>
                </div>
                <div class="d-flex align-center gap-4 mr-4">
                  <v-chip
                    color="primary"
                    size="small"
                    variant="tonal"
                  >
                    {{ getStudentCount(unit.id) }} estudantes
                  </v-chip>
                  <v-chip
                    :color="getStatusColor('APPROVED')"
                    size="small"
                    variant="flat"
                  >
                    {{ getStatusCount(unit.id, 'APPROVED') }} aprovados
                  </v-chip>
                  <v-chip
                    :color="getStatusColor('FAILED')"
                    size="small"
                    variant="flat"
                  >
                    {{ getStatusCount(unit.id, 'FAILED') }} reprovados
                  </v-chip>
                </div>
              </div>
            </v-expansion-panel-title>

            <v-expansion-panel-text>
              <div class="pt-4">
                <!-- Students Table for this UC -->
                <v-data-table
                  :headers="studentHeaders"
                  :items="getFilteredStudentsForUC(unit.id)"
                  :loading="loadingStudents[unit.id]"
                  item-key="id"
                  :sort-by="[{ key: 'student.name', order: 'asc' }]"
                  no-data-text="Nenhum estudante encontrado."
                  :search="searchQuery"
                  density="compact"
                >
                  <template v-slot:[`item.student`]="{ item }">
                    <div class="d-flex align-center">
                      <v-avatar size="32" color="primary" class="mr-3">
                        <span class="text-caption font-weight-bold text-white">
                          {{ getInitials(item.student?.name || '') }}
                        </span>
                      </v-avatar>
                      <div>
                        <div class="font-weight-medium">{{ item.student?.name }}</div>
                        <div class="text-caption ">{{ item.student?.istId }}</div>
                      </div>
                    </div>
                  </template>

                  <template v-slot:[`item.email`]="{ item }">
                    <div class="text-body-2">
                      {{ item.student?.email || 'N/A' }}
                    </div>
                  </template>

                  <template v-slot:[`item.status`]="{ item }">
                    <v-chip
                      :color="getStatusColor(item.status)"
                      size="small"
                      variant="flat"
                    >
                      {{ getStatusText(item.status) }}
                    </v-chip>
                  </template>

                  <template v-slot:[`item.enrollmentDate`]="{ item }">
                    <div class="text-body-2">
                      {{ formatDate(item.enrollmentDate) }}
                    </div>
                  </template>

                  <template v-slot:[`item.finalGrade`]="{ item }">
                    <div v-if="item.finalGrade !== null && item.finalGrade !== undefined">
                      <v-chip
                        :color="getGradeColor(item.finalGrade)"
                        size="small"
                      >
                        {{ item.finalGrade.toFixed(1) }}/20
                      </v-chip>
                    </div>
                    <div v-else class="text-grey">
                      N/A
                    </div>
                  </template>

                  <template v-slot:[`item.completionDate`]="{ item }">
                    <div v-if="item.completionDate" class="text-body-2">
                      {{ formatDate(item.completionDate) }}
                    </div>
                    <div v-else class="text-grey">
                      N/A
                    </div>
                  </template>


                </v-data-table>
              </div>
            </v-expansion-panel-text>
          </v-expansion-panel>
        </v-expansion-panels>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12">
        <v-icon size="64" color="grey-lighten-1">mdi-account-group</v-icon>
        <h3 class="text-h5 mt-4 mb-2 ">Nenhuma unidade curricular encontrada</h3>
        <p class="text-body-1 ">
          {{ searchQuery ? 'Tente ajustar os filtros de pesquisa.' : 'Não há unidades curriculares disponíveis no momento.' }}
        </p>
      </div>
    </div>

    <!-- Student Details Dialog -->
    <v-dialog v-model="showStudentDialog" max-width="600">
      <v-card v-if="selectedStudent">
        <v-card-title>
          <div class="d-flex align-center">
            <v-avatar size="40" color="primary" class="mr-3">
              <span class="text-h6 font-weight-bold text-white">
                {{ getInitials(selectedStudent.student?.name || '') }}
              </span>
            </v-avatar>
            <div>
              <h3>{{ selectedStudent.student?.name }}</h3>
              <p class="text-subtitle-2  mb-0">{{ selectedStudent.student?.istId }}</p>
            </div>
          </div>
        </v-card-title>

        <v-card-text>
          <v-list density="compact">
            <v-list-item>
              <v-list-item-title>Email</v-list-item-title>
              <v-list-item-subtitle>{{ selectedStudent.student?.email || 'N/A' }}</v-list-item-subtitle>
            </v-list-item>
            <v-list-item>
              <v-list-item-title>Estado</v-list-item-title>
              <v-list-item-subtitle>
                <v-chip
                  :color="getStatusColor(selectedStudent.status)"
                  size="small"
                  variant="flat"
                >
                  {{ getStatusText(selectedStudent.status) }}
                </v-chip>
              </v-list-item-subtitle>
            </v-list-item>
            <v-list-item>
              <v-list-item-title>Data de Inscrição</v-list-item-title>
              <v-list-item-subtitle>{{ formatDate(selectedStudent.enrollmentDate) }}</v-list-item-subtitle>
            </v-list-item>
            <v-list-item v-if="selectedStudent.finalGrade">
              <v-list-item-title>Nota Final</v-list-item-title>
              <v-list-item-subtitle>
                <v-chip
                  :color="getGradeColor(selectedStudent.finalGrade)"
                  size="small"
                >
                  {{ selectedStudent.finalGrade.toFixed(1) }}/20
                </v-chip>
              </v-list-item-subtitle>
            </v-list-item>
            <v-list-item v-if="selectedStudent.completionDate">
              <v-list-item-title>Data de Conclusão</v-list-item-title>
              <v-list-item-subtitle>{{ formatDate(selectedStudent.completionDate) }}</v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoleStore } from '../../stores/role'
import RemoteService from '../../services/RemoteService'
import CurricularUnitDto from '../../models/CurricularUnitDto'
import StudentEnrollmentDto from '../../models/StudentEnrollmentDto'

// Stores
const roleStore = useRoleStore()

// Reactive data
const loading = ref(true)
const curricularUnits = ref<CurricularUnitDto[]>([])
const studentEnrollments = ref<Record<number, StudentEnrollmentDto[]>>({})
const loadingStudents = ref<Record<number, boolean>>({})
const expandedPanels = ref<number[]>([])

// Dialog states
const showStudentDialog = ref(false)
const selectedStudent = ref<StudentEnrollmentDto | null>(null)

// Filters
const searchQuery = ref('')
const statusFilter = ref('todos')
const ucFilter = ref('todas')

// Access control
const hasAccess = computed(() => {
  return roleStore.isMainTeacher || roleStore.isTeachingAssistant || roleStore.isAdministrator
})

// Filter options
const statusOptions = [
  { title: 'Todos', value: 'todos' },
  { title: 'Inscritos', value: 'ENROLLED' },
  { title: 'Aprovados', value: 'APPROVED' },
  { title: 'Reprovados', value: 'FAILED' }
]

const ucOptions = ref([
  { title: 'Todas as UCs', value: 'todas' }
])

// Table headers
const studentHeaders = [
  { title: 'Estudante', key: 'student', value: 'student' },
  { title: 'Email', key: 'email', value: 'email' },
  { title: 'Estado', key: 'status', value: 'status' },
  { title: 'Data de Inscrição', key: 'enrollmentDate', value: 'enrollmentDate' },
  { title: 'Nota Final', key: 'finalGrade', value: 'finalGrade' },
  { title: 'Data de Conclusão', key: 'completionDate', value: 'completionDate' }
]

// Computed properties
const filteredCurricularUnits = computed(() => {
  let filtered = curricularUnits.value

  // UC filter
  if (ucFilter.value !== 'todas') {
    filtered = filtered.filter(unit => unit.name === ucFilter.value)
  }

  return filtered
})

const totalStudents = computed(() => {
  return Object.values(studentEnrollments.value)
    .flat()
    .length
})

// Data loading
const loadData = async () => {
  if (!hasAccess.value) return

  loading.value = true
  try {
    // Load curricular units
    curricularUnits.value = await RemoteService.getCurricularUnits()

    // Populate UC filter options
    const ucNames = curricularUnits.value.map(unit => unit.name).filter(Boolean)
    ucOptions.value = [
      { title: 'Todas as UCs', value: 'todas' },
      ...ucNames.map(name => ({ title: name, value: name }))
    ]

    // Load student enrollments for each UC
    for (const unit of curricularUnits.value) {
      if (unit.id) {
        try {
          loadingStudents.value[unit.id] = true
          const enrollments = await RemoteService.getEnrollmentsByCurricularUnit(unit.id)
          studentEnrollments.value[unit.id] = enrollments.map(enrollment => new StudentEnrollmentDto(enrollment))
        } catch (error) {
          console.error(`Error loading enrollments for UC ${unit.id}:`, error)
          studentEnrollments.value[unit.id] = []
        } finally {
          loadingStudents.value[unit.id] = false
        }
      }
    }
  } catch (error) {
    console.error('Error loading data:', error)
  } finally {
    loading.value = false
  }
}

// Helper functions
const getFilteredStudentsForUC = (ucId: number) => {
  const students = studentEnrollments.value[ucId] || []
  
  // Apply status filter
  if (statusFilter.value !== 'todos') {
    return students.filter(student => student.status === statusFilter.value)
  }
  
  return students
}

const getStudentCount = (ucId: number) => {
  return studentEnrollments.value[ucId]?.length || 0
}

const getStatusCount = (ucId: number, status: string) => {
  return studentEnrollments.value[ucId]?.filter(student => student.status === status).length || 0
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'ENROLLED': return 'primary'
    case 'APPROVED': return 'success'
    case 'FAILED': return 'error'
    default: return 'grey'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'ENROLLED': return 'Inscrito'
    case 'APPROVED': return 'Aprovado'
    case 'FAILED': return 'Reprovado'
    default: return 'Desconhecido'
  }
}

const getGradeColor = (grade: number) => {
  if (grade >= 16) return 'success'
  if (grade >= 10) return 'warning'
  return 'error'
}

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('pt-PT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const getInitials = (name: string) => {
  return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2)
}

// Actions
const viewStudentDetails = (student: StudentEnrollmentDto) => {
  selectedStudent.value = student
  showStudentDialog.value = true
}


// Lifecycle
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.students-view {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.v-expansion-panel-title {
  padding: 16px 24px;
}

.v-expansion-panel-text {
  padding: 0 24px 24px 24px;
}

.bg-surface-variant {
  background-color: rgb(var(--v-theme-surface-variant)) !important;
}
</style>
