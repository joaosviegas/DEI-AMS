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
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model="newTest.date"
                label="Data do Teste"
                type="datetime-local"
                :rules="[v => !!v || 'Data é obrigatória']"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model.number="newTest.weight"
                label="Peso na Nota Final (0-1)"
                type="number"
                min="0"
                max="1"
                step="0.01"
                :rules="[
                  v => v !== null && v !== undefined && v !== '' || 'Peso é obrigatório',
                  v => v >= 0 && v <= 1 || 'Peso deve estar entre 0 e 1'
                ]"
                required
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" @click="cancel"></v-btn>
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
  weight: 0
})

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isFormValid = computed(() => {
  return newTest.value.title.trim() !== '' &&
         newTest.value.date !== '' &&
         newTest.value.weight >= 0 &&
         newTest.value.weight <= 1
})

const resetForm = () => {
  newTest.value = {
    title: '',
    date: '',
    weight: 0
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
      weight: newTest.value.weight
    }
    
    await RemoteService.createTest(props.curricularUnitId, testData)
    emit('test-created')
    localDialog.value = false
    resetForm()
  } catch (error) {
    console.error('Error creating test:', error)
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
