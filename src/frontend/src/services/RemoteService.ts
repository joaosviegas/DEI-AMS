import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { useAppearanceStore } from '../stores/appearance'
import DeiError from '../models/DeiError'
import type PersonDto from '../models/PersonDto'
import type CourseDto from '@/models/CourseDto'
import type CurricularUnitDto from '@/models/CurricularUnitDto'
import type ProjectDto from '@/models/ProjectDto'
import type ProjectGroupDto from '@/models/ProjectGroupDto'
import type ProjectSubmissionDto from '@/models/ProjectSubmissionDto'

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

  // Test methods
  static async getTestsByCurricularUnit(curricularUnitId: number): Promise<any[]> {
    return httpClient.get(`/tests/curricular-unit/${curricularUnitId}`)
  }

  static async createTest(curricularUnitId: number, test: any): Promise<any> {
    const params = new URLSearchParams({
      title: test.title,
      date: test.date,
      weight: test.weight.toString()
    })
    return httpClient.post(`/tests/curricular-unit/${curricularUnitId}`, null, { params })
  }

  static async updateTest(testId: number, test: any): Promise<any> {
    const params = new URLSearchParams({
      title: test.title,
      date: test.date,
      weight: test.weight.toString()
    })
    return httpClient.put(`/tests/${testId}`, null, { params })
  }

  static async deleteTest(testId: number): Promise<void> {
    return httpClient.delete(`/tests/${testId}`)
  }

  // Project methods
  static async getProjectsByCurricularUnit(curricularUnitId: number): Promise<ProjectDto[]> {
    return httpClient.get(`/projects/curricular-unit/${curricularUnitId}`)
  }

  static async getProject(projectId: number): Promise<ProjectDto> {
    return httpClient.get(`/projects/${projectId}`)
  }

  static async createProject(curricularUnitId: number, project: any): Promise<ProjectDto> {
    const params = new URLSearchParams({
      title: project.title,
      weight: project.weight.toString(),
      submissionDeadline: project.submissionDeadline,
      description: project.description || ''
    })
    
    if (project.maxGroupSize && project.maxGroupSize > 1) {
      params.append('maxGroupSize', project.maxGroupSize.toString())
    }

    return httpClient.post(`/projects/curricular-unit/${curricularUnitId}`, null, { params })
  }

  static async updateProject(projectId: number, project: any): Promise<ProjectDto> {
    const params = new URLSearchParams({
      title: project.title,
      weight: project.weight.toString(),
      submissionDeadline: project.submissionDeadline,
      description: project.description || ''
    })
    
    if (project.allowedExtensions) {
      params.append('allowedExtensions', project.allowedExtensions)
    }
    
    if (project.maxFileSize) {
      params.append('maxFileSize', project.maxFileSize.toString())
    }

    return httpClient.put(`/projects/${projectId}`, null, { params })
  }

  static async deleteProject(projectId: number): Promise<void> {
    return httpClient.delete(`/projects/${projectId}`)
  }

  // Project group methods
  static async getProjectGroups(projectId: number): Promise<ProjectGroupDto[]> {
    return httpClient.get(`/projects/${projectId}/groups`)
  }

  static async getStudentGroup(projectId: number, studentId: number): Promise<ProjectGroupDto | null> {
    try {
      return httpClient.get(`/projects/${projectId}/groups/student/${studentId}`)
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  }

  // Project submission methods
  static async submitProject(projectId: number, studentId: number, file: File): Promise<ProjectSubmissionDto> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('studentId', studentId.toString())

    return httpClient.post(`/projects/${projectId}/submissions`, formData, {
      transformRequest: [(data, headers) => {
        delete headers['Content-Type'];
        return data;
      }]
    })
  }

  static async submitProjectForGroup(projectId: number, groupId: number, studentId: number, file: File): Promise<ProjectSubmissionDto> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('studentId', studentId.toString())

    return httpClient.post(`/projects/${projectId}/groups/${groupId}/submissions`, formData, {
      transformRequest: [(data, headers) => {
        delete headers['Content-Type'];
        return data;
      }]
    })
  }

  static async getGroupSubmissions(projectId: number, groupId: number): Promise<ProjectSubmissionDto[]> {
    return httpClient.get(`/projects/${projectId}/groups/${groupId}/submissions`)
  }

  static async getLatestGroupSubmission(projectId: number, groupId: number): Promise<ProjectSubmissionDto | null> {
    try {
      return httpClient.get(`/projects/${projectId}/groups/${groupId}/submissions/latest`)
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  }

  static async getProjectSubmissions(projectId: number): Promise<ProjectSubmissionDto[]> {
    return httpClient.get(`/projects/${projectId}/submissions`)
  }

  static async getStudentSubmission(projectId: number, studentId: number): Promise<ProjectSubmissionDto | null> {
    try {
      return httpClient.get(`/projects/${projectId}/submissions/student/${studentId}`)
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  }

  static async gradeSubmission(submissionId: number, grade: number, feedback: string = '', graderId: number): Promise<ProjectSubmissionDto> {
    const params = new URLSearchParams({
      grade: grade.toString(),
      graderId: graderId.toString()
    })
    
    if (feedback) {
      params.append('feedback', feedback)
    }

    return httpClient.post(`/projects/submissions/${submissionId}/grade`, null, { params })
  }

  static async deleteSubmission(submissionId: number): Promise<void> {
    return httpClient.delete(`/projects/submissions/${submissionId}`)
  }

  static async performAutomaticGrading(projectId: number): Promise<string> {
    return httpClient.post(`/projects/${projectId}/auto-grade`)
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

  // File upload and download methods
  static async uploadFile(curricularUnitId: number, file: File, resourceType: 'MATERIAL' | 'SUBMISSION'): Promise<any> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('resourceType', resourceType)

    return httpClient.post(`/resources/curricular-unit/${curricularUnitId}/upload`, formData, {
      transformRequest: [(data, headers) => {
        delete headers['Content-Type'];
        return data;
      }]
    })
  }

  static async getMaterials(curricularUnitId: number): Promise<any[]> {
    return httpClient.get(`/resources/curricular-unit/${curricularUnitId}/type/MATERIAL`)
  }

  static async getSubmissions(curricularUnitId: number): Promise<any[]> {
    return httpClient.get(`/resources/curricular-unit/${curricularUnitId}/type/SUBMISSION`)
  }

  static async downloadFile(resourceId: number): Promise<AxiosResponse<ArrayBuffer>> {
    // Create a separate axios instance to bypass the response interceptor
    const downloadClient = axios.create({
      timeout: 50000,
      baseURL: import.meta.env.VITE_ROOT_API
    })
    
    return downloadClient.get(`/resources/${resourceId}/download`, { 
      responseType: 'arraybuffer'
    })
  }

  static async deleteResource(resourceId: number): Promise<void> {
    return httpClient.delete(`/resources/${resourceId}`)
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
  // Evaluation Grade methods
  static async getEvaluationGrades(evaluationId: number): Promise<any[]> {
    return httpClient.get(`/evaluation-grades/evaluation/${evaluationId}`)
  }

  static async saveEvaluationGrade(evaluationId: number, studentEnrollmentId: number, grade: number): Promise<any> {
    const params = new URLSearchParams({
      evaluationId: evaluationId.toString(),
      studentEnrollmentId: studentEnrollmentId.toString(),
      grade: grade.toString()
    })
    return httpClient.post(`/evaluation-grades`, null, { params })
  }

  static async requestGradeRevision(gradeId: number, reason: string): Promise<any> {
    const params = new URLSearchParams({ reason })
    return httpClient.put(`/evaluation-grades/${gradeId}/request-revision`, null, { params })
  }
}

httpClient.interceptors.request.use((request) => request, RemoteServices.handleError)
httpClient.interceptors.response.use((response) => response.data, RemoteServices.handleError)
