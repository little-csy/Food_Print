import { CommunityChallenge, Post, ChallengeType, DurationOption, BadgeDesign } from './types'

// 5 Preset challenges
export const SAMPLE_CHALLENGES: CommunityChallenge[] = [
    {
        id: '1',
        title: 'Daily Check-in',
        description: 'Keep your streak going!',
        duration: 'Ongoing',
        participants: 1250,
        color: '#8BA888',
        joined: true,
        fullDescription: 'Check in daily to maintain your wellness streak. Every day counts towards a healthier lifestyle.',
        icon: '🔥',
        streak: 8,
        badge: {
            name: 'Streak Master',
            image: '🔥',
            description: 'Maintain a 7-day check-in streak',
            icon: '🔥',
            badgeGradient: '#FFF8E7',
        },
        topParticipants: [
            { name: 'Sarah J.', avatar: 'https://i.pravatar.cc/32?img=1', progress: 45 },
            { name: 'Mike C.', avatar: 'https://i.pravatar.cc/32?img=2', progress: 38 },
            { name: 'Emma W.', avatar: 'https://i.pravatar.cc/32?img=3', progress: 32 },
        ],
    },
    {
        id: '2',
        title: 'Zero Sugar Week',
        description: 'Challenge yourself to avoid sugars for 7 days',
        duration: '7 Days',
        participants: 847,
        color: '#F4A261',
        joined: false,
        fullDescription: 'Eliminate added sugars from your diet for 7 consecutive days and feel the difference in your energy levels.',
        icon: '🍫',
        badge: {
            name: 'Sugar Free',
            image: '🍫',
            description: 'Complete a 7-day sugar-free challenge',
            icon: '🍫',
            badgeGradient: '#FFF3E0',
        },
        topParticipants: [
            { name: 'John D.', avatar: 'https://i.pravatar.cc/32?img=4', progress: 100 },
            { name: 'Lisa M.', avatar: 'https://i.pravatar.cc/32?img=5', progress: 85 },
            { name: 'Alex K.', avatar: 'https://i.pravatar.cc/32?img=6', progress: 71 },
        ],
    },
    {
        id: '3',
        title: 'Hydration Week',
        description: 'Drink 8 glasses of water daily for 7 days',
        duration: '7 Days',
        participants: 956,
        color: '#5BA8C4',
        joined: false,
        fullDescription: 'Stay hydrated by drinking 8 glasses of water daily. Track your water intake and transform your health.',
        icon: '💧',
        badge: {
            name: 'Hydration Hero',
            image: '💧',
            description: 'Complete a 7-day hydration challenge',
            icon: '💧',
            badgeGradient: '#E3F2FD',
        },
        topParticipants: [
            { name: 'Tom B.', avatar: 'https://i.pravatar.cc/32?img=7', progress: 100 },
            { name: 'Nina P.', avatar: 'https://i.pravatar.cc/32?img=8', progress: 92 },
            { name: 'Chris L.', avatar: 'https://i.pravatar.cc/32?img=9', progress: 85 },
        ],
    },
    {
        id: '4',
        title: 'Plant-Based Challenge',
        description: '5 days of plant-based eating',
        duration: '5 Days',
        participants: 623,
        color: '#A8C5A0',
        joined: false,
        fullDescription: 'Explore plant-based nutrition by eating only plant-based foods for 5 consecutive days.',
        icon: '🌱',
        badge: {
            name: 'Plant Powered',
            image: '🌱',
            description: 'Complete a 5-day plant-based challenge',
            icon: '🌱',
            badgeGradient: '#F1F8F4',
        },
        topParticipants: [
            { name: 'Rachel G.', avatar: 'https://i.pravatar.cc/32?img=10', progress: 100 },
            { name: 'David Y.', avatar: 'https://i.pravatar.cc/32?img=11', progress: 80 },
            { name: 'Sophie T.', avatar: 'https://i.pravatar.cc/32?img=12', progress: 60 },
        ],
    },
    {
        id: '5',
        title: 'Protein Power Month',
        description: '30 days of meeting your protein goals',
        duration: '30 Days',
        participants: 1089,
        color: '#C47BA0',
        joined: false,
        fullDescription: 'Build muscle and strength by meeting your daily protein targets for 30 consecutive days.',
        icon: '💪',
        badge: {
            name: 'Protein Powerhouse',
            image: '💪',
            description: 'Complete a 30-day protein challenge',
            icon: '💪',
            badgeGradient: '#FCE8F3',
        },
        topParticipants: [
            { name: 'Mark S.', avatar: 'https://i.pravatar.cc/32?img=13', progress: 100 },
            { name: 'Jason R.', avatar: 'https://i.pravatar.cc/32?img=14', progress: 90 },
            { name: 'Kyle M.', avatar: 'https://i.pravatar.cc/32?img=15', progress: 75 },
        ],
    },
]

// Sample community feed data
export const SAMPLE_POSTS: Post[] = [
    {
        id: '1',
        authorId: 'user1',
        authorName: 'Sarah Johnson',
        authorImage: 'https://i.pravatar.cc/48?img=1',
        timestamp: '2 hours ago',
        text: 'Hit my goal for the 5th day in a row! 💪 Grilled salmon and veggies for dinner tonight.',
        image: 'https://picsum.photos/400/300?random=1',
        likes: 124,
        comments: 8,
        userLiked: false,
    },
    {
        id: '2',
        authorId: 'user2',
        authorName: 'Mike Chen',
        authorImage: 'https://i.pravatar.cc/48?img=2',
        timestamp: '4 hours ago',
        text: 'Started my morning with a protein smoothie bowl 🥤 Ready to crush today!',
        image: 'https://picsum.photos/400/300?random=2',
        likes: 89,
        comments: 5,
        userLiked: false,
    },
    {
        id: '3',
        authorId: 'user3',
        authorName: 'Emma Wilson',
        authorImage: 'https://i.pravatar.cc/48?img=3',
        timestamp: '6 hours ago',
        text: 'Day 3 of my hydration challenge! Feeling refreshed and energized 💧',
        image: 'https://picsum.photos/400/300?random=3',
        likes: 156,
        comments: 12,
        userLiked: false,
    },
]

// Challenge type constants
export const CHALLENGE_TYPES: ChallengeType[] = [
    {
        id: 'nutrition',
        name: 'Nutrition Focus',
        description: 'Dietary goals and eating habits',
        icon: '🥗',
        color: '#C8DCC6',
        gradient: '#8BA888',
    },
    {
        id: 'hydration',
        name: 'Hydration',
        description: 'Water intake and staying hydrated',
        icon: '💧',
        color: '#B8D8E5',
        gradient: '#5BA8C4',
    },
    {
        id: 'restriction',
        name: 'Food Restriction',
        description: 'Eliminate specific foods',
        icon: '🍬',
        color: '#F8E5D3',
        gradient: '#F4A261',
    },
    {
        id: 'fitness',
        name: 'Fitness & Exercise',
        description: 'Physical activity goals',
        icon: '💪',
        color: '#E8C4D8',
        gradient: '#C47BA0',
    },
    {
        id: 'habit',
        name: 'Healthy Habits',
        description: 'Build lasting wellness habits',
        icon: '✨',
        color: '#A8C5A0',
        gradient: '#8BA888',
    },
]

// Duration options
export const DURATION_OPTIONS: DurationOption[] = [
    { label: '3 Days', value: '3', description: 'Quick start challenge' },
    { label: '5 Days', value: '5', description: 'Weekday commitment' },
    { label: '7 Days', value: '7', description: 'One full week' },
    { label: '14 Days', value: '14', description: 'Two week challenge' },
    { label: '21 Days', value: '21', description: 'Habit forming period' },
    { label: '30 Days', value: '30', description: 'Monthly commitment' },
]

// Badge designs
export const BADGE_DESIGNS: BadgeDesign[] = [
    { icon: '🏆', gradient: '#F4A261', name: 'Champion' },
    { icon: '🏅', gradient: '#8BA888', name: 'Achiever' },
    { icon: '🔥', gradient: '#E76F51', name: 'On Fire' },
    { icon: '⭐', gradient: '#5BA8C4', name: 'Star' },
    { icon: '❤️', gradient: '#C47BA0', name: 'Wellness' },
    { icon: '🎯', gradient: '#E9967A', name: 'Bullseye' },
    { icon: '🌿', gradient: '#8BA888', name: 'Nature' },
    { icon: '📈', gradient: '#5BA8C4', name: 'Growth' },
    { icon: '⚡', gradient: '#F4A261', name: 'Energy' },
]

// Quick emoji reactions
export const EMOJI_CATEGORIES = ['💪', '🥗', '🔥', '✨', '🎯', '💚', '🥑', '🍎', '⭐']

// Post Creation-related constants
export const MEAL_CATEGORIES = [
    { emoji: '🥗', label: 'Salad' },
    { emoji: '🐟', label: 'Salmon' },
    { emoji: '🥤', label: 'Smoothie' },
    { emoji: '🥑', label: 'Toast' },
    { emoji: '🍚', label: 'Rice Bowl' },
    { emoji: '🍓', label: 'Fruits' },
    { emoji: '🍱', label: 'Meal Prep' },
    { emoji: '🍝', label: 'Pasta' },
]
