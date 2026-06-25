// 类型定义 — 与后端 DTO 对应，前端不存储任何业务数据
export interface Course {
  id: number; name: string; code: string; instructor: string; courseTime: string;
  weekday: number; location: string; credits: number; category: string; color: string;
  categoryLabel: string; colorClass: string;
}
export interface CardInfo {
  id: number; studentId: number; balance: number; cardNo: string;
  studentName: string; studentNo: string; recentTransactions: Transaction[];
}
export interface Transaction {
  id: number; item: string; amount: number; type: string; category: string;
  location: string; createdAt: string; timeLabel: string;
}
export interface LibraryBook {
  id: number; title: string; author: string; isbn: string; coverColor: string;
  dueDate: string; daysRemaining: number; progress: number; renewed: number;
}
export interface CampusMoment {
  id: number; title: string; tag: string; location: string; likes: number;
  joined: number; maxAttendees: number | null; currentAttendees: number;
  timeLabel: string; createdAt: string;
}
export interface Seat {
  id: number; seatCode: string; floorArea: string; status: string; statusLabel: string;
  bookedStart: string | null; bookedEnd: string | null; timeLabel: string;
}
export interface AcademicProfile {
  gpa: number; maxGpa: number; creditsEarned: number; creditsRequired: number;
  progressPercent: number; strengths: SubjectStrength[];
}
export interface SubjectStrength { subject: string; val: number; }
export interface ChatMessage { id: number; role: 'user' | 'assistant'; content: string; createdAt: string; }
export interface ChatResponse { text: string; messages: ChatMessage[]; }

/** 个人总览聚合数据 */
export interface PersonalOverview {
  studentName: string;
  studentNo: string;
  department: string;
  bookedSeat: Seat | null;
  borrowedBooks: LibraryBook[];
  borrowedCount: number;
  maxBorrowLimit: number;
  joinedEvents: CampusMoment[];
  todayCourses: Course[];
  todayLabel: string;
  semesterInfo: string;
}
