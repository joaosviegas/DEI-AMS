<template>
  <div class="pa-4 text-center">
    <v-dialog v-model="dialog" max-width="400">
      <template v-slot:activator="{ props: activatorProps }">
        <v-btn
          class="text-none font-weight-regular"
          prepend-icon="mdi-plus"
          text="Adicionar Curso"
          v-bind="activatorProps"
          color="primary"
        ></v-btn>
      </template>

      <v-card prepend-icon="mdi-book" title="Adicionar Curso">
        <v-form ref="form" v-model="isFormValid">
          <v-card-text>
            <v-text-field 
              label="Código*" 
              required 
              v-model="newCourse.code" 
              :rules="codeRules"
              placeholder="LEIC-A"
            ></v-text-field>
            <v-text-field 
              label="Nome*" 
              required 
              v-model="newCourse.name" 
              :rules="nameRules"
              placeholder="Licenciatura em Engenharia Informática e de Computadores"
            ></v-text-field>
            <v-select
              :items="[2, 3, 5]"
              label="Duração (em anos)*"
              required
              v-model="newCourse.duration"
              :rules="durationRules"
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
            @click="saveCourse"
          ></v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CourseDto from '../../models/CourseDto'
import RemoteService from '../../services/RemoteService'

const dialog = ref(false)
const form = ref()

const emit = defineEmits(['course-created'])

const isFormValid = ref(false)

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

const newCourse = ref<CourseDto>({
  code: '',
  name: '',
  duration: 0
})

const resetForm = () => {
  newCourse.value = {
    code: '',
    name: '',
    duration: 0
  }
  if (form.value) {
    form.value.reset()
  }
}

const saveCourse = async () => {
  try {
    await RemoteService.createCourse(newCourse.value)

    resetForm()
    dialog.value = false
    emit('course-created')
  } catch (error) {
    console.error('Error creating course:', error)
  }
}
</script>