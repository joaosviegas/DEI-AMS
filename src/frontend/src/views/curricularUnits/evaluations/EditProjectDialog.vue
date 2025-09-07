<template>
  <v-dialog v-model="localDialog" max-width="600">
    <v-card prepend-icon="mdi-pencil" title="Editar Projeto">
      <v-card-text>
        <v-form ref="projectForm">
          <v-row>
            <v-col cols="12">
              <v-text-field
                v-model="editProject.title"
                label="Título do Projeto"
                :rules="[v => !!v || 'Título é obrigatório']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model="editProject.submissionDeadline"
                label="Prazo de Entrega"
                type="datetime-local"
                :rules="[v => !!v || 'Prazo é obrigatório']"
                required
                variant="outlined"
                hint="Prazo limite para submissão"
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model="editProject.revisionDeadline"
                label="Prazo de Revisão"
                type="datetime-local"
                :rules="[v => !!v || 'Prazo de revisão é obrigatório']"
                required
                variant="outlined"
                hint="Prazo limite para revisão das submissões"
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model.number="editProject.weight"
                label="Peso (%)"
                type="number"
                min="0"
                max="100"
                step="1"
                :rules="[
                  v => v !== null && v !== undefined && v !== '' && v > 0 || 'Peso é obrigatório e deve ser maior que 0',
                  v => v >= 1 && v <= 100 || 'Peso deve estar entre 1 e 100%'
                ]"
                required
                suffix="%"
                variant="outlined"
                placeholder="Ex: 25 para 25%"
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-textarea
                v-model="editProject.description"
                label="Descrição do Projeto"
                :rules="[v => !!v || 'Descrição é obrigatória']"
                required
                variant="outlined"
                rows="3"
                hint="Instruções e detalhes do projeto"
              ></v-textarea>
            </v-col>
            <v-col cols="12">
              <v-select
                v-model="projectType"
                label="Tipo de Projeto"
                :items="[
                  { title: 'Individual', value: 'individual' },
                  { title: 'Em Grupo', value: 'group' }
                ]"
                required
                variant="outlined"
              ></v-select>
            </v-col>
            <v-col cols="12" v-if="projectType === 'group'">
              <v-text-field
                v-model.number="editProject.maxGroupSize"
                label="Tamanho Máximo do Grupo"
                type="number"
                min="2"
                max="10"
                :rules="[
                  v => v >= 2 || 'Grupos devem ter pelo menos 2 membros',
                  v => v <= 10 || 'Grupos não podem ter mais de 10 membros'
                ]"
                variant="outlined"
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model.number="editProject.maxFileSize"
                label="Tamanho Máximo (MB)"
                type="number"
                min="1"
                max="100"
                variant="outlined"
                hint="Tamanho máximo do arquivo em MB"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" variant="plain" @click="cancel"></v-btn>
        <v-btn 
          color="primary" 
          text="Guardar Alterações" 
          @click="update" 
          :loading="loading"
          :disabled="!isFormValid || !hasChanges"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import RemoteService from '../../../services/RemoteService'
import ProjectDto from '../../../models/ProjectDto'

const emit = defineEmits(['update:modelValue', 'project-updated'])

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  project: {
    type: Object as () => ProjectDto | undefined,
    default: undefined
  }
})

const projectForm = ref()
const loading = ref(false)
const projectType = ref('individual')
const editProject = ref({
  title: '',
  submissionDeadline: '',
  revisionDeadline: '',
  weight: 0,
  description: '',
  maxGroupSize: 1,
  maxFileSize: 10,
  allowedExtensions: 'py,java,zip,c'
})

// Store original values to detect changes
const originalProject = ref({
  title: '',
  submissionDeadline: '',
  revisionDeadline: '',
  weight: 0,
  description: '',
  maxGroupSize: 1,
  maxFileSize: 10,
  allowedExtensions: 'py,java,zip,c'
})

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isFormValid = computed(() => {
  const base = editProject.value.title.trim() !== '' &&
               editProject.value.submissionDeadline !== '' &&
               editProject.value.revisionDeadline !== '' &&
               editProject.value.weight > 0 &&
               editProject.value.weight <= 100 &&
               editProject.value.description.trim() !== ''
               
  if (projectType.value === 'group') {
    return base && editProject.value.maxGroupSize >= 2 && editProject.value.maxGroupSize <= 10
  }
  
  return base
})

const hasChanges = computed(() => {
  return editProject.value.title !== originalProject.value.title ||
         editProject.value.submissionDeadline !== originalProject.value.submissionDeadline ||
         editProject.value.revisionDeadline !== originalProject.value.revisionDeadline ||
         editProject.value.weight !== originalProject.value.weight ||
         editProject.value.description !== originalProject.value.description ||
         editProject.value.maxGroupSize !== originalProject.value.maxGroupSize ||
         editProject.value.maxFileSize !== originalProject.value.maxFileSize ||
         editProject.value.allowedExtensions !== originalProject.value.allowedExtensions
})

const formatDateTimeForInput = (dateString: string): string => {
  if (!dateString) return ''
  
  // Create date object and adjust for local timezone
  const date = new Date(dateString)
  const tzOffset = date.getTimezoneOffset() * 60000
  const localDate = new Date(date.getTime() - tzOffset)
  
  return localDate.toISOString().slice(0, 16)
}

const loadProjectData = () => {
  if (!props.project) return
  
  const project = props.project
  
  // Convert maxFileSize from bytes to MB for display
  const maxFileSizeMB = project.maxFileSize ? Math.round(project.maxFileSize / (1024 * 1024)) : 10
  
  editProject.value = {
    title: project.title,
    submissionDeadline: formatDateTimeForInput(project.submissionDeadline),
    revisionDeadline: formatDateTimeForInput(project.revisionDeadline),
    weight: Math.round(project.weight * 100), // Convert decimal to percentage
    description: project.description || '',
    maxGroupSize: project.maxGroupSize || 1,
    maxFileSize: maxFileSizeMB,
    allowedExtensions: project.allowedExtensions || 'py,java,zip,c'
  }
  
  // Store original values
  originalProject.value = { ...editProject.value }
  
  // Set project type based on maxGroupSize
  projectType.value = (project.maxGroupSize && project.maxGroupSize > 1) ? 'group' : 'individual'
}

const resetForm = () => {
  loadProjectData()
  if (projectForm.value) {
    projectForm.value.resetValidation()
  }
}

const cancel = () => {
  localDialog.value = false
  resetForm()
}

const update = async () => {
  if (!projectForm.value?.validate() || !props.project?.id) {
    return
  }

  loading.value = true
  try {
    const projectData: any = {
      title: editProject.value.title,
      submissionDeadline: editProject.value.submissionDeadline,
      revisionDeadline: editProject.value.revisionDeadline,
      weight: editProject.value.weight / 100, // Convert percentage to decimal
      description: editProject.value.description,
      allowedExtensions: editProject.value.allowedExtensions,
      maxFileSize: (editProject.value.maxFileSize || 10) * 1024 * 1024 // Convert MB to bytes
    }
    
    // Add maxGroupSize only for group projects
    if (projectType.value === 'group') {
      projectData.maxGroupSize = editProject.value.maxGroupSize
    } else {
      projectData.maxGroupSize = 1
    }
    
    await RemoteService.updateProject(props.project.id, projectData)
    emit('project-updated')
    localDialog.value = false
  } catch (error: any) {
    console.log('Project update failed:', error.message)
  } finally {
    loading.value = false
  }
}

// Watch for project changes to load data
watch(() => props.project, (newProject) => {
  if (newProject) {
    loadProjectData()
  }
}, { immediate: true })

// Reset form when dialog closes
watch(localDialog, (isOpen) => {
  if (!isOpen) {
    resetForm()
  }
})
</script>
