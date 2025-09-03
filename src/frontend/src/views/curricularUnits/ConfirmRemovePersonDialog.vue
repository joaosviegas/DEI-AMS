<template>
  <v-dialog v-model="localDialog" max-width="400">
    <v-card prepend-icon="mdi-account-remove" :title="dialogTitle">
      <v-card-text>
        <span v-html="dialogMessage"></span>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" @click="localDialog = false"></v-btn>
        <v-btn color="red" text="Remover" @click="confirmAction"></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const emit = defineEmits(['update:modelValue', 'confirm'])

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  personType: {
    type: String,
    required: true,
    validator: (value: string) => ['student', 'teacher'].includes(value)
  },
  personName: {
    type: String,
    required: true
  }
})

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogTitle = computed(() => {
  return props.personType === 'student' 
    ? 'Remover Aluno' 
    : 'Remover Professor Assistente'
})

const dialogMessage = computed(() => {
  const personTypeText = props.personType === 'student' ? 'aluno' : 'professor'
  return `Tem certeza que deseja remover o ${personTypeText} <strong>${props.personName}</strong> desta unidade curricular?`
})

const confirmAction = () => {
  emit('confirm')
  localDialog.value = false
}
</script>
