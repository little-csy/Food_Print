import { 
    getImpactMetrics, 
    getFeed, 
    getMyTopics,
    createPost,
    addComment,
    likePost,
    unlikePost,
    followUser,
    unfollowUser,
    getFollowingFeed,
    joinTopic,
} from './index'
import { ApiError } from './client'
import { getSupabaseAccessToken, supabase } from '../lib/supabase'

const UUID_REGEX =
    /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

function getActionSuccess(data: unknown): boolean | null {
    if (data && typeof data === 'object' && 'success' in data) {
        const success = (data as { success?: unknown }).success
        if (typeof success === 'boolean') {
            return success
        }
    }
    return null
}

/**
 * Check current login status
 */
export async function checkAuthStatus() {
    console.log('\n🔐 === Authentication status check === 🔐\n')
    
    try {
        // Check the Supabase session
        const { data: { session } } = await supabase.auth.getSession()
        console.log('Supabase Session:', {
            isLoggedIn: !!session,
            user: session?.user?.email,
            userID: session?.user?.id,
        })
        
        // Check Token
        const token = await getSupabaseAccessToken()
        console.log('Access Token:', {
            hasToken: !!token,
            tokenPreview: token ? `${token.substring(0, 20)}...` : 'null',
            tokenType: token ? token.split('.').length + ' parts' : 'none',
        })
        
        if (!session) {
            console.log('\n❌ Not logged in!')
            console.log('💡 Follow these steps:')
            console.log('   1. Open the app home screen')
            console.log('   2. Click the "Log in" button')
            console.log('   3. Log in with an existing account')
            console.log('   4. Run this test again')
            return false
        }
        
        console.log('\n✅ Logged in! Ready to test authenticated endpoints\n')
        return true
    } catch (error) {
        console.error('❌ Check failed:', error)
        return false
    }
}

/**
 * Complete Community API functional test
 * Call on reload: run pnpm start, then press r to restart
 */
export async function quickTest() {
    console.log('\n🧪 === Community API Functional test === 🧪\n')

    let feedData: any[] = []
    let followTargetUserId: string | null = null

    try {
        // ========================================
        // 1️⃣ Test fetching impact metrics
        // ========================================
        console.log('1️⃣ Testing getImpactMetrics()...')
        try {
            const metrics = await getImpactMetrics()
            console.log('✅ Success:', {
                code: metrics.code,
                hasData: !!metrics.data,
                dataType: typeof metrics.data,
            })
        } catch (e) {
            console.error('❌ Failure:', e instanceof Error ? e.message : e)
        }

        const loggedIn = await checkAuthStatus()
        if (!loggedIn) {
            console.log('\n⏭️ Not logged in; skipping protected endpoint tests 2️⃣-1️⃣1️⃣ (Feed/Follow/Like/Post/Topic)')
            console.log('✅ Public endpoint tests complete\n')
            return
        }

        // ========================================
        // 2️⃣ Test fetching Feed
        // ========================================
        console.log('\n2️⃣ Testing getFeed(0, 5)...')
        try {
            const feed = await getFeed(0, 5)
            feedData = feed.data || []
            const {
                data: { session },
            } = await supabase.auth.getSession()
            const currentUserId = session?.user?.id
            followTargetUserId =
                feedData.find(
                    (p: any) =>
                        typeof p?.userId === 'string' &&
                        UUID_REGEX.test(p.userId) &&
                        p.userId !== currentUserId
                )?.userId ?? null

            console.log('✅ Success, post count:', feedData.length)
            if (feedData.length > 0) {
                console.log('   First post:', {
                    id: feedData[0].id,
                    title: feedData[0].title,
                    authorNickname: feedData[0].authorNickname,
                    likeCount: feedData[0].likeCount,
                })
            }
            console.log('   Follow test target user:', followTargetUserId || 'No usable UUID user found; will skip 7️⃣/8️⃣')
        } catch (e) {
            console.error('❌ Failure:', e instanceof Error ? e.message : e)
        }

        // ========================================
        // 3️⃣ Test creating a post
        // ========================================
        console.log('\n3️⃣ Testing createPost()...')
        try {
            const newPost = await createPost({
                title: '🧪 Test Post',
                content: 'This is an automated test post - safe to delete',
                imageUrl: null,
            })
            const postData = newPost.data as any
            console.log('✅ Success:', {
                id: postData?.id,
                title: postData?.title,
                content: postData?.content,
                createdAt: postData?.createdAt,
            })
        } catch (e) {
            console.error('❌ Failure:', e instanceof Error ? e.message : e)
        }

        // ========================================
        // 4️⃣ Test liking a post
        // ========================================
        if (feedData.length > 0 && feedData[0].id) {
            const postToLike = feedData[0].id
            console.log('\n4️⃣ Testing likePost(postId: ' + postToLike + ')...')
            try {
                const result = await likePost(postToLike)
                console.log('✅ Success:', {
                    code: result.code,
                    message: result.message,
                    actionSuccess: getActionSuccess(result.data),
                    rawData: result.data,
                })
            } catch (e) {
                console.error('❌ Failure:', e instanceof Error ? e.message : e)
            }

            // ========================================
            // 5️⃣ Test unliking a post
            // ========================================
            console.log('\n5️⃣ Testing unlikePost(postId: ' + postToLike + ')...')
            try {
                const result = await unlikePost(postToLike)
                console.log('✅ Success:', {
                    code: result.code,
                    message: result.message,
                    actionSuccess: getActionSuccess(result.data),
                    rawData: result.data,
                })
            } catch (e) {
                console.error('❌ Failure:', e instanceof Error ? e.message : e)
            }

            // ========================================
            // 6️⃣ Test adding a comment
            // ========================================
            console.log('\n6️⃣ Testing addComment(postId: ' + postToLike + ')...')
            try {
                const comment = await addComment(postToLike, {
                    content: '🧪 This is a test comment',
                })
                const commentData = comment.data as any
                console.log('✅ Success:', {
                    id: commentData.id,
                    content: commentData.content,
                    createdAt: commentData.createdAt,
                })
            } catch (e) {
                console.error('❌ Failure:', e instanceof Error ? e.message : e)
            }
        }

        // ========================================
        // 7️⃣ Test following a user
        // ========================================
        if (followTargetUserId) {
            console.log(`\n7️⃣ Testing followUser(userId: "${followTargetUserId}")...`)
            try {
                const result = await followUser(followTargetUserId)
                console.log('✅ Success:', {
                    code: result.code,
                    message: result.message,
                    actionSuccess: getActionSuccess(result.data),
                    rawData: result.data,
                })
            } catch (e) {
                if (e instanceof ApiError && e.status === 404) {
                    console.log('⚠️ 404 Not Found - User does not exist (expected)')
                } else {
                    console.error('❌ Failure:', e instanceof Error ? e.message : e)
                }
            }
        } else {
            console.log('\n7️⃣ Skipping followUser: no usable UUID target user found in the current Feed')
        }

        // ========================================
        // 8️⃣ Test unfollowing a user
        // ========================================
        if (followTargetUserId) {
            console.log(`\n8️⃣ Testing unfollowUser(userId: "${followTargetUserId}")...`)
            try {
                const result = await unfollowUser(followTargetUserId)
                console.log('✅ Success:', {
                    code: result.code,
                    message: result.message,
                    actionSuccess: getActionSuccess(result.data),
                    rawData: result.data,
                })
            } catch (e) {
                if (e instanceof ApiError && e.status === 404) {
                    console.log('⚠️ 404 Not Found - User does not exist (expected)')
                } else {
                    console.error('❌ Failure:', e instanceof Error ? e.message : e)
                }
            }
        } else {
            console.log('\n8️⃣ Skipping unfollowUser: no usable UUID target user found in the current Feed')
        }

        // ========================================
        // 9️⃣ Test fetching followed users Feed
        // ========================================
        console.log('\n9️⃣ Testing getFollowingFeed()...')
        try {
            const followingFeed = await getFollowingFeed()
            console.log('✅ Success, post count:', followingFeed.data?.length || 0)
            if (followingFeed.data?.length) {
                console.log('   First post:', {
                    id: followingFeed.data[0].id,
                    title: followingFeed.data[0].title,
                })
            }
        } catch (e) {
            console.error('❌ Failure:', e instanceof Error ? e.message : e)
        }

        // ========================================
        // 🔟 Test joining a topic
        // ========================================
        console.log('\n🔟 Testing joinTopic(topicName: "Test topic")...')
        try {
            const result = await joinTopic('Test topic')
            console.log('✅ Success:', {
                code: result.code,
                message: result.message,
                actionSuccess: getActionSuccess(result.data),
                rawData: result.data,
            })
        } catch (e) {
            if (e instanceof ApiError && (e.status === 404 || e.status === 400)) {
                console.log('⚠️ Topic operation failed:', e.message)
            } else {
                console.error('❌ Failure:', e instanceof Error ? e.message : e)
            }
        }

        // ========================================
        // 1️⃣1️⃣ Test fetching my topics
        // ========================================
        console.log('\n1️⃣1️⃣ Testing getMyTopics()...')
        try {
            const topics = await getMyTopics()
            console.log('✅ Success:', {
                count: topics.data?.length || 0,
                topics: topics.data,
            })
        } catch (e) {
            if (e instanceof ApiError && e.status === 401) {
                console.log('⚠️ 401 Unauthorized - JWT authentication failed (login required)')
            } else {
                console.error('❌ Failure:', e instanceof Error ? e.message : e)
            }
        }

        console.log('\n✅ === Test complete === ✅\n')
    } catch (error) {
        console.error('\n❌ === Test failed === ❌')
        if (error instanceof ApiError) {
            console.error('API Error:', {
                status: error.status,
                message: error.message,
            })
        } else if (error instanceof Error) {
            console.error('Error:', error.message)
        } else {
            console.error('Unknown error:', error)
        }
        console.log()
    }
}
