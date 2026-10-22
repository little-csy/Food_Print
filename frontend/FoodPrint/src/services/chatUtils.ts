// src/services/chatUtils.ts
import { AIResponse, extractAssistantText } from './ai'

// Use the existing ChatMessage interface
export interface ChatMessage {
    id: string
    text: string
    sender: 'user' | 'assistant'
    timestamp: Date
}

/**
 * Create a user message
 */
export function createUserMessage(text: string): ChatMessage {
    return {
        id: Date.now().toString(),
        text: text.trim(),
        sender: 'user',
        timestamp: new Date(),
    }
}

/**
 * Create an assistant message
 */
export function createAssistantMessage(text: string): ChatMessage {
    return {
        id: (Date.now() + Math.random()).toString(),
        text,
        sender: 'assistant',
        timestamp: new Date(),
    }
}

/**
 * Create an assistant message from an API response
 */
export function createAssistantMessageFromAPI(
    response: AIResponse
): ChatMessage {
    const text = response.success
        ? extractAssistantText(response)
        : `Sorry, something went wrong: ${response.error || 'Unknown error'}`

    return createAssistantMessage(text)
}

/**
 * Create an error message
 */
export function createErrorMessage(error: string): ChatMessage {
    return createAssistantMessage(`Network error: ${error}`)
}
