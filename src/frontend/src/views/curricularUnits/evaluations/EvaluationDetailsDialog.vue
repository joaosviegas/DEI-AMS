<template>
  <v-dialog v-model="localDialog" max-width="900">
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
            <strong>Total de Alunos:</strong> {{ studentGrades.length }}
          </v-col>
        </v-row>

        <v-divider class="mb-4"></v-divider>

        <!-- Grades Table -->
        <div class="d-flex justify-space-between align-center mb-4">
          <h3>Notas dos Alunos</h3>
          <v-btn 
            v-if="canGrade"
            color="primary"
            prepend-icon="mdi-content-save"
            @click="saveAllGrades"
            :loading="saving"
            :disabled="!hasChanges"
          >
            Guardar Notas
          </v-btn>
        </div>

        <v-data-table
          :headers="gradeHeaders"
          :items="studentGrades"
          :loading="loading"
          no-data-text="Sem alunos inscritos."
          item-key="studentId"
        >
          <template v-slot:[`item.studentName`]="{ item }">
            <div class="d-flex align-center">
              <span class="font-weight-medium">{{ item.studentName }}</span>
              <v-chip 
                v-if="item.revisionRequested"
                color="orange"
                size="x-small"
                class="ml-2"
                title="Revisão solicitada"
              >
                R
              </v-chip>
            </div>
          </template>

          <template v-slot:[`item.grade`]="{ item }">
            <v-text-field
              v-if="canGrade"
              v-model.number="item.grade"
              type="number"
              min="0"
              max="20"
              step="0.1"
              density="compact"
              hide-details
              @input="markAsChanged(item)"
              :rules="[
                v => v === null || v === undefined || v === '' || (v >= 0 && v <= 20) || 'Nota deve estar entre 0 e 20'
              ]"
              placeholder="--"
            ></v-text-field>
            <span v-else-if="item.grade !== null">
              {{ formatGrade(item.grade) }}
            </span>
            <span v-else class="text-grey">--</span>
          </template>

          <template v-slot:[`item.status`]="{ item }">
            <v-chip 
              :color="getGradeStatusColor(item.grade)" 
              size="small"
            >
              {{ getGradeStatusText(item.grade) }}
            </v-chip>
          </template>

          <template v-slot:[`item.gradedAt`]="{ item }">
            <span v-if="item.gradedAt">
              {{ formatDateTime(item.gradedAt) }}
            </span>
            <span v-else class="text-grey">--</span>
          </template>

          <template v-slot:[`item.actions`]="{ item }">
            <!-- Teachers: View revision requests -->
            <v-btn 
              v-if="item.revisionRequested && canGrade"
              icon="mdi-eye"
              variant="text"
              size="small"
              @click="viewRevisionRequest(item)"
              title="Ver pedido de revisão"
              color="orange"
            ></v-btn>
            
            <!-- Students: Request revision (only if they have a grade and haven't requested yet) -->
            <v-btn 
              v-if="!canGrade && item.grade !== null && !item.revisionRequested"
              icon="mdi-clipboard-edit"
              variant="text"
              size="small"
              @click="openRevisionDialog(item)"
              title="Solicitar revisão"
              color="orange"
            ></v-btn>
            
            <!-- Students: Show pending revision status -->
            <v-chip
              v-if="!canGrade && item.revisionRequested"
              color="orange"
              size="small"
              prepend-icon="mdi-clock-outline"
            >
              Revisão Pendente
            </v-chip>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </v-dialog>

  <!-- Revision Request Dialog -->
  <v-dialog v-model="showRevisionDialog" max-width="500">
    <v-card prepend-icon="mdi-clipboard-edit" title="Solicitar Revisão de Nota">
      <v-card-text>
        <div v-if="selectedGradeForRevision">
          <div class="mb-4">
            <strong>Avaliação:</strong> {{ evaluation?.title }}<br>
            <strong>Nota Atual:</strong> {{ formatGrade(selectedGradeForRevision.grade) }}/20
          </div>
          
          <p class="mb-4">
            Deseja solicitar uma revisão da sua nota para esta avaliação?
          </p>
          
          <v-textarea
            v-model="revisionReason"
            label="Motivo da solicitação"
            placeholder="Descreva o motivo pelo qual solicita a revisão da nota..."
            rows="4"
            :rules="[v => !!v || 'Motivo é obrigatório']"
            required
          ></v-textarea>
        </div>
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

const roleStore = useRoleStore()
const loading = ref(false)
const saving = ref(false)
const studentGrades = ref<any[]>([])
const hasChanges = ref(false)

// Revision request variables
const showRevisionDialog = ref(false)
const revisionReason = ref('')
const submittingRevision = ref(false)
const selectedGradeForRevision = ref<any>(null)

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const canGrade = computed(() => {
  return roleStore.isMainTeacher || roleStore.isTeachingAssistant
})

const gradeHeaders = computed(() => {
  const headers = [
    { title: 'Aluno', key: 'studentName', value: 'studentName' },
    { title: 'IST ID', key: 'studentIstId', value: 'studentIstId' },
    { title: 'Nota', key: 'grade', value: 'grade' },
    { title: 'Classificação', key: 'status', value: 'status' },
    { title: 'Avaliado em', key: 'gradedAt', value: 'gradedAt' }
  ]
  
  if (canGrade.value || roleStore.isStudent) {
    headers.push({ title: 'Ações', key: 'actions', value: 'actions' })
  }
  
  return headers
})

// Helper functions
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('pt-PT')
}

const formatDateTime = (dateString: string) => {
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
  return grade >= 10 ? 'Positiva' : 'Negativa'
}

const markAsChanged = (item: any) => {
  item.changed = true
  hasChanges.value = true
}

const loadGrades = async () => {
  if (!props.evaluation?.id) return
  
  loading.value = true
  try {
    const grades = await RemoteService.getEvaluationGrades(props.evaluation.id)
    studentGrades.value = grades.map(grade => ({
      id: grade.id,
      studentEnrollmentId: grade.studentEnrollmentId,
      studentId: grade.student.id,
      studentName: grade.student.name,
      studentIstId: grade.student.istId,
      grade: grade.grade,
      gradedAt: grade.gradedAt,
      revisionRequested: grade.revisionRequested || false,
      revisionReason: grade.revisionReason,
      revisionRequestedAt: grade.revisionRequestedAt,
      changed: false
    }))
  } catch (error) {
    console.error('Error loading grades:', error)
  } finally {
    loading.value = false
  }
}

const saveAllGrades = async () => {
  if (!props.evaluation?.id) return
  
  saving.value = true
  try {
    const changedGrades = studentGrades.value.filter(item => item.changed && item.grade !== null)
    
    for (const gradeItem of changedGrades) {
      await RemoteService.saveEvaluationGrade(
        props.evaluation.id, 
        gradeItem.studentEnrollmentId, 
        gradeItem.grade
      )
      gradeItem.changed = false
    }
    
    hasChanges.value = false
    // Reload grades to get updated data
    await loadGrades()
  } catch (error) {
    console.error('Error saving grades:', error)
  } finally {
    saving.value = false
  }
}

// Revision request functions
const openRevisionDialog = (item: any) => {
  selectedGradeForRevision.value = item
  showRevisionDialog.value = true
}

const cancelRevisionRequest = () => {
  showRevisionDialog.value = false
  revisionReason.value = ''
  selectedGradeForRevision.value = null
}

const submitRevisionRequest = async () => {
  if (!selectedGradeForRevision.value?.id || !revisionReason.value.trim()) return
  
  submittingRevision.value = true
  try {
    await RemoteService.requestGradeRevision(selectedGradeForRevision.value.id, revisionReason.value)
    
    // Update local state
    selectedGradeForRevision.value.revisionRequested = true
    selectedGradeForRevision.value.revisionReason = revisionReason.value
    selectedGradeForRevision.value.revisionRequestedAt = new Date().toISOString()
    
    // Close dialog and reset
    cancelRevisionRequest()
    
    // Refresh the grades to get updated data
    await loadGrades()
  } catch (error) {
    console.error('Error requesting revision:', error)
  } finally {
    submittingRevision.value = false
  }
}

const viewRevisionRequest = (item: any) => {
  // For teachers to view revision requests - show alert for now
  alert(`Pedido de revisão de ${item.studentName}:\n\nMotivo: ${item.revisionReason}\n\nSolicitado em: ${formatDateTime(item.revisionRequestedAt)}`)
}

// Watch for evaluation changes to load grades
watch(() => props.evaluation, async () => {
  if (props.evaluation && localDialog.value) {
    await loadGrades()
  }
}, { immediate: true })

// Watch for dialog opening to load grades
watch(localDialog, async (isOpen) => {
  if (isOpen && props.evaluation) {
    await loadGrades()
  }
})
</script>
