<template>
  <v-dialog v-model="localDialog" max-width="600">
    <v-card v-if="group">
      <v-card-title class="d-flex justify-space-between align-center">
        <div>              <span class="text-h5">
                {{ group.name || (group.members[0]?.name ? `${group.members[0]?.name}` : 'Grupo sem nome') }}
              </span>
          <div class="text-subtitle-2 ">
            {{ project?.title }}
          </div>
        </div>
        <v-btn 
          icon="mdi-close" 
          variant="text" 
          size="small"
          @click="localDialog = false"
        ></v-btn>
      </v-card-title>

      <v-card-text>
        <!-- Group/Student Info -->
        <div class="mb-4">
          <v-row>
            <v-col cols="6">
              <div class="text-caption  mb-1">
                NOME DO GRUPO
              </div>
              <div class="text-h6">
                {{ group.name || (group.members[0]?.name ? `${group.members[0]?.name}` : 'Sem nome') }}
              </div>
            </v-col>
            <v-col cols="6">
              <div class="text-caption  mb-1">
                TOTAL DE MEMBROS
              </div>
              <div class="text-h6">
                {{ group.members?.length || 0 }}
                <span class="text-body-2  ml-2" v-if="group.members?.length === 1 && group.members[0]?.istId">
                  (IST ID: {{ group.members[0]?.istId }})
                </span>
              </div>
            </v-col>
          </v-row>
        </div>

        <v-divider class="mb-4"></v-divider>

        <!-- Group Members -->
        <div class="mb-4">
          <div class="d-flex align-center mb-3">
            <span class="text-h6">Membros do Grupo</span>
          </div>

          <v-list density="compact" class="bg-transparent">
            <v-list-item 
              v-for="member in group.members" 
              :key="member.id"
              class="member-item rounded"
            >
              <template v-slot:prepend>
                <v-avatar size="32" color="green">
                  <span class="text-caption font-weight-bold text-white">
                    {{ getInitials(member.name || 'Unknown') }}
                  </span>
                </v-avatar>
              </template>
              
              <v-list-item-title class="font-weight-medium">
                {{ member.name || 'Nome não disponível' }}
              </v-list-item-title>
              <v-list-item-subtitle class="">
                {{ member.istId || 'IST ID não disponível' }}
              </v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item v-if="!group.members || group.members.length === 0">
              <v-list-item-title class="">
                Nenhum membro encontrado
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </div>

        <!-- Submission Status -->
        <div v-if="project" class="mb-4">
          <v-divider class="mb-4"></v-divider>
          
          <div class="d-flex align-center mb-3">
            <v-icon class="mr-2" color="primary">mdi-file-upload</v-icon>
            <span class="text-h6">Status da Submissão</span>
          </div>

          <v-card 
            variant="tonal" 
            :color="getSubmissionStatusColor()"
            class="pa-3"
          >
            <div class="d-flex align-center justify-space-between">
              <div>
                <div class="font-weight-medium">
                  {{ getSubmissionStatusText() }}
                </div>
                <div class="text-caption ">
                  {{ getSubmissionStatusDetails() }}
                </div>
              </div>
              <v-icon size="28" :color="getSubmissionStatusColor()">
                {{ getSubmissionStatusIcon() }}
              </v-icon>
            </div>
          </v-card>
        </div>
      </v-card-text>

      <v-card-actions class="px-6 pb-4">
        <v-btn
          v-if="canViewHistory"
          color="primary"
          variant="text"
          prepend-icon="mdi-history"
          @click="$emit('viewHistory')"
        >
          Ver Histórico
        </v-btn>
        <v-spacer></v-spacer>
        <v-btn
          v-if="canSubmit"
          color="success"
          prepend-icon="mdi-upload"
          @click="$emit('startSubmission')"
        >
          Submeter Projeto
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
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ProjectDto from '../../models/ProjectDto'
import ProjectGroupDto from '../../models/ProjectGroupDto'

const emit = defineEmits(['update:modelValue', 'viewHistory', 'startSubmission'])

const props = defineProps({
  project: {
    type: Object as () => ProjectDto | null,
    default: null
  },
  group: {
    type: Object as () => ProjectGroupDto | null,
    default: null
  },
  modelValue: {
    type: Boolean,
    default: false
  },
  submissionStatus: {
    type: Object as () => { hasSubmission: boolean; isOnTime: boolean; submissionCount: number } | null,
    default: null
  }
})

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const canSubmit = computed(() => {
  return props.project && !props.project.isSubmissionClosed && props.group
})

const canViewHistory = computed(() => {
  return props.project && props.group
})

// Helper functions
const getInitials = (name: string) => {
  return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2)
}

const getSubmissionStatusColor = () => {
  if (!props.submissionStatus) return 'surface-variant'
  
  if (!props.submissionStatus.hasSubmission) {
    return 'warning'
  } else if (props.submissionStatus.isOnTime) {
    return 'success'
  } else {
    return 'error'
  }
}

const getSubmissionStatusText = () => {
  if (!props.submissionStatus) return 'Estado desconhecido'
  
  if (!props.submissionStatus.hasSubmission) {
    return 'Nenhuma submissão'
  } else {
    const count = props.submissionStatus.submissionCount
    return `${count} ${count > 1 ? 'submissões' : 'submissão'}`
  }
}

const getSubmissionStatusDetails = () => {
  if (!props.submissionStatus || !props.project) return ''
  
  if (!props.submissionStatus.hasSubmission) {
    if (props.project.isSubmissionClosed) {
      return 'Prazo de submissão expirado'
    } else {
      const deadline = new Date(props.project.submissionDeadline)
      return `Prazo: ${deadline.toLocaleDateString('pt-PT')}`
    }
  } else {
    return props.submissionStatus.isOnTime ? 
      'Submetido dentro do prazo' : 
      'Submetido fora do prazo'
  }
}

const getSubmissionStatusIcon = () => {
  if (!props.submissionStatus) return 'mdi-help-circle'
  
  if (!props.submissionStatus.hasSubmission) {
    return 'mdi-alert-circle'
  } else if (props.submissionStatus.isOnTime) {
    return 'mdi-check-circle'
  } else {
    return 'mdi-clock-alert'
  }
}
</script>

<style scoped>
.member-item {
  border-radius: 8px;
  transition: background-color 0.2s;
  margin-bottom: 4px;
}

.bg-transparent {
  background-color: transparent !important;
}
</style>