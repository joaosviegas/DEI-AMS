<template>
  <UtilBar />
  <NavBar :navbarItems="filteredNavbarItems" />
</template>

<script setup lang="ts">
import UtilBar from './UtilBar.vue'
import NavBar from './NavBar.vue'
import { useRoleStore } from '../stores/role'
import { computed } from 'vue'

const roleStore = useRoleStore()

const navbarItems = computed(() => [
  {name: 'Início', path: '/', icon: 'mdi-home', roles: ['STUDENT', 'TEACHING_ASSISTANT', 'MAIN_TEACHER', 'ADMINISTRATOR']},
  { name: 'UCs', path: '/curricular-units', icon: 'mdi-book-open-variant', roles: ['STUDENT', 'TEACHING_ASSISTANT', 'MAIN_TEACHER', 'ADMINISTRATOR'] },
  { name: 'Prazos', path: '/deadlines', icon: 'mdi-clock-alert-outline', roles: ['STUDENT'] },
  { name: 'Workflow', path: '/workflow', icon: 'mdi-clipboard-list', roles: ['STUDENT', 'TEACHING_ASSISTANT', 'MAIN_TEACHER'] },
  { name: 'Alunos', path: '/students', icon: 'mdi-school', roles: ['MAIN_TEACHER', 'TEACHING_ASSISTANT'] },
  { name: "Cursos", path: "/courses", icon: "mdi-book-multiple", roles: ['STUDENT', 'TEACHING_ASSISTANT', 'MAIN_TEACHER', 'ADMINISTRATOR'] },
  { name: 'Pessoal', path: '/people', icon: 'mdi-account-group', roles: ['ADMINISTRATOR'] },
  { name: 'Estatísticas', path: '/statistics', icon: 'mdi-chart-bar', roles: ['STUDENT', 'TEACHING_ASSISTANT', 'MAIN_TEACHER', 'ADMINISTRATOR'] },
])

// Filtered menu items based on the current role
const filteredNavbarItems = computed(() => {
  const currentRole = roleStore.currentActiveRole
  
  return navbarItems.value
    .filter(item => item.roles && item.roles.includes(currentRole))
    .map(({ roles, ...item }) => item)
})
</script>
