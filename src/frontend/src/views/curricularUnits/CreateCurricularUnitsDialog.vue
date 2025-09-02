<template>
  <div class="pa-4 text-center">
    <v-dialog v-model="dialog" max-width="500">
      <template v-slot:activator="{ props: activatorProps }">
        <v-btn
          class="text-none font-weight-regular"
          prepend-icon="mdi-plus"
          text="Adicionar Unidade Curricular"
          v-bind="activatorProps"
          color="primary"
        ></v-btn>
      </template>

      <v-card prepend-icon="mdi-school" title="Adicionar Unidade Curricular">
        <v-form ref="form" v-model="isFormValid">
          <v-card-text>
            <v-text-field 
              label="Código*" 
              required 
              v-model="newCurricularUnit.code" 
              :rules="codeRules"
              placeholder="IAC"
            ></v-text-field>
            <v-text-field 
              label="Nome*" 
              required 
              v-model="newCurricularUnit.name" 
              :rules="nameRules"
              placeholder="Introdução à Arquitetura de Computadores"
            ></v-text-field>
            <v-select
              :items="semesterOptions"
              label="Semestre*"
              required
              v-model="newCurricularUnit.semester"
              :rules="semesterRules"
            ></v-select>
            <v-text-field 
              label="ECTS*" 
              required 
              v-model="newCurricularUnit.ects" 
              :rules="ectsRules"
              type="number"
              min="1"
              max="30"
            ></v-text-field>
            <v-select
              :items="mainTeachers"
              label="Professor Regente*"
              required
              v-model="newCurricularUnit.mainTeacherId"
              :rules="mainTeacherRules"
              item-title="name"
              item-value="id"
            ></v-select>
            <v-select
              :items="courseOptions"
              item-title="text"
              item-value="value"
              label="Cursos"
              multiple
              v-model="newCurricularUnit.courseIds"
              :rules="coursesRules"
              :loading="loadingCourses"
              chips
              closable-chips
            ></v-select>
          </v-card-text>
        </v-form>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>

          <v-btn text="Cancelar" variant="plain" @click="dialog = false"></v-btn>

          <v-btn
            color="primary"
            text="Guardar"
            variant="tonal"
            :disabled="!isFormValid"
            @click="saveCurricularUnit"
          ></v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import RemoteService from '../../services/RemoteService'
import PersonDto from '../../models/PersonDto'
import CourseDto from '../../models/CourseDto'

const dialog = ref(false)
const form = ref()

const emit = defineEmits(['curricular-unit-created'])

const isFormValid = ref(false)
const loadingCourses = ref(false)

// Form data
const newCurricularUnit = ref({
  code: '',
  name: '',
  semester: '',
  ects: 0,
  mainTeacherId: null as number | null,
  courseIds: [] as number[]
})

// Data for dropdowns
const mainTeachers = ref<PersonDto[]>([])
const courseOptions = ref<{ text: string, value: number }[]>([])

const semesterOptions = [
  { title: '1º Semestre', value: 'FIRST' },
  { title: '2º Semestre', value: 'SECOND' },
  { title: 'Anual', value: 'ANNUAL' }
]

// Validation rules
const codeRules = [
  (v: string) => !!v || 'Código é obrigatório',
  (v: string) => v.length >= 2 && v.length <= 10 || 'Código deve ter entre 2 e 10 caracteres',
  (v: string) => /^[A-Z0-9-]+$/i.test(v) || 'Código deve conter apenas letras, números e "-"'
]

const nameRules = [
  (v: string) => !!v || 'Nome é obrigatório',
  (v: string) => v.length >= 3 && v.length <= 100 || 'Nome deve ter entre 3 e 100 caracteres'
]

const semesterRules = [(v: string) => !!v || 'Semestre é obrigatório']

const ectsRules = [
  (v: number) => !!v || 'ECTS é obrigatório',
  (v: number) => v >= 1 && v <= 30 || 'ECTS deve estar entre 1 e 30'
]

const mainTeacherRules = [(v: number) => !!v || 'Professor Regente é obrigatório']

const coursesRules = [
  (v: number[]) => v && v.length > 0 || 'Pelo menos um curso é obrigatório'
]

// Load teachers and courses on mount
onMounted(async () => {
  await loadTeachers()
  await loadCourses()
})

const loadTeachers = async () => {
  try {
    const people = await RemoteService.getPeople()
    mainTeachers.value = people.filter(person => 
      person.type === 'MAIN_TEACHER' || person.type === 'ADMINISTRATOR'
    )
  } catch (error) {
    console.error('Error loading teachers:', error)
  }
}

const loadCourses = async () => {
  loadingCourses.value = true
  try {
    const courses = await RemoteService.getCourses()
    courseOptions.value = courses.map(course => ({
      text: `${course.code} - ${course.name}`,
      value: course.id!
    }))
  } catch (error) {
    console.error('Error loading courses:', error)
  } finally {
    loadingCourses.value = false
  }
}

const resetForm = () => {
  newCurricularUnit.value = {
    code: '',
    name: '',
    semester: '',
    ects: 0,
    mainTeacherId: null,
    courseIds: []
  }
  if (form.value) {
    form.value.reset()
  }
}

const saveCurricularUnit = async () => {
  try {
    // First create the curricular unit without courses
    const createdUnit = await RemoteService.createCurricularUnit({
      code: newCurricularUnit.value.code,
      name: newCurricularUnit.value.name,
      semester: newCurricularUnit.value.semester,
      ects: newCurricularUnit.value.ects.toString(),
      mainTeacherId: newCurricularUnit.value.mainTeacherId?.toString()
    })

    // Then add each selected course
    for (const courseId of newCurricularUnit.value.courseIds) {
      await RemoteService.addCourseToCurricularUnit(createdUnit.id!, courseId)
    }

    resetForm()
    dialog.value = false
    emit('curricular-unit-created')
  } catch (error) {
    console.error('Error creating curricular unit:', error)
  }
}
</script>