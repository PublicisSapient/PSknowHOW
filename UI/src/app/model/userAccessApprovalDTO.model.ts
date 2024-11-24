export interface UserAccessApprovalResponseDTO {
  message: string;
  success: boolean;
  data: any[];
}

export interface UserAccessApprovalDTO {
  username: string;
  email: string;
  approved: boolean;
  whitelistDomainEmail: boolean;
}

export interface UserAccessReqPayload {
  status: string;
  role: string;
  message: string;
  userName: string;
}
