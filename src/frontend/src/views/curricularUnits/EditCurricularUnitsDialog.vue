<template>
  <v-dialog v-model="localDialog" max-width="600">
    <v-card prepend-icon="mdi-book-edit" title="Editar Unidade Curricular">
      <v-form ref="form" v-model="isFormValid">
        <v-card-text>
          <v-text-field 
            label="Código*" 
            required 
            v-model="editCurricularUnit.code" 
            :rules="codeRules"
            placeholder="IAC"
          ></v-text-field>
          <v-text-field 
            label="Nome*" 
            required 
            v-model="editCurricularUnit.name" 
            :rules="nameRules"
            placeholder="Introdução à Arquitetura de Computadores"
          ></v-text-field>
          <v-select
            :items="semesterOptions"
            item-title="text"
            item-value="value"
            label="Semestre*"
            required
            v-model="editCurricularUnit.semester"
            :rules="semesterRules"
          ></v-select>
          <v-text-field 
            label="ECTS*" 
            required 
            v-model="editCurricularUnit.ects" 
            type="number"
            min="1"
            max="30"
            :rules="ectsRules"
          ></v-text-field>
          <v-select
            :items="teacherOptions"
            item-title="text"
            item-value="value"
            label="Professor Regente*"
            required
            v-model="editCurricularUnit.mainTeacherId"
            :rules="mainTeacherRules"
            :loading="loadingTeachers"
          ></v-select>
          <v-select
            :items="courseOptions"
            item-title="text"
            item-value="value"
            label="Cursos*"
            required
            multiple
            v-model="editCurricularUnit.courseIds"
            :rules="coursesRules"
            :loading="loadingCourses"
          ></v-select>
        </v-card-text>
      </v-form>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>

        <v-btn text="Cancelar" variant="plain" @click="localDialog = false"></v-btn>

        <v-btn
          color="primary"
          text="Atualizar"
          variant="tonal"
          :disabled="!canUpdate"
          @click="updateCurricularUnit"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import CurricularUnitDto from '../../models/CurricularUnitDto'
import PersonDto from '../../models/PersonDto'
import CourseDto from '../../models/CourseDto'
import RemoteService from '../../services/RemoteService'

const emit = defineEmits(['curricular-unit-updated', 'update:modelValue'])

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

const form = ref()
const isFormValid = ref(false)
const loadingTeachers = ref(false)
const loadingCourses = ref(false)

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// Store the original curricular unit data to compare
const originalCurricularUnit = ref<any>({
  code: '',
  name: '',
  semester: '',
  ects: 0,
  mainTeacherId: null,
  courseIds: []
})

const editCurricularUnit = ref<any>({
  code: '',
  name: '',
  semester: '',
  ects: 0,
  mainTeacherId: null,
  courseIds: []
})

// Options for selects
const semesterOptions = [
  { text: '1º Semestre', value: 'FIRST' },
  { text: '2º Semestre', value: 'SECOND' },
  { text: 'Anual', value: 'ANNUAL' }
]

const teacherOptions = ref<{ text: string, value: number }[]>([])
const courseOptions = ref<{ text: string, value: number }[]>([])

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

const semesterRules = [
  (v: string) => !!v || 'Semestre é obrigatório'
]

const ectsRules = [
  (v: number) => !!v || 'ECTS é obrigatório',
  (v: number) => v >= 1 && v <= 30 || 'ECTS deve estar entre 1 e 30'
]

const mainTeacherRules = [
  (v: number) => !!v || 'Professor regente é obrigatório'
]

const coursesRules = [
  (v: number[]) => v && v.length > 0 || 'Pelo menos um curso é obrigatório'
]

// Check if there's any changes
const hasChanges = computed(() => {
  return (
    editCurricularUnit.value.code !== originalCurricularUnit.value.code ||
    editCurricularUnit.value.name !== originalCurricularUnit.value.name ||
    editCurricularUnit.value.semester !== originalCurricularUnit.value.semester ||
    editCurricularUnit.value.ects !== originalCurricularUnit.value.ects ||
    editCurricularUnit.value.mainTeacherId !== originalCurricularUnit.value.mainTeacherId ||
    JSON.stringify(editCurricularUnit.value.courseIds.sort()) !== JSON.stringify(originalCurricularUnit.value.courseIds.sort())
  )
})

const canUpdate = computed(() => {
  return isFormValid.value && hasChanges.value
})

// Load data for selects
onMounted(async () => {
  await loadTeachers()
  await loadCourses()
})

const loadTeachers = async () => {
  loadingTeachers.value = true
  try {
    const people = await RemoteService.getPeople()
    teacherOptions.value = people
      .filter((person: PersonDto) => person.type === 'MAIN_TEACHER')
      .map((person: PersonDto) => ({
        text: `${person.name} (${person.istId})`,
        value: person.id!
      }))
  } catch (error) {
    console.error('Error loading teachers:', error)
  } finally {
    loadingTeachers.value = false
  }
}

const loadCourses = async () => {
  loadingCourses.value = true
  try {
    const courses = await RemoteService.getCourses()
    courseOptions.value = courses.map((course: CourseDto) => ({
      text: `${course.code} - ${course.name}`,
      value: course.id!
    }))
  } catch (error) {
    console.error('Error loading courses:', error)
  } finally {
    loadingCourses.value = false
  }
}

// Watch for prop changes and update editCurricularUnit
watch(() => props.curricularUnit, (newCurricularUnit) => {
  if (newCurricularUnit) {
    const courseIds = newCurricularUnit.courses.map(course => course.id!)
    
    editCurricularUnit.value = {
      code: newCurricularUnit.code,
      name: newCurricularUnit.name,
      semester: newCurricularUnit.semester,
      ects: newCurricularUnit.ects,
      mainTeacherId: newCurricularUnit.mainTeacher.id,
      courseIds: courseIds
    }
    
    // Store original data for comparison
    originalCurricularUnit.value = {
      code: newCurricularUnit.code,
      name: newCurricularUnit.name,
      semester: newCurricularUnit.semester,
      ects: newCurricularUnit.ects,
      mainTeacherId: newCurricularUnit.mainTeacher.id,
      courseIds: courseIds
    }
  }
}, { immediate: true })

// Reset form when dialog opens
watch(localDialog, (newVal) => {
  if (newVal && props.curricularUnit) {
    const courseIds = props.curricularUnit.courses.map(course => course.id!)
    
    editCurricularUnit.value = {
      code: props.curricularUnit.code,
      name: props.curricularUnit.name,
      semester: props.curricularUnit.semester,
      ects: props.curricularUnit.ects,
      mainTeacherId: props.curricularUnit.mainTeacher.id,
      courseIds: courseIds
    }
    
    originalCurricularUnit.value = {
      code: props.curricularUnit.code,
      name: props.curricularUnit.name,
      semester: props.curricularUnit.semester,
      ects: props.curricularUnit.ects,
      mainTeacherId: props.curricularUnit.mainTeacher.id,
      courseIds: courseIds
    }
  }
})

const updateCurricularUnit = async () => {
  if (!props.curricularUnit?.id || !canUpdate.value) return

  try {
    // First update the basic properties
    const updatedUnit = await RemoteService.updateCurricularUnit(props.curricularUnit.id, {
      code: editCurricularUnit.value.code,
      name: editCurricularUnit.value.name,
      semester: editCurricularUnit.value.semester,
      ects: editCurricularUnit.value.ects.toString(),
      mainTeacherId: editCurricularUnit.value.mainTeacherId.toString()
    })

    // Handle course changes
    const currentCourseIds = props.curricularUnit.courses?.map(c => c.id!) || []
    const newCourseIds = editCurricularUnit.value.courseIds

    // Remove courses that are no longer selected
    for (const courseId of currentCourseIds) {
      if (!newCourseIds.includes(courseId)) {
        await RemoteService.removeCourseFromCurricularUnit(props.curricularUnit.id, courseId)
      }
    }

    // Add newly selected courses
    for (const courseId of newCourseIds) {
      if (!currentCourseIds.includes(courseId)) {
        await RemoteService.addCourseToCurricularUnit(props.curricularUnit.id, courseId)
      }
    }
    
    localDialog.value = false
    emit('curricular-unit-updated')
  } catch (error) {
    console.error('Error updating curricular unit:', error)
  }
}
</script>