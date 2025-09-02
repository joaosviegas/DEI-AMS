import PersonDto from './PersonDto'
import CourseDto from './CourseDto'

export default class CurricularUnitDto {
  id?: number
  code?: string
  name?: string
  semester?: string // "FIRST", "SECOND", "ANNUAL"
  ects?: number
  mainTeacher?: PersonDto
  courses?: CourseDto[]
  assistantTeachers?: PersonDto[]
  students?: PersonDto[]

  constructor(obj?: Partial<CurricularUnitDto>) {
    Object.assign(this, obj)
  }
}