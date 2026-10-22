# FoodPrint Backend - Food Logging API

## Overview

The Food Logging module allows users to record meals and automatically extract nutrition information using AI analysis.

The backend processes a food description or image, analyzes the nutrition content, and stores both the food log and nutrition breakdown in the database.

Example flow:

User input (text or image)  
↓  
AI analysis  
↓  
Food log saved  
↓  
Nutrition breakdown stored  
↓  
API returns result

---

# Base URL

http://localhost:8080/api

---

# 1. Create Food Log

Create a new food log entry.

The backend analyzes the food description using AI and stores the nutrition data.

# Endpoint

POST /api/food-logs

---

# Request Headers

| Header | Description |
|------|------|
| Content-Type | application/json |
| userId | user identifier |

Example:

userId: test-user

---

# Request Body

{
  "text": "chicken breast and rice",
  "date": "2026-03-07"
}

| Field    | Type   | Required | Description           |
| -------- | ------ | -------- | --------------------- |
| text     | string | optional | Food description      |
| imageUrl | string | optional | Image URL of meal     |
| date     | string | optional | Log date (YYYY-MM-DD) |
At least one of the following must be provided:

text
or
imageUrl

# Example Request

POST /api/food-logs
{
  "text": "chicken breast and rice"
}

# Response Example

{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": "test-user",
    "logDate": "2026-03-07",
    "imageUrl": null,
    "originalText": "chicken breast and rice",
    "aiConfidence": 0.95,
    "createdAt": "2026-03-08T00:24:57"
  }
}

# Database Structure

food_logs
Each record represents a meal log.

| Column        | Description         |
| ------------- | ------------------- |
| id            | primary key         |
| user_id       | user identifier     |
| log_date      | meal date           |
| image_url     | optional image      |
| original_text | user input          |
| ai_confidence | AI confidence score |
| created_at    | creation time       |


# food_nutrition_details
Each meal may contain multiple foods.

Example:

Chicken Breast
Rice

These are stored as separate rows.
| Column         | Description               |
| -------------- | ------------------------- |
| id             | primary key               |
| food_log_id    | reference to food_logs.id |
| name_en        | food name (English)       |
| name_zh        | food name (Chinese)       |
| calories       | calories                  |
| protein_g      | protein grams             |
| fat_g          | fat grams                 |
| carbs_g        | carbohydrate grams        |
| fiber_g        | fiber grams               |
| portion_amount | portion amount            |
| portion_unit   | unit                      |
| data_source    | nutrition data source     |


# Example Stored Result
Input:

chicken breast and rice

Stored foods:

| Food           | Calories | Protein |
| -------------- | -------- | ------- |
| Chicken Breast | 165      | 31g     |
| Rice           | 260      | 5g      |

Total calories:

425 kcal

# Future Endpoints

These endpoints will be added later.

# Get Food Logs by Date

GET /api/food-logs?date=2026-03-07

Returns all food logs for the specified date.

# Get Daily Nutrition Summary

GET /api/food-logs/today/summary

Example response:
{
  "calories": 1800,
  "protein": 120,
  "carbs": 200,
  "fat": 60
}

# Development Notes

During local development the AI service may run in mock mode.

This allows developers to test the backend without calling external AI APIs and avoids API rate limit issues.

Example mock result:

Chicken Breast
Rice

# Example Frontend Flow

Typical frontend interaction:

User enters meal description
↓
Frontend calls POST /api/food-logs
↓
Backend analyzes meal
↓
Backend stores nutrition data
↓
Frontend refreshes daily nutrition summary

# Module Status

Current implementation:

Food log creation
AI analysis
Nutrition extraction
Database persistence
REST API

# Maintainer

FoodPrint Backend
Food Logging Module
