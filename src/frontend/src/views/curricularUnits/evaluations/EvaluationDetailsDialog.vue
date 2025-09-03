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
            <v-btn 
              v-if="item.revisionRequested && canGrade"
              icon="mdi-eye"
              variant="text"
              size="small"
              @click="viewRevisionRequest(item)"
              title="Ver pedido de revisão"
            ></v-btn>
            <v-btn 
              v-if="!canGrade && item.grade !== null"
              icon="mdi-clipboard-edit"
              variant="text"
              size="small"
              @click="requestRevision(item)"
              title="Solicitar revisão"
            ></v-btn>
          </template>
        </v-data-table>
      </v-card-text>
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
    { title: 'Estado', key: 'status', value: 'status' },
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
  return grade >= 10 ? 'Aprovado' : 'Reprovado'
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

const viewRevisionRequest = (item: any) => {
  // TODO: Implement revision request dialog
  console.log('View revision request for:', item)
}

const requestRevision = (item: any) => {
  // TODO: Implement revision request dialog
  console.log('Request revision for:', item)
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
