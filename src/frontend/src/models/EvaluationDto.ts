// Abstract base class for evaluation DTOs
export default abstract class EvaluationDto {
  id?: number
  title: string
  date: string // ISO date string
  weight: number
  curricularUnitId: number
  type: 'TEST' | 'PROJECT'

  constructor(
    title: string,
    date: string,
    weight: number,
    curricularUnitId: number,
    type: 'TEST' | 'PROJECT',
    id?: number
  ) {
    this.id = id
    this.title = title
    this.date = date
    this.weight = weight
    this.curricularUnitId = curricularUnitId
    this.type = type
  }
}
