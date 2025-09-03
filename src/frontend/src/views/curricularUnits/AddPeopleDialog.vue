<template>
  <v-dialog v-model="localDialog" max-width="700">
    <v-card prepend-icon="mdi-account-plus" title="Adicionar Pessoas">
      <v-card-text>
        <v-row>
          <v-col cols="12">
            <h3 class="mb-4">{{ curricularUnit?.name }} ({{ curricularUnit?.code }})</h3>
          </v-col>
        </v-row>

        <v-tabs v-model="activeTab" class="mb-4">
          <v-tab>Professores Assistentes</v-tab>
          <v-tab>Alunos</v-tab>
        </v-tabs>

        <v-tabs-window v-model="activeTab">
          <!-- Assistant Teachers Tab -->
          <v-tabs-window-item>
            <v-row>
              <v-col cols="12">
                <v-select
                  :items="availableTeachers"
                  item-title="text"
                  item-value="value"
                  label="Selecionar Professor Assistente"
                  v-model="selectedTeacher"
                  :loading="loadingTeachers"
                  clearable
                  placeholder="Escolha um professor para adicionar"
                ></v-select>
              </v-col>
              <v-col cols="12">
                <v-btn 
                  color="primary" 
                  :disabled="!selectedTeacher"
                  @click="addAssistantTeacher"
                  prepend-icon="mdi-plus"
                >
                  Adicionar Professor
                </v-btn>
              </v-col>
            </v-row>

            <!-- Current Assistant Teachers -->
            <v-divider class="my-4"></v-divider>
            <h4 class="mb-2">Professores Assistentes Atuais</h4>
            <v-list v-if="curricularUnit?.assistantTeachers.length">
              <v-list-item 
                v-for="teacher in curricularUnit.assistantTeachers" 
                :key="teacher.id"
              >
                <template v-slot:prepend>
                  <v-avatar color="blue">
                    <v-icon>mdi-account</v-icon>
                  </v-avatar>
                </template>
                <v-list-item-title>{{ teacher.name }}</v-list-item-title>
                <v-list-item-subtitle>{{ teacher.istId }} - {{ teacher.email }}</v-list-item-subtitle>
                <template v-slot:append>
                  <v-btn 
                    icon="mdi-delete" 
                    variant="text" 
                    color="red"
                    @click="removeAssistantTeacher(teacher.id!)"
                  ></v-btn>
                </template>
              </v-list-item>
            </v-list>
            <p v-else class="text-grey">Nenhum professor assistente adicionado.</p>
          </v-tabs-window-item>

          <!-- Students Tab -->
          <v-tabs-window-item>
            <v-row>
              <v-col cols="12">
                <v-select
                  :items="availableStudents"
                  item-title="text"
                  item-value="value"
                  label="Selecionar Aluno"
                  v-model="selectedStudent"
                  :loading="loadingStudents"
                  clearable
                  placeholder="Escolha um aluno para adicionar"
                ></v-select>
              </v-col>
              <v-col cols="12">
                <v-btn 
                  color="primary" 
                  :disabled="!selectedStudent"
                  @click="addStudent"
                  prepend-icon="mdi-plus"
                >
                  Adicionar Aluno
                </v-btn>
              </v-col>
            </v-row>

            <!-- Current Students -->
            <v-divider class="my-4"></v-divider>
            <h4 class="mb-2">Alunos Atuais</h4>
            <v-list v-if="curricularUnit?.studentEnrollments?.length">
              <v-list-item 
                v-for="enrollment in curricularUnit.studentEnrollments" 
                :key="enrollment.id"
              >
                <template v-slot:prepend>
                  <v-avatar :color="getStatusColor(enrollment.status)">
                    <v-icon>mdi-account-school</v-icon>
                  </v-avatar>
                </template>
                <v-list-item-title>{{ enrollment.student?.name }}</v-list-item-title>
                <v-list-item-subtitle>
                  {{ enrollment.student?.istId }} - {{ enrollment.student?.email }}
                  <v-chip size="small" :color="getStatusColor(enrollment.status)" class="ml-2">
                    {{ enrollment.status }}
                  </v-chip>
                </v-list-item-subtitle>
                <template v-slot:append>
                  <v-btn 
                    icon="mdi-delete" 
                    variant="text" 
                    color="red"
                    @click="removeStudent(enrollment.student?.id!)"
                  ></v-btn>
                </template>
              </v-list-item>
            </v-list>
            <p v-else class="text-grey">Nenhum aluno adicionado.</p>
          </v-tabs-window-item>
        </v-tabs-window>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Fechar" variant="plain" @click="localDialog = false"></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import CurricularUnitDto from '../../models/CurricularUnitDto'
import PersonDto from '../../models/PersonDto'
import StudentEnrollmentDto from '../../models/StudentEnrollmentDto'
import RemoteService from '../../services/RemoteService'

const emit = defineEmits(['people-updated', 'update:modelValue', 'curricular-unit-updated'])

const props = defineProps({
  curricularUnit: {
    type: Object as () => CurricularUnitDto,
    required: true
  },
  modelValue: {
    type: Boolean,
    default: false
  }
})

const activeTab = ref(0)
const loadingTeachers = ref(false)
const loadingStudents = ref(false)
const selectedTeacher = ref<number | null>(null)
const selectedStudent = ref<number | null>(null)

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const availableTeachers = ref<{ text: string, value: number }[]>([])
const availableStudents = ref<{ text: string, value: number }[]>([])

// Helper function to get status color
const getStatusColor = (status: string) => {
  switch (status) {
    case 'ENROLLED': return 'blue'
    case 'APPROVED': return 'green'
    case 'FAILED': return 'red'
    default: return 'grey'
  }
}

onMounted(async () => {
  await loadAvailableTeachers()
  await loadAvailableStudents()
})

const loadAvailableTeachers = async () => {
  loadingTeachers.value = true
  try {
    const people = await RemoteService.getPeople()
    const teachers = people.filter((person: PersonDto) => person.type === 'TEACHING_ASSISTANT')
    
    // Filter out main teacher and current assistant teachers
    const currentTeacherIds = new Set([
      props.curricularUnit?.mainTeacher.id,
      ...props.curricularUnit?.assistantTeachers.map(t => t.id) || []
    ])
    
    availableTeachers.value = teachers
      .filter(teacher => !currentTeacherIds.has(teacher.id))
      .map((teacher: PersonDto) => ({
        text: `${teacher.name} (${teacher.istId})`,
        value: teacher.id!
      }))
  } catch (error) {
    console.error('Error loading teachers:', error)
  } finally {
    loadingTeachers.value = false
  }
}

const loadAvailableStudents = async () => {
  loadingStudents.value = true
  try {
    const people = await RemoteService.getPeople()
    const students = people.filter((person: PersonDto) => person.type === 'STUDENT')
    
    // Debug the curricular unit data
    console.log('Current curricularUnit prop:', props.curricularUnit)
    console.log('StudentEnrollments from prop:', props.curricularUnit?.studentEnrollments)
    console.log('StudentEnrollments length:', props.curricularUnit?.studentEnrollments?.length)
    
    // Filter out current students (from enrollments)
    const currentStudentIds = new Set(
      props.curricularUnit?.studentEnrollments
        ?.map(enrollment => {
          console.log('Processing enrollment:', enrollment)
          return enrollment.student?.id
        })
        ?.filter(id => id !== undefined) || []
    )
    
    console.log('Current enrolled student IDs:', Array.from(currentStudentIds))
    console.log('Total students found:', students.length)
    
    availableStudents.value = students
      .filter(student => {
        const isAlreadyEnrolled = currentStudentIds.has(student.id)
        console.log(`Student ${student.name} (${student.id}): already enrolled = ${isAlreadyEnrolled}`)
        return !isAlreadyEnrolled
      })
      .map((student: PersonDto) => ({
        text: `${student.name} (${student.istId})`,
        value: student.id!
      }))
      
    console.log('Available students after filtering:', availableStudents.value.length)
  } catch (error) {
    console.error('Error loading students:', error)
  } finally {
    loadingStudents.value = false
  }
}

const addAssistantTeacher = async () => {
  if (!selectedTeacher.value || !props.curricularUnit?.id) return

  try {
    const updatedCurricularUnit = await RemoteService.addAssistantTeacher(props.curricularUnit.id, selectedTeacher.value)
    selectedTeacher.value = null
    
    // Emit the updated curricular unit to parent
    emit('curricular-unit-updated', updatedCurricularUnit)
    emit('people-updated')
    await loadAvailableTeachers() // Refresh available teachers
  } catch (error) {
    console.error('Error adding assistant teacher:', error)
  }
}

const removeAssistantTeacher = async (teacherId: number) => {
  if (!props.curricularUnit?.id) return

  try {
    const updatedCurricularUnit = await RemoteService.removeAssistantTeacher(props.curricularUnit.id, teacherId)
    
    // Emit the updated curricular unit to parent
    emit('curricular-unit-updated', updatedCurricularUnit)
    emit('people-updated')
    await loadAvailableTeachers() // Refresh available teachers
  } catch (error) {
    console.error('Error removing assistant teacher:', error)
  }
}

const addStudent = async () => {
  if (!selectedStudent.value || !props.curricularUnit?.id) return

  try {

    
    // Enroll the student
    const enrollmentResult = await RemoteService.enrollStudent(props.curricularUnit.id, selectedStudent.value)
    
    // Get the updated curricular unit
    const updatedCurricularUnit = await RemoteService.getCurricularUnit(props.curricularUnit.id)
    
    // Check each enrollment individually
    if (updatedCurricularUnit.studentEnrollments?.length) {
      updatedCurricularUnit.studentEnrollments.forEach((enrollment, index) => {
      })
    }
    
    selectedStudent.value = null
    
    // Emit the updated curricular unit to parent
    emit('curricular-unit-updated', updatedCurricularUnit)
    emit('people-updated')
    
    // Small delay to ensure parent has updated, then refresh
    setTimeout(async () => {
      await loadAvailableStudents()
    }, 200)
    
  } catch (error) {
    console.error('Error adding student:', error)
  }
}

const removeStudent = async (studentId: number) => {
  if (!props.curricularUnit?.id) return

  try {
    console.log('Removing student:', studentId, 'from curricular unit:', props.curricularUnit.id)
    
    // Unenroll the student and get the updated curricular unit
    await RemoteService.unenrollStudent(props.curricularUnit.id, studentId)
    const updatedCurricularUnit = await RemoteService.getCurricularUnit(props.curricularUnit.id)
    
    // Emit the updated curricular unit to parent
    emit('curricular-unit-updated', updatedCurricularUnit)
    emit('people-updated')
    
    // Force refresh of available students
    await loadAvailableStudents()
  } catch (error) {
  }
}

// Watch for curricular unit changes to refresh available people
watch(() => props.curricularUnit, async () => {
  if (props.curricularUnit) {
    await loadAvailableTeachers()
    await loadAvailableStudents()
  }
}, { deep: true })

// Watch specifically for changes in student enrollments
watch(() => props.curricularUnit?.studentEnrollments, async () => {
  if (props.curricularUnit) {
    await loadAvailableStudents() // Refresh available students when enrollments change
  }
}, { deep: true })

// Watch specifically for changes in assistant teachers
watch(() => props.curricularUnit?.assistantTeachers, async () => {
  if (props.curricularUnit) {
    await loadAvailableTeachers() // Refresh available teachers when assistant teachers change
  }
}, { deep: true })

// Reset selections when dialog opens
watch(localDialog, (newVal) => {
  if (newVal) {
    selectedTeacher.value = null
    selectedStudent.value = null
    activeTab.value = 0
  }
})
</script>
