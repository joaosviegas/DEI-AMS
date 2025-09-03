import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { useAppearanceStore } from '@/stores/appearance'
import DeiError from '@/models/DeiError'
import type PersonDto from '@/models/PersonDto'
import type CourseDto from '@/models/CourseDto'
import type CurricularUnitDto from '@/models/CurricularUnitDto'

const httpClient = axios.create()
httpClient.defaults.timeout = 50000
httpClient.defaults.baseURL = import.meta.env.VITE_ROOT_API
httpClient.defaults.headers.post['Content-Type'] = 'application/json'

export default class RemoteServices {
  static async getPeople(): Promise<PersonDto[]> {
    return httpClient.get('/people')
  }

  static async createPerson(person: PersonDto): Promise<PersonDto> {
    return httpClient.post('/people', person)
  }

  static async deletePerson(id: number): Promise<void> {
    return httpClient.delete(`/people/${id}`)
  }

  static async updatePerson(id: number, person: PersonDto): Promise<PersonDto> {
    return httpClient.put(`/people/${id}`, person)
  }

  static async getCourses(): Promise<CourseDto[]> {
    return httpClient.get('/courses')
  }

  static async createCourse(course: CourseDto): Promise<CourseDto> {
    return httpClient.post('/courses', course)
  }

  static async deleteCourse(id: number): Promise<void> {
    return httpClient.delete(`/courses/${id}`)
  }

  static async updateCourse(id: number, course: CourseDto): Promise<CourseDto> {
    return httpClient.put(`/courses/${id}`, course)
  }

  // CurricularUnit methods
  static async getCurricularUnits(): Promise<CurricularUnitDto[]> {
    return httpClient.get('/curricular-units')
  }

  static async createCurricularUnit(data: any): Promise<CurricularUnitDto> {
    const params = new URLSearchParams(data)
    return httpClient.post('/curricular-units', null, { params })
  }

  static async deleteCurricularUnit(id: number): Promise<void> {
    return httpClient.delete(`/curricular-units/${id}`)
  }

  static async updateCurricularUnit(id: number, data: any): Promise<CurricularUnitDto> {
    const params = new URLSearchParams(data)
    return httpClient.put(`/curricular-units/${id}`, null, { params })
  }

  static async addCourseToCurricularUnit(curricularUnitId: number, courseId: number): Promise<CurricularUnitDto> {
    return httpClient.post(`/curricular-units/${curricularUnitId}/courses/${courseId}`)
  }

  static async removeCourseFromCurricularUnit(curricularUnitId: number, courseId: number): Promise<CurricularUnitDto> {
    return httpClient.delete(`/curricular-units/${curricularUnitId}/courses/${courseId}`)
  }

  static async addAssistantTeacher(curricularUnitId: number, teacherId: number): Promise<CurricularUnitDto> {
    return httpClient.post(`/curricular-units/${curricularUnitId}/assistant-teachers/${teacherId}`)
  }

  static async removeAssistantTeacher(curricularUnitId: number, teacherId: number): Promise<CurricularUnitDto> {
    return httpClient.delete(`/curricular-units/${curricularUnitId}/assistant-teachers/${teacherId}`)
  }

  // New enrollment-based student management
  static async enrollStudent(curricularUnitId: number, studentId: number, status: string = 'ENROLLED'): Promise<any> {
    const params = new URLSearchParams({ curricularUnitId: curricularUnitId.toString(), studentId: studentId.toString(), status })
    return httpClient.post('/student-enrollments', null, { params })
  }

  static async unenrollStudent(curricularUnitId: number, studentId: number): Promise<void> {
    const params = new URLSearchParams({ curricularUnitId: curricularUnitId.toString(), studentId: studentId.toString() })
    return httpClient.delete('/student-enrollments', { params })
  }

  static async updateEnrollmentStatus(enrollmentId: number, status: string, reason?: string): Promise<any> {
    const params = new URLSearchParams({ status })
    if (reason) params.append('reason', reason)
    return httpClient.put(`/student-enrollments/${enrollmentId}/status`, null, { params })
  }

  static async completeEnrollment(enrollmentId: number, grade: number): Promise<any> {
    const params = new URLSearchParams({ grade: grade.toString() })
    return httpClient.put(`/student-enrollments/${enrollmentId}/complete`, null, { params })
  }

  static async withdrawEnrollment(enrollmentId: number, reason?: string): Promise<any> {
    const params = new URLSearchParams()
    if (reason) params.append('reason', reason)
    return httpClient.put(`/student-enrollments/${enrollmentId}/withdraw`, null, { params })
  }

  static async getEnrollmentsByCurricularUnit(curricularUnitId: number): Promise<any[]> {
    return httpClient.get(`/curricular-units/${curricularUnitId}/enrollments`)
  }

  static async getCurricularUnit(id: number): Promise<CurricularUnitDto> {
    return httpClient.get(`/curricular-units/${id}`)
  }

  static async errorMessage(error: any): Promise<string> {
    if (error.message === 'Network Error') {
      return 'Unable to connect to the server'
    } else if (error.message.split(' ')[0] === 'timeout') {
      return 'Request timeout - Server took too long to respond'
    } else {
      return error.response?.data?.message ?? 'Unknown Error'
    }
  }

  static async handleError(error: any): Promise<never> {
    const deiErr = new DeiError(
      await RemoteServices.errorMessage(error),
      error.response?.data?.code ?? -1
    )
    const appearance = useAppearanceStore()
    appearance.pushError(deiErr)
    appearance.loading = false
    throw deiErr
  }
}

httpClient.interceptors.request.use((request) => request, RemoteServices.handleError)
httpClient.interceptors.response.use((response) => response.data, RemoteServices.handleError)
