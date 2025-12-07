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

@Composable
fun SnowApp() {
    SnowTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            val container = remember { SnowContainer() }
            NavHost(navController = navController, startDestination = "feed") {
                composable("feed") {
                    val vm: FeedViewModel = viewModel(factory = container.factory { FeedViewModel(container.postRepository, container.notificationsRepository, container.currentUser.id) })
                    FeedScreen(viewModel = vm, navController = navController)
                }
                composable("my_posts") {
                    val vm: MyPostsViewModel = viewModel(factory = container.factory { MyPostsViewModel(container.postRepository, container.currentUser.id) })
                    MyPostsScreen(viewModel = vm, navController = navController)
                }
                composable("notifications") {
                    val vm: NotificationsViewModel = viewModel(factory = container.factory { NotificationsViewModel(container.notificationsRepository, container.currentUser.id) })
                    NotificationsScreen(viewModel = vm, navController = navController)
                }
                composable("compose_post") {
                    val vm: ComposePostViewModel = viewModel(factory = container.factory { ComposePostViewModel(container.postRepository, container.currentUser) })
                    ComposePostScreen(navController = navController, viewModel = vm)
                }
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

class SnowContainer(
    val postRepository: PostRepository = InMemoryPostRepository(),
    val commentRepository: CommentRepository = InMemoryCommentRepository(),
    val notificationsRepository: NotificationsRepository = InMemoryNotificationsRepository(),
    val currentUser: com.gosnow.app.ui.snowcircle.model.User = currentUser()
) {
    fun <T : ViewModel> factory(create: () -> T): androidx.lifecycle.ViewModelProvider.Factory =
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
        }
}
