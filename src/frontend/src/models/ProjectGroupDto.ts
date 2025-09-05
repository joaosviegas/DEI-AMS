import PersonDto from './PersonDto'

// DTO for Project Groups
export default class ProjectGroupDto {
  id?: number
  projectId: number
  name?: string
  members: PersonDto[]
  createdAt: string // ISO date string

  constructor(
    projectId: number,
    members: PersonDto[] = [],
    createdAt: string = new Date().toISOString(),
    id?: number,
    name?: string
  ) {
    this.id = id
    this.projectId = projectId
    this.name = name
    this.members = members
    this.createdAt = createdAt
  }

  // Factory method to create from backend response
  static fromBackend(data: any): ProjectGroupDto {
    return new ProjectGroupDto(
      data.projectId,
      data.members ? data.members.map((member: any) => new PersonDto(member)) : [],
      data.createdAt,
      data.id,
      data.name
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
