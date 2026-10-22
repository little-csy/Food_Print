/**
 * FoodPrint backend API wrapper
 * Authentication: all requests automatically include Supabase JWT (Authorization: Bearer <token>)
 */

export { apiRequest, apiGet, apiPost, apiPut, apiDelete, ApiError } from './client';
export * from './user';
export * from './food';
export * from './ai';
export * from './community';
export * from './challenge';
export * from './upload';
export * from './types';
