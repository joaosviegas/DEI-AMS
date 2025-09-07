<template>
  <v-row align="center">
    <v-col>
      <h2 class="text-left ml-1">Listagem de Cursos</h2>
    </v-col>
    <v-col cols="auto" v-if="isAdmin">
      <CreateCourseDialog @course-created="getCourses" />
    </v-col>
  </v-row>

  <v-text-field
    v-model="search"
    label="Search"
    prepend-inner-icon="mdi-magnify"
    variant="outlined"
    hide-details
    single-line
  ></v-text-field>

  <v-data-table
    :headers="headers"
    :items="courses"
    :search="search"
    :loading="loading"
    :custom-filter="fuzzySearch"
    item-key="id"
    class="text-left"
    no-data-text="Sem cursos a apresentar."
  >
    <template v-slot:[`item.actions`]="{ item }" v-if="isAdmin">
      <v-icon @click="editCourse(item)" class="mr-2" title="Editar Curso" tonal>mdi-pencil </v-icon>
      <v-icon @click="deleteCourse(item)" title="Remover Curso" tonal>mdi-delete</v-icon>
    </template>
  </v-data-table>

  <EditCourseDialog 
    v-if="isAdmin"
    v-model="showEditDialog"
    :course="selectedCourse"
    @course-updated="getCourses"
  />
  
  <ConfirmDeleteDialog 
    v-if="isAdmin"
    v-model="showDeleteDialog"
    title="Remover Curso"
    :message="`Tem a certeza de que deseja remover permanentemente o curso '${selectedCourse?.name}'?`"
    item-type="curso"
    :item-name="selectedCourse?.name"
    :item-subtitle="`Código: ${selectedCourse?.code} • Duração: ${selectedCourse?.duration} anos`"
    icon="mdi-book-education"
    icon-color="blue"
    confirm-text="Remover Curso"
    warning-message="Esta ação eliminará permanentemente o curso e todas as suas unidades curriculares associadas."
    @confirm="executeDeleteCourse"
  />
</template>

<script setup lang="ts">
import CourseDto from '../../models/CourseDto'
import RemoteService from '../../services/RemoteService'
import CreateCourseDialog from './CreateCourseDialog.vue'
import EditCourseDialog from './EditCourseDialog.vue'
import ConfirmDeleteDialog from '../../components/ConfirmDeleteDialog.vue'
import { reactive, ref, computed } from 'vue'
import { useRoleStore } from '../../stores/role'

let search = ref('')
let loading = ref(true)

const showDeleteDialog = ref(false)
const showEditDialog = ref(false)
const selectedCourse = ref<CourseDto>()

const roleStore = useRoleStore()
const isAdmin = computed(() => roleStore.isAdministrator)

const headers = computed(() => {
  const baseHeaders = [
    { title: 'ID', key: 'id', value: 'id', sortable: true, filterable: false },
    {
      title: 'Código',
      key: 'code',
      value: 'code',
      sortable: true,
      filterable: true
    },
    {
      title: 'Nome',
      key: 'name',
      value: 'name',
      sortable: true,
      filterable: true
    },
    {
      title: 'Duração (anos)',
      key: 'duration',
      value: 'duration',
      sortable: true,
      filterable: false
    }
  ]
  
  if (isAdmin.value) {
    baseHeaders.push({
      title: 'Ações',
      key: 'actions',
      value: 'actions',
      sortable: false,
      filterable: false
    })
  }
  
  return baseHeaders
})

const courses: CourseDto[] = reactive([])

getCourses()
async function getCourses() {
  try {
    courses.splice(0, courses.length)
    courses.push(...(await RemoteService.getCourses()))
    loading.value = false
  } catch (error) {
    console.error('Error loading courses:', error)
    loading.value = false
  }
}

// Open the edit dialog
const editCourse = (course: CourseDto) => {
  selectedCourse.value = course
  showEditDialog.value = true
}

// Open the delete dialog
const deleteCourse = (course: CourseDto) => {
  selectedCourse.value = course
  showDeleteDialog.value = true
}

// Actually execute the deletion
const executeDeleteCourse = async () => {
  if (!selectedCourse.value?.id) return
  
  try {
    await RemoteService.deleteCourse(selectedCourse.value.id)
    
    // Refresh the courses list
    await getCourses()
    
    // Close dialog and reset
    showDeleteDialog.value = false
    selectedCourse.value = undefined
  } catch (error) {
    console.error('Error deleting course:', error)
    alert('Erro ao eliminar curso. Por favor, tente novamente.')
  }
}

const fuzzySearch = (value: string, search: string) => {
  // Regex to match any character in between the search characters
  let searchRegex = new RegExp(search.split('').join('.*'), 'i')
  return searchRegex.test(value)
}
</script>