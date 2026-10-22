/**
 * Place Supabase-generated Database types here
 * Currently only Auth is used; replace table schema types later with supabase gen types output
 */
export type Database = {
    public: {
        Tables: Record<string, unknown>
        Views: Record<string, unknown>
        Functions: Record<string, unknown>
        Enums: Record<string, unknown>
    }
}
