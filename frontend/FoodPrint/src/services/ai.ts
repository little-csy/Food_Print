// src/services/ai.ts
const API_BASE_URL =
    process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080'

export interface AIAnalyzeRequest {
    text?: string
    imageUrl?: string
    userContext?: any
}

export interface AIResponse {
    success: boolean
    data: {
        type:
            | 'FOOD_ANALYSIS'
            | 'DIET_ADVICE'
            | 'DIET_PLAN'
            | 'DIET_ROAST'
            | 'GENERAL_CHAT'
            | 'PROFILE_NEEDED'
        result: any
        message?: string
        conversation?: Array<{
            role: 'user' | 'assistant'
            content: string
        }>
    }
    error?: string
}

/**
 * Analyze text for Text Description and AI Chat
 */
export async function analyzeText(
    text: string,
    token: string
): Promise<AIResponse> {
    try {
        const response = await fetch(`${API_BASE_URL}/api/ai/analyze`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({ text }),
        })

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`)
        }

        const result = await response.json()

        return result
    } catch (error) {
        throw new Error(
            `Analysis failed: ${error instanceof Error ? error.message : 'Unknown error'}`
        )
    }
}

/**
 * Analyze images for Photo Scan
 */
export async function analyzeImage(
    imageUri: string,
    token: string
): Promise<AIResponse> {
    try {
        const formData = new FormData()

        // Handle the URI returned by Expo ImagePicker
        formData.append('image', {
            uri: imageUri,
            type: 'image/jpeg',
            name: 'photo.jpg',
        } as any)

        const response = await fetch(`${API_BASE_URL}/api/ai/analyze/image`, {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${token}`,
                // Do not set Content-Type manually; let the browser set the multipart boundary
            },
            body: formData,
        })

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`)
        }

        const result = await response.json()

        return result
    } catch (error) {
        throw new Error(
            `Image analysis failed: ${error instanceof Error ? error.message : 'Unknown error'}`
        )
    }
}

/**
 * Extract chat content from the AI response
 */
export function extractAssistantText(response: AIResponse): string {
    // Prefer the conversation field when present
    if (response.data.conversation && response.data.conversation.length > 0) {
        const lastAssistant = response.data.conversation
            .filter((msg) => msg.role === 'assistant')
            .pop()
        if (lastAssistant) return lastAssistant.content
    }

    // If a message field exists
    if (response.data.message) {
        return response.data.message
    }

    // If result is a string
    if (typeof response.data.result === 'string') {
        return response.data.result
    }

    // If result is an object, try extracting text or content
    if (typeof response.data.result === 'object' && response.data.result) {
        const result = response.data.result as any
        return (
            result.text ||
            result.content ||
            result.advice ||
            JSON.stringify(result)
        )
    }

    return 'I received your message but could not parse a reply. Please try again in a moment.'
}

/**
 * Normalize the AI response for the analysis screen
 */
export function normalizeAIResponse(response: AIResponse) {
    return {
        type: response.data.type,
        content: response.data.result,
        message: response.data.message,
        success: response.success,
        error: response.error,
    }
}
