import { defineStore } from 'pinia'

export const useRoleStore = defineStore('role', {
  state: () => ({
    currentRole: '',
  }),
  getters: {
    isAdministrator(): boolean {
      return this.currentRole === 'ADMINISTRATOR'
    },
    isMainTeacher(): boolean {
      return this.currentRole === 'MAIN_TEACHER'
    },
    isTeachingAssistant(): boolean {
      return this.currentRole === 'TEACHING_ASSISTANT'
    },
    isStudent(): boolean {
      return this.currentRole === 'STUDENT'
    },
    currentActiveRole(): string {
        return this.currentRole
    }
  },
  actions: {
    switchPerspective(role: string) {
      this.currentRole = role
    }
  },
  persist: true
})