export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  status: string;
  role: string;
  violationCount: number;
  createdAt: string;
}

export interface LoginPayload {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  role: string;
}

export interface PostItem {
  id: number;
  title: string;
  content: string;
  status: string;
  authorUsername?: string;
  authorId?: number;
  positiveProb?: number | null;
  negativeProb?: number | null;
  sentiment?: number | null;
  viewCount: number;
  commentCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CommentItem {
  id: number;
  postId: number;
  content: string;
  status: string;
  authorUsername?: string;
  authorId?: number;
  positiveProb?: number | null;
  negativeProb?: number | null;
  sentiment?: number | null;
  createdAt: string;
}

export interface WarningItem {
  id: number;
  contentId: number;
  contentType: string;
  content: string;
  negativeProb: number;
  status: string;
  createdAt: string;
}

export interface AuditRecordItem {
  id: number;
  warningId: number;
  contentId: number;
  contentType: string;
  adminId: number;
  adminUsername: string;
  result: string;
  remark: string;
  createdAt: string;
}

export interface StatItem {
  totalPostCount: number;
  todayPostCount: number;
  pendingPostCount: number;
  publishedPostCount: number;
  totalUserCount: number;
  activeUserCount: number;
  bannedUserCount: number;
  adminUserCount: number;
  todayWarningCount: number;
  pendingWarningCount: number;
  totalAuditCount: number;
  positivePostCount: number;
  neutralPostCount: number;
  negativePostCount: number;
}

export interface LoginForm {
  username: string;
  password: string;
}

export interface RegisterForm extends LoginForm {
  email: string;
}

export interface PostForm {
  title: string;
  content: string;
}

export interface CommentForm {
  content: string;
}

export interface AuditForm {
  result: 'PASS' | 'REJECT';
  remark: string;
}
