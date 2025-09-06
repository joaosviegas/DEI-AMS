<template>
  <div class="deadlines-view">
    <!-- Header -->
    <div class="d-flex justify-space-between align-center mb-6">
      <div>
        <h1 class="text-h4 font-weight-bold">Prazos de Entrega</h1>
        <p class="text-subtitle-1 ">
          Consulte e submeta projetos de todas as unidades curriculares
        </p>
      </div>
      <v-chip
        :color="getOverallStatusColor()"
        size="large"
        variant="tonal"
        prepend-icon="mdi-clock-outline"
      >
        {{ getOverallStatusText() }}
      </v-chip>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <v-progress-circular
        size="64"
        color="primary"
        indeterminate
      ></v-progress-circular>
      <p class="text-h6 mt-4">A carregar projetos...</p>
    </div>

    <!-- Content -->
    <div v-else>
      <!-- Filter and Search -->
      <v-card flat class="mb-6">
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="searchQuery"
                label="Pesquisar projetos"
                prepend-inner-icon="mdi-magnify"
                clearable
                variant="outlined"
                density="compact"
                hide-details
              ></v-text-field>
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
            <v-col cols="12" md="3">
              <v-select
                v-model="typeFilter"
                :items="typeOptions"
                label="Tipo de projeto"
                variant="outlined"
                density="compact"
                hide-details
              ></v-select>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Projects List -->
      <div v-if="filteredProjects.length > 0">
        <v-row>
          <v-col
            v-for="project in filteredProjects"
            :key="project.id"
            cols="12"
          >
            <v-card
              class="mb-4 project-card"
              :class="{ 'project-card-urgent': isUrgent(project) }"
              elevation="2"
            >
              <!-- Project Header -->
              <v-card-title class="pb-2">
                <div class="d-flex justify-space-between align-center w-100">
                  <div>
                    <h3 class="text-h6">{{ project.title }}</h3>
                    <p class="text-subtitle-2  mb-0">
                      {{ project.curricularUnitName }}
                    </p>
                  </div>
                  <div class="d-flex align-center gap-2">
                    <v-chip
                      :color="project.isGroupProject ? 'primary' : 'success'"
                      size="small"
                      variant="tonal"
                    >
                      {{ project.isGroupProject ? 'Grupo' : 'Individual' }}
                    </v-chip>
                    <v-chip
                      :color="getStatusChipColor(project)"
                      size="small"
                    >
                      {{ getStatusText(project) }}
                    </v-chip>
                  </div>
                </div>
                <v-spacer></v-spacer>
                <v-divider></v-divider>
                <v-spacer></v-spacer>
              </v-card-title>

              <v-card-text class="pt-0">
                <!-- Project Info -->
                <v-row class="mb-4">
                  <v-col cols="12" sm="6" md="3">
                    <div class="text-caption  mb-1">PRAZO</div>
                    <div :class="getDeadlineColor(project)">
                      {{ formatDateTime(project.submissionDeadline) }}
                    </div>
                    <div class="text-caption ">
                      {{ getTimeRemaining(project) }}
                    </div>
                  </v-col>
                  <v-col cols="12" sm="6" md="3">
                    <div class="text-caption mb-1">PESO</div>
                    <div class="font-weight-medium">
                      {{ (project.weight * 100).toFixed(1) }}%
                    </div>
                  </v-col>
                  <v-col cols="12" sm="6" md="3">
                    <div class="text-caption  mb-1">
                      GRUPOS
                    </div>
                    <div class="font-weight-medium">
                      {{ projectGroups[project.id]?.length || 0 }} 
                      {{ projectGroups[project.id]?.length === 1 ? 'grupo' : 'grupos' }}
                    </div>
                  </v-col>
                  <v-col cols="12" sm="6" md="3">
                    <div class="text-caption  mb-1">SUBMISSÕES</div>
                    <div class="font-weight-medium">
                      {{ getSubmissionCount(project) }} submetidas
                    </div>
                  </v-col>
                </v-row>

                <!-- Project Description -->
                <div class="mb-4">
                  <p class="text-body-2 ">
                    {{ project.description || 'Sem descrição disponível.' }}
                  </p>
                </div>

                <!-- Groups/Students Section -->
                <div class="mb-4">
                  <div class="d-flex justify-space-between align-center mb-3">
                    <h4 class="text-subtitle-1">
                      Grupos do Projeto
                    </h4>
                    <v-btn
                      size="small"
                      variant="text"
                      :prepend-icon="expandedProjects.includes(project.id) ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                      @click="toggleProjectExpansion(project.id)"
                    >
                      {{ expandedProjects.includes(project.id) ? 'Recolher' : 'Expandir' }}
                    </v-btn>
                  </div>

                  <v-expand-transition>
                    <div v-show="expandedProjects.includes(project.id)">
                      <v-row>
                        <v-col
                          v-for="group in projectGroups[project.id]"
                          :key="group.id"
                          cols="12"
                          md="6"
                          lg="4"
                        >
                          <v-card
                            variant="tonal"
                            class="group-card"
                            @click="openGroupActions(project, group)"
                            style="cursor: pointer;"
                          >
                            <v-card-text class="pb-2">
                              <div class="d-flex justify-space-between align-center mb-2">
                                <h5 class="font-weight-medium">
                                  {{ group.name || (group.members[0]?.name ? `${group.members[0]?.name}` : `Grupo ${group.id}`) }}
                                </h5>
                                <v-chip
                                  :color="getGroupSubmissionStatus(project.id, group.id).color"
                                  size="x-small"
                                  variant="flat"
                                >
                                  {{ getGroupSubmissionStatus(project.id, group.id).text }}
                                </v-chip>
                              </div>

                              <div class="text-caption  mb-2">
                                {{ group.members.length }} 
                                {{ group.members.length === 1 ? 'membro' : 'membros' }}
                                <span v-if="group.members.length === 1 && group.members[0]?.istId">
                                  • IST ID: {{ group.members[0]?.istId }}
                                </span>
                              </div>

                              <div class="d-flex flex-wrap gap-1">
                                <v-avatar
                                  v-for="member in group.members.slice(0, 3)"
                                  :key="member.id"
                                  size="24"
                                  :title="member.name"
                                >
                                  <span class="text-caption">
                                    {{ getInitials(member.name || '') }}
                                  </span>
                                </v-avatar>
                                <v-chip
                                  v-if="group.members.length > 3"
                                  size="x-small"
                                  variant="outlined"
                                >
                                  +{{ group.members.length - 3 }}
                                </v-chip>
                              </div>
                            </v-card-text>
                          </v-card>
                        </v-col>
                      </v-row>
                    </div>
                  </v-expand-transition>
                </div>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12">
        <v-icon size="64" color="grey-lighten-1">mdi-calendar-clock</v-icon>
        <h3 class="text-h5 mt-4 mb-2 ">Nenhum projeto encontrado</h3>
        <p class="text-body-1 ">
          {{ searchQuery ? 'Tente ajustar os filtros de pesquisa.' : 'Não há projetos disponíveis no momento.' }}
        </p>
      </div>
    </div>

    <!-- Group Actions Dialog -->
    <v-dialog v-model="showGroupActions" max-width="500">
      <v-card v-if="selectedProject && selectedGroup">
        <v-card-title>
          <div>
            <h3>
              {{ selectedGroup.name || (selectedGroup.members[0]?.name ? `${selectedGroup.members[0]?.name}` : `Grupo ${selectedGroup.id}`) }}
            </h3>
            <p class="text-subtitle-2  mb-0">{{ selectedProject.title }}</p>
          </div>
        </v-card-title>
        <v-card-text>
          <div class="mb-4">
            <div class="text-caption  mb-2">
              MEMBROS DO GRUPO
            </div>
            <v-chip-group column>
              <v-chip
                v-for="member in selectedGroup.members"
                :key="member.id"
                size="small"
                variant="outlined"
              >
                <div class="d-flex flex-column align-start">
                  <span>{{ member.name }}</span>
                  <span class="text-caption" v-if="member.istId">
                    IST ID: {{ member.istId }}
                  </span>
                </div>
              </v-chip>
            </v-chip-group>
          </div>

          <div class="mb-4">
            <div class="text-caption  mb-2">STATUS DA SUBMISSÃO</div>
            <v-alert
              :type="getGroupSubmissionStatus(selectedProject.id, selectedGroup.id).alertType as 'warning' | 'success' | 'error' | 'info'"
              variant="tonal"
              density="compact"
            >
              {{ getGroupSubmissionStatus(selectedProject.id, selectedGroup.id).description }}
            </v-alert>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            variant="outlined"
            prepend-icon="mdi-history"
            @click="openGroupHistory(selectedProject, selectedGroup)"
          >
            Ver Histórico
          </v-btn>
          <v-btn
            color="success"
            prepend-icon="mdi-upload"
            @click="openGroupSubmission(selectedProject, selectedGroup)"
            :disabled="selectedProject.isSubmissionClosed"
          >
            Submeter Projeto
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialogs -->
    <GroupDetailsDialog
      v-model="showGroupDetailsDialog"
      :project="selectedProject"
      :group="selectedGroup"
      :submission-status="selectedProject && selectedGroup ? getGroupSubmissionStatusForDialog(selectedProject.id, selectedGroup.id) : null"
      @viewHistory="openGroupHistory(selectedProject, selectedGroup)"
      @startSubmission="openGroupSubmission(selectedProject, selectedGroup)"
    />

    <ProjectSubmissionDialog
      v-model="showSubmissionDialog"
      :project="selectedProject"
      :my-group="selectedGroup"
      :has-existing-submission="hasExistingSubmission"
      @submitted="onSubmissionComplete"
    />

    <SubmissionHistoryDialog
      v-model="showHistoryDialog"
      :project="selectedProject"
      :my-group="selectedGroup"
      @startSubmission="openSubmissionFromHistory"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import RemoteService from '../../services/RemoteService'
import ProjectDto from '../../models/ProjectDto'
import ProjectGroupDto from '../../models/ProjectGroupDto'
import ProjectSubmissionDto from '../../models/ProjectSubmissionDto'
import CurricularUnitDto from '../../models/CurricularUnitDto'
import ProjectSubmissionDialog from './ProjectSubmissionDialog.vue'
import SubmissionHistoryDialog from './SubmissionHistoryDialog.vue'
import GroupDetailsDialog from './GroupDetailsDialog.vue'

// Reactive data
const loading = ref(true)
const projects = ref<ProjectDto[]>([])
const projectGroups = ref<Record<number, ProjectGroupDto[]>>({})
const groupSubmissions = ref<Record<string, ProjectSubmissionDto[]>>({}) // key: projectId-groupId
const expandedProjects = ref<number[]>([])

// Dialog states
const showGroupActions = ref(false)
const showGroupDetailsDialog = ref(false)
const showSubmissionDialog = ref(false)
const showHistoryDialog = ref(false)
const selectedProject = ref<ProjectDto | null>(null)
const selectedGroup = ref<ProjectGroupDto | null>(null)
const hasExistingSubmission = ref(false)

// Filters
const searchQuery = ref('')
const ucFilter = ref('todas')
const typeFilter = ref('todos')

const ucOptions = ref([
  { title: 'Todas as UCs', value: 'todas' }
])

const typeOptions = [
  { title: 'Todos', value: 'todos' },
  { title: 'Individuais', value: 'individual' },
  { title: 'Grupos', value: 'grupo' }
]

// Computed properties
const filteredProjects = computed(() => {
  let filtered = projects.value

  // Text search
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(p => 
      p.title.toLowerCase().includes(query) ||
      (p.curricularUnitName && p.curricularUnitName.toLowerCase().includes(query)) ||
      (p.description && p.description.toLowerCase().includes(query))
    )
  }

  // UC filter
  if (ucFilter.value !== 'todas') {
    filtered = filtered.filter(p => {
      return p.curricularUnitName === ucFilter.value
    })
  }

  // Type filter
  if (typeFilter.value !== 'todos') {
    filtered = filtered.filter(p => {
      if (typeFilter.value === 'individual') return !p.isGroupProject
      if (typeFilter.value === 'grupo') return p.isGroupProject
      return true
    })
  }

  // Sort by deadline (urgent first)
  return filtered.sort((a, b) => {
    const deadlineA = new Date(a.submissionDeadline)
    const deadlineB = new Date(b.submissionDeadline)
    return deadlineA.getTime() - deadlineB.getTime()
  })
})

// Data loading
const loadData = async () => {
  loading.value = true
  try {
    // Load curricular units and their projects
    const curricularUnits = await RemoteService.getCurricularUnits()
    const allProjects: ProjectDto[] = []

    for (const unit of curricularUnits) {
      const unitProjects = await RemoteService.getProjectsByCurricularUnit(unit.id)
      unitProjects.forEach(project => {
        project.curricularUnitName = unit.name
        allProjects.push(project)
      })
    }

    projects.value = allProjects

    // Populate UC filter options
    const ucNames = allProjects.map(p => p.curricularUnitName).filter(Boolean)
    const uniqueUCs = Array.from(new Set(ucNames))
    ucOptions.value = [
      { title: 'Todas as UCs', value: 'todas' },
      ...uniqueUCs.map(uc => ({ title: uc, value: uc }))
    ]

    // Load groups for ALL projects (both individual and group projects)
    for (const project of allProjects) {
      if (project.id) {
        try {
          const groups = await RemoteService.getProjectGroups(project.id)
          projectGroups.value[project.id] = groups

          // Load submissions for each group
          for (const group of groups) {
            if (group.id) {
              try {
                const submissions = await RemoteService.getGroupSubmissions(project.id, group.id)
                groupSubmissions.value[`${project.id}-${group.id}`] = submissions
              } catch (error) {
                console.warn('Could not load submissions for group', group.id, error)
                groupSubmissions.value[`${project.id}-${group.id}`] = []
              }
            }
          }
        } catch (error) {
          console.warn('Could not load groups for project', project.id, error)
          projectGroups.value[project.id] = []
        }
      }
    }
  } catch (error) {
    console.error('Error loading data:', error)
  } finally {
    loading.value = false
  }
}

// Actions
const toggleProjectExpansion = (projectId: number) => {
  const index = expandedProjects.value.indexOf(projectId)
  if (index > -1) {
    expandedProjects.value.splice(index, 1)
  } else {
    expandedProjects.value.push(projectId)
  }
}

const openGroupActions = (project: ProjectDto, group: ProjectGroupDto) => {
  selectedProject.value = project
  selectedGroup.value = group
  showGroupDetailsDialog.value = true
}

const openGroupSubmission = (project: ProjectDto, group: ProjectGroupDto) => {
  selectedProject.value = project
  selectedGroup.value = group
  hasExistingSubmission.value = getGroupSubmissionCount(project.id, group.id || 0) > 0
  showGroupActions.value = false
  showSubmissionDialog.value = true
}

const openGroupHistory = (project: ProjectDto, group: ProjectGroupDto) => {
  selectedProject.value = project
  selectedGroup.value = group
  showGroupActions.value = false
  showHistoryDialog.value = true
}

const openSubmissionFromHistory = () => {
  showHistoryDialog.value = false
  showSubmissionDialog.value = true
}

const onSubmissionComplete = () => {
  showSubmissionDialog.value = false
  // Reload submissions data
  loadData()
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

const getTimeRemaining = (project: ProjectDto) => {
  const deadline = new Date(project.submissionDeadline)
  const now = new Date()
  const diff = deadline.getTime() - now.getTime()

  if (diff < 0) {
    return 'Prazo expirado'
  }

  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))

  if (days > 0) {
    return `${days} dia${days > 1 ? 's' : ''} restantes`
  } else if (hours > 0) {
    return `${hours} hora${hours > 1 ? 's' : ''} restantes`
  } else {
    return 'Menos de 1 hora'
  }
}

const getDeadlineColor = (project: ProjectDto) => {
  const deadline = new Date(project.submissionDeadline)
  const now = new Date()
  const diff = deadline.getTime() - now.getTime()

  if (diff < 0) {
    return 'text-error'
  } else if (diff < 24 * 60 * 60 * 1000) {
    return 'text-warning'
  } else if (diff < 7 * 24 * 60 * 60 * 1000) {
    return 'text-warning'
  } else {
    return 'text-success'
  }
}

const getStatusChipColor = (project: ProjectDto) => {
  const status = getProjectStatus(project)
  switch (status) {
    case 'aberto': return 'success'
    case 'urgente': return 'warning'
    case 'atraso': return 'error'
    case 'fechado': return 'grey'
    default: return 'grey'
  }
}

const getStatusText = (project: ProjectDto) => {
  const status = getProjectStatus(project)
  switch (status) {
    case 'aberto': return 'Aberto'
    case 'urgente': return 'Urgente'
    case 'atraso': return 'Em atraso'
    case 'fechado': return 'Fechado'
    default: return 'Desconhecido'
  }
}

const getProjectStatus = (project: ProjectDto): string => {
  const deadline = new Date(project.submissionDeadline)
  const now = new Date()
  
  if (project.isSubmissionClosed) {
    return 'fechado'
  } else if (deadline < now) {
    return 'atraso'
  } else if (deadline.getTime() - now.getTime() < 24 * 60 * 60 * 1000) {
    return 'urgente'
  } else {
    return 'aberto'
  }
}

const isUrgent = (project: ProjectDto) => {
  const status = getProjectStatus(project)
  return status === 'urgente' || status === 'atraso'
}

const getSubmissionCount = (project: ProjectDto) => {
  if (!project.id) return 0
  
  // Both individual and group projects now have groups
  const groups = projectGroups.value[project.id] || []
  return groups.reduce((total, group) => {
    return total + getGroupSubmissionCount(project.id, group.id || 0)
  }, 0)
}

const getGroupSubmissionCount = (projectId: number, groupId: number) => {
  const submissions = groupSubmissions.value[`${projectId}-${groupId}`] || []
  return submissions.length
}

const getGroupSubmissionStatus = (projectId: number, groupId: number) => {
  const submissions = groupSubmissions.value[`${projectId}-${groupId}`] || []
  const count = submissions.length
  const project = projects.value.find(p => p.id === projectId)
  
  if (count === 0) {
    return {
      color: 'grey',
      text: 'Não submetido',
      alertType: 'warning' as const,
      description: 'Este grupo ainda não fez nenhuma submissão.'
    }
  } else {
    const latest = submissions[submissions.length - 1]
    const isOnTime = new Date(latest.submissionDate) <= new Date(project?.submissionDeadline || '')
    
    return {
      color: isOnTime ? 'success' : 'warning',
      text: `${count} submissão${count > 1 ? 'ões' : ''}`,
      alertType: 'success' as const,
      description: `Este grupo fez ${count} submissão${count > 1 ? 'ões' : ''}. ${isOnTime ? 'Submetido dentro do prazo.' : 'A última submissão foi feita fora do prazo.'}`
    }
  }
}

const getGroupSubmissionStatusForDialog = (projectId: number, groupId: number) => {
  const submissions = groupSubmissions.value[`${projectId}-${groupId}`] || []
  const count = submissions.length
  
  if (count === 0) {
    return {
      hasSubmission: false,
      isOnTime: false,
      submissionCount: 0
    }
  } else {
    const latest = submissions[submissions.length - 1]
    const isOnTime = new Date(latest.submissionDate) <= new Date(projects.value.find(p => p.id === projectId)?.submissionDeadline || '')
    
    return {
      hasSubmission: true,
      isOnTime: isOnTime,
      submissionCount: count
    }
  }
}

const getOverallStatusColor = () => {
  const urgentCount = projects.value.filter(p => isUrgent(p)).length
  if (urgentCount > 0) return 'warning'
  return 'success'
}

const getOverallStatusText = () => {
  const urgentCount = projects.value.filter(p => isUrgent(p)).length
  const totalCount = projects.value.length
  
  if (urgentCount > 0) {
    return `${urgentCount} de ${totalCount} urgentes`
  }
  return `${totalCount} projetos`
}

const getInitials = (name: string) => {
  return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2)
}

// Lifecycle
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.deadlines-view {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.project-card {
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.project-card-urgent {
  border-left-color: rgb(var(--v-theme-warning));
}

.project-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12) !important;
}

.group-card {
  transition: all 0.2s ease;
}

.group-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1) !important;
}

.bg-surface-variant {
  background-color: rgb(var(--v-theme-surface-variant)) !important;
}
</style>
