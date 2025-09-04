<template>
  <v-dialog v-model="localDialog" max-width="500">
    <v-card>
      <v-card-title class="d-flex align-center">
        <v-icon color="white" class="mr-2">mdi-delete</v-icon>
        <span>{{ title || 'Confirmar Eliminação' }}</span>
      </v-card-title>

      <v-card-text>
        <p class="mb-4">
          {{ message || `Tem a certeza de que deseja eliminar ${itemType || 'este item'}?` }}
        </p>
        
        <div v-if="itemName" class="bg-grey-lighten-4 pa-3 rounded mb-4">
          <div class="d-flex align-center">
            <v-icon class="mr-2" :color="iconColor || 'primary'">{{ icon || 'mdi-help-circle' }}</v-icon>
            <div>
              <div class="font-weight-medium">{{ itemName }}</div>
              <div v-if="itemSubtitle" class="text-caption text-black">{{ itemSubtitle }}</div>
            </div>
          </div>
        </div>

        <v-alert 
          v-if="showWarning" 
          color="orange" 
          variant="tonal" 
          icon="mdi-alert"
          class="mb-4"
        >
          {{ warningMessage || 'Esta ação não pode ser desfeita.' }}
        </v-alert>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn 
          text="Cancelar" 
          variant="plain"
          @click="localDialog = false"
          :disabled="loading"
        ></v-btn>
        <v-btn 
          :text="confirmText || 'Eliminar'"
          color="red" 
          @click="confirmDelete"
          :loading="loading"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const emit = defineEmits(['update:modelValue', 'confirm'])

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    default: ''
  },
  itemType: {
    type: String,
    default: ''
  },
  itemName: {
    type: String,
    default: ''
  },
  itemSubtitle: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  },
  iconColor: {
    type: String,
    default: ''
  },
  confirmText: {
    type: String,
    default: ''
  },
  showWarning: {
    type: Boolean,
    default: true
  },
  warningMessage: {
    type: String,
    default: ''
  }
})

const loading = ref(false)

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const confirmDelete = async () => {
  loading.value = true
  try {
    emit('confirm')
  } finally {
    loading.value = false
    localDialog.value = false
  }
}

// Method to be called by parent to stop loading state
const stopLoading = () => {
  loading.value = false
}

// Expose method for parent components
defineExpose({
  stopLoading
})
</script>
