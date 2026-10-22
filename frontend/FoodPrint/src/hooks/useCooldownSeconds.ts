import { useCallback, useEffect, useRef, useState } from 'react'

const TICK_MS = 1000

/**
 * Countdown in seconds. For limits such as one verification code per minute, call startCooldown(60) after success.
 */
export function useCooldownSeconds() {
    const [secondsRemaining, setSecondsRemaining] = useState(0)
    const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)

    const clearTimer = useCallback(() => {
        if (timerRef.current != null) {
            clearInterval(timerRef.current)
            timerRef.current = null
        }
    }, [])

    useEffect(() => () => clearTimer(), [clearTimer])

    const startCooldown = useCallback(
        (durationSeconds: number) => {
            clearTimer()
            const n = Math.max(0, Math.floor(durationSeconds))
            setSecondsRemaining(n)
            if (n <= 0) {
                return
            }
            timerRef.current = setInterval(() => {
                setSecondsRemaining((s) => {
                    if (s <= 1) {
                        clearTimer()
                        return 0
                    }
                    return s - 1
                })
            }, TICK_MS)
        },
        [clearTimer]
    )

    return {
        secondsRemaining,
        isCoolingDown: secondsRemaining > 0,
        startCooldown,
    }
}
