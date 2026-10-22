import { getSupabaseAccessToken } from '../lib/supabase'
import { ApiError, apiPost, getEffectiveBaseUrl } from './client'
import type {
    AIResponseDto,
    AiLogSaveRequest,
    AnalyzeRequest,
    ApiResponse,
    FoodLogDto,
} from './types'

const BASE = 'api/ai'

/** POST /api/ai/analyze - Unified analysis (text / image URL) */
export async function analyze(
    body: AnalyzeRequest
): Promise<ApiResponse<AIResponseDto>> {
    return apiPost<AIResponseDto>(`${BASE}/analyze`, body)
}

/**
 * POST /api/ai/log - Save AI analysis, including user edits to analysisResult
 * Header: userId (consistent with api/food-logs; identifies the owning user)
 */
export async function saveAiAnalysisLog(
    body: AiLogSaveRequest,
    userId: string
): Promise<ApiResponse<FoodLogDto | null>> {
    return apiPost<FoodLogDto | null>(`${BASE}/log`, body, {
        headers: { userId },
    })
}

/** Use Blob for Web; for React Native photo library / camera uploads, use an object with `uri` */
export type AnalyzeImageNativeFile = {
    uri: string
    name?: string
    type?: string
}

export type AnalyzeImageInput = Blob | AnalyzeImageNativeFile

function isNativePickerFile(x: AnalyzeImageInput): x is AnalyzeImageNativeFile {
    return (
        typeof x === 'object' &&
        x !== null &&
        'uri' in x &&
        typeof (x as AnalyzeImageNativeFile).uri === 'string'
    )
}

/**
 * POST /api/ai/analyze/image - Image upload analysis（multipart/form-data）
 * - Web: Pass `Blob`
 * - React Native: pass `{ uri, type?, name? }`; avoid `fetch(fileUri).blob()`, which often fails on Android
 */
export async function analyzeImage(
    image: AnalyzeImageInput,
    filename = 'image.jpg'
): Promise<ApiResponse<AIResponseDto>> {
    const formData = new FormData()
    if (typeof Blob !== 'undefined' && image instanceof Blob) {
        formData.append('image', image, filename)
    } else if (isNativePickerFile(image)) {
        const name = image.name ?? filename
        const type = image.type ?? 'image/jpeg'
        formData.append('image', {
            uri: image.uri,
            name,
            type,
        } as unknown as Blob)
    } else {
        throw new TypeError(
            'analyzeImage: expected Blob or { uri, name?, type? }'
        )
    }

    const token = await getSupabaseAccessToken()
    const url = `${getEffectiveBaseUrl()}/${BASE}/analyze/image`
    const headers: Record<string, string> = {}
    if (token) headers['Authorization'] = `Bearer ${token}`

    const res = await fetch(url, {
        method: 'POST',
        headers,
        body: formData,
    })

    const data = (await res.json()) as ApiResponse<AIResponseDto>
    if (!res.ok) {
        throw new ApiError(res.status, data.message, data)
    }
    return data
}
