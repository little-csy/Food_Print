import { supabase } from '../lib/supabase'
import { readAsStringAsync } from 'expo-file-system/legacy'

/**
 * Upload image to Supabase Storage
 * @param imageUri - Local image URI
 * @param folder - Storage folder (e.g. 'posts', 'challenges')
 * @returns Public URL after upload URL
 */
export async function uploadImageToSupabase(imageUri: string, folder: string = 'posts'): Promise<string> {
  try {
    // Generate a unique filename
    const timestamp = Date.now()
    const randomId = Math.random().toString(36).substring(7)
    const fileName = `${folder}_${timestamp}_${randomId}.jpg`
    const filePath = `${folder}/${fileName}`

    // Read the local file as Base64
    const fileBase64 = await readAsStringAsync(imageUri, {
      encoding: 'base64',
    })

    // Upload to Supabase Storage
    const { data, error } = await supabase.storage
      .from('public')
      .upload(filePath, base64ToUint8Array(fileBase64), {
        contentType: 'image/jpeg',
      })

    if (error) {
      console.error('❌ Image upload failed:', error)
      throw error
    }

    // Get public URL
    const { data: publicData } = supabase.storage.from('public').getPublicUrl(filePath)

    if (!publicData?.publicUrl) {
      throw new Error('Failed to get public URL')
    }

    console.log('✅ Image uploaded successfully:', publicData.publicUrl)
    return publicData.publicUrl
  } catch (error) {
    console.error('❌ [API] Image upload error:', error)
    throw error
  }
}

/**
 * Base64 Convert string to Uint8Array
 */
function base64ToUint8Array(base64: string): Uint8Array {
  const binaryString = atob(base64)
  const bytes = new Uint8Array(binaryString.length)
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i)
  }
  return bytes
}
