import Ionicons from '@expo/vector-icons/Ionicons'
import React, { useCallback, useMemo } from 'react'
import { Image, Pressable, StyleSheet, Text, TextInput, View } from 'react-native'

import type { FoodAnalysisResult, FoodNutritionDetail } from '../../api/types'

const COLORS = {
    card: '#FFFFFF',
    primary: '#97B08A',
    dark: '#1D3557',
    sub: '#7D8A97',
    iconBg: '#E8F0E4',
    warn: '#F39C12',
}

export interface FoodLineDraft {
    id: string
    name: string
    calories: string
    proteinG: string
    fatG: string
    carbsG: string
    portionLabel: string
    flagged?: boolean
    flagReason?: string
}

export interface MealLogDraft {
    displayText: string
    originalInput: string
    calories: string
    proteinG: string
    fatG: string
    carbsG: string
    foodLines: FoodLineDraft[]
}

function numFromFood(f: FoodNutritionDetail, key: keyof FoodNutritionDetail): string {
    const v = f[key]
    if (typeof v === 'number' && !Number.isNaN(v)) return String(Math.round(v))
    return '0'
}

function lineFromFood(f: FoodNutritionDetail, idx: number): FoodLineDraft {
    const portion =
        f.portionAmount != null && f.portionUnit
            ? `${f.portionAmount}${f.portionUnit}`
            : f.portionUnit || ''
    return {
        id: `food-${idx}-${f.nameEn ?? f.nameZh ?? idx}`,
        name: String(f.nameEn || f.nameZh || '').trim(),
        calories: numFromFood(f, 'calories'),
        proteinG: numFromFood(f, 'proteinG'),
        fatG: numFromFood(f, 'fatG'),
        carbsG: numFromFood(f, 'carbsG'),
        portionLabel: portion,
        flagged: f.flagged,
        flagReason: f.flagReason,
    }
}

/**
 * Without AI: split the description on commas, semicolons, or newlines; initialize nutrition to 0 and open the same editing UI as after Analyze.
 */
export function buildManualMealDraftFromDescription(raw: string): MealLogDraft {
    const trimmed = raw.trim()
    const originalInput = trimmed || 'Meal'
    const parts = trimmed
        ? trimmed.split(/\s*[,;\n]+\s*/).map((s) => s.trim()).filter(Boolean)
        : []
    const names = parts.length > 0 ? parts : [originalInput]
    const foodLines: FoodLineDraft[] = names.map((name, i) => ({
        id: `food-manual-${i}-${Math.random().toString(36).slice(2, 10)}`,
        name,
        calories: '0',
        proteinG: '0',
        fatG: '0',
        carbsG: '0',
        portionLabel: '',
    }))
    return {
        displayText: names.join(', '),
        originalInput,
        calories: '0',
        proteinG: '0',
        fatG: '0',
        carbsG: '0',
        foodLines,
    }
}

export function buildMealDraftFromAnalysis(
    fa: FoodAnalysisResult,
    originalInput: string
): MealLogDraft {
    const foods = fa.foods ?? []
    const lines = foods.map((f, i) => lineFromFood(f, i))
    const s = fa.summary
    const sum = (pick: (l: FoodLineDraft) => number) =>
        lines.reduce((acc, l) => acc + (Number(pick(l)) || 0), 0)

    const c =
        typeof s?.totalCalories === 'number' && !Number.isNaN(s.totalCalories)
            ? Math.round(s.totalCalories)
            : Math.round(sum((l) => Number(l.calories)))
    const p =
        typeof s?.totalProteinG === 'number' && !Number.isNaN(s.totalProteinG)
            ? Math.round(s.totalProteinG)
            : Math.round(sum((l) => Number(l.proteinG)))
    const f =
        typeof s?.totalFatG === 'number' && !Number.isNaN(s.totalFatG)
            ? Math.round(s.totalFatG)
            : Math.round(sum((l) => Number(l.fatG)))
    const cb =
        typeof s?.totalCarbsG === 'number' && !Number.isNaN(s.totalCarbsG)
            ? Math.round(s.totalCarbsG)
            : Math.round(sum((l) => Number(l.carbsG)))

    const names = lines.map((l) => l.name).filter(Boolean)
    const display = names.length > 0 ? names.join(', ') : originalInput.trim()

    return {
        displayText: display,
        originalInput: originalInput.trim(),
        calories: String(c),
        proteinG: String(p),
        fatG: String(f),
        carbsG: String(cb),
        foodLines: lines,
    }
}

export function parseNonNegNumber(s: string): number {
    const n = Math.round(Number(String(s).replace(/,/g, '.')))
    return Number.isFinite(n) && n >= 0 ? n : 0
}

export function mealDraftNutritionPayload(draft: MealLogDraft) {
    return {
        calories: parseNonNegNumber(draft.calories),
        proteinG: parseNonNegNumber(draft.proteinG),
        fat: parseNonNegNumber(draft.fatG),
        carbs: parseNonNegNumber(draft.carbsG),
    }
}

/** Aggregate nutrition from food rows, matching "Sum lines → totals"; use these totals when rows exist */
export function nutritionTotalsFromFoodLines(draft: MealLogDraft) {
    return {
        calories: draft.foodLines.reduce((a, l) => a + parseNonNegNumber(l.calories), 0),
        proteinG: draft.foodLines.reduce((a, l) => a + parseNonNegNumber(l.proteinG), 0),
        fat: draft.foodLines.reduce((a, l) => a + parseNonNegNumber(l.fatG), 0),
        carbs: draft.foodLines.reduce((a, l) => a + parseNonNegNumber(l.carbsG), 0),
    }
}

export function draftToRecognizedFoods(d: MealLogDraft): string[] {
    const fromLines = d.foodLines.map((l) => l.name.trim()).filter(Boolean)
    if (fromLines.length > 0) return fromLines
    const t = d.displayText.trim()
    return t ? [t] : []
}

function parsePortionFromDraftLabel(
    label: string
): Pick<FoodNutritionDetail, 'portionAmount' | 'portionUnit'> {
    const t = label.trim()
    if (!t) return {}
    const m = /^(\d+(?:\.\d+)?)\s*([a-zA-Z\u4e00-\u9fff]+)?$/.exec(t)
    if (!m) return {}
    const amt = Number(m[1])
    return {
        portionAmount: Number.isFinite(amt) ? amt : undefined,
        portionUnit: m[2] || undefined,
    }
}

/**
 * Convert the current form draft to the backend `analysisResult`, including user-modified values.
 */
export function buildAnalysisResultFromDraft(
    draft: MealLogDraft,
    base?: FoodAnalysisResult | null
): FoodAnalysisResult {
    const totals =
        draft.foodLines.length > 0
            ? nutritionTotalsFromFoodLines(draft)
            : mealDraftNutritionPayload(draft)
    const lineToDetail = (line: FoodLineDraft): FoodNutritionDetail => {
        const portion = parsePortionFromDraftLabel(line.portionLabel)
        return {
            nameEn: line.name.trim() || undefined,
            calories: parseNonNegNumber(line.calories),
            proteinG: parseNonNegNumber(line.proteinG),
            fatG: parseNonNegNumber(line.fatG),
            carbsG: parseNonNegNumber(line.carbsG),
            ...portion,
            flagged: line.flagged,
            flagReason: line.flagReason,
        }
    }

    const foods: FoodNutritionDetail[] =
        draft.foodLines.length > 0
            ? draft.foodLines.map(lineToDetail)
            : [
                  {
                      nameEn: draft.displayText.trim() || 'Meal',
                      calories: totals.calories,
                      proteinG: totals.proteinG,
                      fatG: totals.fat,
                      carbsG: totals.carbs,
                  },
              ]

    return {
        foods,
        summary: {
            totalCalories: totals.calories,
            totalProteinG: totals.proteinG,
            totalFatG: totals.fat,
            totalCarbsG: totals.carbs,
            foodCount: foods.length,
        },
        confidence: base?.confidence,
    }
}

export interface FoodAnalysisEditorProps {
    analysis: FoodAnalysisResult
    imageUri?: string | null
    draft: MealLogDraft
    onChangeDraft: (next: MealLogDraft) => void
    editable?: boolean
    /** manual: hide AI confidence and identify this as manual entry */
    entryKind?: 'ai' | 'manual'
}

export function FoodAnalysisEditor({
    analysis,
    imageUri,
    draft,
    onChangeDraft,
    editable = true,
    entryKind = 'ai',
}: FoodAnalysisEditorProps) {
    const confPct = useMemo(() => {
        if (entryKind === 'manual') return null
        const c = analysis.confidence
        if (typeof c === 'number' && c >= 0 && c <= 1) return Math.round(c * 100)
        return null
    }, [analysis.confidence, entryKind])

    const setSummary = useCallback(
        (field: 'calories' | 'proteinG' | 'fatG' | 'carbsG', value: string) => {
            onChangeDraft({ ...draft, [field]: value })
        },
        [draft, onChangeDraft]
    )

    const setDisplayText = useCallback(
        (displayText: string) => {
            onChangeDraft({ ...draft, displayText })
        },
        [draft, onChangeDraft]
    )

    const setLine = useCallback(
        (id: string, patch: Partial<FoodLineDraft>) => {
            const nextLines = draft.foodLines.map((l) =>
                l.id === id ? { ...l, ...patch } : l
            )
            const t = nutritionTotalsFromFoodLines({ ...draft, foodLines: nextLines })
            onChangeDraft({
                ...draft,
                foodLines: nextLines,
                calories: String(t.calories),
                proteinG: String(t.proteinG),
                fatG: String(t.fat),
                carbsG: String(t.carbs),
            })
        },
        [draft, onChangeDraft]
    )

    const recalcFromLines = useCallback(() => {
        const t = nutritionTotalsFromFoodLines(draft)
        onChangeDraft({
            ...draft,
            calories: String(t.calories),
            proteinG: String(t.proteinG),
            fatG: String(t.fat),
            carbsG: String(t.carbs),
        })
    }, [draft, onChangeDraft])

    return (
        <View style={styles.wrap}>
            {entryKind === 'manual' ? (
                <Text style={styles.confHint}>
                    Manual entry — no AI. Add or edit foods and numbers below, then save.
                </Text>
            ) : confPct != null ? (
                <Text style={styles.confHint}>
                    AI confidence ~{confPct}% — you can edit values before saving.
                </Text>
            ) : (
                <Text style={styles.confHint}>Edit nutrition values as needed, then save.</Text>
            )}

            {imageUri ? (
                <Image source={{ uri: imageUri }} style={styles.previewImg} resizeMode="cover" />
            ) : null}

            <Text style={styles.label}>Log title / display name</Text>
            <TextInput
                style={styles.input}
                value={draft.displayText}
                onChangeText={setDisplayText}
                placeholder="e.g. Grilled chicken salad"
                placeholderTextColor={COLORS.sub}
                editable={editable}
            />

            <View style={styles.summaryRow}>
                {(
                    [
                        ['calories', 'kcal', draft.calories],
                        ['proteinG', 'Protein', draft.proteinG],
                        ['fatG', 'Fat', draft.fatG],
                        ['carbsG', 'Carbs', draft.carbsG],
                    ] as const
                ).map(([key, label, val]) => (
                    <View key={key} style={styles.summaryItem}>
                        <TextInput
                            style={styles.summaryInput}
                            keyboardType="numeric"
                            value={val}
                            onChangeText={(t) => setSummary(key, t)}
                            editable={editable && draft.foodLines.length === 0}
                        />
                        <Text style={styles.summaryLabel}>{label}</Text>
                    </View>
                ))}
            </View>
            {draft.foodLines.length > 0 ? (
                <Text style={styles.totalsHint}>
                    Totals follow line items — edit rows below to change what gets saved.
                </Text>
            ) : null}

            {draft.foodLines.length > 0 ? (
                <>
                    <View style={styles.rowBetween}>
                        <Text style={styles.subTitle}>Line items</Text>
                        <Pressable
                            style={({ pressed }) => [styles.linkBtn, pressed && { opacity: 0.7 }]}
                            onPress={recalcFromLines}
                            disabled={!editable}
                        >
                            <Text style={styles.linkBtnText}>Sum lines → totals</Text>
                        </Pressable>
                    </View>
                    {draft.foodLines.map((line) => (
                        <View key={line.id} style={styles.foodCard}>
                            <TextInput
                                style={styles.foodNameInput}
                                value={line.name}
                                onChangeText={(t) => setLine(line.id, { name: t })}
                                placeholder="Food name"
                                placeholderTextColor={COLORS.sub}
                                editable={editable}
                            />
                            {line.portionLabel ? (
                                <Text style={styles.portion}>{line.portionLabel}</Text>
                            ) : null}
                            <View style={styles.miniRow}>
                                {(
                                    [
                                        ['calories', 'kcal'],
                                        ['proteinG', 'P'],
                                        ['fatG', 'F'],
                                        ['carbsG', 'C'],
                                    ] as const
                                ).map(([k, short]) => (
                                    <View key={k} style={styles.miniCell}>
                                        <Text style={styles.miniLab}>{short}</Text>
                                        <TextInput
                                            style={styles.miniIn}
                                            keyboardType="numeric"
                                            value={line[k]}
                                            onChangeText={(t) => setLine(line.id, { [k]: t })}
                                            editable={editable}
                                        />
                                    </View>
                                ))}
                            </View>
                            {line.flagged && line.flagReason ? (
                                <View style={styles.flagRow}>
                                    <Ionicons name="warning" size={14} color={COLORS.warn} />
                                    <Text style={styles.flagText}>{line.flagReason}</Text>
                                </View>
                            ) : null}
                        </View>
                    ))}
                </>
            ) : null}
        </View>
    )
}

const styles = StyleSheet.create({
    wrap: {
        marginTop: 8,
    },
    confHint: {
        color: COLORS.sub,
        fontSize: 13,
        marginBottom: 12,
        lineHeight: 18,
    },
    previewImg: {
        width: '100%',
        height: 160,
        borderRadius: 12,
        marginBottom: 14,
    },
    label: {
        color: COLORS.dark,
        fontSize: 14,
        fontWeight: '600',
        marginBottom: 6,
    },
    input: {
        backgroundColor: COLORS.iconBg,
        borderRadius: 12,
        paddingHorizontal: 12,
        paddingVertical: 10,
        fontSize: 15,
        color: COLORS.dark,
        marginBottom: 14,
    },
    summaryRow: {
        flexDirection: 'row',
        backgroundColor: COLORS.iconBg,
        borderRadius: 14,
        paddingVertical: 10,
        paddingHorizontal: 4,
        marginBottom: 16,
        justifyContent: 'space-between',
    },
    summaryItem: {
        flex: 1,
        alignItems: 'center',
    },
    summaryInput: {
        fontSize: 16,
        fontWeight: '700',
        color: COLORS.dark,
        textAlign: 'center',
        minWidth: 44,
        paddingVertical: 4,
    },
    summaryLabel: {
        fontSize: 11,
        color: COLORS.sub,
        marginTop: 2,
    },
    subTitle: {
        fontSize: 15,
        fontWeight: '700',
        color: COLORS.dark,
    },
    totalsHint: {
        color: COLORS.sub,
        fontSize: 12,
        lineHeight: 17,
        marginBottom: 10,
        marginTop: -8,
    },
    rowBetween: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 10,
    },
    linkBtn: {
        paddingVertical: 4,
        paddingHorizontal: 6,
    },
    linkBtnText: {
        color: COLORS.primary,
        fontSize: 13,
        fontWeight: '600',
    },
    foodCard: {
        borderBottomWidth: 1,
        borderBottomColor: '#F0F0F0',
        paddingBottom: 12,
        marginBottom: 12,
    },
    foodNameInput: {
        fontSize: 15,
        fontWeight: '600',
        color: COLORS.dark,
        marginBottom: 4,
    },
    portion: {
        fontSize: 12,
        color: COLORS.sub,
        marginBottom: 8,
    },
    miniRow: {
        flexDirection: 'row',
        gap: 8,
    },
    miniCell: {
        flex: 1,
    },
    miniLab: {
        fontSize: 11,
        color: COLORS.sub,
        marginBottom: 2,
    },
    miniIn: {
        backgroundColor: '#F8F9FA',
        borderRadius: 8,
        paddingHorizontal: 8,
        paddingVertical: 6,
        fontSize: 14,
        color: COLORS.dark,
    },
    flagRow: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 6,
        marginTop: 8,
    },
    flagText: {
        flex: 1,
        fontSize: 12,
        color: COLORS.warn,
    },
})
