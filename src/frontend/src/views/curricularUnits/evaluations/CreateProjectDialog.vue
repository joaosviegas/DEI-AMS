<template>
  <v-dialog v-model="localDialog" max-width="600">
    <v-card prepend-icon="mdi-folder-plus" title="Criar Projeto">
      <v-card-text>
        <v-form ref="projectForm">
          <v-row>
            <v-col cols="12">
              <v-text-field
                v-model="newProject.title"
                label="Título do Projeto"
                :rules="[v => !!v || 'Título é obrigatório']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model="newProject.submissionDeadline"
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
                v-model.number="newProject.weight"
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
                v-model="newProject.description"
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
                v-model.number="newProject.maxGroupSize"
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
                v-model.number="newProject.maxFileSize"
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
          text="Criar Projeto" 
          @click="create" 
          :loading="loading"
          :disabled="!isFormValid"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import RemoteService from '../../../services/RemoteService'

const emit = defineEmits(['update:modelValue', 'project-created'])

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  curricularUnitId: {
    type: Number,
    required: true
  }
})

const projectForm = ref()
const loading = ref(false)
const projectType = ref('individual')
const newProject = ref({
  title: '',
  submissionDeadline: '',
  weight: 0,
  description: '',
  maxGroupSize: 3,
  maxFileSize: 10
})

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isFormValid = computed(() => {
  const base = newProject.value.title.trim() !== '' &&
               newProject.value.submissionDeadline !== '' &&
               newProject.value.weight > 0 &&
               newProject.value.weight <= 100 &&
               newProject.value.description.trim() !== ''
               
  if (projectType.value === 'group') {
    return base && newProject.value.maxGroupSize >= 2 && newProject.value.maxGroupSize <= 10
  }
  
  return base
})

const resetForm = () => {
  newProject.value = {
    title: '',
    submissionDeadline: '',
    weight: 0,
    description: '',
    maxGroupSize: 3,
    maxFileSize: 10
  }
  projectType.value = 'individual'
  if (projectForm.value) {
    projectForm.value.resetValidation()
  }
}

const cancel = () => {
  localDialog.value = false
  resetForm()
}

const create = async () => {
  if (!projectForm.value?.validate()) {
    return
  }

  loading.value = true
  try {
    const projectData: any = {
      title: newProject.value.title,
      submissionDeadline: newProject.value.submissionDeadline,
      weight: newProject.value.weight / 100, // Convert percentage to decimal
      description: newProject.value.description,
      allowedExtensions: 'py,c,zip,java', // Default allowed extensions
      maxFileSize: (newProject.value.maxFileSize || 10) * 1024 * 1024 // Convert MB to bytes
    }
    
    // Add maxGroupSize only for group projects
    if (projectType.value === 'group') {
      projectData.maxGroupSize = newProject.value.maxGroupSize
    }
    
    await RemoteService.createProject(props.curricularUnitId, projectData)
    emit('project-created')
    localDialog.value = false
    resetForm()
  } catch (error: any) {
    // Error is already handled by the interceptor and shown to user
    // Just catch it to prevent uncaught promise warning
    console.log('Project creation failed:', error.message)
  } finally {
    loading.value = false
  }
}

// Reset form when dialog closes
watch(localDialog, (isOpen) => {
  if (!isOpen) {
    resetForm()
  }
})
</script>
