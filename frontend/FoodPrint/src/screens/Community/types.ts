// Community Related type definitions

export interface TopParticipant {
  name: string;
  avatar: string;
  progress: number;
}

export interface Badge {
  name: string;
  image: string;
  description: string;
  icon: string;
  badgeGradient: string;
}

export interface CommunityChallenge {
  id: string;
  userChallengeId?: string;
  title: string;
  description: string;
  duration: string;
  participants: number;
  color: string;
  joined: boolean;
  fullDescription: string;
  badge: Badge;
  topParticipants: TopParticipant[];
  icon: string;
  streak?: number;
  status?: string;
  startDate?: string;
  endDate?: string;
  completedDays?: number;
  progressPercent?: number;
  checkins?: ChallengeCheckin[];
  type?: string;
  targetValue?: number;
}

export interface ChallengeCheckin {
  date: string;
  passed: boolean;
  actualValue: number;
  note: string;
}

export interface Comment {
  id: string;
  authorId?: string;
  authorName: string;
  authorImage: string;
  text: string;
  timestamp: string;
}

export interface Post {
  id: string;
  authorId: string;
  authorName: string;
  authorImage: string;
  timestamp: string;
  text: string;
  image: string;
  likes: number;
  comments: number;
  userLiked?: boolean;
  commentList?: Comment[];
}

export interface CreateChallengeFormData {
  type: string;
  title: string;
  description: string;
  duration: string;
  badge: number;
  color: string;
  gradient: string;
  selectedTypeData: any;
}

export interface ChallengeType {
  id: string;
  name: string;
  description: string;
  icon: string;
  color: string;
  gradient: string;
}

export interface DurationOption {
  label: string;
  value: string;
  description: string;
}

export interface BadgeDesign {
  icon: string;
  gradient: string;
  name: string;
}

// Challenge Detailed type configuration
export interface ChallengeTypeConfig {
  type: 'CALORIE_CONTROL' | 'PROTEIN_CHAMPION' | 'LOG_STREAK' | 'LOW_CARB' | 'LIGHT_EATER';
  title: string;
  description: string;
  icon: string;
  color: string;
  durationDays: number;
  targetValue: number;
}
