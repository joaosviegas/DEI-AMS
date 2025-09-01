<template>
    <v-dialog v-model="localDialog" max-width="400">
        <v-card prepend-icon="mdi-account-remove" title="Remover Pessoa">
            <v-card-text>
                Tem certeza que deseja remover permanentemente <strong>{{ person?.name }}</strong>?
            </v-card-text>

            <v-divider></v-divider>

            <v-card-actions>

                <v-spacer></v-spacer>

                <v-btn text="Cancelar" @click="localDialog = false"></v-btn>
                
                <v-btn color="red" text="Remover" @click="deletePerson"></v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type PersonDto from '@/models/people/PersonDto'
import RemoteService from '@/services/RemoteService'

const emit = defineEmits(['person-deleted', 'update:modelValue'])

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

const localDialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const deletePerson = async () => {
    if (!props.person?.id) return

    try {
        await RemoteService.deletePerson(props.person.id)
        emit('person-deleted')
    } catch (error) {
        console.error('Erro ao remover pessoa:', error)
    } finally {
        localDialog.value = false
    }
}
</script>