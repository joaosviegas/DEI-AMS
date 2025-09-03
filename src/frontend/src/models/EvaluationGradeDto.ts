import PersonDto from './PersonDto'

// DTO for evaluation grades
export default class EvaluationGradeDto {
  id?: number
  evaluationId: number
  student: PersonDto
  grade: number | null
  gradedAt: string | null
  comments: string | null
  revisionRequested: boolean
  revisionReason: string | null
  revisionRequestedAt: string | null

  constructor(
    evaluationId: number,
    student: PersonDto,
    grade: number | null = null,
    gradedAt: string | null = null,
    comments: string | null = null,
    revisionRequested: boolean = false,
    revisionReason: string | null = null,
    revisionRequestedAt: string | null = null,
    id?: number
  ) {
    this.id = id
    this.evaluationId = evaluationId
    this.student = student
    this.grade = grade
    this.gradedAt = gradedAt
    this.comments = comments
    this.revisionRequested = revisionRequested
    this.revisionReason = revisionReason
    this.revisionRequestedAt = revisionRequestedAt
  }

  // Factory method to create from backend response
  static fromBackend(data: any): EvaluationGradeDto {
    return new EvaluationGradeDto(
      data.evaluationId,
      new PersonDto(data.student),
      data.grade,
      data.gradedAt,
      data.comments,
      data.revisionRequested,
      data.revisionReason,
      data.revisionRequestedAt,
      data.id
    )
  }

  // Check if grade is passing (>= 10.0)
  isPassing(): boolean {
    return this.grade !== null && this.grade >= 10.0
  }

  // Check if grade has been assigned
  hasGrade(): boolean {
    return this.grade !== null
  }
}
