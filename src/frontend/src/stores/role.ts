import { defineStore } from 'pinia'

export const useRoleStore = defineStore('role', {
  state: () => ({
    currentRole: 'ADMINISTRATOR',
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
  persist: true
})