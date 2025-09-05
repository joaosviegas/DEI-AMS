import PersonDto from './PersonDto'
import ProjectGroupDto from './ProjectGroupDto'

// DTO for Project Submissions
export default class ProjectSubmissionDto {
  id?: number
  projectId: number
  group?: ProjectGroupDto
  submittedBy: PersonDto
  originalFilename: string
  storedFilename: string
  fileSize: number
  mimeType?: string
  fileExtension?: string
  submissionDate: string // ISO date string
  comments?: string
  automaticGrade?: number
  automaticFeedback?: string
  isLatest: boolean

  constructor(
    projectId: number,
    submittedBy: PersonDto,
    originalFilename: string,
    storedFilename: string,
    fileSize: number,
    submissionDate: string = new Date().toISOString(),
    isLatest: boolean = true,
    id?: number,
    group?: ProjectGroupDto,
    mimeType?: string,
    fileExtension?: string,
    comments?: string,
    automaticGrade?: number,
    automaticFeedback?: string
  ) {
    this.id = id
    this.projectId = projectId
    this.group = group
    this.submittedBy = submittedBy
    this.originalFilename = originalFilename
    this.storedFilename = storedFilename
    this.fileSize = fileSize
    this.mimeType = mimeType
    this.fileExtension = fileExtension
    this.submissionDate = submissionDate
    this.comments = comments
    this.automaticGrade = automaticGrade
    this.automaticFeedback = automaticFeedback
    this.isLatest = isLatest
  }

  // Factory method to create from backend response
  static fromBackend(data: any): ProjectSubmissionDto {
    return new ProjectSubmissionDto(
      data.projectId,
      new PersonDto(data.submittedBy),
      data.originalFilename,
      data.storedFilename,
      data.fileSize,
      data.submissionDate,
      data.isLatest || true,
      data.id,
      data.group ? ProjectGroupDto.fromBackend(data.group) : undefined,
      data.mimeType,
      data.fileExtension,
      data.comments,
      data.automaticGrade,
      data.automaticFeedback
    )
  }

  // Helper methods
  isGroupSubmission(): boolean {
    return this.group !== undefined
  }

  isIndividualSubmission(): boolean {
    return this.group === undefined
  }

  getFormattedFileSize(): string {
    const bytes = this.fileSize
    if (bytes < 1024) return `${bytes} B`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`
  }

  getSubmissionType(): string {
    return this.isGroupSubmission() ? 'Group' : 'Individual'
  }

  getSubmitterDisplayName(): string {
    return this.submittedBy.name || 'Unknown'
  }

  getSubmissionDateFormatted(): string {
    return new Date(this.submissionDate).toLocaleString()
  }

  hasAutomaticGrade(): boolean {
    return this.automaticGrade !== undefined && this.automaticGrade !== null
  }

  getStatusLabel(): string {
    if (this.isLatest) return 'Current'
    return 'Previous Version'
  }

  getStatusColor(): string {
    if (this.isLatest) return 'primary'
    return 'secondary'
  }
}
