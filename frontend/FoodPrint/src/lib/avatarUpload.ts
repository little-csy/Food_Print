import type { ImagePickerAsset } from 'expo-image-picker'

import { compressImagePickerAssetForUpload } from './compressImageForUpload'
import { supabase } from './supabase'

const AVATAR_BUCKET = 'avatars'

function getMimeType(asset: ImagePickerAsset): string {
    return asset.mimeType?.trim() || 'image/jpeg'
}

function getExtension(mimeType: string): string {
    if (mimeType.includes('png')) {
        return 'png'
    }
    if (mimeType.includes('webp')) {
        return 'webp'
    }
    return 'jpg'
}

function buildAvatarPath(userId: string, mimeType: string): string {
    const safeUserId = userId.replace(/[^a-zA-Z0-9_-]/g, '')
    const suffix = Math.random().toString(36).slice(2, 10)
    return `${safeUserId}/avatar-${Date.now()}-${suffix}.${getExtension(mimeType)}`
}

export async function uploadAvatarAsset(
    userId: string,
    asset: ImagePickerAsset
): Promise<string> {
    const prepared = await compressImagePickerAssetForUpload(asset)
    const mimeType = getMimeType(prepared)
    const imageResponse = await fetch(prepared.uri)

    if (!imageResponse.ok) {
        throw new Error('Failed to read the selected avatar image.')
    }

    const imageBytes = await imageResponse.arrayBuffer()
    const objectPath = buildAvatarPath(userId, mimeType)
    const { error } = await supabase.storage
        .from(AVATAR_BUCKET)
        .upload(objectPath, imageBytes, {
            cacheControl: '3600',
            contentType: mimeType,
            upsert: true,
        })

    if (error) {
        const bucketHint = /bucket|not found/i.test(error.message)
            ? ' Create a public Supabase Storage bucket named "avatars" and try again.'
            : ''
        throw new Error(`${error.message}${bucketHint}`)
    }

    const { data } = supabase.storage.from(AVATAR_BUCKET).getPublicUrl(objectPath)

    if (!data.publicUrl) {
        throw new Error('Failed to build the uploaded avatar URL.')
    }

    return data.publicUrl
}
