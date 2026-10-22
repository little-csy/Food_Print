import type { Action } from 'expo-image-manipulator'
import { manipulateAsync, SaveFormat } from 'expo-image-manipulator'
import type { ImagePickerAsset } from 'expo-image-picker'

/** Maximum long edge in pixels; limits upload size and backend processing time */
const MAX_EDGE_PX = 1680
/** JPEG quality from 0 to 1; slightly below 1 significantly reduces file size */
const JPEG_COMPRESS = 0.78

function jpegFileName(asset: ImagePickerAsset): string {
    const raw = asset.fileName?.trim()
    if (raw) {
        const base = raw.replace(/\.[^/.]+$/, '')
        return `${base || 'photo'}.jpg`
    }
    return 'photo.jpg'
}

/**
 * Compress before upload: resize the long edge if necessary and convert to JPEG to reduce multipart size.
 * Return the original asset on failure without blocking upload.
 */
export async function compressImagePickerAssetForUpload(
    asset: ImagePickerAsset
): Promise<ImagePickerAsset> {
    try {
        const w = asset.width ?? 0
        const h = asset.height ?? 0
        const actions: Action[] = []
        if (w > 0 && h > 0 && (w > MAX_EDGE_PX || h > MAX_EDGE_PX)) {
            if (w >= h) {
                actions.push({ resize: { width: MAX_EDGE_PX } })
            } else {
                actions.push({ resize: { height: MAX_EDGE_PX } })
            }
        }

        const result = await manipulateAsync(asset.uri, actions, {
            compress: JPEG_COMPRESS,
            format: SaveFormat.JPEG,
        })

        return {
            ...asset,
            uri: result.uri,
            width: result.width,
            height: result.height,
            mimeType: 'image/jpeg',
            fileName: jpegFileName(asset),
        }
    } catch {
        return asset
    }
}
