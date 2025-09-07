<template>
  <v-container>
    <v-row class="mb-4">
      <v-col>
        <h2 class="text-h4 font-weight-bold">
          <v-icon large class="mr-2">mdi-calendar-month</v-icon>
          Calendário de Avaliações
        </h2>
        <p class="text-subtitle-1 text-medium-emphasis">
          Consulte todas as avaliações e identifique conflitos de horários
        </p>
      </v-col>
    </v-row>

    <!-- Loading indicator -->
    <v-card v-if="loading" class="mb-6">
      <v-card-text class="d-flex justify-center align-center pa-6">
        <v-progress-circular indeterminate color="primary" class="mr-3"></v-progress-circular>
        <span>Carregando calendário de avaliações...</span>
      </v-card-text>
    </v-card>

    <div v-else>
      <!-- Summary Card -->
      <v-card class="mb-6" :color="getSummaryColor()">
        <v-card-item>
          <template v-slot:prepend>
            <v-avatar :color="getSummaryIconColor()" class="text-white">
              <v-icon>{{ getSummaryIcon() }}</v-icon>
            </v-avatar>
          </template>
          <v-card-title>{{ getSummaryTitle() }}</v-card-title>
          <v-card-subtitle>{{ getSummarySubtitle() }}</v-card-subtitle>
        </v-card-item>
      </v-card>

      <!-- Conflicts Alert -->
      <v-alert
        v-if="conflicts.length > 0"
        type="error"
        variant="outlined"
        density="comfortable"
        class="mb-6"
      >
        <template v-slot:prepend>
          <v-icon>mdi-alert-circle</v-icon>
        </template>
        <v-alert-title>
          {{ conflicts.length }} Conflito{{ conflicts.length !== 1 ? 's' : '' }} Detectado{{ conflicts.length !== 1 ? 's' : '' }}
        </v-alert-title>
        Existem avaliações com horários coincidentes. Consulte a secção "Conflitos" para mais detalhes.
      </v-alert>

      <!-- Filter Controls -->
      <v-card class="mb-6">
        <v-card-title class="pb-2">
          <v-icon class="mr-2">mdi-filter</v-icon>
          Filtros
        </v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="3">
              <v-select
                v-model="selectedSemester"
                :items="semesterOptions"
                label="Semestre"
                clearable
                @update:model-value="filterEvaluations"
              ></v-select>
            </v-col>
            <v-col cols="12" md="3">
              <v-select
                v-model="selectedType"
                :items="evaluationTypes"
                label="Tipo de Avaliação"
                clearable
                @update:model-value="filterEvaluations"
              ></v-select>
            </v-col>
            <v-col cols="12" md="4">
              <v-select
                v-model="selectedCurricularUnit"
                :items="curricularUnits"
                label="Unidade Curricular"
                clearable
                @update:model-value="filterEvaluations"
              ></v-select>
            </v-col>
            <v-col cols="12" md="2" class="d-flex align-center">
              <v-btn color="primary" variant="outlined" @click="clearFilters">
                <v-icon start>mdi-filter-remove</v-icon>
                Limpar
              </v-btn>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Tabs for different views -->
      <v-card>
        <v-tabs v-model="activeTab" align-tabs="center" color="primary">
          <v-tab value="calendar" prepend-icon="mdi-calendar">
            Calendário⠀
            <v-badge v-if="filteredEvaluations.length > 0" :content="filteredEvaluations.length" color="primary" class="ml-2"></v-badge>
          </v-tab>
          <v-tab value="list" prepend-icon="mdi-format-list-bulleted">
            Lista⠀
            <v-badge v-if="filteredEvaluations.length > 0" :content="filteredEvaluations.length" color="info" class="ml-2"></v-badge>
          </v-tab>
          <v-tab value="conflicts" prepend-icon="mdi-alert-circle">
            Conflitos⠀
            <v-badge v-if="conflicts.length > 0" :content="conflicts.length" color="error" class="ml-2"></v-badge>
          </v-tab>
          <v-tab value="deadlines" prepend-icon="mdi-clock-alert">
            Prazos de Revisão⠀
            <v-badge v-if="revisionDeadlines.length > 0" :content="revisionDeadlines.length" color="warning" class="ml-2"></v-badge>
          </v-tab>
        </v-tabs>

        <v-window v-model="activeTab">
          <!-- Calendar View -->
          <v-window-item value="calendar">
            <v-card-text>
              <!-- V-Calendar Integration -->
              <VCalendar
                :attributes="calendarAttributes"
                expanded
                :min-date="new Date(2024, 0, 1)"
                :max-date="new Date(2025, 11, 31)"
                class="calendar-picker"
                @dayclick="onDayClick"
              />

              <!-- Selected Day Details -->
              <v-card v-if="selectedDate && getDayEvaluations(selectedDate).length > 0" class="mt-6" elevation="2">
                <v-card-title class="bg-primary text-white">
                  <v-icon class="mr-2">mdi-calendar-today</v-icon>
                  {{ formatSelectedDate(selectedDate) }}
                </v-card-title>
                
                <v-card-text class="pa-0">
                  <v-list>
                    <v-list-item
                      v-for="evaluation in getDayEvaluations(selectedDate)"
                      :key="evaluation.id"
                      @click="viewDetails(evaluation)"
                      class="cursor-pointer"
                    >
                      <template v-slot:prepend>
                        <v-avatar :color="getTypeColor(evaluation.type)" size="small">
                          <v-icon color="white" size="small">{{ getTypeIcon(evaluation.type) }}</v-icon>
                        </v-avatar>
                      </template>

                      <v-list-item-title>{{ evaluation.code + '-' +evaluation.title }}</v-list-item-title>
                      <v-list-item-subtitle>
                        {{ evaluation.curricularUnitName }} • {{ formatTime(evaluation.date) }}
                        <v-chip 
                          v-if="isConflict(evaluation)" 
                          size="x-small" 
                          color="error" 
                          class="ml-2"
                        >
                          <v-icon start size="x-small">mdi-alert-circle</v-icon>
                          Conflito
                        </v-chip>
                      </v-list-item-subtitle>

                      <template v-slot:append>
                        <v-btn icon="mdi-eye" variant="text" size="small"></v-btn>
                      </template>
                    </v-list-item>
                  </v-list>
                </v-card-text>
              </v-card>
            </v-card-text>
          </v-window-item>

          <!-- List View (original table) -->
          <v-window-item value="list">
            <v-card-text>
              <v-data-table
                :headers="calendarHeaders"
                :items="filteredEvaluations"
                :sort-by="[{ key: 'date', order: 'asc' }]"
                class="elevation-0"
                no-data-text="Nenhuma avaliação encontrada para os filtros selecionados."
              >
                <template v-slot:[`item.type`]="{ item }">
                  <v-chip 
                    :color="getTypeColor(item.type)" 
                    size="small"
                  >
                    {{ getTypeText(item.type) }}
                  </v-chip>
                </template>

                <template v-slot:[`item.date`]="{ item }">
                  <div class="d-flex align-center">
                    <v-icon :color="isConflict(item) ? 'error' : 'default'" class="mr-2">
                      {{ isConflict(item) ? 'mdi-alert-circle' : 'mdi-calendar-clock' }}
                    </v-icon>
                    {{ formatDateTime(item.date) }}
                  </div>
                </template>

                <template v-slot:[`item.weight`]="{ item }">
                  {{ Math.round(item.weight * 100) }}%
                </template>

                <template v-slot:[`item.revisionDeadline`]="{ item }">
                  <div :class="isRevisionDeadlineApproaching(item) ? 'text-warning font-weight-bold' : ''">
                    {{ formatDateTime(item.revisionDeadline) }}
                  </div>
                </template>

                <template v-slot:[`item.actions`]="{ item }">
                  <v-btn icon variant="text" size="small" @click="viewDetails(item)">
                    <v-icon title="Ver Detalhes">mdi-eye</v-icon>
                  </v-btn>
                </template>
              </v-data-table>
            </v-card-text>
          </v-window-item>

          <!-- Conflicts View -->
          <v-window-item value="conflicts">
            <v-card-text>
              <div v-if="conflicts.length === 0" class="text-center py-8">
                <v-icon size="64" color="success" class="mb-4">mdi-check-circle</v-icon>
                <p class="text-h6 mb-2">Nenhum Conflito Detectado</p>
                <p class="text-body-2 text-medium-emphasis">
                  Todas as avaliações têm horários distintos.
                </p>
              </div>
              
              <div v-else>
                <v-alert type="info" variant="outlined" class="mb-4">
                  <v-alert-title>Conflitos de Horários</v-alert-title>
                  Os professores regentes das UCs afetadas serão notificados automaticamente por email.
                </v-alert>

                <v-expansion-panels multiple>
                  <v-expansion-panel
                    v-for="(conflict, index) in conflicts"
                    :key="index"
                    :title="`Conflito ${index + 1}: ${formatDateTime(conflict.date)}`"
                  >
                    <template v-slot:text>
                      <v-list>
                        <v-list-item
                          v-for="evaluation in conflict.evaluations"
                          :key="evaluation.id"
                          :prepend-icon="getTypeIcon(evaluation.type)"
                        >
                          <v-list-item-title>{{ evaluation.title }}</v-list-item-title>
                          <v-list-item-subtitle>
                            {{ evaluation.curricularUnitName }} ({{ getTypeText(evaluation.type) }})
                          </v-list-item-subtitle>
                        </v-list-item>
                      </v-list>
                    </template>
                  </v-expansion-panel>
                </v-expansion-panels>
              </div>
            </v-card-text>
          </v-window-item>

          <!-- Revision Deadlines View -->
          <v-window-item value="deadlines">
            <v-card-text>
              <v-data-table
                :headers="deadlineHeaders"
                :items="revisionDeadlines"
                :sort-by="[{ key: 'revisionDeadline', order: 'asc' }]"
                class="elevation-0"
                no-data-text="Nenhum prazo de revisão próximo."
              >
                <template v-slot:[`item.type`]="{ item }">
                  <v-chip 
                    :color="getTypeColor(item.type)" 
                    size="small"
                  >
                    {{ getTypeText(item.type) }}
                  </v-chip>
                </template>

                <template v-slot:[`item.revisionDeadline`]="{ item }">
                  <div :class="getDeadlineClass(item.revisionDeadline)">
                    <v-icon :color="getDeadlineColor(item.revisionDeadline)" class="mr-2">
                      mdi-clock-alert
                    </v-icon>
                    {{ formatDateTime(item.revisionDeadline) }}
                  </div>
                </template>

                <template v-slot:[`item.daysUntilDeadline`]="{ item }">
                  <v-chip 
                    :color="getDaysUntilDeadlineColor(item.daysUntilDeadline)" 
                    size="small"
                  >
                    {{ item.daysUntilDeadline > 0 ? `${item.daysUntilDeadline} dias` : 'Expirado' }}
                  </v-chip>
                </template>
              </v-data-table>
            </v-card-text>
          </v-window-item>
        </v-window>
      </v-card>
    </div>

    <!-- Details Dialog -->
    <v-dialog v-model="detailsDialog" max-width="800px">
      <v-card v-if="selectedEvaluation">
        <v-card-title class="bg-primary text-white d-flex justify-space-between align-center">
          <div class="d-flex align-center">
            <v-icon :icon="getTypeIcon(selectedEvaluation.type)" class="mr-2" color="white"></v-icon>
            <span>{{ selectedEvaluation.title }}</span>
          </div>
          <v-btn icon variant="text" color="white" @click="detailsDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>
        
        <v-card-text class="pa-4 pt-6">
          <v-row>
            <v-col cols="12" md="6">
              <v-list>
                <v-list-item>
                  <v-list-item-title>Unidade Curricular</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedEvaluation.curricularUnitName }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Tipo</v-list-item-title>
                  <v-list-item-subtitle>{{ getTypeText(selectedEvaluation.type) }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Data e Hora</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDateTime(selectedEvaluation.date) }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Peso na Avaliação</v-list-item-title>
                  <v-list-item-subtitle>{{ Math.round(selectedEvaluation.weight * 100) }}%</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-list>
                <v-list-item>
                  <v-list-item-title>Professor Regente</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedEvaluation.mainTeacherName }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Prazo de Revisão</v-list-item-title>
                  <v-list-item-subtitle :class="getDeadlineClass(selectedEvaluation.revisionDeadline)">
                    {{ formatDateTime(selectedEvaluation.revisionDeadline) }}
                  </v-list-item-subtitle>
                </v-list-item>
                <v-list-item v-if="isConflict(selectedEvaluation)">
                  <v-list-item-title>Status</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip color="error" size="small">
                      <v-icon start>mdi-alert-circle</v-icon>
                      Em Conflito
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
          
          <div v-if="selectedEvaluation.description" class="mt-4">
            <h4 class="mb-2">Descrição:</h4>
            <p>{{ selectedEvaluation.description }}</p>
          </div>
        </v-card-text>
        
        <v-divider></v-divider>
        
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" prepend-icon="mdi-close" @click="detailsDialog = false">
            Fechar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Calendar, DatePicker } from 'v-calendar'
import 'v-calendar/style.css'
import RemoteService from '../../services/RemoteService'

// Register v-calendar components
const VCalendar = Calendar

// State
const loading = ref(true)
const activeTab = ref('calendar')
const detailsDialog = ref(false)
const selectedDate = ref<Date | null>(new Date())

// Data
const evaluations = ref<any[]>([])
const curricularUnits = ref<any[]>([])
const selectedEvaluation = ref<any>(null)

// Filters
const selectedSemester = ref<string | null>(null)
const selectedType = ref<string | null>(null)
const selectedCurricularUnit = ref<string | null>(null)

// Filter options
const semesterOptions = [
  { title: 'Primeiro Semestre', value: 'FIRST' },
  { title: 'Segundo Semestre', value: 'SECOND' }
]

const evaluationTypes = [
  { title: 'Teste', value: 'TEST' },
  { title: 'Projeto', value: 'PROJECT' }
]

// Table headers
const calendarHeaders = [
  { title: 'Avaliação', key: 'title', align: 'start' as const, sortable: true },
  { title: 'UC', key: 'curricularUnitName', align: 'start' as const, sortable: true },
  { title: 'Tipo', key: 'type', align: 'center' as const, sortable: true },
  { title: 'Data/Hora', key: 'date', align: 'center' as const, sortable: true },
  { title: 'Peso', key: 'weight', align: 'center' as const, sortable: true },
  { title: 'Prazo Revisão', key: 'revisionDeadline', align: 'center' as const, sortable: true },
  { title: 'Ações', key: 'actions', align: 'end' as const, sortable: false }
]

const deadlineHeaders = [
  { title: 'Avaliação', key: 'title', align: 'start' as const, sortable: true },
  { title: 'UC', key: 'curricularUnitName', align: 'start' as const, sortable: true },
  { title: 'Tipo', key: 'type', align: 'center' as const, sortable: true },
  { title: 'Prazo de Revisão', key: 'revisionDeadline', align: 'center' as const, sortable: true },
  { title: 'Tempo Restante', key: 'daysUntilDeadline', align: 'center' as const, sortable: true }
]

// Computed properties
const filteredEvaluations = computed(() => {
  let filtered = [...evaluations.value]

  if (selectedSemester.value) {
    filtered = filtered.filter(evaluation => evaluation.semester === selectedSemester.value)
  }

  if (selectedType.value) {
    filtered = filtered.filter(evaluation => evaluation.type === selectedType.value)
  }

  if (selectedCurricularUnit.value) {
    filtered = filtered.filter(evaluation => evaluation.curricularUnitName === selectedCurricularUnit.value)
  }

  return filtered
})

const conflicts = computed(() => {
  const conflictMap = new Map<string, any[]>()
  
  filteredEvaluations.value.forEach(evaluation => {
    const dateKey = new Date(evaluation.date).toISOString().slice(0, 16) // YYYY-MM-DDTHH:MM
    
    if (!conflictMap.has(dateKey)) {
      conflictMap.set(dateKey, [])
    }
    conflictMap.get(dateKey)!.push(evaluation)
  })

  // Only return dates with more than one evaluation
  return Array.from(conflictMap.entries())
    .filter(([, evals]) => evals.length > 1)
    .map(([date, evals]) => ({
      date,
      evaluations: evals
    }))
})

const revisionDeadlines = computed(() => {
  const now = new Date()
  const thirtyDaysFromNow = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000)
  
  return filteredEvaluations.value
    .filter(evaluation => {
      const deadline = new Date(evaluation.revisionDeadline)
      return deadline <= thirtyDaysFromNow
    })
    .map(evaluation => ({
      ...evaluation,
      daysUntilDeadline: Math.ceil((new Date(evaluation.revisionDeadline).getTime() - now.getTime()) / (24 * 60 * 60 * 1000))
    }))
})

// V-Calendar specific computed properties
const calendarAttributes = computed(() => {
  const attributesByDate = new Map()
  
  // Group evaluations by date
  filteredEvaluations.value.forEach(evaluation => {
    const dateKey = new Date(evaluation.date).toDateString()
    
    if (!attributesByDate.has(dateKey)) {
      attributesByDate.set(dateKey, {
        date: new Date(evaluation.date),
        evaluations: [],
        hasConflict: false,
        hasDeadline: false
      })
    }
    
    const dayData = attributesByDate.get(dateKey)
    dayData.evaluations.push(evaluation)
    
    if (isConflict(evaluation)) {
      dayData.hasConflict = true
    }
    
    if (isRevisionDeadlineApproaching(evaluation)) {
      dayData.hasDeadline = true
    }
  })
  
  // Create attributes for each day
  const attributes: any[] = []
  
  attributesByDate.forEach(dayData => {
    let color = 'gray'
    
    // Priority: Conflicts (red) > Deadlines (orange) > Project (green) > Test (blue)
    if (dayData.hasConflict) {
      color = 'red'
    } else if (dayData.hasDeadline) {
      color = 'orange'
    } else if (dayData.evaluations.some(e => e.type === 'PROJECT')) {
      color = 'green'
    } else {
      color = 'blue'
    }
    
    attributes.push({
      key: `day-${dayData.date.toDateString()}`,
      dates: dayData.date,
      highlight: {
        color: color,
        fillMode: 'outline'
      },
      popover: {
        label: dayData.evaluations.map(e => e.title).join(', '),
        visibility: 'hover'
      }
    })
  })
  
  return attributes
})

// Helper functions
const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString('pt-PT', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getTypeColor = (type: string) => {
  return type === 'TEST' ? 'blue' : 'green'
}

const getTypeText = (type: string) => {
  return type === 'TEST' ? 'Teste' : 'Projeto'
}

const getTypeIcon = (type: string) => {
  return type === 'TEST' ? 'mdi-file-document-edit' : 'mdi-folder-multiple'
}

const isConflict = (evaluation: any) => {
  return conflicts.value.some(conflict => 
    conflict.evaluations.some(e => e.id === evaluation.id)
  )
}

const isRevisionDeadlineApproaching = (evaluation: any) => {
  const now = new Date()
  const deadline = new Date(evaluation.revisionDeadline)
  const daysUntil = Math.ceil((deadline.getTime() - now.getTime()) / (24 * 60 * 60 * 1000))
  return daysUntil <= 7 && daysUntil > 0
}

// Calendar specific helper functions
const getDayEvaluations = (date: Date) => {
  if (!date) return []
  
  const dateStr = date.toISOString().split('T')[0] // YYYY-MM-DD format
  
  return filteredEvaluations.value.filter(evaluation => {
    const evalDate = new Date(evaluation.date).toISOString().split('T')[0]
    return evalDate === dateStr
  })
}

const formatSelectedDate = (date: Date) => {
  return date.toLocaleDateString('pt-PT', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatTime = (dateString: string) => {
  return new Date(dateString).toLocaleTimeString('pt-PT', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const onDayClick = (day: any) => {
  selectedDate.value = day.date
}

const getDeadlineClass = (deadline: string) => {
  const now = new Date()
  const deadlineDate = new Date(deadline)
  const daysUntil = Math.ceil((deadlineDate.getTime() - now.getTime()) / (24 * 60 * 60 * 1000))
  
  if (daysUntil < 0) return 'text-error font-weight-bold'
  if (daysUntil <= 3) return 'text-error font-weight-bold'
  if (daysUntil <= 7) return 'text-warning font-weight-bold'
  return ''
}

const getDeadlineColor = (deadline: string) => {
  const now = new Date()
  const deadlineDate = new Date(deadline)
  const daysUntil = Math.ceil((deadlineDate.getTime() - now.getTime()) / (24 * 60 * 60 * 1000))
  
  if (daysUntil < 0) return 'error'
  if (daysUntil <= 3) return 'error'
  if (daysUntil <= 7) return 'warning'
  return 'default'
}

const getDaysUntilDeadlineColor = (days: number) => {
  if (days < 0) return 'error'
  if (days <= 3) return 'error'
  if (days <= 7) return 'warning'
  return 'success'
}

// Summary functions
const getSummaryColor = () => {
  if (conflicts.value.length > 0) return 'error'
  if (revisionDeadlines.value.some(item => item.daysUntilDeadline <= 7)) return 'warning'
  return 'success'
}

const getSummaryIconColor = () => {
  if (conflicts.value.length > 0) return 'error-darken-2'
  if (revisionDeadlines.value.some(item => item.daysUntilDeadline <= 7)) return 'warning-darken-2'
  return 'success-darken-2'
}

const getSummaryIcon = () => {
  if (conflicts.value.length > 0) return 'mdi-alert-circle'
  if (revisionDeadlines.value.some(item => item.daysUntilDeadline <= 7)) return 'mdi-clock-alert'
  return 'mdi-calendar-check'
}

const getSummaryTitle = () => {
  if (conflicts.value.length > 0) return 'Conflitos Detectados'
  if (revisionDeadlines.value.some(item => item.daysUntilDeadline <= 7)) return 'Prazos Próximos'
  return 'Calendário Atualizado'
}

const getSummarySubtitle = () => {
  const totalEvals = filteredEvaluations.value.length
  const conflictCount = conflicts.value.length
  const urgentDeadlines = revisionDeadlines.value.filter(item => item.daysUntilDeadline <= 7).length
  
  let subtitle = `${totalEvals} ${totalEvals !== 1 ? 'avaliações' : 'avaliação'} registada${totalEvals !== 1 ? 's' : ''}`
  
  if (conflictCount > 0) {
    subtitle += `, ${conflictCount} conflito${conflictCount !== 1 ? 's' : ''}`
  }
  
  if (urgentDeadlines > 0) {
    subtitle += `, ${urgentDeadlines} prazo${urgentDeadlines !== 1 ? 's' : ''} próximo${urgentDeadlines !== 1 ? 's' : ''}`
  }
  
  return subtitle + '.'
}

// Actions
const viewDetails = (evaluation: any) => {
  selectedEvaluation.value = evaluation
  detailsDialog.value = true
}

const filterEvaluations = () => {
  // Triggers reactive updates
}

const clearFilters = () => {
  selectedSemester.value = null
  selectedType.value = null
  selectedCurricularUnit.value = null
}

// Data fetching
const fetchEvaluations = async () => {
  try {
    loading.value = true
    
    // Fetch all evaluations and curricular units
    const [evaluationsData, curricularUnitsData] = await Promise.all([
      RemoteService.getAllEvaluations(),
      RemoteService.getCurricularUnits()
    ])
    
    curricularUnits.value = curricularUnitsData.map((uc: any) => ({
      title: uc.name,
      value: uc.name
    }))
    
    evaluations.value = evaluationsData.sort((a, b) => 
      new Date(a.date).getTime() - new Date(b.date).getTime()
    )
    
    // Send conflict notifications after loading
    await notifyConflicts()
    
  } catch (error) {
    console.error('Error fetching evaluations:', error)
    // Fallback to individual endpoints
    try {
      const [tests, projects] = await Promise.all([
        RemoteService.getTests(),
        RemoteService.getProjects()
      ])
      
      const allEvaluations: any[] = []
      
      // Add tests
      tests.forEach((test: any) => {
        allEvaluations.push({
          ...test,
          type: 'TEST',
          date: test.date,
          revisionDeadline: test.revisionDeadline
        })
      })
      
      // Add projects  
      projects.forEach((project: any) => {
        allEvaluations.push({
          ...project,
          type: 'PROJECT',
          date: project.submissionDeadline,
          revisionDeadline: project.revisionDeadline
        })
      })
      
      evaluations.value = allEvaluations.sort((a, b) => 
        new Date(a.date).getTime() - new Date(b.date).getTime()
      )
      
      await notifyConflicts()
    } catch (fallbackError) {
      console.error('Error in fallback fetch:', fallbackError)
      evaluations.value = []
      curricularUnits.value = []
    }
  } finally {
    loading.value = false
  }
}

const notifyConflicts = async () => {
  if (conflicts.value.length > 0) {
    try {
      await RemoteService.notifyConflicts(conflicts.value)
    } catch (error) {
      console.error('Error notifying conflicts:', error)
    }
  }
}

// Lifecycle
onMounted(async () => {
  await fetchEvaluations()
})
</script>

<style scoped>
.max-width-300 {
  max-width: 300px;
}

:deep(.v-card-title) {
  font-weight: bold;
}

:deep(.v-card-subtitle) {
  margin-top: -4px;
}

/* V-Calendar custom styles */
.calendar-picker {
  width: 100%;
}


:deep(.vc-day) {
  min-height: 60px;
}

:deep(.vc-day:hover) {
  background-color: rgb(var(--v-theme-surface-variant));
}

:deep(.vc-day.is-today) {
  background-color: rgb(var(--v-theme-primary-container));
  color: rgb(var(--v-theme-on-primary-container));
  font-weight: bold;
}

/* Selected date card */
.cursor-pointer {
  cursor: pointer;
}

.cursor-pointer:hover {
  background-color: rgb(var(--v-theme-surface-variant));
}
</style>
