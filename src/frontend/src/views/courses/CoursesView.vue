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
      <v-icon @click="editCourse(item)" class="mr-2" disabled>mdi-pencil</v-icon>
      <v-icon @click="deleteCourse(item)" disabled>mdi-delete</v-icon>
    </template>
  </v-data-table>

  <!-- 
  <EditCourseDialog 
    v-if="isAdmin"
    v-model="showEditDialog"
    :course="selectedCourse"
    @course-updated="getCourses"
  />

  <DeleteCourseDialog 
    v-if="isAdmin"
    v-model="showDeleteDialog"
    :course="selectedCourse"
    @course-deleted="getCourses"
  />
  -->
</template>

<script setup lang="ts">
import type CourseDto from '@/models/courses/CourseDto'
import RemoteService from '@/services/RemoteService'
import CreateCourseDialog from './CreateCourseDialog.vue'
// import EditCourseDialog from './EditCourseDialog.vue'
// import DeleteCourseDialog from './DeleteCourseDialog.vue'
import { reactive, ref, computed } from 'vue'

let search = ref('')
let loading = ref(true)

const showDeleteDialog = ref(false)
const showEditDialog = ref(false)
const selectedCourse = ref<CourseDto>()

// For now, assume admin check - you'll implement this based on your auth system
const isAdmin = computed(() => {
  // TODO: Replace with actual admin check
  // return userStore.user?.type === 'ADMINISTRATOR'
  return true // Temporary - shows all buttons
})

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
    console.log('Courses loaded:', courses)
  } catch (error) {
    console.error('Error loading courses:', error)
    loading.value = false
  }
}

// Open the edit dialog TODO
const editCourse = (course: CourseDto) => {
  console.log('Editing course:', course)
  selectedCourse.value = course
  showEditDialog.value = true
}

// Open the delete dialog TODO
const deleteCourse = (course: CourseDto) => {
  console.log('Deleting course:', course)
  selectedCourse.value = course
  showDeleteDialog.value = true
}

const fuzzySearch = (value: string, search: string) => {
  // Regex to match any character in between the search characters
  let searchRegex = new RegExp(search.split('').join('.*'), 'i')
  return searchRegex.test(value)
}
</script>