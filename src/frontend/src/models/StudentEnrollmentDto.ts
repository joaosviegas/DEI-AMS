import PersonDto from './PersonDto'

export default class StudentEnrollmentDto {
  id?: number
  student?: PersonDto
  status?: string // "ENROLLED", "APPROVED", "FAILED"
  enrollmentDate?: string
  finalGrade?: number

  constructor(obj?: Partial<StudentEnrollmentDto>) {
    Object.assign(this, obj)
  }
}
