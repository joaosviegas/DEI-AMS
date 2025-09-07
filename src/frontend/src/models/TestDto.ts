import EvaluationDto from './EvaluationDto'

// DTO for Test evaluations
export default class TestDto extends EvaluationDto {
  constructor(
    title: string,
    date: string,
    weight: number,
    curricularUnitId: number,
    revisionDeadline: string,
    id?: number
  ) {
    super(title, date, weight, curricularUnitId, 'TEST', revisionDeadline, id)
  }

  // Factory method to create from backend response
  static fromBackend(data: any): TestDto {
    return new TestDto(
      data.title,
      data.date,
      data.weight,
      data.curricularUnitId,
      data.revisionDeadline,
      data.id
    )
  }
}
