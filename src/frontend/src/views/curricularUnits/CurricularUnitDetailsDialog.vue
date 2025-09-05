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
          <v-tab>Recursos</v-tab>
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
            <!-- Create Button (Only for Main Teachers) -->
            <div v-if="canManageEvaluations" class="mb-4 d-flex justify-end gap-2">
              <v-btn
                color="primary" 
                prepend-icon="mdi-plus"
                @click="showCreateTestDialog = true"
              >
                Criar Teste
              </v-btn>
              <v-btn
                color="secondary" 
                prepend-icon="mdi-folder-plus"
                @click="showCreateProjectDialog = true"
              >
                Criar Projeto
              </v-btn>
            </div>

            <!-- Hidden file inputs for test uploads -->
            <input
              ref="testStatementInput"
              type="file"
              style="display: none"
              @change="handleTestStatementUpload"
              accept=".pdf"
            />
            <input
              ref="testCorrectionInput"
              type="file"
              style="display: none"
              @change="handleTestCorrectionUpload"
              accept=".pdf"
            />

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
                <!-- Student actions -->
                <v-btn
                  v-if="roleStore.isStudent"
                  icon="mdi-eye"
                  variant="text"
                  size="small"
                  @click="openEvaluationDetails(item)"
                  title="Ver Notas"
                  class="mr-1"
                ></v-btn>

                <!-- Student test file downloads (only for TEST type) -->
                <template v-if="roleStore.isStudent && item.type === 'TEST'">
                  <v-btn
                    v-if="getTestStatement(item.id)"
                    icon="mdi-file-document-outline"
                    variant="text"
                    size="small"
                    color="blue"
                    @click="downloadResource(getTestStatement(item.id)!)"
                    title="Descarregar Enunciado"
                    class="mr-1"
                  ></v-btn>
                  <v-btn
                    v-if="getTestCorrection(item.id)"
                    icon="mdi-file-check-outline"
                    variant="text"
                    size="small"
                    color="green"
                    @click="downloadResource(getTestCorrection(item.id)!)"
                    title="Descarregar Correção"
                    class="mr-1"
                  ></v-btn>
                </template>
                
                <!-- Teacher actions -->
                <v-btn 
                  v-if="canGradeEvaluations"
                  icon="mdi-clipboard-edit"
                  variant="text"
                  size="small"
                  @click="openEvaluationDetails(item)"
                  title="Atribuir Notas"
                  class="mr-1"
                ></v-btn>

                <!-- Teacher test file uploads (only for TEST type) -->
                <template v-if="canGradeEvaluations && item.type === 'TEST'">
                  <v-btn
                    icon="mdi-upload"
                    variant="text"
                    size="small"
                    :color="getTestStatement(item.id) ? 'blue' : 'white'"
                    @click="triggerTestStatementUpload(item.id)"
                    title="Carregar Enunciado"
                    class="mr-1"
                  ></v-btn>
                  <v-btn
                    icon="mdi-upload"
                    variant="text"
                    size="small"
                    :color="getTestCorrection(item.id) ? 'green' : 'white'"
                    @click="triggerTestCorrectionUpload(item.id)"
                    title="Carregar Correção"
                    class="mr-1"
                  ></v-btn>
                </template>

                <v-btn
                  v-if="canManageEvaluations"
                  icon="mdi-pencil"
                  variant="text"
                  size="small"
                  @click="openEditEvaluationDialog(item)"
                  title="Editar"
                  class="mr-1"
                ></v-btn>
                <v-btn 
                  v-if="canManageEvaluations"
                  icon="mdi-delete"
                  variant="text"
                  size="small"
                  @click="confirmDeleteTest(item)"
                  title="Eliminar"
                ></v-btn>
              </template>
            </v-data-table>
          </v-tabs-window-item>
          <!-- Resources Tab -->
          <v-tabs-window-item>
            <!-- Upload File Buttons (Only for Teachers) -->
            <div v-if="canGradeEvaluations" class="mb-4 d-flex justify-end gap-2">
              <input
                ref="fileInput"
                type="file"
                style="display: none"
                @change="handleFileUpload"
                accept=".pdf"
              />
              <v-btn 
                color="primary" 
                prepend-icon="mdi-upload"
                @click="triggerFileUpload"
              >
                Carregar Material
              </v-btn>
            </div>

            <v-data-table
              :headers="resourceHeaders"
              :items="allResources"
              class="mt-4"
              no-data-text="Sem recursos a apresentar."
              :loading="loadingResources"
            >
              <template v-slot:[`item.fileSize`]="{ item }">
                {{ formatFileSize(item.fileSize) }}
              </template>
              <template v-slot:[`item.uploadDate`]="{ item }">
                {{ formatDate(item.uploadDate) }}
              </template>
              <template v-slot:[`item.actions`]="{ item }">
                <v-btn
                  icon="mdi-download"
                  variant="text"
                  size="small"
                  @click="downloadResource(item)"
                  title="Descarregar"
                ></v-btn>
                <v-btn
                  v-if="canGradeEvaluations"
                  icon="mdi-delete"
                  variant="text"
                  size="small"
                  @click="confirmDeleteResource(item)"
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
  
  <!-- Create Project Dialog -->
  <CreateProjectDialog
    v-model="showCreateProjectDialog"
    :curricular-unit-id="curricularUnit?.id || 0"
    @project-created="loadEvaluations"
  />
  
  <!-- Evaluation Details Dialog -->
  <EvaluationDetailsDialog
    v-model="showEvaluationDetailsDialog"
    :evaluation="selectedEvaluation"
  />

  <!-- Edit Test Dialog -->
  <EditTestDialog
    v-model="showEditTestDialog"
    :test="selectedEvaluationForEdit"
    @test-updated="loadEvaluations"
  />

  <!-- Edit Project Dialog -->
  <EditProjectDialog
    v-model="showEditProjectDialog"
    :project="selectedProjectForEdit"
    @project-updated="loadEvaluations"
  />

  <!-- Delete Test Confirmation Dialog -->
  <ConfirmDeleteDialog
    v-model="showDeleteDialog"
    :title="`Eliminar ${evaluationToDelete?.type === 'TEST' ? 'Teste' : 'Projeto'}`"
    :message="`Tem a certeza de que deseja eliminar ${evaluationToDelete?.type === 'TEST' ? 'o teste' : 'o projeto'} '${evaluationToDelete?.title}'?`"
    :item-name="evaluationToDelete?.title"
    :item-subtitle="`${formatDate(evaluationToDelete?.date || '')} • Peso: ${(evaluationToDelete?.weight || 0) * 100}%`"
    icon="mdi-file-document-outline"
    icon-color="green"
    :confirm-text="`Eliminar ${evaluationToDelete?.type === 'TEST' ? 'Teste' : 'Projeto'}`"
    warning-message="Esta ação eliminará permanentemente a avaliação e todas as notas associadas."
    @confirm="deleteEvaluation"
  />

  <!-- Delete Resource Confirmation Dialog -->
  <ConfirmDeleteDialog
    v-model="showDeleteResourceDialog"
    title="Eliminar Recurso"
    :message="`Tem a certeza de que deseja eliminar o arquivo '${resourceToDelete?.name}'?`"
    item-type="recurso"
    :item-name="resourceToDelete?.name"
    :item-subtitle="`${formatFileSize(resourceToDelete?.fileSize || 0)}`"
    icon="mdi-file-outline"
    icon-color="orange"
    confirm-text="Eliminar Recurso"
    warning-message="Esta ação eliminará permanentemente o arquivo."
    @confirm="deleteResource"
  />
  
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import CurricularUnitDto from '../../models/CurricularUnitDto'
import TestDto from '../../models/TestDto'
import ProjectDto from '../../models/ProjectDto'
import ResourceDto from '../../models/ResourceDto'
import RemoteService from '../../services/RemoteService'
import { useRoleStore } from '../../stores/role'
import AddPeopleDialog from './ManagePeopleDialog.vue'
import CreateTestDialog from './evaluations/CreateTestDialog.vue'
import CreateProjectDialog from './evaluations/CreateProjectDialog.vue'
import EvaluationDetailsDialog from './evaluations/EvaluationDetailsDialog.vue'
import EditTestDialog from './evaluations/EditTestDialog.vue'
import EditProjectDialog from './evaluations/EditProjectDialog.vue'
import ConfirmDeleteDialog from '../../components/ConfirmDeleteDialog.vue'

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
const fileInput = ref<HTMLInputElement>()
const testStatementInput = ref<HTMLInputElement>()
const testCorrectionInput = ref<HTMLInputElement>()

const activeTab = ref(0)
const roleStore = useRoleStore()

// Evaluations data
const allEvaluations = ref<(TestDto | ProjectDto)[]>([])
const loadingEvaluations = ref(false)
const showCreateTestDialog = ref(false)
const showCreateProjectDialog = ref(false)
const showEvaluationDetailsDialog = ref(false)
const selectedEvaluation = ref<TestDto | ProjectDto | undefined>()

// Resources data
const allResources = ref<ResourceDto[]>([])
const loadingResources = ref(false)

// Test files tracking per evaluation ID
const testFiles = ref<{[evaluationId: number]: { statement?: ResourceDto, correction?: ResourceDto }}>({})
const currentUploadEvaluationId = ref<number | null>(null)

// Edit dialog
const showEditTestDialog = ref(false)
const selectedEvaluationForEdit = ref<TestDto | undefined>()
const showEditProjectDialog = ref(false)
const selectedProjectForEdit = ref<ProjectDto | undefined>()

// Delete confirmation dialog
const showDeleteDialog = ref(false)
const evaluationToDelete = ref<TestDto | ProjectDto | null>(null)

// Delete resource confirmation dialog
const showDeleteResourceDialog = ref(false)
const resourceToDelete = ref<ResourceDto | null>(null)

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

const resourceHeaders = [
  { title: 'Nome', key: 'name', value: 'name' },
  { title: 'Tamanho', key: 'fileSize', value: 'fileSize' },
  { title: 'Data', key: 'uploadDate', value: 'uploadDate' },
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

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

// Helper functions for test files
const getTestStatement = (evaluationId: number): ResourceDto | null => {
  return testFiles.value[evaluationId]?.statement || null
}

const getTestCorrection = (evaluationId: number): ResourceDto | null => {
  return testFiles.value[evaluationId]?.correction || null
}

// Debug computed property
const debugTestFiles = computed(() => {
  console.log('Current testFiles:', testFiles.value)
  return testFiles.value
})

// Evaluation management functions
const loadEvaluations = async () => {
  if (!curricularUnit.value?.id) return
  
  loadingEvaluations.value = true
  try {
    // Load both tests and projects
    const [tests, projects] = await Promise.all([
      RemoteService.getTestsByCurricularUnit(curricularUnit.value.id),
      RemoteService.getProjectsByCurricularUnit(curricularUnit.value.id)
    ])
    
    const testDtos = tests.map(TestDto.fromBackend)
    const projectDtos = projects.map(ProjectDto.fromBackend)
    
    // Combine and sort by date
    allEvaluations.value = [...testDtos, ...projectDtos].sort((a, b) => 
      new Date(a.date).getTime() - new Date(b.date).getTime()
    )
  } catch (error) {
    console.error('Error loading evaluations:', error)
  } finally {
    loadingEvaluations.value = false
  }
}

const openEvaluationDetails = (evaluation: TestDto | ProjectDto) => {
  selectedEvaluation.value = evaluation
  showEvaluationDetailsDialog.value = true
}
// Opens the corresponding edit dialog based on evaluation type
const openEditEvaluationDialog = (evaluation: TestDto | ProjectDto) => {
  if (evaluation.type === 'TEST') {
    selectedEvaluationForEdit.value = evaluation as TestDto
    showEditTestDialog.value = true
  } else if (evaluation.type === 'PROJECT') {
    selectedProjectForEdit.value = evaluation as ProjectDto
    showEditProjectDialog.value = true
  }
}

const confirmDeleteTest = (evaluation: TestDto | ProjectDto) => {
  evaluationToDelete.value = evaluation
  showDeleteDialog.value = true
}

const deleteEvaluation = async () => {
  if (!evaluationToDelete.value?.id) return
  
  try {
    if (evaluationToDelete.value.type === 'TEST') {
      await RemoteService.deleteTest(evaluationToDelete.value.id)
    } else {
      await RemoteService.deleteProject(evaluationToDelete.value.id)
    }
    
    // Refresh evaluations list
    await loadEvaluations()
    
    // Reset and close dialog
    evaluationToDelete.value = null
    showDeleteDialog.value = false
  } catch (error) {
    console.error('Error deleting evaluation:', error)
  }
}

// Resource management functions
const saveTestFilesToStorage = () => {
  if (curricularUnit.value?.id) {
    localStorage.setItem(
      `testFiles_${curricularUnit.value.id}`, 
      JSON.stringify(testFiles.value)
    )
  }
}

const loadTestFilesFromStorage = () => {
  if (!curricularUnit.value?.id) return
  
  const saved = localStorage.getItem(`testFiles_${curricularUnit.value.id}`)
  if (saved) {
    const savedTestFiles = JSON.parse(saved)
    // Rebuild test files references from loaded resources
    for (const [evalId, files] of Object.entries(savedTestFiles)) {
      const evaluationId = parseInt(evalId)
      const filesObj = files as any
      testFiles.value[evaluationId] = {
        statement: filesObj.statement ? allResources.value.find((r: ResourceDto) => r.id === filesObj.statement.id) : undefined,
        correction: filesObj.correction ? allResources.value.find((r: ResourceDto) => r.id === filesObj.correction.id) : undefined
      }
    }
  }
}

const loadResources = async () => {
  if (!curricularUnit.value?.id) {
    console.log('No curricular unit ID available for loading resources')
    return
  }
  
  loadingResources.value = true
  try {
    console.log('Loading resources for curricular unit:', curricularUnit.value.id)
    // Only load materials since students won't upload submissions here
    const materials = await RemoteService.getMaterials(curricularUnit.value.id)
    console.log('Loaded materials:', materials)
    allResources.value = materials
    
    // Load test files tracking after resources are loaded
    loadTestFilesFromStorage()
    
    console.log('Resources updated in component:', allResources.value)
    console.log('Test files:', testFiles.value)
  } catch (error) {
    console.error('Error loading resources:', error)
  } finally {
    loadingResources.value = false
  }
}

const triggerFileUpload = () => {
  fileInput.value?.click()
}

const triggerTestStatementUpload = (evaluationId: number) => {
  console.log('triggerTestStatementUpload called with evaluationId:', evaluationId)
  currentUploadEvaluationId.value = evaluationId
  testStatementInput.value?.click()
}

const triggerTestCorrectionUpload = (evaluationId: number) => {
  currentUploadEvaluationId.value = evaluationId
  testCorrectionInput.value?.click()
}

const handleFileUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file || !curricularUnit.value?.id) {
    console.log('No file selected or no curricular unit ID')
    return
  }
  
  try {
    console.log('Uploading file:', file.name, 'Size:', file.size, 'bytes', 'Type:', file.type)
    console.log('Curricular Unit ID:', curricularUnit.value.id)
    
    const response = await RemoteService.uploadFile(curricularUnit.value.id, file, 'MATERIAL')
    console.log('Upload response:', response)
    
    await loadResources() // Refresh the list
    console.log('Resources reloaded after upload')
    
    // Clear the input
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    console.log('File uploaded successfully')
  } catch (error) {
    console.error('Error uploading file:', error)
  }
}

const handleTestStatementUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file || !curricularUnit.value?.id || !currentUploadEvaluationId.value) {
    console.log('No file selected, no curricular unit ID, or no evaluation ID')
    return
  }
  
  const evaluationId = currentUploadEvaluationId.value
  
  try {
    console.log('Uploading test statement for evaluation:', evaluationId, 'file:', file.name)
    
    // If there's already a test statement file for this evaluation, delete it first
    const existingStatement = getTestStatement(evaluationId)
    if (existingStatement) {
      await RemoteService.deleteResource(existingStatement.id)
    }
    
    const response = await RemoteService.uploadFile(curricularUnit.value.id, file, 'MATERIAL')
    console.log('Test statement upload response:', response)
    
    // Reload resources first to get the new file
    await loadResources()
    
    // Find the newly uploaded file and track it AFTER resources are loaded
    // Note: Backend adds UUID prefix to fileName, so we match by checking if fileName ends with original name
    const uploadedFile = allResources.value.find(resource => 
      resource.fileName.endsWith(file.name) || resource.name === file.name
    )
    console.log('Looking for uploaded file:', file.name, 'found:', uploadedFile)
    
    if (uploadedFile) {
      if (!testFiles.value[evaluationId]) {
        testFiles.value[evaluationId] = {}
      }
      testFiles.value[evaluationId].statement = uploadedFile
      saveTestFilesToStorage()
      console.log('Saved to storage, current testFiles:', testFiles.value)
    } else {
      console.error('Could not find uploaded file in resources!')
    }
    
    // Clear the input and reset evaluation ID
    if (testStatementInput.value) {
      testStatementInput.value.value = ''
    }
    currentUploadEvaluationId.value = null
    console.log('Test statement uploaded successfully for evaluation:', evaluationId)
  } catch (error) {
    console.error('Error uploading test statement:', error)
    currentUploadEvaluationId.value = null
  }
}

const handleTestCorrectionUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file || !curricularUnit.value?.id || !currentUploadEvaluationId.value) {
    console.log('No file selected, no curricular unit ID, or no evaluation ID')
    return
  }
  
  const evaluationId = currentUploadEvaluationId.value
  
  try {
    console.log('Uploading test correction for evaluation:', evaluationId, 'file:', file.name)
    
    // If there's already a test correction file for this evaluation, delete it first
    const existingCorrection = getTestCorrection(evaluationId)
    if (existingCorrection) {
      await RemoteService.deleteResource(existingCorrection.id)
    }
    
    const response = await RemoteService.uploadFile(curricularUnit.value.id, file, 'MATERIAL')
    console.log('Test correction upload response:', response)
    
    // Reload resources first to get the new file
    await loadResources()
    
    // Find the newly uploaded file and track it AFTER resources are loaded
    // Note: Backend adds UUID prefix to fileName, so we match by checking if fileName ends with original name
    const uploadedFile = allResources.value.find(resource => 
      resource.fileName.endsWith(file.name) || resource.name === file.name
    )
    console.log('Looking for uploaded file:', file.name, 'found:', uploadedFile)
    
    if (uploadedFile) {
      if (!testFiles.value[evaluationId]) {
        testFiles.value[evaluationId] = {}
      }
      testFiles.value[evaluationId].correction = uploadedFile
      console.log('Tracked correction file for evaluation:', evaluationId, uploadedFile)
      saveTestFilesToStorage()
      console.log('Saved to storage, current testFiles:', testFiles.value)
    } else {
      console.error('Could not find uploaded file in resources!')
    }
    
    // Clear the input and reset evaluation ID
    if (testCorrectionInput.value) {
      testCorrectionInput.value.value = ''
    }
    currentUploadEvaluationId.value = null
    console.log('Test correction uploaded successfully for evaluation:', evaluationId)
  } catch (error) {
    console.error('Error uploading test correction:', error)
    currentUploadEvaluationId.value = null
  }
}

const downloadResource = async (resource: ResourceDto) => {
  try {
    const response = await RemoteService.downloadFile(resource.id)
    
    // Check if response has data
    if (!response.data || response.data.byteLength === 0) {
      console.error('Empty file response')
      return
    }
    
    // Create blob from arraybuffer with proper content type
    const blob = new Blob([response.data], { type: 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = resource.fileName || resource.name // Use fileName from backend
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    console.log('File downloaded successfully')
  } catch (error) {
    console.error('Error downloading file:', error)
  }
}

const confirmDeleteResource = (resource: ResourceDto) => {
  resourceToDelete.value = resource
  showDeleteResourceDialog.value = true
}

const deleteResource = async () => {
  if (!resourceToDelete.value?.id) return
  
  try {
    await RemoteService.deleteResource(resourceToDelete.value.id)
    
    // Refresh resources list
    await loadResources()
    
    // Reset and close dialog
    resourceToDelete.value = null
    showDeleteResourceDialog.value = false
  } catch (error) {
    console.error('Error deleting resource:', error)
  }
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

// Event handlers for ManagePeopleDialog
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

// Watch for curricular unit changes to load evaluations and resources
watch(() => curricularUnit.value, async (newCU) => {
  if (newCU) {
    await loadEvaluations()
    await loadResources()
  }
}, { immediate: true })

// Also watch props.curricularUnit for initial load
watch(() => props.curricularUnit, async (newCU) => {
  if (newCU && !selectedCurricularUnit.value) {
    await loadEvaluations()
    await loadResources()
  }
}, { immediate: true })
</script>

<style scoped>
.test-file-actions {
  white-space: nowrap;
}
</style>
