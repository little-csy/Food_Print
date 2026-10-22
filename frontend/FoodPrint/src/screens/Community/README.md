# Community Screen Modular Structure

## Directory Structure

```
screens/Community/
├── index.ts                  # Main export file
├── CommunityScreen.tsx       # Main screen component (simplified version)
├── types.ts                  # TypeScript Type definitions
├── constants.ts              # Constants and sample data
├── styles.ts                 # All stylesheets (pending migration)
├── components/
│   ├── index.ts
│   ├── ChallengeCard.tsx     # Challenge Card component
│   ├── PostCard.tsx          # Post Card component
│   ├── ChallengeDetailModal.tsx   # Challenge details modal
│   ├── CreateChallengeModal.tsx   # 4 Step creation wizard modal
│   └── CreatePostModal.tsx        # Create post modal
└── hooks/
    ├── index.ts
    └── useCommunityState.ts  # Centralized state management Hook
```

## Usage

### Import the main component directly
```typescript
import { CommunityScreen } from 'src/screens/Community'
```

### Import specific types and constants
```typescript
import { CommunityChallenge, Post, SAMPLE_CHALLENGES } from 'src/screens/Community'
```

### Use in Other Screens Hook
```typescript
import { useCommunityState } from 'src/screens/Community'

const state = useCommunityState()
// Community state can now be shared across multiple screens
```

## File Descriptions

### types.ts
- `TopParticipant`: Participant information
- `Badge`: Badge data
- `CommunityChallenge`: Main challenge data
- `Post`: Post data
- `CreateChallengeFormData`: Creation form data
- `ChallengeType`, `DurationOption`, `BadgeDesign`: Creation form option types

### constants.ts
- `SAMPLE_CHALLENGES`: 5 Preset challenges
- `SAMPLE_POSTS`: 3 Sample posts
- `CHALLENGE_TYPES`: 5 Challenge types
- `DURATION_OPTIONS`: 6 Duration options
- `BADGE_DESIGNS`: 9 Badge designs
- `EMOJI_CATEGORIES`: 9 Quick emoji reactions
- `MEAL_CATEGORIES`: 8 Food categories

### hooks/useCommunityState.ts
Manage all state centrally:
- Challenge Related: challenges, selectedChallenge, showChallengeDetail, scrollOffset
- Post Related: posts, showCreatePost
- Creation form: createStep, formData, titleError, descError
- All action functions: scroll, handleJoinChallenge, handleCreatePost, etc.

### components/
Independent components receive the required props and are easy to test and reuse individually:
- **ChallengeCard**: Display a single challenge card
- **PostCard**: Display a single post card
- **ChallengeDetailModal**: challenge details modal (pending migration)
- **CreateChallengeModal**: four-step creation wizard modal (pending migration)
- **CreatePostModal**: create post modal (pending migration)

## Migration Plan

### Phase One (✅ Complete)
- ✅ Create folders and index.ts
- ✅ Extract types.ts
- ✅ Extract constants.ts
- ✅ Create useCommunityState Hook
- ✅ Create ChallengeCard and PostCard components.

### Phase Two (Pending)
- ⏳ Create styles.ts and export all styles.
- ⏳ Extract the ChallengeDetailModal component.
- ⏳ Extract the CreateChallengeModal component.
- ⏳ Extract the CreatePostModal component.

### Phase Three (Pending)
- ⏳ Rewrite CommunityScreen.tsx
  - Using useCommunityState Hook
  - Import all components
  - Import all styles
  - Reduce the file to 200–300 lines.

### Phase Four (Pending)
- ⏳ Delete the original CommunityScreen.tsx file.
- ⏳ Update the import path in navigation/AppNavigator.tsx.
- ⏳ Test all features

## Benefits

1. **Code separation:** each file has one responsibility and is easy to maintain.
2. **Reusability:** components and hooks can be used on other screens.
3. **Type safety:** centralized type definitions.
4. **Testability:** small independent components are easier to unit test.
5. **Extensibility:** new features require only new components or hooks.
6. **Performance:** import on demand to reduce initial loading.

## Future Improvements

- Add more tests
- Performance Optimization(memoization)
- Add error handling
- Add loading states
- Integrate the actual API
