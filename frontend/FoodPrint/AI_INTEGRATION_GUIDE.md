# 🚀 AI Feature Integration Testing Guide

## ✅ Completed Integrations

### 1. AI Service layer
- **File**: `src/services/ai.ts`
- **Feature**: 
  - `analyzeText()` - call the text analysis API
  - `analyzeImage()` - call the image analysis API
  - `extractAssistantText()` - Response adaptation

### 2. Authentication service layer
- **File**: `src/services/auth.ts`
- **Feature**: 
  - `getUserToken()` - get the authentication token (currently mocked)
  - `isDevelopmentMode()` - Development mode detection
  - Reserve an interface for real authentication integration

### 3. Chat Utilities
- **File**: `src/services/chatUtils.ts`
- **Features:** ChatMessage format adapters and creation helpers

### 4. RecordScreen UI Integration ✅ Complete
- **Text Description**: Connect to `POST /api/ai/analyze`
- **Photo Scan**: Connect to `POST /api/ai/analyze/image` 
- **AI Chat**: Connect to `POST /api/ai/analyze`
- **UI states:** loading, error, and success
- **Authentication:** token retrieval and validation

---

## 🔧 Pending Configuration

### 1. Real JWT Authentication Integration ⚠️ Important
**Location:** the `getUserToken()` function in `src/services/auth.ts`
```typescript
// Replace with real Supabase authentication:
const { data: { session } } = await supabaseClient.auth.getSession();
return session?.access_token || null;
```

### 2. Network Address Configuration (Physical Device Debugging)
**File**: `.env` 
```bash
# For physical device debugging, replace localhost with the computer LAN IP
EXPO_PUBLIC_API_BASE_URL=http://192.168.1.100:8080
```

---

## 🧪 Testing Steps

### 1. Start the backend
```bash
cd SHIT_CODE/backend
# Set environment variables (requires a real GEMINI_API_KEY)
export GEMINI_API_KEY=your_key_here
mvn spring-boot:run
```

### 2. Start the frontend
```bash
cd FoodPrint_fe
pnpm start
```

### 3. Testing Text Description
1. Click "Text Description" tab
2. Enter text, such as: "I ate chicken breast and broccoli today"
3. Click "Analyze Now"
4. Observe:
   - The button changes to "Analyzing..." and displays a loading indicator.
   - On success, an Alert displays the results.

### 4. Testing Photo Scan  
1. Click "Photo Scan" tab
2. Select "Take Photo" or "Upload from Gallery".
3. Select a food image
4. Observe:
   - Start analysis automatically and display loading
   - On success, an Alert displays the recognition results.

### 5. Testing AI Chat
1. Click the chat button at the bottom right
2. Enter a message, such as: "Hello, I would like to learn about healthy eating"
3. Click Send
4. Observe:
   - The user message appears immediately
   - The Send button enters the loading state.
   - AI The reply appears in the chat area

---

## 🐛 Troubleshooting

### 1. Authentication Issues
**Symptom:** a "Please log in first" message appears or token retrieval fails.
**Solution**: 
1. A mock token is currently used; check `src/services/auth.ts`.
2. Production requires real Supabase authentication.
3. Development mode displays a "Using mock token" notice.

### 2. Network connection failed
**Symptom:** a network error message appears.
**Troubleshooting order**:
1. Check whether the backend is running (`http://localhost:8080`)
2. Check `EXPO_PUBLIC_API_BASE_URL` in `.env`.
3. On a physical device, replace `localhost` with the computer LAN IP.
4. Check the console for detailed network error logs

### 3. Backend API Errors
**Symptom:** HTTP 4xx/5xx errors
**Troubleshoot**:
1. Check whether `GEMINI_API_KEY` is set.
2. Check backend logs
3. Check whether the backend accepts the mock token.
4. Check the request format

### 4. Image upload failed
**Symptom:** image processing fails.
**Possible causes**:
1. Unsupported image format (only JPEG and PNG are supported)
2. File too large (backend limit: 10 MB)
3. multipart/form-data Invalid format
4. Missing camera or photo library permissions on the physical device

---

## 📝 Next Tasks

### 🔴 High Priority (Required)
1. **Supabase authentication:** replace the mock implementation in `src/services/auth.ts`.
2. **Backend JWT validation:** ensure the backend accepts frontend authentication tokens.
3. **Error handling:** provide clearer messages and retries.

### 🟡 Medium Priority (Recommended)
1. **Results UI:** improve AI analysis result display instead of relying only on Alerts.
2. **Offline caching:** save chat history and analysis records.
3. **Image preview:** show the uploaded image before and after analysis.
4. **Loading states:** use smoother loading animations.

### 🟢 Low Priority (Optional)
1. **Export results:** save analysis to the food calendar or local storage.
2. **Performance:** deduplicate and cache API calls.
3. **User experience:** add input suggestions, quick phrases, and voice input.
4. **Multilingual support:** add internationalization.

---

## 🚀 Features Ready to Test

You can now test:
- ✅ **Text analysis:** enter a food description to get AI analysis results.
- ✅ **Image recognition:** upload a food image to get recognition results.
- ✅ **AI chat:** have a conversation with AI.
- ✅ **Loading states:** all operations include loading and error handling.

Only requires:
1. Start the backend (set GEMINI_API_KEY).
2. Start the frontend(pnpm start)
3. Choose a platform and test!