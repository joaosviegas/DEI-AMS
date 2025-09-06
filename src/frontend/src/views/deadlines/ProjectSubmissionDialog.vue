<template>
  <v-dialog v-model="localDialog" max-width="600" persistent>
    <v-card>
      <v-card-title class="d-flex justify-space-between align-center">
        <div>
          <span class="text-h5">Submeter Projeto</span>
          <div class="text-subtitle-2 ">{{ project?.title }}</div>
        </div>
        <v-btn 
          icon="mdi-close" 
          variant="text" 
          size="small"
          @click="cancelSubmission"
          :disabled="uploading"
        ></v-btn>
      </v-card-title>

      <v-card-text>
        <v-form ref="form" v-model="isFormValid" @submit.prevent="submitProject">
          <!-- Project Info -->
          <div class="mb-4">
            <div class="d-flex justify-space-between align-center mb-2">
              <div>
                <strong>Prazo:</strong> {{ formatDateTime(project?.submissionDeadline) }}
              </div>
              <v-chip 
                :color="getDeadlineColor()" 
                size="small"
              >
                {{ getTimeRemaining() }}
              </v-chip>
            </div>
            
            <div v-if="myGroup" class="mb-2">
              <strong>Grupo:</strong> {{ myGroup.name || (myGroup.members[0]?.name || 'Grupo sem nome') }}
              <div class="text-caption ">
                {{ myGroup.members.length > 1 
                    ? 'Esta submissão será feita em nome de todo o grupo'
                    : 'Esta submissão será feita em seu nome'
                }}
              </div>
            </div>
          </div>

          <v-divider class="mb-4"></v-divider>

          <!-- File Upload -->
          <div class="mb-4">
            <h3 class="mb-3">Selecionar Ficheiro</h3>
            
            <v-file-input
              v-model="selectedFile"
              :rules="fileRules"
              :accept="allowedExtensionsString"
              label="Escolher ficheiro para submissão"
              prepend-icon="mdi-file-upload"
              show-size
              clearable
              variant="outlined"
              :disabled="uploading"
              @change="onFileChange"
            ></v-file-input>

            <!-- File Requirements -->
            <div class="mt-2">
              <div class="text-caption  mb-1">
                <strong>Requisitos:</strong>
              </div>
              <div class="text-caption ">
                • Extensões permitidas: 
                <span v-if="project?.allowedExtensions">
                  {{ project.allowedExtensions }}
                </span>
                <span v-else>Todas</span>
              </div>
              <div class="text-caption ">
                • Tamanho máximo: {{ formatFileSize(project?.maxFileSize) }}
              </div>
            </div>
          </div>

          <!-- Upload Progress -->
          <div v-if="uploading" class="mb-4">
            <div class="d-flex justify-space-between align-center mb-2">
              <span>A submeter ficheiro...</span>
              <span>{{ Math.round(uploadProgress) }}%</span>
            </div>
            <v-progress-linear
              :model-value="uploadProgress"
              color="primary"
              height="4"
            ></v-progress-linear>
          </div>

          <!-- Previous Submissions Warning -->
          <div v-if="hasExistingSubmission" class="mb-4">
            <v-alert
              type="warning"
              variant="tonal"
              density="compact"
            >
              <div class="text-subtitle-2">Submissão Anterior Encontrada</div>
              <div class="text-caption">
                Esta nova submissão irá substituir a submissão anterior como a versão mais recente.
                A submissão anterior será mantida no histórico.
              </div>
            </v-alert>
          </div>

          <!-- Submit Button -->
          <div class="d-flex justify-end gap-2">
            <v-btn
              variant="text"
              @click="cancelSubmission"
              :disabled="uploading"
            >
              Cancelar
            </v-btn>
            <v-btn
              type="submit"
              color="primary"
              prepend-icon="mdi-upload"
              :disabled="!hasValidFile || uploading"
              :loading="uploading"
            >
              Submeter
            </v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import ProjectDto from '../../models/ProjectDto'
import ProjectGroupDto from '../../models/ProjectGroupDto'
import RemoteService from '../../services/RemoteService'

const emit = defineEmits(['update:modelValue', 'submitted'])

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
  },
  hasExistingSubmission: {
    type: Boolean,
    default: false
  }
})

// Form and file data
const form = ref()
const isFormValid = ref(false)
const selectedFile = ref<File[] | File | null>(null)
const uploading = ref(false)
const uploadProgress = ref(0)

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const hasValidFile = computed(() => {
  if (!selectedFile.value) {
    return false
  }
  
  // Handle both array and single file cases
  let file: File | null = null
  
  if (Array.isArray(selectedFile.value)) {
    if (selectedFile.value.length === 0) {
      return false
    }
    file = selectedFile.value[0]
  } else if (selectedFile.value instanceof File) {
    file = selectedFile.value
  } else {
    return false
  }
  
  if (!file || !(file instanceof File)) {
    return false
  }
  
  // Check file size
  if (props.project?.maxFileSize && file.size > props.project.maxFileSize) {
    return false
  }
  
  // Check file extension
  if (props.project?.allowedExtensions) {
    const extension = file.name.split('.').pop()?.toLowerCase()
    const allowedExts = props.project.allowedExtensions.toLowerCase().split(',').map(s => s.trim())
    
    if (!extension || !allowedExts.includes(extension)) {
      return false
    }
  }
  
  return true
})

const allowedExtensionsString = computed(() => {
  if (!props.project?.allowedExtensions) return ''
  
  return props.project.allowedExtensions
    .split(',')
    .map(ext => `.${ext.trim()}`)
    .join(',')
})

// File validation rules
const fileRules = computed(() => [
  (value: File[] | File | null) => {
    // Handle different input types from v-file-input
    let files: File[] = []
    
    if (!value) {
      return 'Ficheiro é obrigatório'
    }
    
    if (Array.isArray(value)) {
      files = value
    } else if (value instanceof File) {
      files = [value]
    } else {
      return 'Ficheiro é obrigatório'
    }
    
    if (files.length === 0) {
      return 'Ficheiro é obrigatório'
    }
    
    const file = files[0]
    if (!file || !(file instanceof File)) {
      return 'Ficheiro é obrigatório'
    }
    
    // Check file size
    if (props.project?.maxFileSize && file.size > props.project.maxFileSize) {
      return `Ficheiro demasiado grande. Máximo: ${formatFileSize(props.project.maxFileSize)}`
    }
    
    // Check file extension
    if (props.project?.allowedExtensions) {
      const extension = file.name.split('.').pop()?.toLowerCase()
      const allowedExts = props.project.allowedExtensions.toLowerCase().split(',').map(s => s.trim())
      
      if (!extension || !allowedExts.includes(extension)) {
        return `Extensão não permitida. Permitidas: ${props.project.allowedExtensions}`
      }
    }
    
    return true
  }
])

// Helper functions
const formatDateTime = (dateString: string | undefined) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleString('pt-PT', {
    day: '2-digit',
    month: '2-digit', 
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatFileSize = (bytes: number | null | undefined) => {
  if (!bytes) return 'Sem limite'
  
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const getDeadlineColor = () => {
  if (!props.project?.submissionDeadline) return 'grey'
  
  const deadline = new Date(props.project.submissionDeadline)
  const now = new Date()
  const diff = deadline.getTime() - now.getTime()

  if (diff < 0) {
    return 'red'
  } else if (diff < 24 * 60 * 60 * 1000) { // Less than 1 day
    return 'orange'
  } else if (diff < 7 * 24 * 60 * 60 * 1000) { // Less than 1 week
    return 'amber'
  } else {
    return 'green'
  }
}

const getTimeRemaining = () => {
  if (!props.project?.submissionDeadline) return 'N/A'
  
  const deadline = new Date(props.project.submissionDeadline)
  const now = new Date()
  const diff = deadline.getTime() - now.getTime()

  if (diff < 0) {
    return 'Prazo expirado'
  }

  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))

  if (days > 0) {
    return `${days} dia${days > 1 ? 's' : ''} restantes`
  } else if (hours > 0) {
    return `${hours} hora${hours > 1 ? 's' : ''} restantes`
  } else if (minutes > 0) {
    return `${minutes} minuto${minutes > 1 ? 's' : ''} restantes`
  } else {
    return 'Menos de 1 minuto'
  }
}

// File handling
const onFileChange = (files: File[]) => {
  // Trigger form validation after file change
  if (form.value) {
    form.value.validate()
  }
}

const submitProject = async () => {
  if (!props.project?.id || !selectedFile.value) return

  // Get the file from either array or direct file
  let file: File | null = null
  if (Array.isArray(selectedFile.value)) {
    file = selectedFile.value[0] || null
  } else if (selectedFile.value instanceof File) {
    file = selectedFile.value
  }
  
  if (!file) return
  
  uploading.value = true
  uploadProgress.value = 0

  try {
    // Create FormData for file upload
    const formData = new FormData()
    formData.append('file', file)
    formData.append('projectId', props.project.id.toString())

    // Simulate upload progress (real implementation would track actual upload)
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += Math.random() * 10
      }
    }, 200)

    // Submit the file
    // Determine which student ID to use for submission
    let studentId: number;
    
    studentId = props.myGroup.members[0].id;
    
    if (props.myGroup?.id) {
      // Submit for group (works for both individual and group projects)
      await RemoteService.submitProjectForGroup(
        props.project.id,
        props.myGroup.id,
        studentId,
        file
      )
    } else {
      throw new Error('No group information available for submission')
    }

    clearInterval(progressInterval)
    uploadProgress.value = 100

    // Close dialog and notify parent
    setTimeout(() => {
      emit('submitted')
      resetForm()
    }, 500)

  } catch (error) {
    console.error('Error submitting project:', error)
    console.error('Error details:', {
      message: error?.message,
      response: error?.response,
      status: error?.response?.status,
      data: error?.response?.data
    })
    
    uploading.value = false
    uploadProgress.value = 0
    
    // Show more detailed error to user
    const errorMessage = error?.response?.data?.message || error?.message || 'Erro desconhecido ao submeter o projeto'
    alert(`Erro ao submeter o projeto: ${errorMessage}`)
  }
}

const cancelSubmission = () => {
  if (!uploading.value) {
    resetForm()
    emit('update:modelValue', false)
  }
}

const resetForm = () => {
  selectedFile.value = null
  uploading.value = false
  uploadProgress.value = 0
  isFormValid.value = false
  form.value?.reset()
}

// Reset form when dialog is closed
watch(localDialog, (isOpen) => {
  if (!isOpen) {
    resetForm()
  }
})
</script>
