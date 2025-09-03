<template>
  <v-dialog v-model="localDialog" max-width="1200">
    <v-card v-if="curricularUnit">
      <v-card-title class="d-flex justify-space-between align-center">
        <span class="text-h5">{{ curricularUnit.name }}</span>
        <v-btn 
          icon="mdi-close" 
          variant="text" 
          size="small"
          @click="localDialog = false"
        ></v-btn>
      </v-card-title>

      <v-card-text>
        <v-row>
          <v-col cols="6">
            <strong>Código:</strong> {{ curricularUnit.code }}
          </v-col>
          <v-col cols="6">
            <strong>ECTS:</strong> {{ curricularUnit.ects }}
          </v-col>
        </v-row>
        <v-row>
          <v-col cols="6">
            <strong>Semestre:</strong> {{ getSemesterText(curricularUnit.semester) }}
          </v-col>
          <v-col cols="6">
            <strong>Professor Regente:</strong> {{ curricularUnit.mainTeacher.name }}
          </v-col>
        </v-row>
        <v-row>
          <v-col cols="6">
            <strong>Cursos:</strong>
            <div class="mt-2">
              <v-chip 
                v-for="course in curricularUnit.courses" 
                :key="course.id" 
                size="small" 
                class="mr-1 mb-1"
              >
                {{ course.code }}
              </v-chip>
            </div>
          </v-col>
        </v-row>

        <v-divider class="my-4"></v-divider>

        <v-tabs v-model="activeTab" class="mt-6">
          <v-tab>Professores</v-tab>
          <v-tab>Alunos</v-tab>
        </v-tabs>

        <v-tabs-window v-model="activeTab">
          <!-- Teachers Tab -->
          <v-tabs-window-item>
            <v-data-table
              :headers="teacherHeaders"
              :items="allTeachers"
              class="mt-4"
              no-data-text="Sem professores a apresentar."
            >
              <template v-slot:[`item.type`]="{ item }">
                <v-chip 
                  :color="item.type === 'Regente' ? 'red' : 'blue'" 
                  size="small"
                >
                  {{ item.type }}
                </v-chip>
              </template>
            </v-data-table>
          </v-tabs-window-item>

          <!-- Students Tab -->
          <v-tabs-window-item>
            <v-data-table
              :headers="studentHeaders"
              :items="allStudents"
              class="mt-4"
              no-data-text="Sem alunos a apresentar."
            >
              <template v-slot:[`item.status`]="{ item }">
              </template>
              <!-- TODO: Status will be implemented later when backend supports it -->
            </v-data-table>
          </v-tabs-window-item>
        </v-tabs-window>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import CurricularUnitDto from '../../models/CurricularUnitDto'

const emit = defineEmits(['update:modelValue'])

const props = defineProps({
  curricularUnit: {
    type: Object as () => CurricularUnitDto | undefined,
    default: undefined
  },
  modelValue: {
    type: Boolean,
    default: false
  }
})

const activeTab = ref(0)

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const teacherHeaders = [
  { title: 'Nome', key: 'name', value: 'name' },
  { title: 'IST ID', key: 'istId', value: 'istId' },
  { title: 'Email', key: 'email', value: 'email' },
  { title: 'Tipo', key: 'type', value: 'type' }
]

const studentHeaders = [
  { title: 'Nome', key: 'name', value: 'name' },
  { title: 'IST ID', key: 'istId', value: 'istId' },
  { title: 'Email', key: 'email', value: 'email' }
  // TODO: Status will be added later when backend supports it
  // { title: 'Estado', key: 'status', value: 'status' }
]

const allTeachers = computed(() => {
  if (!props.curricularUnit) return []
  
  const teachers = []
  
  // Add main teacher
  teachers.push({
    ...props.curricularUnit.mainTeacher,
    type: 'Regente'
  })
  
  // Add assistant teachers
  props.curricularUnit.assistantTeachers.forEach(teacher => {
    teachers.push({
      ...teacher,
      type: 'Assistente'
    })
  })
  
  return teachers
})

const allStudents = computed(() => {
  if (!props.curricularUnit) return []
  return props.curricularUnit.students || []
})

// Helper functions for display
const getSemesterText = (semester: string) => {
  const semesterMap: { [key: string]: string } = {
    'FIRST': '1º Semestre',
    'SECOND': '2º Semestre'
  }
  return semesterMap[semester] || semester
}
</script>
