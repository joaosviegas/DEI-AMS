<template>
  <v-dialog v-model="localDialog" max-width="600">
    <v-card prepend-icon="mdi-pencil" title="Editar Teste">
      <v-card-text>
        <v-form ref="form" v-model="valid">
          <v-row>
            <v-col cols="12">
              <v-text-field
                v-model="editedTest.title"
                label="Título do Teste"
                :rules="[v => !!v || 'Título é obrigatório']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12">
              <v-text-field
                v-model="editedTest.date"
                label="Data do Teste"
                type="datetime-local"
                :rules="[v => !!v || 'Data é obrigatória']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12">
              <v-text-field
                v-model="editedTest.revisionDeadline"
                label="Prazo de Revisão"
                type="datetime-local"
                :rules="[
                  v => !!v || 'Prazo de revisão é obrigatório',
                  v => !editedTest.date || new Date(v) > new Date(editedTest.date) || 'Prazo de revisão deve ser posterior à data do teste'
                ]"
                required
                variant="outlined"
                hint="Data limite para pedidos de revisão de nota"
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12">
              <v-text-field
                v-model.number="editedTest.weight"
                label="Peso (%)"
                type="number"
                min="0"
                max="100"
                step="0.1"
                :rules="weightRules"
                required
                variant="outlined"
                suffix="%"
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
          text="Guardar Alterações" 
          @click="saveTest"
          :loading="loading"
          :disabled="!valid || !hasChanges"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import TestDto from '../../../models/TestDto'
import RemoteService from '../../../services/RemoteService'

const emit = defineEmits(['update:modelValue', 'test-updated'])

const props = defineProps({
  test: {
    type: Object as () => TestDto | undefined,
    default: undefined
  },
  modelValue: {
    type: Boolean,
    default: false
  }
})

const form = ref()
const valid = ref(false)
const loading = ref(false)

const editedTest = ref({
  id: 0,
  title: '',
  date: '',
  weight: 0,
  revisionDeadline: ''
})

// Store original values to detect changes
const originalTest = ref({
  id: 0,
  title: '',
  date: '',
  weight: 0,
  revisionDeadline: ''
})

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// Validation rules for weight
const weightRules = [
  (v: number) => v !== null && v !== undefined || 'Peso é obrigatório',
  (v: number) => (v >= 0 && v <= 100) || 'Peso deve estar entre 0 e 100%',
]

// Check if form has changes
const hasChanges = computed(() => {
  return editedTest.value.title !== originalTest.value.title ||
         editedTest.value.date !== originalTest.value.date ||
         editedTest.value.weight !== originalTest.value.weight ||
         editedTest.value.revisionDeadline !== originalTest.value.revisionDeadline
})

const formatDateTimeForInput = (dateString: string): string => {
  if (!dateString) return ''
  
  // Create date object and adjust for local timezone
  const date = new Date(dateString)
  const tzOffset = date.getTimezoneOffset() * 60000
  const localDate = new Date(date.getTime() - tzOffset)
  
  return localDate.toISOString().slice(0, 16)
}

// Watch for test prop changes to populate form
watch(() => props.test, (newTest) => {
  if (newTest) {
    editedTest.value = {
      id: newTest.id || 0,
      title: newTest.title || '',
      date: formatDateTimeForInput(newTest.date || ''),
      weight: (newTest.weight || 0) * 100, // Convert from decimal to percentage
      revisionDeadline: formatDateTimeForInput(newTest.revisionDeadline || '')
    }
    
    // Store original values for change detection
    originalTest.value = { ...editedTest.value }
  }
}, { immediate: true })

const cancel = () => {
  localDialog.value = false
  resetForm()
}

const resetForm = () => {
  if (form.value) {
    form.value.reset()
  }
  editedTest.value = {
    id: 0,
    title: '',
    date: '',
    weight: 0,
    revisionDeadline: ''
  }
  originalTest.value = { ...editedTest.value }
}

const saveTest = async () => {
  if (!form.value || !await form.value.validate()) return

  loading.value = true
  
  // Prepare test data for API (convert percentage back to decimal)
  const testData = {
    id: editedTest.value.id,
    title: editedTest.value.title,
    date: editedTest.value.date,
    weight: editedTest.value.weight / 100, // Convert percentage to decimal
    revisionDeadline: editedTest.value.revisionDeadline
  }

  try {
    await RemoteService.updateTest(testData.id, testData)
    
    emit('test-updated')
    localDialog.value = false
    resetForm()
  } catch (error: any) {

    console.log('Test update failed:', error.message)
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
