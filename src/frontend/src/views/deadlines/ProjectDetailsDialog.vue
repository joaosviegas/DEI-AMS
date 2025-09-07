<template>
  <v-dialog v-model="localDialog" max-width="800">
    <v-card v-if="project">
      <v-card-title class="d-flex justify-space-between align-center">
        <div>
          <span class="text-h5">{{ project.title }}</span>
          <v-chip 
            :color="project.isGroupProject ? 'blue' : 'green'"
            size="small"
            class="ml-2"
          >
            {{ project.isGroupProject ? 'Projeto de Grupo' : 'Projeto Individual' }}
          </v-chip>
        </div>
        <v-btn 
          icon="mdi-close" 
          variant="text" 
          size="small"
          @click="localDialog = false"
        ></v-btn>
      </v-card-title>

      <v-card-text>
        <!-- Project Information -->
        <v-row class="mb-4">
          <v-col cols="12" md="6">
            <div class="mb-2">
              <strong>Unidade Curricular:</strong>
              <div>{{ project.curricularUnitName || 'N/A' }}</div>
            </div>
          </v-col>
          <v-col cols="12" md="6">
            <div class="mb-2">
              <strong>Peso:</strong>
              <div>{{ (project.weight * 100).toFixed(1) }}%</div>
            </div>
          </v-col>
          <v-col cols="12" md="6">
            <div class="mb-2">
              <strong>Prazo de Entrega:</strong>
              <div :class="getDeadlineColor(project)">
                {{ formatDateTime(project.submissionDeadline) }}
              </div>
            </div>
          </v-col>
          <v-col cols="12" md="6">
            <div class="mb-2">
              <strong>Estado:</strong>
              <v-chip 
                :color="getStatusChipColor(project)"
                size="small"
              >
                {{ getStatusText(project) }}
              </v-chip>
            </div>
          </v-col>
        </v-row>

        <!-- Group Information (for group projects) -->
        <div v-if="project.isGroupProject" class="mb-4">
          <v-divider class="mb-3"></v-divider>
          <h3 class="mb-3">Grupos do Projeto</h3>
          
          <div v-if="loading">
            <v-skeleton-loader type="list-item-two-line" :loading="true"></v-skeleton-loader>
          </div>
          
          <div v-else-if="projectGroups.length > 0">
            <v-row>
              <v-col 
                v-for="group in projectGroups" 
                :key="group.id" 
                cols="12" 
                md="6"
              >
                <v-card 
                  variant="tonal"
                  class="mb-3 group-card" 
                  @click="openGroupDetails(group)"
                  style="cursor: pointer; transition: all 0.2s ease-in-out;"
                >
                  <v-card-text class="pb-2">
                    <div class="d-flex justify-space-between align-center mb-3">
                      <h4 class="font-weight-medium">{{ 'Grupo ' + group.id || 'Grupo sem nome' }}</h4>
                      <v-chip color="primary" size="small" variant="outlined">
                        {{ group.members.length }} 
                        {{ group.members.length === 1 ? 'membro' : 'membros' }}
                      </v-chip>
                    </div>
                    
                    <div class="mb-3">
                      <div class="d-flex flex-wrap gap-1">
                        <v-avatar
                          v-for="member in group.members.slice(0, 3)"
                          :key="member.id"
                          size="28"
                          color="primary"
                          :title="member.name"
                        >
                          <span class="text-caption text-white font-weight-bold">
                            {{ getInitials(member.name || '') }}
                          </span>
                        </v-avatar>
                        <v-chip
                          v-if="group.members.length > 3"
                          size="small"
                          variant="outlined"
                          color="primary"
                        >
                          +{{ group.members.length - 3 }}
                        </v-chip>
                      </div>
                    </div>
                    
                    <v-divider class="mb-3"></v-divider>
                    <div class="d-flex justify-center gap-2">
                      <v-btn 
                        size="small" 
                        color="primary" 
                        variant="text"
                        prepend-icon="mdi-eye"
                        @click.stop="openGroupDetails(group)"
                      >
                        Detalhes
                      </v-btn>
                      <v-btn 
                        size="small" 
                        color="success" 
                        variant="text"
                        prepend-icon="mdi-upload"
                        @click.stop="openSubmissionDialog(group)"
                        :disabled="project?.isSubmissionClosed"
                      >
                        Submeter
                      </v-btn>
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </div>
          
          <div v-else>
            <v-alert
              type="info"
              variant="tonal"
            >
              <v-alert-title>Nenhum Grupo Criado</v-alert-title>
              <div>Os grupos ainda não foram criados para este projeto.</div>
            </v-alert>
          </div>
        </div>

        <!-- Project Description -->
        <v-divider class="mb-3"></v-divider>
        <h3 class="mb-3">Descrição do Projeto</h3>
        <div class="project-description mb-4">
          {{ project.description || 'Sem descrição disponível.' }}
        </div>

        <!-- Submission Requirements -->
        <v-divider class="mb-3"></v-divider>
        <h3 class="mb-3">Requisitos de Submissão</h3>
        <v-row>
          <v-col cols="12" md="6">
            <div class="mb-2">
              <strong>Extensões Permitidas:</strong>
              <div v-if="project.allowedExtensions">
                <v-chip
                  v-for="ext in project.allowedExtensions.split(',')"
                  :key="ext"
                  size="small"
                  class="mr-1 mb-1"
                  variant="outlined"
                >
                  .{{ ext.trim() }}
                </v-chip>
              </div>
              <div v-else class="text-grey">Todas as extensões</div>
            </div>
          </v-col>
          <v-col cols="12" md="6">
            <div class="mb-2">
              <strong>Tamanho Máximo:</strong>
              <div>{{ formatFileSize(project.maxFileSize) }}</div>
            </div>
          </v-col>
        </v-row>

        <!-- Submission Status -->
        <v-divider class="mb-3"></v-divider>
        <h3 class="mb-3">Estado da Submissão</h3>
        <div v-if="project.isSubmissionClosed">
          <v-alert type="error" variant="tonal">
            <v-alert-title>Prazo Expirado</v-alert-title>
            <div>O prazo de submissão para este projeto já expirou.</div>
          </v-alert>
        </div>
        <div v-else>
          <v-alert type="info" variant="tonal">
            <v-alert-title>Submissão Aberta</v-alert-title>
            <div>As submissões estão abertas até {{ formatDateTime(project.submissionDeadline) }}.</div>
          </v-alert>
        </div>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        
        <v-btn
          variant="text"
          @click="$emit('viewHistory')"
        >
          Ver Histórico
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
import { ref, computed, watch } from 'vue'
import ProjectDto from '../../models/ProjectDto'
import ProjectGroupDto from '../../models/ProjectGroupDto'
import RemoteService from '../../services/RemoteService'

const emit = defineEmits(['update:modelValue', 'submit', 'viewHistory', 'groupDetails', 'groupSubmission'])

const props = defineProps({
  project: {
    type: Object as () => ProjectDto | null,
    default: null
  },
  modelValue: {
    type: Boolean,
    default: false
  }
})

const loading = ref(false)
const projectGroups = ref<ProjectGroupDto[]>([])

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// Data loading
const loadProjectGroups = async () => {
  if (!props.project?.id || !props.project.isGroupProject) {
    projectGroups.value = []
    return
  }
  
  loading.value = true
  try {
    projectGroups.value = await RemoteService.getProjectGroups(props.project.id)
  } catch (error) {
    console.error('Error loading project groups:', error)
    projectGroups.value = []
  } finally {
    loading.value = false
  }
}

// Watch for dialog opening to load data
watch(localDialog, (isOpen) => {
  if (isOpen && props.project) {
    loadProjectGroups()
  }
})

// Watch for project changes to reload data
watch(() => props.project, () => {
  if (localDialog.value && props.project) {
    loadProjectGroups()
  }
})

// Actions
const openGroupDetails = (group: ProjectGroupDto) => {
  emit('groupDetails', group)
}

const openSubmissionDialog = (group: ProjectGroupDto) => {
  emit('groupSubmission', group)
}

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

const formatFileSize = (bytes: number | null | undefined) => {
  if (!bytes) return 'Sem limite'
  
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const getDeadlineColor = (project: ProjectDto) => {
  const deadline = new Date(project.submissionDeadline)
  const now = new Date()
  const diff = deadline.getTime() - now.getTime()

  if (diff < 0) {
    return 'text-red'
  } else if (diff < 24 * 60 * 60 * 1000) { // Less than 1 day
    return 'text-orange'
  } else if (diff < 7 * 24 * 60 * 60 * 1000) { // Less than 1 week
    return 'text-amber'
  } else {
    return 'text-success'
  }
}

const getStatusChipColor = (project: ProjectDto) => {
  const deadline = new Date(project.submissionDeadline)
  const now = new Date()
  
  if (project.isSubmissionClosed) {
    return 'grey'
  } else if (deadline < now) {
    return 'red'
  } else if (deadline.getTime() - now.getTime() < 24 * 60 * 60 * 1000) {
    return 'orange'
  } else {
    return 'green'
  }
}

const getStatusText = (project: ProjectDto) => {
  const deadline = new Date(project.submissionDeadline)
  const now = new Date()
  
  if (project.isSubmissionClosed) {
    return 'Fechado'
  } else if (deadline < now) {
    return 'Em atraso'
  } else if (deadline.getTime() - now.getTime() < 24 * 60 * 60 * 1000) {
    return 'Urgente'
  } else {
    return 'Aberto'
  }
}

const getInitials = (name: string) => {
  return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2)
}
</script>

<style scoped>
.project-description {
  white-space: pre-wrap;
  line-height: 1.5;
}

.text-red {
  color: rgb(var(--v-theme-error)) !important;
}

.text-orange {
  color: rgb(var(--v-theme-warning)) !important;
}

.text-amber {
  color: #FF8F00 !important;
}

.group-card {
  transition: all 0.2s ease-in-out;
  border-radius: 12px;
}

.group-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12) !important;
}

.group-card .v-card-text {
  background-color: rgb(var(--v-theme-surface));
}
</style>
