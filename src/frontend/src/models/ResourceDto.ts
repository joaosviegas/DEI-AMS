export default interface ResourceDto {
  id: number;
  name: string;
  fileName: string;
  fileSize: number;
  uploadDate: string;
  curricularUnitId: number;
  curricularUnitName: string;
  resourceType: 'MATERIAL' | 'SUBMISSION';
}
