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
  { name: 'UCs', path: '/curricular-units', icon: 'mdi-school', roles: ['STUDENT', 'TEACHING_ASSISTANT', 'MAIN_TEACHER', 'ADMINISTRATOR'] },
  { name: "Cursos", path: "/courses", icon: "mdi-book-open-variant", roles: ['STUDENT', 'TEACHING_ASSISTANT', 'MAIN_TEACHER', 'ADMINISTRATOR'] },
  { name: 'Pessoal', path: '/people', icon: 'mdi-account-group', roles: ['ADMINISTRATOR'] },
  { name: 'Estatísticas', path: '/statistics', icon: 'mdi-chart-bar', roles: ['STUDENT', 'TEACHING_ASSISTANT', 'MAIN_TEACHER', 'ADMINISTRATOR'] },
])

// Filtered menu items based on the current role
const filteredNavbarItems = computed(() => {
  const currentRole = roleStore.currentActiveRole
  
  return navbarItems.value
    .filter(item => item.roles.includes(currentRole))
    .map(({ roles, ...item }) => item)
})
</script>
