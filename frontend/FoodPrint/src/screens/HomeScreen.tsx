import Ionicons from '@expo/vector-icons/Ionicons'
import MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons'
import * as ImagePicker from 'expo-image-picker'
import type { ImagePickerAsset } from 'expo-image-picker'
import { useFocusEffect, useNavigation } from '@react-navigation/native'
import type { BottomTabNavigationProp } from '@react-navigation/bottom-tabs'
import React, { useCallback, useEffect, useMemo, useState } from 'react'
import {
    ActivityIndicator,
    Alert,
    Image,
    KeyboardAvoidingView,
    Modal,
    Platform,
    Pressable,
    RefreshControl,
    ScrollView,
    StyleSheet,
    Text,
    TextInput,
    View,
} from 'react-native'
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context'
import Svg, { Circle } from 'react-native-svg'

import { analyze, analyzeImage as apiAnalyzeImage, saveAiAnalysisLog } from '../api/ai'
import { ApiError } from '../api/client'
import {
    buildLast7DaySlots,
    createManualFoodLog,
    getFoodLogsToday,
    getWeeklyOverview,
    mergeTodayLogsIntoWeeklyTodayBar,
    mergeWeeklyIntoSlots,
    parseTodayFoodLogsPayload,
    parseWeeklyOverviewPayload,
    toYMD,
} from '../api/food'
import type {
    AIResponseDto,
    ApiResponse,
    DailyCalorieBarDto,
    FoodLogDto,
    TodayFoodLogItemDto,
    UserNutritionContext,
    WeeklyOverviewPayload,
} from '../api/types'
import {
    buildAnalysisResultFromDraft,
    buildManualMealDraftFromDescription,
    buildMealDraftFromAnalysis,
    FoodAnalysisEditor,
    type MealLogDraft,
} from '../components/food/FoodAnalysisEditor'
import { StreamingAssistantMarkdown } from '../components/common/StreamingAssistantMarkdown'
import { useAuth } from '../context/AuthContext'
import { compressImagePickerAssetForUpload } from '../lib/compressImageForUpload'
import type { RootTabParamList } from '../navigation/types'

// ─── Colors ──────────────────────────────────────────────────
const COLORS = {
    bg: '#F4F7F2',
    card: '#FFFFFF',
    primary: '#97B08A',
    primaryDark: '#7A9A6D',
    dark: '#1D3557',
    sub: '#7D8A97',
    remaining: '#5B9A50',
    iconBg: '#E8F0E4',
    shadow: '#000',
}

// ─── Types ───────────────────────────────────────────────────
type HomeScreenNavigationProp = BottomTabNavigationProp<RootTabParamList, 'Home'>

interface MealItem {
    id: string
    name: string
    mealType: string
    time: string
    /** Omit calories when unavailable; the list displays — */
    calories?: number
    icon: keyof typeof MaterialCommunityIcons.glyphMap
}

type LogMealPhase = 'describe' | 'review'

const DEFAULT_CALORIE_GOAL = 2000

/** Android allowsEditing + crop can cause selection issues; enable cropping only on iOS */
const PICK_IMAGE_OPTIONS: ImagePicker.ImagePickerOptions = {
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    allowsEditing: Platform.OS === 'ios',
    quality: 0.92,
    ...(Platform.OS === 'ios' ? { aspect: [4, 3] as [number, number] } : {}),
}

function mimeTypeForPickerAsset(asset: ImagePickerAsset): string {
    if (asset.mimeType && asset.mimeType.length > 0) return asset.mimeType
    const path = (asset.fileName ?? asset.uri).toLowerCase()
    if (path.endsWith('.png')) return 'image/png'
    if (path.endsWith('.webp')) return 'image/webp'
    if (path.endsWith('.heic') || path.endsWith('.heif')) return 'image/heic'
    return 'image/jpeg'
}

function fileNameForPickerAsset(asset: ImagePickerAsset): string {
    const n = asset.fileName?.trim()
    if (n && /\.[a-z0-9]{2,4}$/i.test(n)) return n
    if (n) {
        const ext = mimeTypeForPickerAsset(asset).includes('png') ? 'png' : 'jpg'
        return `${n}.${ext}`
    }
    return 'photo.jpg'
}

function nowPacificLocalDateTime(): string {
    const parts = new Intl.DateTimeFormat('en-CA', {
        timeZone: 'America/Los_Angeles',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
    }).formatToParts(new Date())

    const get = (type: Intl.DateTimeFormatPartTypes) =>
        parts.find((p) => p.type === type)?.value ?? ''

    const yyyy = get('year')
    const mm = get('month')
    const dd = get('day')
    const hh = get('hour')
    const mi = get('minute')
    const ss = get('second')
    return `${yyyy}-${mm}-${dd}T${hh}:${mi}:${ss}`
}

function formatMealTimeLabel(raw: string | null | undefined): string {
    const value = String(raw ?? '').trim()
    if (!value) return '—'

    // Without timezone information, display backend local hours and minutes directly to avoid a second conversion.
    const hhmm = /(?:T|\s)(\d{1,2}):(\d{2})(?::\d{2})?$/.exec(value)
    const hasTimezone = /[zZ]$/.test(value) || /[+-]\d{2}:\d{2}$/.test(value)
    if (!hasTimezone && hhmm) {
        const h = Number(hhmm[1])
        const mm = hhmm[2]
        if (Number.isFinite(h) && h >= 0 && h <= 23) {
            return `${String(h).padStart(2, '0')}:${mm}`
        }
    }

    // Use Date conversion only for timezone-aware values, such as 2026-04-20T07:05:09Z.
    const normalized = /^\d{4}-\d{2}-\d{2}\s/.test(value)
        ? value.replace(' ', 'T')
        : value
    const dt = new Date(normalized)
    if (!Number.isNaN(dt.getTime())) {
        return dt.toLocaleTimeString('en-US', {
            hour: '2-digit',
            hour12: false,
            minute: '2-digit',
        })
    }

    // Fallback: extract HH:mm directly from the string
    const m = /(\d{1,2}):(\d{2})/.exec(value)
    if (!m) return '—'
    const h = Number(m[1])
    const mm = m[2]
    if (!Number.isFinite(h) || h < 0 || h > 23) return `${m[1]}:${mm}`
    return `${String(h).padStart(2, '0')}:${mm}`
}

function todayItemToMeal(log: TodayFoodLogItemDto): MealItem {
    const nameSrc =
        (log.title && log.title.trim()) ||
        (log.foodItems?.filter(Boolean).join(', ') ?? '')
    const name = nameSrc.length > 0 ? nameSrc : 'Food entry'

    const time = formatMealTimeLabel(log.mealTime)

    const short = name.length > 80 ? `${name.slice(0, 77)}...` : name
    const cal = Number(log.totalCalories)
    return {
        id: log.id,
        name: short,
        mealType: 'Logged',
        time,
        calories: Number.isFinite(cal) ? Math.round(cal) : undefined,
        icon: 'silverware-fork-knife',
    }
}

/** Align the saved FoodLogDto with /today items for optimistic updates */
function foodLogDtoToTodayItem(log: FoodLogDto): TodayFoodLogItemDto {
    const title =
        (log.originalText && log.originalText.trim()) ||
        (log.text && log.text.trim()) ||
        log.recognizedFoods?.filter(Boolean).join(', ') ||
        'Meal'
    const recognizedList =
        log.recognizedFoods?.filter(
            (x): x is string => typeof x === 'string' && x.trim().length > 0
        ) ?? []
    const foodItems =
        recognizedList.length > 0
            ? recognizedList
            : title
              ? [title]
              : ['Meal']
    const n = log.nutritionData
    const rawCal = n?.calories ?? log.todayCalories ?? 0
    const totalCalories =
        typeof rawCal === 'number' && !Number.isNaN(rawCal) ? Math.round(rawCal) : 0

    const toOptRound = (x: unknown): number | undefined => {
        if (typeof x === 'number' && !Number.isNaN(x)) return Math.round(x)
        const num = Number(x)
        return Number.isFinite(num) ? Math.round(num) : undefined
    }
    const summ = log.analysisResult?.summary
    const analysisFoods = log.analysisResult?.foods
    const manualFoods = log.foods
    let pFood = 0
    let fFood = 0
    let cFood = 0
    const accumulateFoodRows = (
        rows: { proteinG?: number; fatG?: number; carbsG?: number }[] | undefined
    ) => {
        if (!rows?.length) return
        for (const row of rows) {
            pFood += row.proteinG ?? 0
            fFood += row.fatG ?? 0
            cFood += row.carbsG ?? 0
        }
    }
    accumulateFoodRows(analysisFoods)
    accumulateFoodRows(manualFoods)
    const hadLineFoods =
        (analysisFoods?.length ?? 0) + (manualFoods?.length ?? 0) > 0
    const totalProtein = toOptRound(
        n?.proteinG ??
            n?.protein ??
            log.todayProtein ??
            summ?.totalProteinG ??
            (hadLineFoods ? pFood : undefined)
    )
    const totalFat = toOptRound(
        n?.fat ??
            log.todayFat ??
            summ?.totalFatG ??
            (hadLineFoods ? fFood : undefined)
    )
    const totalCarbs = toOptRound(
        n?.carbs ??
            log.todayCarbs ??
            summ?.totalCarbsG ??
            (hadLineFoods ? cFood : undefined)
    )

    const out: TodayFoodLogItemDto = {
        id: log.id,
        title: title || 'Meal',
        foodItems,
        totalCalories,
        mealTime:
            log.mealTime ??
            log.createdAt ??
            log.logDate ??
            new Date().toISOString(),
    }
    if (totalProtein !== undefined) out.totalProtein = totalProtein
    if (totalFat !== undefined) out.totalFat = totalFat
    if (totalCarbs !== undefined) out.totalCarbs = totalCarbs
    return out
}

/** Chart labels: fixed English weekdays so the chart does not follow system locale (e.g. Monday). */
const WEEKDAY_SHORT_EN = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'] as const

const MONTH_SHORT_EN = [
    'Jan',
    'Feb',
    'Mar',
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec',
] as const

function shortWeekdayLabel(dateStr: string): string {
    const [y, m, d] = dateStr.split('-').map(Number)
    if (!y || !m || !d) return '?'
    const dt = new Date(y, m - 1, d)
    return WEEKDAY_SHORT_EN[dt.getDay()] ?? '?'
}

/** English chart subtitle, no device locale */
function formatChartDayTitle(dateStr: string): string {
    const [y, m, d] = dateStr.split('-').map(Number)
    if (!y || !m || !d) return dateStr
    const dt = new Date(y, m - 1, d)
    const wd = WEEKDAY_SHORT_EN[dt.getDay()] ?? '?'
    const mon = MONTH_SHORT_EN[m - 1] ?? ''
    return `${wd}, ${mon} ${d}`
}

function formatBarMacros(bar: DailyCalorieBarDto): string | null {
    const p = bar.totalProtein
    const f = bar.totalFat
    const c = bar.totalCarbs
    if (p == null && f == null && c == null) return null
    const parts: string[] = []
    if (typeof p === 'number' && !Number.isNaN(p)) parts.push(`${Math.round(p)}P`)
    if (typeof f === 'number' && !Number.isNaN(f)) parts.push(`${Math.round(f)}F`)
    if (typeof c === 'number' && !Number.isNaN(c)) parts.push(`${Math.round(c)}C`)
    return parts.length > 0 ? parts.join(' ') : null
}

function WeeklyCalorieHistogram({
    bars,
    calorieGoal,
    loading,
    onSelectDate,
    selectedDate,
}: {
    bars: DailyCalorieBarDto[]
    calorieGoal: number
    loading: boolean
    selectedDate: string
    onSelectDate: (date: string) => void
}) {
    const maxCal = useMemo(() => {
        const peak = Math.max(1, calorieGoal, ...bars.map((b) => b.totalCalories))
        return peak * 1.08
    }, [bars, calorieGoal])

    const barMaxH = 104

    const weekAverage = useMemo(() => {
        if (bars.length === 0) return 0
        return Math.round(
            bars.reduce((sum, b) => sum + b.totalCalories, 0) / bars.length
        )
    }, [bars])

    return (
        <View style={styles.histogramCard}>
            <View style={styles.histogramHeader}>
                <Text style={styles.histogramTitle}>7-day intake</Text>
                <View style={styles.histogramHeaderRight}>
                    {loading ? (
                        <ActivityIndicator size="small" color={COLORS.primary} />
                    ) : (
                        <Text style={styles.histogramAvgWrap}>
                            <Text style={styles.histogramAvgMuted}>Weekly avg </Text>
                            <Text style={styles.histogramAvgValue}>
                                {weekAverage.toLocaleString('en-US')}
                            </Text>
                            <Text style={styles.histogramAvgMuted}> kcal/d</Text>
                        </Text>
                    )}
                </View>
            </View>
            <View style={styles.histogramRow}>
                {bars.map((bar) => {
                    const isSelected = bar.date === selectedDate
                    const ratio = maxCal > 0 ? bar.totalCalories / maxCal : 0
                    const fillH = loading
                        ? 16
                        : bar.totalCalories > 0
                            ? Math.max(10, ratio * barMaxH)
                            : 6
                    const fillOpacity = loading
                        ? 0.35
                        : bar.totalCalories > 0
                            ? isSelected
                                ? 1
                                : 0.88
                            : 0.45
                    return (
                        <Pressable
                            key={bar.date}
                            accessibilityLabel={`${shortWeekdayLabel(bar.date)}, ${formatChartDayTitle(bar.date)}`}
                            accessibilityRole="button"
                            accessibilityState={{ selected: isSelected }}
                            android_ripple={{ color: 'rgba(0,0,0,0.06)' }}
                            onPress={() => onSelectDate(bar.date)}
                            style={styles.histogramCol}
                        >
                            <Text style={styles.histogramCal} numberOfLines={1}>
                                {loading ? '—' : bar.totalCalories > 0 ? Math.round(bar.totalCalories) : '—'}
                            </Text>
                            <View style={styles.histogramTrack}>
                                <View
                                    style={[
                                        styles.histogramFill,
                                        isSelected && styles.histogramFillSelected,
                                        { height: fillH, opacity: fillOpacity },
                                    ]}
                                />
                            </View>
                            <Text style={styles.histogramDay}>{shortWeekdayLabel(bar.date)}</Text>
                            {(() => {
                                const m = formatBarMacros(bar)
                                return m ? (
                                    <Text style={styles.histogramMacros} numberOfLines={1}>
                                        {m}
                                    </Text>
                                ) : null
                            })()}
                        </Pressable>
                    )
                })}
            </View>
        </View>
    )
}

// ─── Helpers ─────────────────────────────────────────────────

/** SVG-based ring progress indicator */
function CalorieRing({ progress }: { progress: number }) {
    const size = 80
    const strokeWidth = 7
    const radius = (size - strokeWidth) / 2
    const circumference = 2 * Math.PI * radius
    const pct = Math.min(progress, 1)
    const strokeDashoffset = circumference * (1 - pct)

    return (
        <View style={ringStyles.container}>
            <Svg width={size} height={size}>
                {/* Background ring */}
                <Circle
                    cx={size / 2}
                    cy={size / 2}
                    r={radius}
                    stroke="#E8F0E4"
                    strokeWidth={strokeWidth}
                    fill="none"
                />

                {/* Progress ring */}
                <Circle
                    cx={size / 2}
                    cy={size / 2}
                    r={radius}
                    stroke={COLORS.primary}
                    strokeWidth={strokeWidth}
                    fill="none"
                    strokeDasharray={circumference}
                    strokeDashoffset={strokeDashoffset}
                    strokeLinecap="round"
                    transform={`rotate(-90 ${size / 2} ${size / 2})`}
                />
            </Svg>

            {/* Center the 46% text precisely with an absolutely positioned container over the SVG */}
            <View style={ringStyles.textContainer}>
                <Text style={ringStyles.percentageText}>
                    {Math.round(pct * 100)}%
                </Text>
            </View>
        </View>
    )
}

const ringStyles = StyleSheet.create({
    container: {
        alignItems: 'center',
        justifyContent: 'center',
        width: 80,      // Set explicit outer container dimensions
        height: 80,
        position: 'relative',
    },
    // Center the 46% text precisely with an absolutely positioned text container
    textContainer: {
        alignItems: 'center',
        height: '100%',
        justifyContent: 'center',
        left: 0,
        position: 'absolute',
        top: 0,
        width: '100%',
    },
    percentageText: {
        color: COLORS.dark,
        fontSize: 14,
        fontWeight: '600',
        textAlign: 'center',
    },
})

// ─── Meal Item Renderer ──────────────────────────────────────
function MealItemCard({ meal }: { meal: MealItem }) {
    return (
        <View style={styles.mealCard}>
            {/* Icon */}
            <View style={styles.mealIconBox}>
                <MaterialCommunityIcons
                    name={meal.icon}
                    size={20}
                    color={COLORS.primary}
                />
            </View>

            {/* Info */}
            <View style={styles.mealInfo}>
                <Text style={styles.mealName}>{meal.name}</Text>
                <Text style={styles.mealMeta}>
                    {meal.mealType} • {meal.time}
                </Text>
            </View>

            {/* Calories */}
            <View style={styles.mealCalories}>
                <Text style={styles.mealCalNum}>
                    {meal.calories != null ? meal.calories : '—'}
                </Text>
                <Text style={styles.mealCalUnit}>cal</Text>
            </View>
        </View>
    )
}

// ─── Main Component ──────────────────────────────────────────
export function HomeScreen() {
    const _navigation = useNavigation<HomeScreenNavigationProp>()
    const { userProfile, authUserId } = useAuth()
    const insets = useSafeAreaInsets()
    /** Modal scroll bottom padding: base spacing + system safe area, keeping buttons clear of small screens and the Home Indicator */
    /** Apply only to modal ScrollView content; extra overlay paddingBottom can duplicate tab / safe area spacing */
    const modalScrollBottomPad = 24 + Math.max(insets.bottom, 8)

    const calorieGoal = userProfile?.dailyCalories ?? DEFAULT_CALORIE_GOAL

    const userContext: UserNutritionContext | undefined = userProfile
        ? {
            heightCm: userProfile.height,
            weightKg: userProfile.weight,
            age: userProfile.age,
            gender: userProfile.gender,
            goal: userProfile.goal,
            dailyCalorieTarget: userProfile.dailyCalories,
        }
        : undefined

    const [weeklyBars, setWeeklyBars] = useState<DailyCalorieBarDto[]>(() =>
        buildLast7DaySlots(new Date())
    )
    const [todayLogs, setTodayLogs] = useState<TodayFoodLogItemDto[]>([])
    const [weeklyLoading, setWeeklyLoading] = useState(true)
    const [logsLoading, setLogsLoading] = useState(true)
    const [refreshing, setRefreshing] = useState(false)

    const [addMealVisible, setAddMealVisible] = useState(false)
    const [newMealDesc, setNewMealDesc] = useState('')
    const [savingLog, setSavingLog] = useState(false)
    const [logMealPhase, setLogMealPhase] = useState<LogMealPhase>('describe')
    const [logInputMode, setLogInputMode] = useState<'text' | 'photo' | 'manual'>('text')
    /** Manual mode: optional comma / newline-separated prefill, separate from the Text mode description */
    const [manualPrefill, setManualPrefill] = useState('')
    const [analyzingFood, setAnalyzingFood] = useState(false)
    const [aiResult, setAiResult] = useState<AIResponseDto | null>(null)
    const [analysisImageUri, setAnalysisImageUri] = useState<string | null>(null)
    const [mealDraft, setMealDraft] = useState<MealLogDraft | null>(null)
    /** Save through AI log or manual foods; both share the Review UI */
    const [mealLogSource, setMealLogSource] = useState<'ai' | 'manual' | null>(null)
    /** Photo mode: preview the selected image, then request AI analysis after confirmation */
    const [photoPreviewAsset, setPhotoPreviewAsset] = useState<ImagePickerAsset | null>(null)

    const meals = useMemo(() => todayLogs.map(todayItemToMeal), [todayLogs])

    const todayYmd = toYMD(new Date())

    const [weeklyChartDate, setWeeklyChartDate] = useState('')

    useEffect(() => {
        if (weeklyBars.length === 0) return
        setWeeklyChartDate((prev) => {
            if (prev && weeklyBars.some((b) => b.date === prev)) return prev
            if (weeklyBars.some((b) => b.date === todayYmd)) return todayYmd
            return weeklyBars[weeklyBars.length - 1].date
        })
    }, [weeklyBars, todayYmd])

    /** Sum macros from /food-logs/today items; fall back to the daily weekly-overview entry if fields are missing or all zero */
    const macroTotalsFromLogs = useMemo(() => {
        return todayLogs.reduce(
            (acc, log) => ({
                protein: acc.protein + (log.totalProtein ?? 0),
                fat: acc.fat + (log.totalFat ?? 0),
                carbs: acc.carbs + (log.totalCarbs ?? 0),
            }),
            { protein: 0, fat: 0, carbs: 0 }
        )
    }, [todayLogs])

    const todayBarForMacros = useMemo(
        () => weeklyBars.find((b) => b.date === todayYmd),
        [weeklyBars, todayYmd]
    )

    /** Prefer per-log nutrition; if all values are zero, use daily P/F/C from the weekly overview to match the bar chart */
    const macroDisplayTotals = useMemo(() => {
        const fromLogs = macroTotalsFromLogs
        const logHasAny =
            fromLogs.protein + fromLogs.fat + fromLogs.carbs > 0.5
        if (logHasAny) return fromLogs
        return {
            protein: todayBarForMacros?.totalProtein ?? 0,
            fat: todayBarForMacros?.totalFat ?? 0,
            carbs: todayBarForMacros?.totalCarbs ?? 0,
        }
    }, [macroTotalsFromLogs, todayBarForMacros])

    const macroDisplay = useMemo(
        () => [
            {
                label: 'Protein',
                value: logsLoading ? '—' : `${Math.round(macroDisplayTotals.protein)}g`,
            },
            {
                label: 'Carbs',
                value: logsLoading ? '—' : `${Math.round(macroDisplayTotals.carbs)}g`,
            },
            {
                label: 'Fat',
                value: logsLoading ? '—' : `${Math.round(macroDisplayTotals.fat)}g`,
            },
        ],
        [logsLoading, macroDisplayTotals]
    )

    const totalFromLogs = useMemo(
        () => meals.reduce((sum, m) => sum + (m.calories ?? 0), 0),
        [meals]
    )

    const todayBarCalories = useMemo(() => {
        const hit = weeklyBars.find((b) => b.date === todayYmd)
        return hit?.totalCalories ?? 0
    }, [weeklyBars, todayYmd])

    const totalCalories =
        totalFromLogs > 0 ? totalFromLogs : todayBarCalories

    const remainingCalories = calorieGoal - totalCalories

    const fetchHomePayload = useCallback(async () => {
        const end = new Date()
        const endStr = toYMD(end)
        const slots = buildLast7DaySlots(end)
        const weeklyPromise: Promise<ApiResponse<WeeklyOverviewPayload>> = authUserId
            ? getWeeklyOverview(authUserId, endStr)
            : Promise.resolve({ code: 0, message: '', data: null })
        const logsPromise: Promise<ApiResponse<TodayFoodLogItemDto[]>> = authUserId
            ? getFoodLogsToday(authUserId)
            : Promise.resolve({ code: 0, message: '', data: null })
        const [weeklyRes, logsRes] = await Promise.allSettled([
            weeklyPromise,
            logsPromise,
        ])

        let weekly = slots
        if (weeklyRes.status === 'fulfilled' && weeklyRes.value.data != null) {
            weekly = mergeWeeklyIntoSlots(
                slots,
                parseWeeklyOverviewPayload(weeklyRes.value.data)
            )
        }

        let logs: TodayFoodLogItemDto[] = []
        if (logsRes.status === 'fulfilled' && logsRes.value.data != null) {
            logs = parseTodayFoodLogsPayload(logsRes.value.data)
        }

        weekly = mergeTodayLogsIntoWeeklyTodayBar(weekly, logs, endStr)

        return { weekly, logs }
    }, [authUserId])

    /**
     * pull: controls only refreshing; keep it separate from initial loading so a shared finally does not clear refreshing accidentally.
     * Do not assign loadHomeData directly to RefreshControl onRefresh; it would receive event as an argument.
     */
    const loadHomeData = useCallback(
        async (opts?: { pull?: boolean }) => {
            const isPull = opts?.pull === true
            if (isPull) setRefreshing(true)
            else {
                setWeeklyLoading(true)
                setLogsLoading(true)
            }
            try {
                const { weekly, logs } = await fetchHomePayload()
                setWeeklyBars(weekly)
                setTodayLogs(logs)
            } catch {
                setWeeklyBars(buildLast7DaySlots(new Date()))
                setTodayLogs([])
            } finally {
                if (isPull) setRefreshing(false)
                else {
                    setWeeklyLoading(false)
                    setLogsLoading(false)
                }
            }
        },
        [fetchHomePayload]
    )

    useFocusEffect(
        useCallback(() => {
            void loadHomeData()
        }, [loadHomeData])
    )

    const handleAddMeal = () => {
        setAddMealVisible(true)
    }

    const resetLogModal = () => {
        setNewMealDesc('')
        setManualPrefill('')
        setLogMealPhase('describe')
        setLogInputMode('text')
        setAnalyzingFood(false)
        setAiResult(null)
        setAnalysisImageUri(null)
        setMealDraft(null)
        setMealLogSource(null)
        setPhotoPreviewAsset(null)
    }

    const handleCloseAddMeal = () => {
        setAddMealVisible(false)
        resetLogModal()
    }

    /** Return from Review / unstructured AI results to step one and restore the previous Text / Photo / Manual segment */
    const goBackToLogDescribeOptions = useCallback(() => {
        const wasManual = mealLogSource === 'manual'
        const wasPhoto = mealLogSource === 'ai' && Boolean(analysisImageUri)
        setLogMealPhase('describe')
        setAiResult(null)
        setMealDraft(null)
        setMealLogSource(null)
        setAnalysisImageUri(null)
        setPhotoPreviewAsset(null)
        setLogInputMode(wasManual ? 'manual' : wasPhoto ? 'photo' : 'text')
    }, [mealLogSource, analysisImageUri])

    const applyAiResponseToState = useCallback(
        (data: AIResponseDto | null | undefined, opts: { imageUri?: string | null; inputFallback: string }) => {
            setAiResult(data ?? null)
            if (opts.imageUri !== undefined) {
                setAnalysisImageUri(opts.imageUri ?? null)
            }
            if (data?.type === 'FOOD_ANALYSIS' && data.foodAnalysis) {
                setMealLogSource('ai')
                setMealDraft(buildMealDraftFromAnalysis(data.foodAnalysis, opts.inputFallback))
            } else {
                setMealLogSource(null)
                setMealDraft(null)
            }
            setLogMealPhase('review')
        },
        []
    )

    const handleAnalyzeFoodText = async () => {
        const text = newMealDesc.trim()
        if (!text) {
            Alert.alert('Notice', 'Please describe what you ate')
            return
        }
        setAnalyzingFood(true)
        try {
            const res = await analyze({ text, userContext })
            applyAiResponseToState(res.data, { imageUri: null, inputFallback: text })
        } catch (e: unknown) {
            const msg =
                e instanceof ApiError
                    ? e.message
                    : e instanceof Error
                      ? e.message
                      : 'Analysis failed'
            Alert.alert('Error', msg)
        } finally {
            setAnalyzingFood(false)
        }
    }

    const handleImageAnalysis = async (asset: ImagePickerAsset): Promise<boolean> => {
        setAnalyzingFood(true)
        try {
            const prepared = await compressImagePickerAssetForUpload(asset)
            const res = await apiAnalyzeImage({
                uri: prepared.uri,
                type: mimeTypeForPickerAsset(prepared),
                name: fileNameForPickerAsset(prepared),
            })
            const fallback = newMealDesc.trim() || 'Photo meal'
            applyAiResponseToState(res.data, { imageUri: prepared.uri, inputFallback: fallback })
            return true
        } catch (e: unknown) {
            const msg =
                e instanceof ApiError
                    ? e.message
                    : e instanceof Error
                      ? e.message
                      : 'Image analysis failed'
            Alert.alert('Error', msg)
            return false
        } finally {
            setAnalyzingFood(false)
        }
    }

    const handleTakePhoto = async () => {
        try {
            const { status } = await ImagePicker.requestCameraPermissionsAsync()
            if (status !== 'granted') {
                Alert.alert('Permission denied', 'Camera permission is required')
                return
            }
            const result = await ImagePicker.launchCameraAsync(PICK_IMAGE_OPTIONS)
            if (!result.canceled && result.assets?.[0]) {
                setPhotoPreviewAsset(result.assets[0])
            }
        } catch {
            Alert.alert('Error', 'Failed to take photo')
        }
    }

    const handlePickGallery = async () => {
        try {
            const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync()
            if (status !== 'granted') {
                Alert.alert('Permission denied', 'Photo library permission is required')
                return
            }
            const result = await ImagePicker.launchImageLibraryAsync(PICK_IMAGE_OPTIONS)
            if (!result.canceled && result.assets?.[0]) {
                setPhotoPreviewAsset(result.assets[0])
            }
        } catch {
            Alert.alert('Error', 'Failed to pick image')
        }
    }

    const handleAnalyzePickedPhoto = async () => {
        if (!photoPreviewAsset) {
            Alert.alert('Notice', 'Please choose a photo first')
            return
        }
        const ok = await handleImageAnalysis(photoPreviewAsset)
        if (ok) setPhotoPreviewAsset(null)
    }

    const handleResetDraftFromAi = () => {
        if (mealLogSource !== 'ai') return
        if (aiResult?.type === 'FOOD_ANALYSIS' && aiResult.foodAnalysis) {
            const src = mealDraft?.originalInput ?? newMealDesc.trim()
            setMealDraft(buildMealDraftFromAnalysis(aiResult.foodAnalysis, src))
        }
    }

    const handleEnterManualReview = () => {
        const draft = buildManualMealDraftFromDescription(manualPrefill)
        const foodAnalysis = buildAnalysisResultFromDraft(draft, null)
        setMealLogSource('manual')
        setAiResult({ type: 'FOOD_ANALYSIS', foodAnalysis })
        setMealDraft(draft)
        setAnalysisImageUri(null)
        setLogMealPhase('review')
    }

    const handleSaveMealFromDraft = async () => {
        if (!mealDraft) return
        if (!aiResult?.foodAnalysis) {
            Alert.alert('Notice', 'Nothing to save. Go back and add foods or run AI analysis.')
            return
        }
        if (!authUserId) {
            Alert.alert('Error', 'Not signed in or user id missing. Please log in again.')
            return
        }
        const title = mealDraft.displayText.trim() || mealDraft.originalInput.trim()
        if (!title) {
            Alert.alert('Notice', 'Please enter a title or description')
            return
        }
        setSavingLog(true)
        try {
            if (mealLogSource === 'manual') {
                const foods = buildAnalysisResultFromDraft(mealDraft, null).foods ?? []
                const named = foods.filter(
                    (f) => String(f.nameEn ?? f.nameZh ?? '').trim().length > 0
                )
                if (named.length === 0) {
                    Alert.alert('Notice', 'Add at least one food with a name before saving.')
                    return
                }
                const created = await createManualFoodLog(
                    {
                        title,
                        mealTime: nowPacificLocalDateTime(),
                        foods: named,
                    },
                    authUserId
                )
                handleCloseAddMeal()
                await loadHomeData()
                const row = created.data
                if (row?.id) {
                    const item = foodLogDtoToTodayItem(row)
                    setTodayLogs((prev) =>
                        prev.some((l) => l.id === item.id) ? prev : [item, ...prev]
                    )
                }
                return
            }

            const analysisResult = buildAnalysisResultFromDraft(
                mealDraft,
                aiResult.foodAnalysis
            )
            const created = await saveAiAnalysisLog(
                {
                    title,
                    mealTime: nowPacificLocalDateTime(),
                    analysisResult,
                },
                authUserId
            )
            handleCloseAddMeal()
            await loadHomeData()
            const row = created.data
            if (row?.id) {
                const item = foodLogDtoToTodayItem(row)
                setTodayLogs((prev) =>
                    prev.some((l) => l.id === item.id) ? prev : [item, ...prev]
                )
            }
        } catch (e: unknown) {
            const msg =
                e instanceof ApiError
                    ? e.message
                    : e instanceof Error
                      ? e.message
                      : 'Could not save meal'
            Alert.alert('Error', msg)
        } finally {
            setSavingLog(false)
        }
    }

    const goalForRing = Math.max(1, calorieGoal)

    return (
        <SafeAreaView style={styles.safe} edges={['left', 'right']}>
            <ScrollView
                style={styles.scroll}
                contentContainerStyle={styles.scrollContent}
                contentInsetAdjustmentBehavior="never"
                showsVerticalScrollIndicator={false}
                refreshControl={
                    <RefreshControl
                        refreshing={refreshing}
                        onRefresh={() => void loadHomeData({ pull: true })}
                        tintColor={COLORS.primary}
                    />
                }
            >
                {/* ── Calorie Card ── */}
                <View style={[styles.calorieCard, { marginHorizontal: 16 }]}>
                    <View style={styles.calorieTop}>
                        {/* Left info */}
                        <View style={styles.calorieLeft}>
                            <Text style={styles.calorieLabel}>
                                Today&apos;s Calories
                            </Text>
                            <View style={styles.calorieRow}>
                                <Text style={styles.calorieCurrent}>
                                    {logsLoading ? '—' : totalCalories}
                                </Text>
                                <Text style={styles.calorieGoal}>
                                    {' '}
                                    / {calorieGoal}
                                </Text>
                            </View>
                            <Text style={styles.calorieRemaining}>
                                {logsLoading ? '…' : `${remainingCalories} cal remaining`}
                            </Text>
                        </View>
                    </View>

                    {/* Ring - positioned absolutely */}
                    <View style={styles.ringContainer}>
                        <CalorieRing
                            progress={logsLoading ? 0 : totalCalories / goalForRing}
                        />
                    </View>

                    {/* Macros */}
                    <View style={styles.macroRow}>
                        {macroDisplay.map((m) => (
                            <View key={m.label} style={styles.macroItem}>
                                <Text style={styles.macroValue}>
                                    {m.value}
                                </Text>
                                <Text style={styles.macroLabel}>
                                    {m.label}
                                </Text>
                            </View>
                        ))}
                    </View>
                </View>

                {/* ── Section Header ── */}
                <View style={[styles.sectionHeader, { marginHorizontal: 16 }]}>
                    <Text style={styles.sectionTitle}>Today&apos;s Meals</Text>
                    <View style={styles.sectionHeaderRight}>
                        <Pressable
                            accessibilityLabel="Refresh food logs"
                            disabled={refreshing}
                            hitSlop={12}
                            onPress={() => void loadHomeData({ pull: true })}
                            style={({ pressed }) => [
                                styles.refreshMealsBtn,
                                pressed && styles.refreshMealsBtnPressed,
                            ]}
                        >
                            {refreshing ? (
                                <ActivityIndicator size="small" color={COLORS.primary} />
                            ) : (
                                <Ionicons
                                    name="refresh"
                                    size={22}
                                    color={refreshing ? COLORS.sub : COLORS.primaryDark}
                                />
                            )}
                        </Pressable>
                        <Pressable
                            accessibilityLabel="Log meal"
                            onPress={handleAddMeal}
                            style={({ pressed }) => [
                                styles.logMealHeaderBtn,
                                pressed && styles.logMealHeaderBtnPressed,
                            ]}
                        >
                            <Text style={styles.logMealHeaderBtnText}>+ Log meal</Text>
                        </Pressable>
                    </View>
                </View>

                {/* ── Meal List (placeholder + data) ── */}
                <View style={{ paddingHorizontal: 16 }}>
                    {logsLoading ? (
                        <>
                            <View style={[styles.mealCard, styles.mealPlaceholder]}>
                                <View style={styles.mealIconBox}>
                                    <ActivityIndicator color={COLORS.sub} />
                                </View>
                                <View style={styles.mealInfo}>
                                    <View style={styles.skeletonLine} />
                                    <View style={[styles.skeletonLine, { width: '50%', marginTop: 8 }]} />
                                </View>
                            </View>
                            <View style={[styles.mealCard, styles.mealPlaceholder]}>
                                <View style={styles.mealIconBox}>
                                    <MaterialCommunityIcons
                                        name="food"
                                        size={20}
                                        color={COLORS.sub}
                                    />
                                </View>
                                <View style={styles.mealInfo}>
                                    <View style={styles.skeletonLine} />
                                    <View style={[styles.skeletonLine, { width: '40%', marginTop: 8 }]} />
                                </View>
                            </View>
                        </>
                    ) : meals.length === 0 ? (
                        <View style={styles.emptyMeals}>
                            <MaterialCommunityIcons
                                name="silverware-fork-knife"
                                size={36}
                                color={COLORS.sub}
                            />
                            <Text style={styles.emptyMealsTitle}>No meals yet</Text>
                            <Text style={styles.emptyMealsSub}>
                                Tap Log meal: use Text or Photo for AI, or Manual to type foods and nutrition yourself.
                            </Text>
                        </View>
                    ) : (
                        meals.map((meal) => <MealItemCard key={meal.id} meal={meal} />)
                    )}
                </View>

                {/* ── 7-day intake (below Today meals list) ── */}
                <View style={{ marginHorizontal: 16, marginBottom: 16, marginTop: 8 }}>
                    <WeeklyCalorieHistogram
                        bars={weeklyBars}
                        calorieGoal={calorieGoal}
                        loading={weeklyLoading}
                        onSelectDate={setWeeklyChartDate}
                        selectedDate={weeklyChartDate}
                    />
                </View>

                <View style={{ height: 24 }} />
            </ScrollView>

            {/* ── Add Meal Modal ── */}
            <Modal
                visible={addMealVisible}
                transparent
                animationType="slide"
                onRequestClose={handleCloseAddMeal}
            >
                <KeyboardAvoidingView
                    style={styles.modalOverlay}
                    behavior={Platform.OS === 'ios' ? 'padding' : undefined}
                >
                    <View style={[styles.modalContainer, styles.logMealModalContainer]}>
                        <View style={styles.modalHeader}>
                            <Text style={styles.modalTitle}>
                                {logMealPhase === 'describe' ? 'Log meal' : 'Review & save'}
                            </Text>
                            <Pressable onPress={handleCloseAddMeal} disabled={savingLog || analyzingFood}>
                                <Ionicons name="close" size={24} color={COLORS.dark} />
                            </Pressable>
                        </View>

                        <ScrollView
                            style={styles.modalContent}
                            contentContainerStyle={{ paddingBottom: modalScrollBottomPad }}
                            keyboardShouldPersistTaps="handled"
                        >
                            {logMealPhase === 'describe' ? (
                                <>
                                    <Text style={styles.formHint}>
                                        Choose Text or Photo to run AI, or Manual log to enter nutrition yourself (no
                                        AI).
                                    </Text>
                                    <View style={styles.logSegmentWrap}>
                                        <Pressable
                                            style={[styles.logSegBtn, logInputMode === 'text' && styles.logSegBtnOn]}
                                            onPress={() => {
                                                setPhotoPreviewAsset(null)
                                                setLogInputMode('text')
                                            }}
                                        >
                                            <View
                                                style={[
                                                    styles.logSegAiBadge,
                                                    logInputMode === 'text' && styles.logSegAiBadgeOn,
                                                ]}
                                                accessibilityElementsHidden
                                                importantForAccessibility="no-hide-descendants"
                                            >
                                                <Text
                                                    style={[
                                                        styles.logSegAiBadgeTxt,
                                                        logInputMode === 'text' && styles.logSegAiBadgeTxtOn,
                                                    ]}
                                                >
                                                    AI
                                                </Text>
                                            </View>
                                            <Text
                                                style={[
                                                    styles.logSegTxt,
                                                    logInputMode === 'text' && styles.logSegTxtOn,
                                                ]}
                                            >
                                                Text
                                            </Text>
                                        </Pressable>
                                        <Pressable
                                            style={[styles.logSegBtn, logInputMode === 'photo' && styles.logSegBtnOn]}
                                            onPress={() => setLogInputMode('photo')}
                                        >
                                            <View
                                                style={[
                                                    styles.logSegAiBadge,
                                                    logInputMode === 'photo' && styles.logSegAiBadgeOn,
                                                ]}
                                                accessibilityElementsHidden
                                                importantForAccessibility="no-hide-descendants"
                                            >
                                                <Text
                                                    style={[
                                                        styles.logSegAiBadgeTxt,
                                                        logInputMode === 'photo' && styles.logSegAiBadgeTxtOn,
                                                    ]}
                                                >
                                                    AI
                                                </Text>
                                            </View>
                                            <Text
                                                style={[
                                                    styles.logSegTxt,
                                                    logInputMode === 'photo' && styles.logSegTxtOn,
                                                ]}
                                            >
                                                Photo
                                            </Text>
                                        </Pressable>
                                        <Pressable
                                            style={[
                                                styles.logSegBtn,
                                                logInputMode === 'manual' && styles.logSegBtnOn,
                                            ]}
                                            onPress={() => {
                                                setPhotoPreviewAsset(null)
                                                setLogInputMode('manual')
                                            }}
                                            accessibilityLabel="Manual log"
                                        >
                                            <Text
                                                style={[
                                                    styles.logSegTxt,
                                                    logInputMode === 'manual' && styles.logSegTxtOn,
                                                ]}
                                                numberOfLines={1}
                                                adjustsFontSizeToFit
                                            >
                                                Manual
                                            </Text>
                                        </Pressable>
                                    </View>

                                    {logInputMode === 'text' ? (
                                        <View style={styles.formGroup}>
                                            <Text style={styles.formLabel}>What did you eat?</Text>
                                            <TextInput
                                                style={[styles.textInput, styles.textInputMultiline]}
                                                value={newMealDesc}
                                                onChangeText={setNewMealDesc}
                                                placeholder="e.g. Two eggs, whole wheat toast, black coffee"
                                                placeholderTextColor={COLORS.sub}
                                                multiline
                                                editable={!savingLog && !analyzingFood}
                                            />
                                            <Pressable
                                                style={[
                                                    styles.addMealBtn,
                                                    { marginTop: 12 },
                                                    (analyzingFood || !newMealDesc.trim()) && styles.modalBtnDisabled,
                                                ]}
                                                onPress={() => void handleAnalyzeFoodText()}
                                                disabled={analyzingFood || !newMealDesc.trim() || savingLog}
                                            >
                                                {analyzingFood ? (
                                                    <ActivityIndicator color="#fff" />
                                                ) : (
                                                    <Text style={styles.addMealText}>Analyze with AI</Text>
                                                )}
                                            </Pressable>
                                        </View>
                                    ) : logInputMode === 'photo' ? (
                                        <View style={styles.formGroup}>
                                            <Text style={styles.formHint}>
                                                Optional note (kept as context with your photo log):
                                            </Text>
                                            <TextInput
                                                style={styles.textInput}
                                                value={newMealDesc}
                                                onChangeText={setNewMealDesc}
                                                placeholder="Optional short note"
                                                placeholderTextColor={COLORS.sub}
                                                editable={!analyzingFood && !savingLog}
                                            />
                                            {photoPreviewAsset ? (
                                                <>
                                                    <Image
                                                        source={{ uri: photoPreviewAsset.uri }}
                                                        style={styles.photoPreview}
                                                        resizeMode="cover"
                                                    />
                                                    <View style={styles.photoPreviewActions}>
                                                        <Pressable
                                                            style={({ pressed }) => [
                                                                styles.photoPreviewChip,
                                                                pressed && { opacity: 0.75 },
                                                                (analyzingFood || savingLog) && styles.modalBtnDisabled,
                                                            ]}
                                                            onPress={() => void handleTakePhoto()}
                                                            disabled={analyzingFood || savingLog}
                                                        >
                                                            <Ionicons name="camera" size={18} color={COLORS.dark} />
                                                            <Text style={styles.photoPreviewChipText}>Retake</Text>
                                                        </Pressable>
                                                        <Pressable
                                                            style={({ pressed }) => [
                                                                styles.photoPreviewChip,
                                                                pressed && { opacity: 0.75 },
                                                                (analyzingFood || savingLog) && styles.modalBtnDisabled,
                                                            ]}
                                                            onPress={() => void handlePickGallery()}
                                                            disabled={analyzingFood || savingLog}
                                                        >
                                                            <Ionicons name="images" size={18} color={COLORS.dark} />
                                                            <Text style={styles.photoPreviewChipText}>Library</Text>
                                                        </Pressable>
                                                    </View>
                                                    <Pressable
                                                        onPress={() => setPhotoPreviewAsset(null)}
                                                        disabled={analyzingFood || savingLog}
                                                        style={({ pressed }) => [
                                                            styles.photoRemoveBtn,
                                                            pressed && { opacity: 0.7 },
                                                        ]}
                                                    >
                                                        <Text style={styles.photoRemoveBtnText}>Remove photo</Text>
                                                    </Pressable>
                                                    <Pressable
                                                        style={[
                                                            styles.addMealBtn,
                                                            { marginTop: 12 },
                                                            (analyzingFood || savingLog) && styles.modalBtnDisabled,
                                                        ]}
                                                        onPress={() => void handleAnalyzePickedPhoto()}
                                                        disabled={analyzingFood || savingLog}
                                                    >
                                                        {analyzingFood ? (
                                                            <ActivityIndicator color="#fff" />
                                                        ) : (
                                                            <Text style={styles.addMealText}>Analyze with AI</Text>
                                                        )}
                                                    </Pressable>
                                                </>
                                            ) : (
                                                <>
                                                    <Pressable
                                                        style={[
                                                            styles.addMealBtn,
                                                            { marginTop: 12 },
                                                            analyzingFood && styles.modalBtnDisabled,
                                                        ]}
                                                        onPress={() => void handleTakePhoto()}
                                                        disabled={analyzingFood || savingLog}
                                                    >
                                                        <Text style={styles.addMealText}>Take photo</Text>
                                                    </Pressable>
                                                    <Pressable
                                                        style={[
                                                            styles.secondaryOutlineBtn,
                                                            analyzingFood && styles.modalBtnDisabled,
                                                        ]}
                                                        onPress={() => void handlePickGallery()}
                                                        disabled={analyzingFood || savingLog}
                                                    >
                                                        <Text style={styles.secondaryOutlineBtnText}>
                                                            Choose from library
                                                        </Text>
                                                    </Pressable>
                                                </>
                                            )}
                                        </View>
                                    ) : (
                                        <View style={styles.formGroup}>
                                            <Text style={styles.formLabel}>Manual log (no AI)</Text>
                                            <Text style={styles.formHint}>
                                                Optional: comma or new line separated foods to create rows. Leave
                                                empty to start with one blank row.
                                            </Text>
                                            <TextInput
                                                style={[styles.textInput, styles.textInputMultiline]}
                                                value={manualPrefill}
                                                onChangeText={setManualPrefill}
                                                placeholder="e.g. Chicken breast, rice, broccoli"
                                                placeholderTextColor={COLORS.sub}
                                                multiline
                                                editable={!savingLog}
                                            />
                                            <Pressable
                                                style={[
                                                    styles.addMealBtn,
                                                    { marginTop: 12 },
                                                    savingLog && styles.modalBtnDisabled,
                                                ]}
                                                onPress={() => handleEnterManualReview()}
                                                disabled={savingLog}
                                            >
                                                <Text style={styles.addMealText}>Continue to food editor</Text>
                                            </Pressable>
                                        </View>
                                    )}

                                    <View style={[styles.modalActions, { marginTop: 8 }]}>
                                        <Pressable
                                            style={[styles.modalBtn, styles.modalBtnCancel]}
                                            onPress={handleCloseAddMeal}
                                            disabled={savingLog}
                                        >
                                            <Text style={styles.modalBtnCancelText}>Cancel</Text>
                                        </Pressable>
                                    </View>
                                </>
                            ) : (
                                <>
                                    {mealDraft && aiResult?.foodAnalysis ? (
                                        <>
                                            <Pressable
                                                style={styles.backLink}
                                                onPress={() => {
                                                    if (!savingLog) goBackToLogDescribeOptions()
                                                }}
                                                disabled={savingLog}
                                            >
                                                <Text style={styles.backLinkText}>← Back to log options</Text>
                                            </Pressable>
                                            <FoodAnalysisEditor
                                                analysis={aiResult.foodAnalysis}
                                                imageUri={analysisImageUri}
                                                draft={mealDraft}
                                                onChangeDraft={setMealDraft}
                                                entryKind={mealLogSource === 'manual' ? 'manual' : 'ai'}
                                            />
                                            {mealLogSource === 'ai' ? (
                                                <Pressable
                                                    style={[
                                                        styles.modalBtn,
                                                        styles.modalBtnCancel,
                                                        { marginTop: 12 },
                                                    ]}
                                                    onPress={handleResetDraftFromAi}
                                                    disabled={savingLog}
                                                >
                                                    <Text style={styles.modalBtnCancelText}>
                                                        Reset fields from AI
                                                    </Text>
                                                </Pressable>
                                            ) : null}
                                            <View style={styles.modalActions}>
                                                <Pressable
                                                    style={[styles.modalBtn, styles.modalBtnCancel]}
                                                    onPress={() => {
                                                        if (!savingLog) goBackToLogDescribeOptions()
                                                    }}
                                                    disabled={savingLog}
                                                >
                                                    <Text style={styles.modalBtnCancelText}>Edit input</Text>
                                                </Pressable>
                                                <Pressable
                                                    style={[
                                                        styles.modalBtn,
                                                        styles.modalBtnSave,
                                                        savingLog && styles.modalBtnDisabled,
                                                    ]}
                                                    onPress={() => void handleSaveMealFromDraft()}
                                                    disabled={savingLog}
                                                >
                                                    {savingLog ? (
                                                        <ActivityIndicator color="#fff" />
                                                    ) : (
                                                        <Text style={styles.modalBtnSaveText}>Save log</Text>
                                                    )}
                                                </Pressable>
                                            </View>
                                        </>
                                    ) : aiResult ? (
                                        <>
                                            {aiResult.type === 'PROFILE_NEEDED' && aiResult.profilePrompt ? (
                                                <Text style={styles.resultBlock}>{aiResult.profilePrompt}</Text>
                                            ) : aiResult.adviceText ? (
                                                <StreamingAssistantMarkdown
                                                    markdown={aiResult.adviceText}
                                                    textColor={COLORS.dark}
                                                    linkColor={COLORS.primary}
                                                />
                                            ) : (
                                                <Text style={styles.resultBlock}>
                                                    No structured food analysis in this response. Try different wording,
                                                    use Photo, open AI Chat, or go back and choose the Manual tab.
                                                </Text>
                                            )}
                                            <Pressable
                                                style={[styles.modalBtn, styles.modalBtnCancel, { marginTop: 16 }]}
                                                onPress={goBackToLogDescribeOptions}
                                            >
                                                <Text style={styles.modalBtnCancelText}>Back</Text>
                                            </Pressable>
                                        </>
                                    ) : null}
                                </>
                            )}
                        </ScrollView>
                    </View>
                </KeyboardAvoidingView>
            </Modal>
        </SafeAreaView>
    )
}

// ─── Styles ──────────────────────────────────────────────────
const styles = StyleSheet.create({
    // ── Layout ──
    safe: {
        backgroundColor: COLORS.bg,
        flex: 1,
    },
    scroll: {
        flex: 1,
    },
    scrollContent: {
        paddingHorizontal: 0,
        paddingTop: 8,
    },

    // ── Calorie Card ──
    calorieCard: {
        backgroundColor: COLORS.card,
        borderRadius: 22,
        elevation: 2,
        marginBottom: 16,
        padding: 20,
        position: 'relative',
        shadowColor: COLORS.shadow,
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 6,
    },
    calorieGoal: {
        color: COLORS.sub,
        fontSize: 18,
        fontWeight: '500',
    },
    calorieLabel: {
        color: COLORS.sub,
        fontSize: 13,
        marginBottom: 4,
    },
    calorieLeft: {
        flex: 1,
    },
    calorieRemaining: {
        color: COLORS.remaining,
        fontSize: 14,
        fontWeight: '600',
        marginTop: 6,
    },
    calorieRow: {
        alignItems: 'baseline',
        flexDirection: 'row',
    },
    calorieTop: {
        alignItems: 'center',
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 18,
    },
    calorieCurrent: {
        color: COLORS.dark,
        fontSize: 38,
        fontWeight: '800',
    },

    // ── Ring Container ──
    ringContainer: {
        position: 'absolute',
        right: 22,
        top: 20,
    },

    // ── Macros ──
    macroItem: {
        alignItems: 'center',
        flex: 1,
    },
    macroLabel: {
        color: COLORS.sub,
        fontSize: 12,
        marginTop: 2,
    },
    macroRow: {
        flexDirection: 'row',
        paddingTop: 16,
    },
    macroValue: {
        color: COLORS.dark,
        fontSize: 16,
        fontWeight: '700',
    },

    // ── 7-day histogram ──
    histogramCard: {
        backgroundColor: COLORS.card,
        borderRadius: 18,
        elevation: 1,
        padding: 16,
        shadowColor: COLORS.shadow,
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.06,
        shadowRadius: 4,
    },
    histogramAvgMuted: {
        color: COLORS.sub,
        fontSize: 12,
    },
    histogramAvgValue: {
        color: COLORS.primaryDark,
        fontSize: 12,
        fontVariant: ['tabular-nums'],
        fontWeight: '800',
    },
    histogramAvgWrap: {
        textAlign: 'right',
    },
    histogramCal: {
        color: COLORS.dark,
        fontSize: 10,
        fontWeight: '600',
        marginBottom: 6,
        textAlign: 'center',
    },
    histogramCol: {
        alignItems: 'center',
        flex: 1,
        minWidth: 0,
    },
    histogramDay: {
        color: COLORS.sub,
        fontSize: 11,
        fontWeight: '500',
        marginTop: 6,
    },
    histogramFill: {
        backgroundColor: COLORS.primary,
        borderRadius: 5,
        minHeight: 4,
        width: '72%',
    },
    histogramFillSelected: {
        backgroundColor: COLORS.primaryDark,
        width: '82%',
    },
    histogramHeader: {
        alignItems: 'center',
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 12,
    },
    histogramHeaderRight: {
        alignItems: 'flex-end',
        maxWidth: '52%',
    },
    histogramMacros: {
        color: COLORS.sub,
        fontSize: 8,
        fontWeight: '500',
        marginTop: 2,
        maxWidth: '100%',
        textAlign: 'center',
    },
    histogramRow: {
        alignItems: 'flex-end',
        flexDirection: 'row',
        justifyContent: 'space-between',
        minHeight: 148,
    },
    histogramTitle: {
        color: COLORS.dark,
        flex: 1,
        fontSize: 16,
        fontWeight: '700',
        marginRight: 8,
    },
    histogramTrack: {
        alignItems: 'center',
        backgroundColor: COLORS.iconBg,
        borderRadius: 6,
        height: 112,
        justifyContent: 'flex-end',
        overflow: 'hidden',
        width: '100%',
    },

    emptyMeals: {
        alignItems: 'center',
        backgroundColor: COLORS.card,
        borderRadius: 16,
        marginBottom: 10,
        paddingHorizontal: 16,
        paddingVertical: 28,
    },
    emptyMealsSub: {
        color: COLORS.sub,
        fontSize: 14,
        marginTop: 6,
        textAlign: 'center',
    },
    emptyMealsTitle: {
        color: COLORS.dark,
        fontSize: 16,
        fontWeight: '600',
        marginTop: 10,
    },
    mealPlaceholder: {
        opacity: 0.85,
    },
    skeletonLine: {
        backgroundColor: '#E4E9E0',
        borderRadius: 4,
        height: 14,
        width: '70%',
    },

    // ── Section Header ──
    sectionHeader: {
        alignItems: 'center',
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 10,
    },
    sectionHeaderRight: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 14,
    },
    sectionTitle: {
        color: COLORS.dark,
        fontSize: 17,
        fontWeight: '600',
    },
    refreshMealsBtn: {
        padding: 4,
    },
    refreshMealsBtnPressed: {
        opacity: 0.6,
    },
    logMealHeaderBtn: {
        backgroundColor: COLORS.primary,
        borderRadius: 20,
        paddingHorizontal: 14,
        paddingVertical: 8,
    },
    logMealHeaderBtnPressed: {
        backgroundColor: COLORS.primaryDark,
    },
    logMealHeaderBtnText: {
        color: '#fff',
        fontSize: 14,
        fontWeight: '600',
    },

    // ── Meal Card ──
    mealCalNum: {
        color: COLORS.dark,
        fontSize: 16,
        fontWeight: '700',
    },
    mealCalUnit: {
        color: COLORS.sub,
        fontSize: 12,
    },
    mealCalories: {
        alignItems: 'flex-end',
    },
    mealCard: {
        alignItems: 'center',
        backgroundColor: COLORS.card,
        borderRadius: 16,
        elevation: 1,
        flexDirection: 'row',
        marginBottom: 10,
        padding: 12,
        shadowColor: COLORS.shadow,
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.04,
        shadowRadius: 3,
    },
    mealIconBox: {
        alignItems: 'center',
        backgroundColor: COLORS.iconBg,
        borderRadius: 10,
        height: 40,
        justifyContent: 'center',
        marginRight: 12,
        width: 40,
    },
    mealInfo: {
        flex: 1,
    },
    mealMeta: {
        color: '#9BA3A8',
        fontSize: 12,
        marginTop: 2,
    },
    mealName: {
        color: COLORS.dark,
        fontSize: 15,
        fontWeight: '500',
    },

    // ── Add Meal Button ──
    addMealBtn: {
        alignItems: 'center',
        backgroundColor: COLORS.primary,
        borderRadius: 16,
        justifyContent: 'center',
        marginTop: 6,
        paddingVertical: 14,
    },
    addMealBtnPressed: {
        backgroundColor: COLORS.primaryDark,
    },
    addMealText: {
        color: '#fff',
        fontSize: 16,
        fontWeight: '600',
    },

    // ── FAB (Chat) ── (now handled by FloatingChatButton)
    // FAB styles moved to FloatingChatButton

    // ── Modal Styles ──
    modalOverlay: {
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        flex: 1,
        justifyContent: 'flex-end',
    },
    modalContainer: {
        backgroundColor: COLORS.card,
        borderTopLeftRadius: 20,
        borderTopRightRadius: 20,
        maxHeight: '80%',
        minHeight: '50%',
    },
    /** Log meal: larger viewing area for preview and editing */
    logMealModalContainer: {
        maxHeight: '94%',
        minHeight: '72%',
    },
    modalHeader: {
        alignItems: 'center',
        borderBottomColor: '#F0F0F0',
        borderBottomWidth: 1,
        flexDirection: 'row',
        justifyContent: 'space-between',
        padding: 20,
    },
    modalTitle: {
        color: COLORS.dark,
        fontSize: 20,
        fontWeight: '700',
    },
    modalContent: {
        flex: 1,
        padding: 20,
    },

    // ── Form Styles ──
    formGroup: {
        marginBottom: 20,
    },
    formHint: {
        color: COLORS.sub,
        fontSize: 14,
        lineHeight: 20,
        marginBottom: 16,
    },
    formLabel: {
        color: COLORS.dark,
        fontSize: 16,
        fontWeight: '600',
        marginBottom: 8,
    },
    textInput: {
        backgroundColor: COLORS.bg,
        borderColor: '#E0E0E0',
        borderRadius: 12,
        borderWidth: 1,
        color: COLORS.dark,
        fontSize: 16,
        padding: 12,
    },
    textInputMultiline: {
        minHeight: 100,
        textAlignVertical: 'top',
    },
    modalActions: {
        flexDirection: 'row',
        gap: 12,
        marginTop: 20,
    },
    modalBtn: {
        alignItems: 'center',
        borderRadius: 12,
        flex: 1,
        paddingVertical: 14,
    },
    modalBtnCancel: {
        backgroundColor: COLORS.bg,
        borderColor: '#E0E0E0',
        borderWidth: 1,
    },
    modalBtnSave: {
        backgroundColor: COLORS.primary,
    },
    modalBtnDisabled: {
        opacity: 0.65,
    },
    modalEmptyText: {
        color: COLORS.sub,
        fontSize: 15,
        paddingVertical: 24,
        textAlign: 'center',
    },
    modalBtnCancelText: {
        color: COLORS.sub,
        fontSize: 16,
        fontWeight: '600',
    },
    modalBtnSaveText: {
        color: '#fff',
        fontSize: 16,
        fontWeight: '600',
    },

    backLink: {
        marginBottom: 12,
        paddingVertical: 4,
    },
    backLinkText: {
        color: COLORS.primary,
        fontSize: 15,
        fontWeight: '600',
    },
    logSegBtn: {
        alignItems: 'center',
        borderRadius: 10,
        flex: 1,
        overflow: 'visible',
        paddingVertical: 10,
        position: 'relative',
    },
    logSegAiBadge: {
        alignItems: 'center',
        backgroundColor: 'rgba(29,53,87,0.12)',
        borderRadius: 5,
        justifyContent: 'center',
        paddingHorizontal: 5,
        paddingVertical: 2,
        position: 'absolute',
        right: 4,
        top: 3,
    },
    logSegAiBadgeOn: {
        backgroundColor: 'rgba(255,255,255,0.4)',
    },
    logSegAiBadgeTxt: {
        color: COLORS.dark,
        fontSize: 9,
        fontWeight: '800',
        letterSpacing: 0.4,
    },
    logSegAiBadgeTxtOn: {
        color: '#fff',
    },
    logSegBtnOn: {
        backgroundColor: COLORS.primary,
    },
    logSegTxt: {
        color: COLORS.sub,
        fontSize: 12,
        fontWeight: '600',
    },
    logSegTxtOn: {
        color: '#fff',
    },
    logSegmentWrap: {
        backgroundColor: COLORS.bg,
        borderRadius: 14,
        flexDirection: 'row',
        gap: 6,
        marginBottom: 16,
        padding: 4,
    },
    photoPreview: {
        backgroundColor: '#E8ECE8',
        borderRadius: 14,
        height: 220,
        marginTop: 12,
        width: '100%',
    },
    photoPreviewActions: {
        flexDirection: 'row',
        gap: 10,
        marginTop: 12,
    },
    photoPreviewChip: {
        alignItems: 'center',
        backgroundColor: COLORS.bg,
        borderColor: '#E0E0E0',
        borderRadius: 12,
        borderWidth: 1,
        flex: 1,
        flexDirection: 'row',
        gap: 8,
        justifyContent: 'center',
        paddingVertical: 12,
    },
    photoPreviewChipText: {
        color: COLORS.dark,
        fontSize: 15,
        fontWeight: '600',
    },
    photoRemoveBtn: {
        alignSelf: 'center',
        marginTop: 10,
        paddingVertical: 8,
    },
    photoRemoveBtnText: {
        color: COLORS.sub,
        fontSize: 14,
        fontWeight: '600',
    },
    resultBlock: {
        color: COLORS.sub,
        fontSize: 15,
        lineHeight: 22,
    },
    secondaryOutlineBtn: {
        alignItems: 'center',
        backgroundColor: COLORS.bg,
        borderColor: '#E0E0E0',
        borderRadius: 12,
        borderWidth: 1,
        marginTop: 10,
        paddingVertical: 14,
    },
    secondaryOutlineBtnText: {
        color: COLORS.dark,
        fontSize: 15,
        fontWeight: '600',
    },
})
