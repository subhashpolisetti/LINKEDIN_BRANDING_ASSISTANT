// User types
export interface User {
  id: string;
  email: string;
  name?: string;
  picture?: string;
}

// Resume types
export interface Resume {
  id: string;
  text: string;
  filename: string;
  createdAt: string;
  userId: string;
  cachedAnalysis?: string;
}

// Job types
export interface Job {
  id: string;
  title: string;
  companyName: string;
  jobPostingUrl: string;
  description: string;
  location: string;
  skills: string[];
  employmentType: string;
  experienceLevel: string;
  listedAt: string;
  source: string;
  matchScore?: number;
  matchedKeywords?: string[];
  missingKeywords?: string[];
}

// Profile types
export interface Experience {
  title: string;
  company: string;
  duration: string;
  description: string;
}

export interface Education {
  degree: string;
  school: string;
  duration: string;
  description: string;
}

export interface Project {
  name: string;
  description: string;
  technologies: string;
  duration: string;
}

export interface Certification {
  name: string;
  issuer: string;
  date: string;
  description: string;
}

export interface Award {
  name: string;
  issuer: string;
  date: string;
  description: string;
}

export interface Recommendation {
  text: string;
  recommender: string;
}

export interface Profile {
  name: string;
  headline: string;
  location: string;
  about: string;
  profilePicture?: string;
  profileStrength?: number;
  experience: Experience[];
  education: Education[];
  projects: Project[];
  certifications: Certification[];
  skills: string[];
  awards: Award[];
  recommendations: Recommendation[];
}

// Analysis types
export interface JobAnalysis {
  matchScore: string;
  matchedKeywords: string[];
  missingKeywords: string[];
  tailoredPoints?: string[];
}

export interface ProfileAnalysis {
  overallStrength: number;
  sections: {
    basicInfo: number;
    experience: number;
    education: number;
    skills: number;
    additional: number;
  };
  recommendations: Record<string, string>;
}

// API Response types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  error?: string;
}

// Route types
export interface ProtectedRouteProps {
  children: React.ReactNode;
}

// Component Props types
export interface LoadingProps {
  size?: 'small' | 'medium' | 'large';
  message?: string;
}

export interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
}

export interface ConfirmDialogProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
}
