<template>
    <v-dialog v-model="localDialog" max-width="400">
        <v-card prepend-icon="mdi-book-remove" title="Remover Unidade Curricular">
            <v-card-text>
                Tem certeza que deseja remover permanentemente a unidade curricular <strong>{{ curricularUnit?.name }}</strong>?
            </v-card-text>

            <v-divider></v-divider>

            <v-card-actions>

                <v-spacer></v-spacer>

                <v-btn text="Cancelar" @click="localDialog = false"></v-btn>
                
                <v-btn color="red" text="Remover" @click="deleteCurricularUnit"></v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import CurricularUnitDto from '../../models/CurricularUnitDto'
import RemoteService from '../../services/RemoteService'

const emit = defineEmits(['curricular-unit-deleted', 'update:modelValue'])

const props = defineProps({
  curricularUnit: {
    type: Object as () => CurricularUnitDto,
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

const deleteCurricularUnit = async () => {
    if (!props.curricularUnit?.id) return

    try {
        await RemoteService.deleteCurricularUnit(props.curricularUnit.id)
        emit('curricular-unit-deleted')
    } catch (error) {
        console.error('Erro ao remover unidade curricular:', error)
    } finally {
        localDialog.value = false
    }
}
</script>