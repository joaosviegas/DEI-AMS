<template>
  <v-dialog v-model="localDialog" max-width="900">
    <v-card v-if="project">
      <v-card-title class="d-flex justify-space-between align-center">
        <div>
          <span class="text-h5">Histórico de Submissões</span>
          <div class="text-subtitle-2 ">{{ project.title }}</div>
        </div>
        <v-btn 
          icon="mdi-close" 
          variant="text" 
          size="small"
          @click="localDialog = false"
        ></v-btn>
      </v-card-title>

      <v-card-text>
        <div v-if="myGroup" class="mb-4">
          <v-alert
            type="info"
            variant="tonal"
            density="compact"
          >
            <div class="d-flex align-center gap-2">
              <div>
                <strong>{{ myGroup.members.length > 1 ? 'Projeto de Grupo:' : 'Projeto Individual:' }}</strong> 
                {{ myGroup.name || (myGroup.members[0]?.name || `Grupo ${myGroup.id}`) }}
                <div class="text-caption">
                  {{ myGroup.members.length > 1 
                      ? 'As submissões são partilhadas por todos os membros do grupo'
                      : 'Suas submissões pessoais para este projeto'
                  }}
                </div>
                <div v-if="getDisplayGrade()?.hasGrade()" class="text-caption mt-1">
                  <v-icon size="small" class="mr-1">mdi-school</v-icon>
                  <strong>Nota atribuída: {{ getDisplayGrade()?.grade }}/20</strong>
                  <span v-if="getDisplayGrade()?.revisionRequested" class="text-warning ml-2">
                    (Revisão solicitada)
                  </span>
                </div>
              </div>
            </div>
          </v-alert>
        </div>

        <!-- Submissions Table -->
        <v-data-table
          :headers="headers"
          :items="submissions"
          :loading="loading"
          item-key="id"
          :sort-by="[{ key: 'submissionDate', order: 'desc' }]"
          no-data-text="Nenhuma submissão encontrada."
        >
          <template v-slot:[`item.submissionDate`]="{ item }">
            <div>
              <div class="font-weight-medium">{{ formatDateTime(item.submissionDate) }}</div>
              <div class="text-caption ">
                {{ getTimeAgo(item.submissionDate) }}
              </div>
            </div>
          </template>

          <template v-slot:[`item.filename`]="{ item }">
            <div class="d-flex align-center">
              <v-icon class="mr-2" size="small">
                {{ getFileIcon(item.originalFilename) }}
              </v-icon>
              <div>
                <div class="font-weight-medium">{{ item.originalFilename }}</div>
                <div class="text-caption ">{{ formatFileSize(item.fileSize) }}</div>
              </div>
            </div>
          </template>

          <template v-slot:[`item.submitter`]="{ item }">
            <div v-if="item.submittedBy">
              <div class="font-weight-medium">{{ item.submittedBy.name }}</div>
              <div class="text-caption ">{{ item.submittedBy.istId }}</div>
            </div>
            <div v-else class="">N/A</div>
          </template>

          <template v-slot:[`item.version`]="{ item }">
            <div class="d-flex align-center gap-2">
              <v-chip
                :color="item.isLatest ? 'primary' : 'grey'"
                size="small"
                :variant="item.isLatest ? 'flat' : 'outlined'"
              >
                v{{ item.version || '1' }}
              </v-chip>
              <v-chip
                v-if="item.isLatest"
                color="success"
                size="x-small"
                prepend-icon="mdi-check"
              >
                Atual
              </v-chip>
            </div>
          </template>

          <template v-slot:[`item.grade`]="{ item }">
            <div v-if="getDisplayGrade() && getDisplayGrade()?.hasGrade()">
              <v-chip
                :color="getGradeColor(getDisplayGrade()?.grade || 0)"
                size="small"
              >
                {{ getDisplayGrade()?.grade?.toFixed(1) }}/20
              </v-chip>
              <div v-if="getDisplayGrade()?.comments" class="text-caption text-grey mt-1">
                {{ getDisplayGrade()?.comments }}
              </div>
              <div v-if="getDisplayGrade()?.revisionRequested" class="text-caption text-warning mt-1">
                <v-icon size="small" class="mr-1">mdi-alert</v-icon>
                Revisão solicitada
              </div>
            </div>
            <div v-else>
              <v-chip color="grey" size="small" variant="outlined">
                Não avaliado
              </v-chip>
            </div>
          </template>

          <template v-slot:[`item.actions`]="{ item }">
            <div class="d-flex gap-1">
              <v-btn
                icon="mdi-download"
                size="small"
                variant="text"
                @click="downloadSubmission(item)"
                title="Descarregar ficheiro"
              ></v-btn>
              
              <v-btn
                icon="mdi-eye"
                size="small"
                variant="text"
                @click="viewSubmissionDetails(item)"
                title="Ver detalhes"
              ></v-btn>
            </div>
          </template>
        </v-data-table>

        <!-- Empty State -->
        <div v-if="!loading && submissions.length === 0" class="text-center py-8">
          <v-icon size="64" color="grey-lighten-1">mdi-file-upload-outline</v-icon>
          <h3 class="text-grey-lighten-1 mt-4 mb-2">Nenhuma Submissão</h3>
          <p class="text-grey-lighten-1">
            Ainda não foram feitas submissões para este projeto.
          </p>
          <v-btn
            v-if="canSubmit"
            color="primary"
            class="mt-4"
            @click="$emit('startSubmission')"
            prepend-icon="mdi-upload"
          >
            Fazer Primeira Submissão
          </v-btn>
        </div>
      </v-card-text>

      <v-card-actions v-if="submissions.length > 0">
        <v-spacer></v-spacer>
        <v-btn
          v-if="canSubmit"
          color="primary"
          @click="$emit('startSubmission')"
          prepend-icon="mdi-upload"
        >
          Nova Submissão
        </v-btn>
        <v-btn
          variant="text"
          @click="localDialog = false"
        >
          Fechar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <!-- Submission Details Dialog -->
  <v-dialog v-model="showDetailsDialog" max-width="500">
    <v-card v-if="selectedSubmission">
      <v-card-title>Detalhes da Submissão</v-card-title>
      <v-card-text>
        <v-list density="compact">
          <v-list-item>
            <v-list-item-title>Ficheiro</v-list-item-title>
            <v-list-item-subtitle>{{ selectedSubmission.originalFilename }}</v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>Tamanho</v-list-item-title>
            <v-list-item-subtitle>{{ formatFileSize(selectedSubmission.fileSize) }}</v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>Data de Submissão</v-list-item-title>
            <v-list-item-subtitle>{{ formatDateTime(selectedSubmission.submissionDate) }}</v-list-item-subtitle>
          </v-list-item>
          <v-list-item v-if="selectedSubmission.submittedBy">
            <v-list-item-title>Submetido por</v-list-item-title>
            <v-list-item-subtitle>
              {{ selectedSubmission.submittedBy.name }} ({{ selectedSubmission.submittedBy.istId }})
            </v-list-item-subtitle>
          </v-list-item>
          <v-list-item v-if="getDisplayGrade()?.hasGrade()">
            <v-list-item-title>Nota de Avaliação</v-list-item-title>
            <v-list-item-subtitle>{{ getDisplayGrade()?.grade }}/20</v-list-item-subtitle>
          </v-list-item>
          <v-list-item v-if="getDisplayGrade()?.comments">
            <v-list-item-title>Comentários do Professor</v-list-item-title>
            <v-list-item-subtitle>{{ getDisplayGrade()?.comments }}</v-list-item-subtitle>
          </v-list-item>
          <v-list-item v-if="getDisplayGrade()?.revisionRequested">
            <v-list-item-title>Revisão Solicitada</v-list-item-title>
            <v-list-item-subtitle>{{ getDisplayGrade()?.revisionReason }}</v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn variant="text" @click="showDetailsDialog = false">Fechar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import ProjectDto from '../../models/ProjectDto'
import ProjectGroupDto from '../../models/ProjectGroupDto'
import ProjectSubmissionDto from '../../models/ProjectSubmissionDto'
import EvaluationGradeDto from '../../models/EvaluationGradeDto'
import RemoteService from '../../services/RemoteService'

const emit = defineEmits(['update:modelValue', 'startSubmission'])

const props = defineProps({
  project: {
    type: Object as () => ProjectDto | null,
    default: null
  },
  modelValue: {
    type: Boolean,
    default: false
  },
  myGroup: {
    type: Object as () => ProjectGroupDto | null,
    default: null
  }
})

// Reactive data
const loading = ref(false)
const submissions = ref<(ProjectSubmissionDto & { version?: number })[]>([])
const evaluationGrades = ref<EvaluationGradeDto[]>([])
const showDetailsDialog = ref(false)
const selectedSubmission = ref<(ProjectSubmissionDto & { version?: number }) | null>(null)

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const canSubmit = computed(() => {
  if (!props.project) return false
  if (props.project.isSubmissionClosed) return false
  if (!props.myGroup) return false
  return true
})

// Table headers
const headers = [
  { title: 'Data de Submissão', key: 'submissionDate', value: 'submissionDate' },
  { title: 'Ficheiro', key: 'filename', value: 'filename' },
  { title: 'Submetido por', key: 'submitter', value: 'submitter' },
  { title: 'Versão', key: 'version', value: 'version' },
  { title: 'Avaliação', key: 'grade', value: 'grade' },
  { title: 'Ações', key: 'actions', value: 'actions', sortable: false }
]

// Helper functions
const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString('pt-PT', {
    day: '2-digit',
    month: '2-digit', 
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatFileSize = (bytes: number) => {
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const getTimeAgo = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days > 0) {
    return `há ${days} dia${days > 1 ? 's' : ''}`
  } else if (hours > 0) {
    return `há ${hours} hora${hours > 1 ? 's' : ''}`
  } else if (minutes > 0) {
    return `há ${minutes} minuto${minutes > 1 ? 's' : ''}`
  } else {
    return 'agora'
  }
}

const getFileIcon = (filename: string) => {
  const extension = filename.split('.').pop()?.toLowerCase()
  
  switch (extension) {
    case 'pdf':
      return 'mdi-file-pdf-box'
    case 'zip':
    case 'rar':
    case '7z':
      return 'mdi-folder-zip'
    case 'py':
      return 'mdi-language-python'
    case 'java':
      return 'mdi-language-java'
    case 'js':
    case 'ts':
      return 'mdi-language-javascript'
    case 'c':
    case 'cpp':
    case 'h':
      return 'mdi-language-c'
    case 'txt':
      return 'mdi-file-document-outline'
    case 'md':
      return 'mdi-language-markdown'
    default:
      return 'mdi-file-outline'
  }
}

const getGradeColor = (grade: number) => {
  if (grade >= 16) return 'green'
  if (grade >= 10) return 'orange'
  return 'red'
}

// Get evaluation grade for a group member
const getEvaluationGradeForMember = (memberId: number): EvaluationGradeDto | null => {
  return evaluationGrades.value.find(grade => grade.student.id === memberId) || null
}

// Get the grade to display
const getDisplayGrade = () => {
  if (!props.myGroup?.members || props.myGroup.members.length === 0) return null

  return getEvaluationGradeForMember(props.myGroup.members[0].id)
}

// Actions
const downloadSubmission = async (submission: ProjectSubmissionDto) => {
  try {
    if (!submission.id) {
      console.error('Submission ID is missing')
      return
    }

    const response = await RemoteService.downloadSubmission(submission.id)
    
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
    link.download = submission.originalFilename || `submission_${submission.id}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
  } catch (error) {
    console.error('Error downloading submission:', error)
    // You could show a toast notification here instead
    alert('Erro ao fazer download da submissão. Tente novamente.')
  }
}

const viewSubmissionDetails = (submission: ProjectSubmissionDto) => {
  selectedSubmission.value = submission
  showDetailsDialog.value = true
}

// Data loading
const loadSubmissions = async () => {
  if (!props.project?.id) return
  
  loading.value = true
  try {
    // Load submissions
    if (props.myGroup?.id) {
      // Load submissions for the group (works for both individual and group projects)
      submissions.value = await RemoteService.getGroupSubmissions(props.project.id, props.myGroup.id)
    } else {
      // Fallback: Load individual submissions - get all submissions and filter for current user
      // For demo purposes, assume user ID 1
      const allSubmissions = await RemoteService.getProjectSubmissions(props.project.id)
      submissions.value = allSubmissions.filter(s => s.submittedBy.id === 1) // Demo: filter for user 1
    }
    
    // Add version numbers to submissions
    submissions.value = submissions.value
      .sort((a, b) => new Date(a.submissionDate).getTime() - new Date(b.submissionDate).getTime())
      .map((submission, index) => ({
        ...submission,
        version: index + 1
      }))

    // Load evaluation grades for this project
    try {
      const grades = await RemoteService.getEvaluationGrades(props.project.id)
      evaluationGrades.value = grades.map(grade => EvaluationGradeDto.fromBackend(grade))
    } catch (error) {
      evaluationGrades.value = []
    }
  } catch (error) {
    console.error('Error loading submissions:', error)
    submissions.value = []
    evaluationGrades.value = []
  } finally {
    loading.value = false
  }
}

// Watch for dialog opening to load data
watch(localDialog, (isOpen) => {
  if (isOpen && props.project) {
    loadSubmissions()
  }
})
</script>
