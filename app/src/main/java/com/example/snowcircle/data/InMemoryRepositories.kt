package com.example.snowcircle.data

import com.example.snowcircle.model.Comment
import com.example.snowcircle.model.NotificationItem
import com.example.snowcircle.model.NotificationType
import com.example.snowcircle.model.Post
import com.example.snowcircle.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

private const val currentUserId = "me"

class InMemoryPostRepository : PostRepository {
    private val mutex = Mutex()
    private val users = listOf(
        User("me", "Me", "https://i.pravatar.cc/150?img=3"),
        User("u1", "Ada", "https://i.pravatar.cc/150?img=1"),
        User("u2", "Ben", "https://i.pravatar.cc/150?img=2"),
        User("u3", "Cindy", "https://i.pravatar.cc/150?img=4")
    )

    private val posts = mutableListOf(
        Post(
            id = "p1",
            author = users[1],
            resortName = "Niseko",
            createdAt = "2h ago",
            content = "Bluebird day in Niseko! Powder is insane.",
            imageUrls = listOf(
                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
                "https://images.unsplash.com/photo-1489515217757-5fd1be406fef",
                "https://images.unsplash.com/photo-1500048993953-d23a436266cf"
            ),
            likeCount = 24,
            commentCount = 6,
            isLikedByMe = false
        ),
        Post(
            id = "p2",
            author = users[0],
            resortName = "Hakuba",
            createdAt = "5h ago",
            content = "Finally landed my first 360 today!",
            imageUrls = listOf("https://images.unsplash.com/photo-1489515217757-5fd1be406fef"),
            likeCount = 12,
            commentCount = 3,
            isLikedByMe = true,
            canDelete = true
        ),
        Post(
            id = "p3",
            author = users[2],
            resortName = "Whistler",
            createdAt = "1d ago",
            content = "Anyone riding tomorrow? Looking for buddies.",
            imageUrls = emptyList(),
            likeCount = 5,
            commentCount = 1,
            isLikedByMe = false
        ),
        Post(
            id = "p4",
            author = users[3],
            resortName = "Zermatt",
            createdAt = "3d ago",
            content = "Sunset laps with the crew ✨",
            imageUrls = listOf(
                "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429",
                "https://images.unsplash.com/photo-1469474968028-56623f02e42e",
                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
                "https://images.unsplash.com/photo-1489515217757-5fd1be406fef"
            ),
            likeCount = 30,
            commentCount = 10,
            isLikedByMe = false
        )
    )

    override suspend fun getFeedPosts(resortFilter: String?): List<Post> {
        delay(300)
        return mutex.withLock {
            val filtered = resortFilter?.let { res ->
                posts.filter { it.resortName?.contains(res, ignoreCase = true) == true }
            } ?: posts.toList()
            filtered
        }
    }

    override suspend fun getMyPosts(currentUserId: String): List<Post> {
        delay(200)
        return mutex.withLock { posts.filter { it.author.id == currentUserId } }
    }

    override suspend fun getPostById(postId: String): Post? {
        delay(150)
        return mutex.withLock { posts.find { it.id == postId } }
    }

    override suspend fun toggleLike(postId: String, currentUserId: String): Post? {
        delay(150)
        return mutex.withLock {
            val idx = posts.indexOfFirst { it.id == postId }
            if (idx == -1) return@withLock null
            val post = posts[idx]
            val liked = !post.isLikedByMe
            val updated = post.copy(
                isLikedByMe = liked,
                likeCount = if (liked) post.likeCount + 1 else post.likeCount - 1
            )
            posts[idx] = updated
            updated
        }
    }

    override suspend fun deletePost(postId: String, currentUserId: String) {
        delay(100)
        mutex.withLock { posts.removeAll { it.id == postId && it.author.id == currentUserId } }
    }

    override suspend fun createPost(content: String, resortName: String?, images: List<String>, currentUser: User): Post {
        delay(200)
        return mutex.withLock {
            val newPost = Post(
                id = "p${Random.nextInt(1000, 9999)}",
                author = currentUser,
                resortName = resortName,
                createdAt = "just now",
                content = content,
                imageUrls = images,
                likeCount = 0,
                commentCount = 0,
                isLikedByMe = false,
                canDelete = true
            )
            posts.add(0, newPost)
            newPost
        }
    }
}

class InMemoryCommentRepository : CommentRepository {
    private val mutex = Mutex()
    private val comments = mutableListOf(
        Comment("c1", "p1", null, User("u2", "Ben", "https://i.pravatar.cc/150?img=2"), "1h ago", "Looks amazing!", 3, false),
        Comment("c2", "p1", "c1", User("u1", "Ada", "https://i.pravatar.cc/150?img=1"), "55m ago", "Come join tomorrow?", 1, false),
        Comment("c3", "p1", null, User("u3", "Cindy", "https://i.pravatar.cc/150?img=4"), "30m ago", "Niseko on my bucket list", 0, false),
        Comment("c4", "p2", null, User("me", "Me", "https://i.pravatar.cc/150?img=3"), "20m ago", "Thanks for the stoke!", 2, true, canDelete = true)
    )

    override suspend fun getCommentsForPost(postId: String): List<Comment> {
        delay(250)
        return mutex.withLock { comments.filter { it.postId == postId } }
    }

    override suspend fun addComment(postId: String, body: String, parentId: String?, currentUser: User): Comment {
        delay(200)
        return mutex.withLock {
            val newComment = Comment(
                id = "c${Random.nextInt(10000, 99999)}",
                postId = postId,
                parentId = parentId,
                author = currentUser,
                createdAt = "just now",
                body = body,
                likeCount = 0,
                isLikedByMe = false,
                canDelete = true
            )
            comments.add(newComment)
            newComment
        }
    }

    override suspend fun deleteComment(commentId: String, currentUserId: String) {
        delay(150)
        mutex.withLock { comments.removeAll { it.id == commentId && it.author.id == currentUserId } }
    }

    override suspend fun toggleCommentLike(commentId: String, currentUserId: String): Comment? {
        delay(120)
        return mutex.withLock {
            val idx = comments.indexOfFirst { it.id == commentId }
            if (idx == -1) return@withLock null
            val comment = comments[idx]
            val liked = !comment.isLikedByMe
            val updated = comment.copy(
                isLikedByMe = liked,
                likeCount = if (liked) comment.likeCount + 1 else comment.likeCount - 1
            )
            comments[idx] = updated
            updated
        }
    }
}

class InMemoryNotificationsRepository : NotificationsRepository {
    private val mutex = Mutex()
    private val users = listOf(
        User("u1", "Ada", "https://i.pravatar.cc/150?img=1"),
        User("u2", "Ben", "https://i.pravatar.cc/150?img=2"),
        User("u3", "Cindy", "https://i.pravatar.cc/150?img=4")
    )
    private val notifications = mutableListOf(
        NotificationItem(1, NotificationType.LIKE_POST, "2h ago", "p2", null, users[0], false),
        NotificationItem(2, NotificationType.COMMENT_POST, "5h ago", "p1", "c1", users[1], true),
        NotificationItem(3, NotificationType.REPLY_COMMENT, "1d ago", "p1", "c2", users[2], false)
    )

    override suspend fun getNotifications(currentUserId: String): List<NotificationItem> {
        delay(200)
        return mutex.withLock { notifications.toList() }
    }

    override suspend fun markAllRead(currentUserId: String) {
        delay(100)
        mutex.withLock {
            repeat(notifications.size) { index ->
                notifications[index] = notifications[index].copy(isRead = true)
            }
        }
    }

    override suspend fun markRead(notificationId: Long) {
        delay(80)
        mutex.withLock {
            val idx = notifications.indexOfFirst { it.id == notificationId }
            if (idx != -1) notifications[idx] = notifications[idx].copy(isRead = true)
        }
    }
}

fun currentUser(): User = User(currentUserId, "Me", "https://i.pravatar.cc/150?img=3")
