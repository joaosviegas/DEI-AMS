import { defineStore } from 'pinia'

export const useRoleStore = defineStore('role', {
  state: () => ({
    currentRole: 'administrator',
  }),
  getters: {
    isAdministrator(): boolean {
      return this.currentRole === 'administrator'
    },
    isMainTeacher(): boolean {
      return this.currentRole === 'main_teacher'
    },
    isTeachingAssistant(): boolean {
      return this.currentRole === 'teaching_assistant'
    },
    isStudent(): boolean {
      return this.currentRole === 'student'
    },
    currentActiveRole(): string {
        return this.currentRole
    }
  },
  persist: true
})