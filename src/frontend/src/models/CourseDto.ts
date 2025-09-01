export default class CourseDto {
  id?: number
  code?: string
  name?: string
  duration?: number

  constructor(obj?: Partial<CourseDto>) {
    Object.assign(this, obj)
} 
}