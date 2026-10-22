# FoodPrint Frontend Supabase Integration Guide

This document explains Supabase configuration (Auth and environment variables) and integration testing with the backend API.

---

## 1. Prepare the Supabase Project

### 1. Create a Project

1. Open [Supabase Dashboard](https://supabase.com/dashboard) and log in.
2. Click **New Project**, select an organization, enter the project name, database password, and region, then create it.
3. Wait until the project is ready (about 1–2 minutes).

### 2. Get the URL and Anon Key

1. Within the project, go to **Settings → API**.
2. Copy:
   - **Project URL** → Use as `EXPO_PUBLIC_SUPABASE_URL`
   - **anon public** key → use as `EXPO_PUBLIC_SUPABASE_ANON_KEY` (intended for client use and may be exposed).

### 3. Configure Auth (Email and Password)

1. Go to **Authentication → Providers**.
2. Confirm **Email** is enabled.
3. For email verification, edit confirmation templates under **Authentication → Email Templates**. During development, you can temporarily disable "Confirm email" under **Auth → Settings** for immediate login.

### 4. Match the Backend Configuration

- The backend `SUPABASE_URL` and `SUPABASE_ANON_KEY` must match the frontend configuration for JWT validation.
- For a direct **Supabase DB** connection, configure `SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`, etc. in the backend `.env` (see the backend README).

---

## 2. Frontend Environment Variables

### 1. Create .env

Create `.env` in the **project root** alongside `app.json`; use `.env.example` as a reference:

```bash
EXPO_PUBLIC_SUPABASE_URL=https://xxxxx.supabase.co
EXPO_PUBLIC_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080
```

- For local debugging, `EXPO_PUBLIC_API_BASE_URL` is usually `http://localhost:8080`.
- For physical device debugging, use the computer LAN IP, such as `http://192.168.1.100:8080`, so requests do not go to the device localhost.

### 2. Load .env in Expo

Expo 54 loads `.env` from the project root into `process.env` by default through Metro/Babel. If it does not take effect:

- Confirm `.env` is in the project root and contains no spelling errors.
- Restart the development server: press `r` after `pnpm start`, or run `pnpm start` again.

Code reads variables through `src/lib/env.ts`, supporting both `EXPO_PUBLIC_*` and unprefixed names.

---

## 3. API Layer and Authentication Flow

### 1. Directory Structure

```
src/
├── api/
│   ├── client.ts    # fetch wrapper with JWT (Authorization: Bearer <token>)
│   ├── types.ts     # Matches the backend DTO / ApiResponse
│   ├── user.ts      # GET/PUT /api/users/me, body-data
│   ├── food.ts      # POST/GET/PUT/DELETE /api/food-logs
│   ├── ai.ts        # POST /api/ai/analyze, /api/ai/analyze/image
│   └── index.ts     # Unified exports
├── lib/
│   ├── env.ts       # SUPABASE_URL, SUPABASE_ANON_KEY, API_BASE_URL
│   └── supabase.ts  # createClient, getSupabaseAccessToken()
├── context/
│   └── AuthContext.tsx  # Supabase Auth login / registration / logout and session synchronization
└── types/
    └── supabase.ts  # Database type placeholder (can later generate with supabase gen types)
```

### 2. Authentication Flow Summary

1. Users call `login()` / `register()` from the login or registration screen.
2. **If Supabase is configured:** `AuthContext` calls `supabase.auth.signInWithPassword` / `signUp`. On success, Supabase saves the session using AsyncStorage persistence in this project.
3. Subsequent API requests, such as `getMe()` and `createFoodLog()`, use `getSupabaseAccessToken()` in `api/client.ts` to retrieve the session `access_token` and attach `Authorization: Bearer <token>`.
4. The Spring Boot backend validates the JWT using OAuth2 Resource Server and JWKS, then identifies the current user.

### 3. Calling APIs from a Screen

```ts
import { getMe, updateBodyData } from '../api'
import { createFoodLog } from '../api'
import { useAuth } from '../context/AuthContext'

// Get the current user with JWT; the backend creates the profile automatically
const res = await getMe()
if (res.data) console.log(res.data)

// Update body measurements
await updateBodyData({ heightCm: 170, weightKg: 65, goal: 'maintain' })

// Create a food log from text
await createFoodLog({ text: 'A latte', date: '2026-03-09' })
```

When logged out or the token has expired, `getSupabaseAccessToken()` returns `null` and the backend may return 401. A shared frontend interceptor can redirect to the login screen.

---

## 4. Troubleshooting

| Issue | Possible causes | Solution |
|------|----------|------|
| Login / registration keeps loading or fails | Missing or incorrect Supabase URL / key | Check `.env` and Dashboard → Settings → API |
| Backend returns 401 | Missing or invalid token | Confirm login and a non-null `getSupabaseAccessToken()` value; backend JWKS must match the Supabase project |
| Physical device cannot reach the backend | Using localhost | Set `EXPO_PUBLIC_API_BASE_URL` to the computer LAN IP |
| .env does not take effect | Wrong path or server not restarted | Keep `.env` in the project root and restart `pnpm start` |

---

## 5. Checklist

- [ ] Supabase project created and Auth → Email enabled.
- [ ] URL and anon key copied from Settings → API to `.env`.
- [ ] `EXPO_PUBLIC_SUPABASE_URL`, `EXPO_PUBLIC_SUPABASE_ANON_KEY`, and `EXPO_PUBLIC_API_BASE_URL` filled in the frontend `.env`.
- [ ] Backend configured for the same Supabase project URL, anon key, and database, and running.
- [ ] Registration and login work; `getMe()` or `createFoodLog()` returns 200 rather than 401 after login.

After these steps, the frontend obtains a JWT through Supabase Auth, and the API layer automatically attaches it to backend requests.
