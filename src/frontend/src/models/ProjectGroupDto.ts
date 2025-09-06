import PersonDto from './PersonDto'

// DTO for Project Groups
export default class ProjectGroupDto {
  id?: number
  projectId: number
  projectTitle?: string
  groupName?: string
  name?: string // Keep for backward compatibility
  members: PersonDto[]
  createdAt: string // ISO date string
  finalGrade?: number | null
  memberCount?: number
  maxGroupSize?: number
  isFull?: boolean
  isEmpty?: boolean
  canAddMoreMembers?: boolean
  latestSubmission?: any

  constructor(
    projectId: number,
    members: PersonDto[] = [],
    createdAt: string = new Date().toISOString(),
    id?: number,
    name?: string,
    groupName?: string,
    finalGrade?: number | null,
    projectTitle?: string,
    memberCount?: number,
    maxGroupSize?: number,
    isFull?: boolean,
    isEmpty?: boolean,
    canAddMoreMembers?: boolean,
    latestSubmission?: any
  ) {
    this.id = id
    this.projectId = projectId
    this.name = name
    this.groupName = groupName || name // Use groupName if provided, fallback to name
    this.members = members
    this.createdAt = createdAt
    this.finalGrade = finalGrade
    this.projectTitle = projectTitle
    this.memberCount = memberCount
    this.maxGroupSize = maxGroupSize
    this.isFull = isFull
    this.isEmpty = isEmpty
    this.canAddMoreMembers = canAddMoreMembers
    this.latestSubmission = latestSubmission
  }

  // Factory method to create from backend response
  static fromBackend(data: any): ProjectGroupDto {
    return new ProjectGroupDto(
      data.projectId,
      data.members ? data.members.map((member: any) => new PersonDto(member)) : [],
      data.createdAt,
      data.id,
      data.name,
      data.groupName,
      data.finalGrade,
      data.projectTitle,
      data.memberCount,
      data.maxGroupSize,
      data.isFull,
      data.isEmpty,
      data.canAddMoreMembers,
      data.latestSubmission
    )
  }

  // Helper methods
  getMemberCount(): number {
    return this.members.length
  }

  getMemberNames(): string {
    return this.members.map(member => member.name || 'Unknown').join(', ')
  }

  hasMember(personId: number): boolean {
    return this.members.some(member => member.id === personId)
  }

  getGroupDisplayName(): string {
    if (this.name) return this.name
    if (this.members.length === 1) return this.members[0].name || 'Unknown'
    return `Group of ${this.members.length}`
  }
}
