import { useEffect, useState } from 'react'

import { AssistantMarkdown, type AssistantMarkdownProps } from './AssistantMarkdown'

const WORDS_PER_FRAME = 2
const MS_PER_FRAME = 28
/** Display very short content directly to avoid unnecessary animation */
const INSTANT_MAX_LEN = 24

/**
 * Split complete Markdown into frames, appending words in batches to reduce flicker from incomplete ** markup and code blocks.
 */
function buildFrames(full: string, wordsPerFrame: number): string[] {
    if (!full) {
        return ['']
    }
    if (full.length <= INSTANT_MAX_LEN) {
        return [full]
    }

    const tokens = full.split(/(\s+)/)
    const frames: string[] = []
    let acc = ''
    let wordsInChunk = 0

    for (const t of tokens) {
        acc += t
        if (t.trim().length > 0) {
            wordsInChunk++
            if (wordsInChunk >= wordsPerFrame) {
                frames.push(acc)
                wordsInChunk = 0
            }
        }
    }

    if (frames.length === 0 || frames[frames.length - 1] !== full) {
        frames.push(full)
    }

    return frames
}

export type StreamingAssistantMarkdownProps = AssistantMarkdownProps

/**
 * Simulate streaming by progressively revealing sections of already-complete text on the frontend.
 * True incremental model output requires a backend SSE / chunked response.
 */
export function StreamingAssistantMarkdown({
    markdown,
    textColor,
    linkColor,
}: StreamingAssistantMarkdownProps) {
    const [shown, setShown] = useState(() => buildFrames(markdown, WORDS_PER_FRAME)[0] ?? '')

    useEffect(() => {
        const frames = buildFrames(markdown, WORDS_PER_FRAME)
        setShown(frames[0] ?? '')

        if (frames.length <= 1) {
            return undefined
        }

        let i = 0
        const id = setInterval(() => {
            i++
            if (i >= frames.length) {
                clearInterval(id)
                return
            }
            setShown(frames[i])
        }, MS_PER_FRAME)

        return () => clearInterval(id)
    }, [markdown])

    return (
        <AssistantMarkdown markdown={shown} textColor={textColor} linkColor={linkColor} />
    )
}
