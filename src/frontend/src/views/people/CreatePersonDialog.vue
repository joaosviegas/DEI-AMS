<template>
  <div class="pa-4 text-center">
    <v-dialog v-model="dialog" max-width="400">
      <template v-slot:activator="{ props: activatorProps }">
        <v-btn
          class="text-none font-weight-regular"
          prepend-icon="mdi-plus"
          text="Adicionar Pessoa"
          v-bind="activatorProps"
          color="primary"
        ></v-btn>
      </template>

      <v-card prepend-icon="mdi-account" title="Nova">
        <v-form ref="form" v-model="isFormValid">
          <v-card-text>
            <v-text-field label="Nome*" required v-model="newPerson.name" :rules="nameRules"></v-text-field>
            <v-text-field label="IST ID*" required v-model="newPerson.istId" :rules="istIdRules"></v-text-field>
            <v-text-field label="E-mail*" required v-model="newPerson.email" :rules="emailRules"></v-text-field>

              <v-select
              :items="['Administrador', 'Professor Regente', 'Professor Assistente', 'Aluno']"
              label="Categoria*"
              required
              v-model="newPerson.type"
              :rules="typeRules"
              ></v-select>
          </v-card-text>
        </v-form>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>

          <v-btn text="Close" variant="plain" @click="dialog = false"></v-btn>

          <v-btn
            color="primary"
            text="Save"
            variant="tonal"
            :disabled="!isFormValid"
            @click="
              dialog = false,
              savePerson()
            "
          ></v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type PersonDto from '@/models/people/PersonDto'
import RemoteService from '@/services/RemoteService'

const dialog = ref(false)

const emit = defineEmits(['person-created'])

const isFormValid = ref(false)

// Validation rules
const nameRules = [(v: string) => !!v || 'Nome é obrigatório']
const istIdRules = [(v: string) => !!v|| 'IST ID é obrigatório',
  (v: string) => /^\d+$/.test(v) || 'IST ID deve conter apenas números'
]
const typeRules = [(v: string) => !!v || 'Categoria é obrigatória']

const emailRules = [
  (value: string) => !!value || 'E-mail é obrigatório',
  (value: string) => {
    const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return pattern.test(value) || 'E-mail deve ser válido'
  }
]

const typeMappings = {
  'Administrador': 'ADMINISTRATOR',
  'Professor Regente': 'MAIN_TEACHER',
  'Professor Assistente': 'TEACHING_ASSISTANT',
  'Aluno': 'STUDENT'
}

const newPerson = ref<PersonDto>({
  name: '',
  type: ''
})

const savePerson = async () => {
  newPerson.value.type = typeMappings[newPerson.value.type as keyof typeof typeMappings]
  await RemoteService.createPerson(newPerson.value)
  newPerson.value = {
    name: '',
    type: ''
  }
  emit('person-created')
}
</script>
