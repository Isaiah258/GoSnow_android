package com.gosnow.app.ui.snowcircle.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gosnow.app.ui.snowcircle.data.CommentRepository
import com.gosnow.app.ui.snowcircle.data.InMemoryCommentRepository
import com.gosnow.app.ui.snowcircle.data.InMemoryNotificationsRepository
import com.gosnow.app.ui.snowcircle.data.InMemoryPostRepository
import com.gosnow.app.ui.snowcircle.data.NotificationsRepository
import com.gosnow.app.ui.snowcircle.data.PostRepository
import com.gosnow.app.ui.snowcircle.data.currentUser
import com.gosnow.app.ui.snowcircle.ui.compose.ComposePostScreen
import com.gosnow.app.ui.snowcircle.ui.compose.ComposePostViewModel
import com.gosnow.app.ui.snowcircle.ui.detail.PostDetailScreen
import com.gosnow.app.ui.snowcircle.ui.detail.PostDetailViewModel
import com.gosnow.app.ui.snowcircle.ui.feed.FeedScreen
import com.gosnow.app.ui.snowcircle.ui.feed.FeedViewModel
import androidx.compose.runtime.collectAsState
import com.gosnow.app.ui.snowcircle.ui.myposts.MyPostsScreen
import com.gosnow.app.ui.snowcircle.ui.myposts.MyPostsViewModel
import com.gosnow.app.ui.snowcircle.ui.notifications.NotificationsScreen
import com.gosnow.app.ui.snowcircle.ui.notifications.NotificationsViewModel
import com.gosnow.app.ui.snowcircle.ui.theme.SnowTheme
import com.gosnow.app.ui.snowcircle.ui.viewer.ImageViewerScreen
import androidx.compose.ui.platform.LocalContext
import com.gosnow.app.datasupabase.SupabaseClientProvider
import android.content.Context

import com.gosnow.app.BuildConfig


import com.gosnow.app.ui.snowcircle.data.supabase.SupabaseResortsCommentRepository
import com.gosnow.app.ui.snowcircle.data.supabase.SupabaseResortsNotificationsRepository
import com.gosnow.app.ui.snowcircle.data.supabase.SupabaseResortsPostRepository
import com.gosnow.app.ui.snowcircle.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth

// ... imports 保持不变

@Composable
fun SnowApp() {
    SnowTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            val appContext = LocalContext.current.applicationContext
            val container = remember { SnowContainer(context = appContext, useMock = false) }

            NavHost(navController = navController, startDestination = "feed") {
                composable("feed") {
                    val vm: FeedViewModel = viewModel(factory = container.factory { FeedViewModel(container.postRepository, container.notificationsRepository, container.currentUser.id) })
                    FeedScreen(viewModel = vm, navController = navController)
                }

                // ... my_posts, notifications 路由保持不变 ...
                composable("my_posts") {
                    val vm: MyPostsViewModel = viewModel(factory = container.factory { MyPostsViewModel(container.postRepository, container.currentUser.id) })
                    MyPostsScreen(viewModel = vm, navController = navController)
                }
                composable("notifications") {
                    val vm: NotificationsViewModel = viewModel(factory = container.factory { NotificationsViewModel(container.notificationsRepository, container.currentUser.id) })
                    NotificationsScreen(viewModel = vm, navController = navController)
                }

                // ✅ 修改：传入 onPublished 回调，设置刷新标志
                composable("compose_post") {
                    val vm: ComposePostViewModel = viewModel(factory = container.factory { ComposePostViewModel(container.postRepository, container.currentUser) })
                    ComposePostScreen(
                        navController = navController,
                        viewModel = vm,
                        onPublished = {
                            // 设置 flag，让上一个页面（feed）知道需要刷新
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refresh_feed", true)

                            navController.popBackStack()
                        }
                    )
                }

                // ... post_detail, image_viewer 路由保持不变 ...
                composable("post_detail/{postId}") { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                    val vm: PostDetailViewModel = viewModel(factory = container.factory { PostDetailViewModel(postId, container.postRepository, container.commentRepository, container.currentUser.id) })
                    PostDetailScreen(viewModel = vm, navController = navController)
                }
                composable("image_viewer/{postId}/{startIndex}") { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                    val startIndex = backStackEntry.arguments?.getString("startIndex")?.toIntOrNull() ?: 0
                    val vm: PostDetailViewModel = viewModel(factory = container.factory { PostDetailViewModel(postId, container.postRepository, container.commentRepository, container.currentUser.id) })
                    val post = vm.uiState.collectAsState().value.post
                    val urls = post?.imageUrls ?: emptyList()
                    if (urls.isNotEmpty()) {
                        ImageViewerScreen(urls = urls, startIndex = startIndex, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

// ... SnowContainer 类保持不变




class SnowContainer(
    context: Context,
    private val useMock: Boolean = false
) {
    private val appContext = context.applicationContext
    private val supabase: SupabaseClient = SupabaseClientProvider.supabaseClient

    private val authedUserId: String? = supabase.auth.currentUserOrNull()?.id

    // 1) 当前用户：真实环境用 auth 的 uuid；mock 继续用你原来的 currentUser()
    val currentUser: User =
        if (useMock) currentUser()
        else User(
            id = authedUserId ?: error("未登录：supabase.auth.currentUserOrNull() == null"),
            displayName = "我",
            avatarUrl = null
        )

    // 2) Repository：mock / supabase 切换
    val postRepository: PostRepository =
        if (useMock) InMemoryPostRepository()
        else SupabaseResortsPostRepository(
            context = appContext,
            supabase = supabase,
            postMediaBucket = "post-media",
            publicStorageBaseUrl = "${BuildConfig.SUPABASE_URL.trimEnd('/')}/storage/v1/object/public"
        )

    val commentRepository: CommentRepository =
        if (useMock) InMemoryCommentRepository()
        else SupabaseResortsCommentRepository(supabase)

    val notificationsRepository: NotificationsRepository =
        if (useMock) InMemoryNotificationsRepository()
        else SupabaseResortsNotificationsRepository(supabase)

    fun <T : ViewModel> factory(create: () -> T): androidx.lifecycle.ViewModelProvider.Factory =
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
        }
}



