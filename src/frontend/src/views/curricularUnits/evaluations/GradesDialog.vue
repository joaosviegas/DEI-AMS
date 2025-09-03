<template>
  <v-dialog v-model="localDialog" max-width="800">
    <v-card v-if="evaluation">
      <v-card-title class="d-flex justify-space-between align-center">
        <div>
          <span class="text-h5">{{ evaluation.title }}</span>
          <v-chip 
            :color="evaluation.type === 'TEST' ? 'green' : 'blue'" 
            size="small"
            class="ml-2"
          >
            {{ evaluation.type === 'TEST' ? 'Teste' : 'Projeto' }}
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
        <v-row class="mb-4">
          <v-col cols="4">
            <strong>Data:</strong> {{ formatDate(evaluation.date) }}
          </v-col>
          <v-col cols="4">
            <strong>Peso:</strong> {{ evaluation.weight * 100 }}%
          </v-col>
          <v-col cols="4">
            <strong>Estado:</strong> 
            <v-chip 
              :color="getGradeStatusColor(myGrade?.grade)" 
              size="small"
              class="ml-1"
            >
              {{ getGradeStatusText(myGrade?.grade) }}
            </v-chip>
          </v-col>
        </v-row>

        <v-divider class="mb-4"></v-divider>

        <!-- My Grade Section -->
        <div class="mb-6">
          <h3 class="mb-3">A Minha Nota</h3>
          
          <v-card variant="outlined" class="pa-4">
            <v-row align="center">
              <v-col cols="3">
                <div class="text-center">
                  <div class="text-h4 font-weight-bold" :class="getGradeColorClass(myGrade?.grade)">
                    {{ myGrade?.grade !== null ? formatGrade(myGrade.grade) : '--' }}
                  </div>
                  <div class="text-caption text-grey">Nota (0-20)</div>
                </div>
              </v-col>
              <v-col cols="6">
                <div v-if="myGrade?.gradedAt">
                  <div class="text-body-2 mb-1">
                    <strong>Avaliado em:</strong> {{ formatDateTime(myGrade.gradedAt) }}
                  </div>
                </div>
                <div v-else class="text-body-2 text-grey">
                  Ainda não foi avaliado
                </div>
                
                <div v-if="myGrade?.revisionRequested" class="mt-2">
                  <v-chip color="orange" size="small" prepend-icon="mdi-clock-outline">
                    Revisão solicitada
                  </v-chip>
                  <div class="text-caption mt-1">
                    Solicitada em: {{ formatDateTime(myGrade.revisionRequestedAt) }}
                  </div>
                  <div v-if="myGrade.revisionReason" class="text-caption mt-1">
                    <strong>Motivo:</strong> {{ myGrade.revisionReason }}
                  </div>
                </div>
              </v-col>
              <v-col cols="3" class="text-right">
                <v-btn
                  v-if="myGrade?.grade !== null && !myGrade?.revisionRequested"
                  color="orange"
                  variant="outlined"
                  prepend-icon="mdi-clipboard-edit"
                  @click="showRevisionDialog = true"
                  size="small"
                >
                  Solicitar Revisão
                </v-btn>
                <v-btn
                  v-else-if="myGrade?.revisionRequested"
                  color="grey"
                  variant="outlined"
                  prepend-icon="mdi-clock-outline"
                  disabled
                  size="small"
                >
                  Revisão Pendente
                </v-btn>
              </v-col>
            </v-row>
          </v-card>
        </div>

        <!-- Class Statistics (Optional) -->
        <div v-if="showStatistics">
          <h3 class="mb-3">Estatísticas da Turma</h3>
          <v-card variant="outlined" class="pa-4">
            <v-row>
              <v-col cols="3" class="text-center">
                <div class="text-h6 font-weight-bold text-green">{{ statistics.passed }}</div>
                <div class="text-caption">Aprovados</div>
              </v-col>
              <v-col cols="3" class="text-center">
                <div class="text-h6 font-weight-bold text-red">{{ statistics.failed }}</div>
                <div class="text-caption">Reprovados</div>
              </v-col>
              <v-col cols="3" class="text-center">
                <div class="text-h6 font-weight-bold">{{ statistics.average.toFixed(1) }}</div>
                <div class="text-caption">Média</div>
              </v-col>
              <v-col cols="3" class="text-center">
                <div class="text-h6 font-weight-bold">{{ statistics.total }}</div>
                <div class="text-caption">Total</div>
              </v-col>
            </v-row>
          </v-card>
        </div>
      </v-card-text>
    </v-card>
  </v-dialog>

  <!-- Revision Request Dialog -->
  <v-dialog v-model="showRevisionDialog" max-width="500">
    <v-card prepend-icon="mdi-clipboard-edit" title="Solicitar Revisão de Nota">
      <v-card-text>
        <p class="mb-4">
          Deseja solicitar uma revisão da sua nota para <strong>{{ evaluation?.title }}</strong>?
        </p>
        
        <v-textarea
          v-model="revisionReason"
          label="Motivo da solicitação"
          placeholder="Descreva o motivo pelo qual solicita a revisão da nota..."
          rows="4"
          :rules="[v => !!v || 'Motivo é obrigatório']"
          required
        ></v-textarea>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" @click="cancelRevisionRequest"></v-btn>
        <v-btn 
          color="orange" 
          text="Solicitar Revisão" 
          @click="submitRevisionRequest"
          :loading="submittingRevision"
          :disabled="!revisionReason.trim()"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import TestDto from '../../../models/TestDto'
import RemoteService from '../../../services/RemoteService'
import { useRoleStore } from '../../../stores/role'

const emit = defineEmits(['update:modelValue'])

const props = defineProps({
  evaluation: {
    type: Object as () => TestDto | undefined,
    default: undefined
  },
  modelValue: {
    type: Boolean,
    default: false
  }
})

const loading = ref(false)
const roleStore = useRoleStore()
const myGrade = ref<any>(null)
const showRevisionDialog = ref(false)
const revisionReason = ref('')
const submittingRevision = ref(false)
const showStatistics = ref(true) // Can be toggled
const statistics = ref({
  passed: 0,
  failed: 0,
  average: 0,
  total: 0
})

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// Helper functions
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('pt-PT')
}

const formatDateTime = (dateString: string | null) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('pt-PT')
}

const formatGrade = (grade: number) => {
  return grade.toFixed(1)
}

const getGradeStatusColor = (grade: number | null) => {
  if (grade === null) return 'grey'
  return grade >= 10 ? 'green' : 'red'
}

const getGradeStatusText = (grade: number | null) => {
  if (grade === null) return 'Não avaliado'
  return grade >= 10 ? 'Aprovado' : 'Reprovado'
}

const getGradeColorClass = (grade: number | null) => {
  if (grade === null) return 'text-grey'
  return grade >= 10 ? 'text-green' : 'text-red'
}

const loadMyGrade = async () => {
  if (!props.evaluation?.id) return
  
  loading.value = true
  try {
    const grades = await RemoteService.getEvaluationGrades(props.evaluation.id)
    
    // Find my grade (assuming we have current user context)
    // TODO: Replace with actual current user ID from auth context
    const currentUserId = 1 // This should come from auth store
    myGrade.value = grades.find(grade => grade.student.id === currentUserId) || null
    
    // Calculate statistics
    const gradedStudents = grades.filter(g => g.grade !== null)
    statistics.value = {
      total: grades.length,
      passed: gradedStudents.filter(g => g.grade >= 10).length,
      failed: gradedStudents.filter(g => g.grade < 10).length,
      average: gradedStudents.length > 0 
        ? gradedStudents.reduce((sum, g) => sum + g.grade, 0) / gradedStudents.length 
        : 0
    }
  } catch (error) {
    console.error('Error loading my grade:', error)
  } finally {
    loading.value = false
  }
}

const cancelRevisionRequest = () => {
  showRevisionDialog.value = false
  revisionReason.value = ''
}

const submitRevisionRequest = async () => {
  if (!myGrade.value?.id || !revisionReason.value.trim()) return
  
  submittingRevision.value = true
  try {
    await RemoteService.requestGradeRevision(myGrade.value.id, revisionReason.value)
    
    // Update local state
    myGrade.value.revisionRequested = true
    myGrade.value.revisionReason = revisionReason.value
    myGrade.value.revisionRequestedAt = new Date().toISOString()
    
    // Close dialog and reset
    cancelRevisionRequest()
  } catch (error) {
    console.error('Error requesting revision:', error)
  } finally {
    submittingRevision.value = false
  }
}

// Watch for evaluation changes to load grade
watch(() => props.evaluation, async () => {
  if (props.evaluation && localDialog.value) {
    await loadMyGrade()
  }
}, { immediate: true })

// Watch for dialog opening to load grade
watch(localDialog, async (isOpen) => {
  if (isOpen && props.evaluation) {
    await loadMyGrade()
  }
})
</script>

<style scoped>
.text-green {
  color: rgb(var(--v-theme-success)) !important;
}

.text-red {
  color: rgb(var(--v-theme-error)) !important;
}

.text-grey {
  color: rgb(var(--v-theme-on-surface-variant)) !important;
}
</style>
