<template>
  <v-dialog v-model="localDialog" max-width="500">
    <v-card prepend-icon="mdi-clipboard-plus" title="Criar Teste">
      <v-card-text>
        <v-form ref="testForm">
          <v-row>
            <v-col cols="12">
              <v-text-field
                v-model="newTest.title"
                label="Título do Teste"
                :rules="[v => !!v || 'Título é obrigatório']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model="newTest.date"
                label="Data do Teste"
                type="datetime-local"
                :rules="[v => !!v || 'Data é obrigatória']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model="newTest.revisionDeadline"
                label="Prazo de Revisão"
                type="datetime-local"
                :rules="[
                  v => !!v || 'Prazo de revisão é obrigatório',
                  v => !newTest.date || new Date(v) > new Date(newTest.date) || 'Prazo de revisão deve ser posterior à data do teste'
                ]"
                required
                variant="outlined"
                hint="Data limite para pedidos de revisão de nota"
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model.number="newTest.weight"
                label="Peso (%)"
                type="number"
                min="0"
                max="100"
                step="0.1"
                :rules="[
                  v => v !== null && v !== undefined && v !== '' || 'Peso é obrigatório',
                  v => v >= 0 && v <= 100 || 'Peso deve estar entre 0 e 100%'
                ]"
                required
                suffix="%"
                variant="outlined"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" variant="plain" @click="cancel"></v-btn>
        <v-btn 
          color="primary" 
          text="Criar" 
          @click="create" 
          :loading="loading"
          :disabled="!isFormValid"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import RemoteService from '../../../services/RemoteService'

const emit = defineEmits(['update:modelValue', 'test-created'])

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  curricularUnitId: {
    type: Number,
    required: true
  }
})

const testForm = ref()
const loading = ref(false)
const newTest = ref({
  title: '',
  date: '',
  weight: 0,
  revisionDeadline: ''
})

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isFormValid = computed(() => {
  return newTest.value.title.trim() !== '' &&
         newTest.value.date !== '' &&
         newTest.value.revisionDeadline !== '' &&
         newTest.value.weight >= 0 &&
         newTest.value.weight <= 100 &&
         (!newTest.value.date || !newTest.value.revisionDeadline || 
          new Date(newTest.value.revisionDeadline) > new Date(newTest.value.date))
})

const resetForm = () => {
  newTest.value = {
    title: '',
    date: '',
    weight: 0,
    revisionDeadline: ''
  }
  if (testForm.value) {
    testForm.value.resetValidation()
  }
}

const cancel = () => {
  localDialog.value = false
  resetForm()
}

const create = async () => {
  if (!testForm.value?.validate()) {
    return
  }

  loading.value = true
  try {
    const testData = {
      title: newTest.value.title,
      date: newTest.value.date,
      weight: newTest.value.weight / 100, // Convert percentage to decimal
      revisionDeadline: newTest.value.revisionDeadline
    }
    
    await RemoteService.createTest(props.curricularUnitId, testData)
    emit('test-created')
    localDialog.value = false
    resetForm()
  } catch (error: any) {
    // Error is already handled by the interceptor and shown to user
    // Just catch it to prevent uncaught promise warning
    console.log('Test creation failed:', error.message)
  } finally {
    loading.value = false
  }
}

// Reset form when dialog closes
watch(localDialog, (isOpen) => {
  if (!isOpen) {
    resetForm()
  }
})
</script>
