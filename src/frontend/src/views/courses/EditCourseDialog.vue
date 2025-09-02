<template>
  <v-dialog v-model="localDialog" max-width="400">
    <v-card prepend-icon="mdi-book-edit" title="Editar Curso">
      <v-form ref="form" v-model="isFormValid">
        <v-card-text>
          <v-text-field 
            label="Código*" 
            required 
            v-model="editCourse.code" 
            :rules="codeRules"
            placeholder="LEIC-A"
          ></v-text-field>
          <v-text-field 
            label="Nome*" 
            required 
            v-model="editCourse.name" 
            :rules="nameRules"
            placeholder="Licenciatura em Engenharia Informática e de Computadores"
          ></v-text-field>
          <v-select
            :items="[2, 3, 5]"
            label="Duração (em anos)*"
            required
            v-model="editCourse.duration"
            :rules="durationRules"
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
          @click="updateCourse"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import CourseDto from '../../models/CourseDto'
import RemoteService from '../../services/RemoteService'

const emit = defineEmits(['course-updated', 'update:modelValue'])

const props = defineProps({
  course: {
    type: Object as () => CourseDto,
    required: true
  },
  modelValue: {
    type: Boolean,
    default: false
  }
})

const form = ref()
const isFormValid = ref(false)

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// Store the original course data to compare
const originalCourse = ref<CourseDto>({
  code: '',
  name: '',
  duration: 0
})

// Validation rules
const codeRules = [
  (v: string) => !!v || 'Código é obrigatório',
  (v: string) => v.length >= 3 && v.length <= 10 || 'Código deve ter entre 3 e 10 caracteres',
  (v: string) => /^[A-Z0-9-]+$/i.test(v) || 'Código deve conter apenas letras, números e "-"'
]

const nameRules = [
  (v: string) => !!v || 'Nome é obrigatório',
  (v: string) => v.length >= 3 && v.length <= 100 || 'Nome deve ter entre 3 e 100 caracteres'
]

const durationRules = [(v: number) => !!v || 'Duração é obrigatória']

const editCourse = ref<CourseDto>({
  code: '',
  name: '',
  duration: 0
})

// Check if there's any changes
const hasChanges = computed(() => {
  return (
    editCourse.value.code !== originalCourse.value.code ||
    editCourse.value.name !== originalCourse.value.name ||
    editCourse.value.duration !== originalCourse.value.duration
  )
})

const canUpdate = computed(() => {
  return isFormValid.value && hasChanges.value
})

// Watch for prop changes and update editCourse
watch(() => props.course, (newCourse) => {
  if (newCourse) {
    editCourse.value = {
      ...newCourse
    }
    
    // Store original data for comparison
    originalCourse.value = {
      ...newCourse
    }
  }
}, { immediate: true })

// Reset form when dialog opens
watch(localDialog, (newVal) => {
  if (newVal && props.course) {
    editCourse.value = {
      ...props.course
    }
    
    originalCourse.value = {
      ...props.course
    }
  }
})

const updateCourse = async () => {
  if (!props.course?.id || !canUpdate.value) return

  try {
    await RemoteService.updateCourse(props.course.id, editCourse.value)
    
    localDialog.value = false
    emit('course-updated')
  } catch (error) {
    console.error('Error updating course:', error)
  }
}
</script>