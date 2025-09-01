<template>
  <v-dialog v-model="localDialog" max-width="400">
    <v-card prepend-icon="mdi-account-edit" title="Editar Pessoa">
      <v-form ref="form" v-model="isFormValid">
        <v-card-text>
          <v-text-field label="Nome*" required v-model="editPerson.name" :rules="nameRules"></v-text-field>
          <v-text-field label="IST ID*" required v-model="editPerson.istId" :rules="istIdRules" placeholder="ist123456"></v-text-field>
          <v-text-field label="E-mail*" required v-model="editPerson.email" :rules="emailRules"></v-text-field>

          <v-select
            :items="['Administrador', 'Professor Regente', 'Professor Assistente', 'Aluno']"
            label="Categoria*"
            required
            v-model="editPerson.type"
            :rules="typeRules"
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
          @click="updatePerson"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type PersonDto from '@/models/people/PersonDto'
import RemoteService from '@/services/RemoteService'

const emit = defineEmits(['person-updated', 'update:modelValue'])

const props = defineProps({
  person: {
    type: Object as () => PersonDto,
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

// Store the original person data to compare
const originalPerson = ref<PersonDto>({
  name: '',
  istId: '',
  email: '',
  type: ''
})

// Validation rules
const nameRules = [(v: string) => !!v || 'Nome é obrigatório']
const istIdRules = [
  (v: string) => !!v || 'IST ID é obrigatório',
  (v: string) => /^ist/i.test(v) || 'IST ID deve começar com "ist"',
  (v: string) => /^ist\d+$/i.test(v) || 'IST ID deve conter apenas números após "ist"',
  (v: string) => v.length >= 4 && v.length <= 10 || 'IST ID deve ter entre 4 e 9 caracteres (ist + 1-7 dígitos)',
  (v: string) => {
    const match = v.match(/^ist(\d+)$/i)
    if (!match) return true // Other rules will catch format issues
    const number = parseInt(match[1])
    return (number >= 1 && number <= 9999999) || 'Número deve estar entre 1 e 9999999'
  }
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

const reverseTypeMappings = {
  'ADMINISTRATOR': 'Administrador',
  'MAIN_TEACHER': 'Professor Regente',
  'TEACHING_ASSISTANT': 'Professor Assistente',
  'STUDENT': 'Aluno'
}

const editPerson = ref<PersonDto>({
  name: '',
  istId: '',
  email: '',
  type: ''
})

// Check if there's any changes
const hasChanges = computed(() => {
  return (
    editPerson.value.name !== originalPerson.value.name ||
    editPerson.value.istId !== originalPerson.value.istId ||
    editPerson.value.email !== originalPerson.value.email ||
    editPerson.value.type !== originalPerson.value.type
  )
})

const canUpdate = computed(() => {
  return isFormValid.value && hasChanges.value
})

// Watch for prop changes and update editPerson
watch(() => props.person, (newPerson) => {
  if (newPerson) {
    const displayType = reverseTypeMappings[newPerson.type as keyof typeof reverseTypeMappings] || newPerson.type
    
    editPerson.value = {
      ...newPerson,
      type: displayType
    }
    
    // Store original data for comparison
    originalPerson.value = {
      ...newPerson,
      type: displayType
    }
  }
}, { immediate: true })

// Reset form when dialog opens
watch(localDialog, (newVal) => {
  if (newVal && props.person) {
    const displayType = reverseTypeMappings[props.person.type as keyof typeof reverseTypeMappings] || props.person.type
    
    editPerson.value = {
      ...props.person,
      type: displayType
    }
    
    originalPerson.value = {
      ...props.person,
      type: displayType
    }
  }
})

const updatePerson = async () => {
  if (!props.person?.id || !canUpdate.value) return

  try {
    const personToUpdate = { ...editPerson.value }
    personToUpdate.type = typeMappings[personToUpdate.type as keyof typeof typeMappings]
    
    await RemoteService.updatePerson(props.person.id, personToUpdate)
    
    localDialog.value = false
    emit('person-updated')
  } catch (error) {
    console.error('Error updating person:', error)
  }
}
</script>