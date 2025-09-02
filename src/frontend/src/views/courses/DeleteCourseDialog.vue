<template>
    <v-dialog v-model="localDialog" max-width="400">
        <v-card prepend-icon="mdi-book-remove" title="Remover Curso">
            <v-card-text>
                Tem certeza que deseja remover permanentemente o curso <strong>{{ course?.name }}</strong>?
            </v-card-text>

            <v-divider></v-divider>

            <v-card-actions>

                <v-spacer></v-spacer>

                <v-btn text="Cancelar" @click="localDialog = false"></v-btn>
                
                <v-btn color="red" text="Remover" @click="deleteCourse"></v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import CourseDto from '../../models/CourseDto'
import RemoteService from '../../services/RemoteService'

const emit = defineEmits(['course-deleted', 'update:modelValue'])

const props = defineProps({
  course: {
    type: Object as () => CourseDto,
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

const deleteCourse = async () => {
    if (!props.course?.id) return

    try {
        await RemoteService.deleteCourse(props.course.id)
        emit('course-deleted')
    } catch (error) {
        console.error('Erro ao remover curso:', error)
    } finally {
        localDialog.value = false
    }
}
</script>