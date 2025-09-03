import PersonDto from './PersonDto'
import CourseDto from './CourseDto'
import StudentEnrollmentDto from './StudentEnrollmentDto'

export default class CurricularUnitDto {
  id?: number
  code?: string
  name?: string
  semester?: string // "FIRST", "SECOND"
  ects?: number
  mainTeacher?: PersonDto
  courses?: CourseDto[]
  assistantTeachers?: PersonDto[]
  studentEnrollments?: StudentEnrollmentDto[]

  constructor(obj?: Partial<CurricularUnitDto>) {
    Object.assign(this, obj)
  }
}