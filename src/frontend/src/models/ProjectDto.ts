import EvaluationDto from './EvaluationDto'

// DTO for Project evaluations
export default class ProjectDto extends EvaluationDto {
  maxGroupSize?: number
  description?: string
  allowedExtensions?: string
  maxFileSize?: number
  submissionDeadline: string // ISO date string
  curricularUnitName?: string
  isCompleted: boolean
  isUpcoming: boolean
  isIndividual: boolean
  isGroupProject: boolean
  isSubmissionOpen: boolean
  isSubmissionClosed: boolean
  groupCount: number
  submissionCount: number

  constructor(
    title: string,
    date: string,
    weight: number,
    curricularUnitId: number,
    submissionDeadline: string,
    isCompleted: boolean = false,
    isUpcoming: boolean = false,
    isIndividual: boolean = true,
    isGroupProject: boolean = false,
    isSubmissionOpen: boolean = true,
    isSubmissionClosed: boolean = false,
    groupCount: number = 0,
    submissionCount: number = 0,
    id?: number,
    maxGroupSize?: number,
    description?: string,
    allowedExtensions?: string,
    maxFileSize?: number,
    curricularUnitName?: string
  ) {
    super(title, date, weight, curricularUnitId, 'PROJECT', id)
    this.submissionDeadline = submissionDeadline
    this.isCompleted = isCompleted
    this.isUpcoming = isUpcoming
    this.isIndividual = isIndividual
    this.isGroupProject = isGroupProject
    this.isSubmissionOpen = isSubmissionOpen
    this.isSubmissionClosed = isSubmissionClosed
    this.groupCount = groupCount
    this.submissionCount = submissionCount
    this.maxGroupSize = maxGroupSize
    this.description = description
    this.allowedExtensions = allowedExtensions
    this.maxFileSize = maxFileSize
    this.curricularUnitName = curricularUnitName
  }

  // Factory method to create from backend response
  static fromBackend(data: any): ProjectDto {
    return new ProjectDto(
      data.title,
      data.date,
      data.weight,
      data.curricularUnitId,
      data.submissionDeadline,
      data.isCompleted || false,
      data.isUpcoming || false,
      data.isIndividual || true,
      data.isGroupProject || false,
      data.isSubmissionOpen || true,
      data.isSubmissionClosed || false,
      data.groupCount || 0,
      data.submissionCount || 0,
      data.id,
      data.maxGroupSize,
      data.description,
      data.allowedExtensions,
      data.maxFileSize,
      data.curricularUnitName
    )
  }

  // Helper methods for UI logic
  getFormattedFileSize(): string {
    if (!this.maxFileSize) return 'No limit'
    
    const bytes = this.maxFileSize
    if (bytes < 1024) return `${bytes} B`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`
  }

  getAllowedExtensionsArray(): string[] {
    if (!this.allowedExtensions) return []
    return this.allowedExtensions.split(',').map(ext => ext.trim())
  }

  getProjectTypeLabel(): string {
    return this.isIndividual ? 'Individual' : `Group (max ${this.maxGroupSize || 'unlimited'})`
  }

  getStatusLabel(): string {
    if (this.isCompleted) return 'Completed'
    if (this.isUpcoming) return 'Upcoming'
    if (this.isSubmissionClosed) return 'Submission Closed'
    if (this.isSubmissionOpen) return 'Submission Open'
    return 'Unknown'
  }

  getStatusColor(): string {
    if (this.isCompleted) return 'success'
    if (this.isUpcoming) return 'warning'
    if (this.isSubmissionClosed) return 'error'
    if (this.isSubmissionOpen) return 'primary'
    return 'default'
  }
}
