package com.foodprint.ai.service;

/**
 * Enum for classifying user text input intent.
 */
public enum UserIntent {
    FOOD_LOG, // "I ate chicken with rice for lunch"
    DIET_ADVICE, // "How should I eat to lose weight?"
    DIET_PLAN, // "Make me a weekly meal plan"
    DIET_ROAST, // "Evaluate my diet today" or "Roast me"
    GENERAL_CHAT // "I'm tired today"
}
