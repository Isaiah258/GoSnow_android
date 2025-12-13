package com.gosnow.app.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosnow.app.datasupabase.CarpoolInsert
import com.gosnow.app.datasupabase.CarpoolItem
import com.gosnow.app.datasupabase.DiscoverRepository
import com.gosnow.app.datasupabase.LostAndFoundInsert
import com.gosnow.app.datasupabase.LostAndFoundItem
import com.gosnow.app.datasupabase.LostFoundType
import com.gosnow.app.datasupabase.ResortRef
import com.gosnow.app.datasupabase.RoommateInsert
import com.gosnow.app.datasupabase.RoommateItem
import com.gosnow.app.datasupabase.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val DEFAULT_PAGE_SIZE = 10

data class PagedUiState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null
)

private fun SupabaseClient.currentUserIdOrNull(): String? {
    // 官方 Kotlin: currentSessionOrNull() 用来拿 session（user 在 session 里）
    return auth.currentSessionOrNull()?.user?.id
}

/* =========================
 * Lost & Found VM
 * ========================= */
class LostAndFoundVM(
    private val supabase: SupabaseClient,
    private val repo: DiscoverRepository,
    private val pageSize: Long = DEFAULT_PAGE_SIZE.toLong()
) : ViewModel() {

    private val _resorts = MutableStateFlow<List<ResortRef>>(emptyList())
    val resorts: StateFlow<List<ResortRef>> = _resorts.asStateFlow()

    private val _selectedResort = MutableStateFlow<ResortRef?>(null)
    val selectedResort: StateFlow<ResortRef?> = _selectedResort.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _keyword = MutableStateFlow("")
    val keyword: StateFlow<String> = _keyword.asStateFlow()

    private val _publicState = MutableStateFlow(PagedUiState<LostAndFoundItem>())
    val publicState: StateFlow<PagedUiState<LostAndFoundItem>> = _publicState.asStateFlow()

    private val _myState = MutableStateFlow(PagedUiState<LostAndFoundItem>())
    val myState: StateFlow<PagedUiState<LostAndFoundItem>> = _myState.asStateFlow()

    private var keywordJob: Job? = null

    init {
        viewModelScope.launch {
            loadResortsIfNeeded()
            refreshPublic()
        }
    }

    fun selectResort(resort: ResortRef?) {
        _selectedResort.value = resort
        refreshPublic()
    }

    fun selectDate(date: LocalDate?) {
        _selectedDate.value = date
        refreshPublic()
    }

    fun setKeyword(text: String) {
        _keyword.value = text
        keywordJob?.cancel()
        keywordJob = viewModelScope.launch {
            delay(300)
            refreshPublic()
        }
    }

    fun refreshPublic() = viewModelScope.launch {
        _publicState.update { it.copy(isLoading = true, error = null) }
        runCatching {
            repo.listLostAndFound(
                resortId = _selectedResort.value?.id,
                date = _selectedDate.value,
                keyword = _keyword.value,
                offset = 0,
                pageSize = pageSize
            )
        }.onSuccess { list ->
            _publicState.value = PagedUiState(
                items = list,
                isLoading = false,
                endReached = list.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _publicState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    fun loadMorePublic() = viewModelScope.launch {
        val cur = _publicState.value
        if (cur.isLoading || cur.endReached) return@launch
        _publicState.update { it.copy(isLoading = true, error = null) }

        val offset = cur.items.size.toLong()
        runCatching {
            repo.listLostAndFound(
                resortId = _selectedResort.value?.id,
                date = _selectedDate.value,
                keyword = _keyword.value,
                offset = offset,
                pageSize = pageSize
            )
        }.onSuccess { more ->
            val merged = cur.items + more
            _publicState.value = cur.copy(
                items = merged,
                isLoading = false,
                endReached = more.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _publicState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    fun refreshMy() = viewModelScope.launch {
        val myUserId = supabase.currentUserIdOrNull()
        if (myUserId == null) {
            _myState.value = PagedUiState(items = emptyList(), isLoading = false, endReached = true, error = "请先登录")
            return@launch
        }

        _myState.update { it.copy(isLoading = true, error = null) }
        runCatching {
            repo.listMyLostAndFound(
                myUserId = myUserId,
                offset = 0,
                pageSize = pageSize
            )
        }.onSuccess { list ->
            _myState.value = PagedUiState(
                items = list,
                isLoading = false,
                endReached = list.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _myState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    fun loadMoreMy() = viewModelScope.launch {
        val myUserId = supabase.currentUserIdOrNull() ?: run {
            _myState.update { it.copy(error = "请先登录") }
            return@launch
        }

        val cur = _myState.value
        if (cur.isLoading || cur.endReached) return@launch
        _myState.update { it.copy(isLoading = true, error = null) }

        val offset = cur.items.size.toLong()
        runCatching {
            repo.listMyLostAndFound(
                myUserId = myUserId,
                offset = offset,
                pageSize = pageSize
            )
        }.onSuccess { more ->
            val merged = cur.items + more
            _myState.value = cur.copy(
                items = merged,
                isLoading = false,
                endReached = more.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _myState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    suspend fun publish(
        type: LostFoundType,
        description: String,
        contact: String,
        resortId: Long?
    ): Result<Unit> {
        val myUserId = supabase.currentUserIdOrNull() ?: return Result.failure(IllegalStateException("请先登录"))

        val payload = LostAndFoundInsert(
            resortId = resortId,
            itemDescription = description,
            contactInfo = contact,
            type = if (type == LostFoundType.LOST) "LOST" else "FOUND",
            userId = myUserId,
            imageUrl = null
        )

        return runCatching {
            repo.publishLostAndFound(payload)
        }
    }

    suspend fun deleteMy(itemId: Long): Result<Unit> {
        val myUserId = supabase.currentUserIdOrNull() ?: return Result.failure(IllegalStateException("请先登录"))
        return runCatching {
            repo.deleteLostAndFound(itemId, myUserId)
            _myState.update { it.copy(items = it.items.filterNot { x -> x.id == itemId }) }
        }
    }

    private suspend fun loadResortsIfNeeded() {
        if (_resorts.value.isNotEmpty()) return
        runCatching { repo.fetchResorts() }
            .onSuccess { _resorts.value = it }
            .onFailure { /* 不阻塞主流程 */ }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val client = SupabaseClientProvider.supabaseClient
                return LostAndFoundVM(
                    supabase = client,
                    repo = DiscoverRepository(client)
                ) as T
            }
        }
    }
}

/* =========================
 * Carpool VM
 * ========================= */
class CarpoolVM(
    private val supabase: SupabaseClient,
    private val repo: DiscoverRepository,
    private val pageSize: Long = DEFAULT_PAGE_SIZE.toLong()
) : ViewModel() {

    private val isoOffset = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val zone = ZoneId.systemDefault()

    private val _resorts = MutableStateFlow<List<ResortRef>>(emptyList())
    val resorts: StateFlow<List<ResortRef>> = _resorts.asStateFlow()

    private val _selectedResort = MutableStateFlow<ResortRef?>(null)
    val selectedResort: StateFlow<ResortRef?> = _selectedResort.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _publicState = MutableStateFlow(PagedUiState<CarpoolItem>())
    val publicState: StateFlow<PagedUiState<CarpoolItem>> = _publicState.asStateFlow()

    private val _myState = MutableStateFlow(PagedUiState<CarpoolItem>())
    val myState: StateFlow<PagedUiState<CarpoolItem>> = _myState.asStateFlow()

    init {
        viewModelScope.launch {
            loadResortsIfNeeded()
            refreshPublic()
        }
    }

    fun selectResort(resort: ResortRef?) {
        _selectedResort.value = resort
        refreshPublic()
    }

    fun selectDate(date: LocalDate?) {
        _selectedDate.value = date
        refreshPublic()
    }

    fun refreshPublic() = viewModelScope.launch {
        _publicState.update { it.copy(isLoading = true, error = null) }

        runCatching {
            repo.listCarpool(
                resortId = _selectedResort.value?.id,
                date = _selectedDate.value,
                offset = 0,
                pageSize = pageSize
            )
        }.onSuccess { list ->
            _publicState.value = PagedUiState(
                items = list,
                isLoading = false,
                endReached = list.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _publicState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    fun loadMorePublic() = viewModelScope.launch {
        val cur = _publicState.value
        if (cur.isLoading || cur.endReached) return@launch
        _publicState.update { it.copy(isLoading = true, error = null) }

        val offset = cur.items.size.toLong()
        runCatching {
            repo.listCarpool(
                resortId = _selectedResort.value?.id,
                date = _selectedDate.value,
                offset = offset,
                pageSize = pageSize
            )
        }.onSuccess { more ->
            val merged = cur.items + more
            _publicState.value = cur.copy(
                items = merged,
                isLoading = false,
                endReached = more.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _publicState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    fun refreshMy() = viewModelScope.launch {
        val myUserId = supabase.currentUserIdOrNull()
        if (myUserId == null) {
            _myState.value = PagedUiState(items = emptyList(), isLoading = false, endReached = true, error = "请先登录")
            return@launch
        }

        _myState.update { it.copy(isLoading = true, error = null) }
        runCatching {
            repo.listMyCarpool(
                myUserId = myUserId,
                offset = 0,
                pageSize = pageSize
            )
        }.onSuccess { list ->
            _myState.value = PagedUiState(
                items = list,
                isLoading = false,
                endReached = list.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _myState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    fun loadMoreMy() = viewModelScope.launch {
        val myUserId = supabase.currentUserIdOrNull() ?: run {
            _myState.update { it.copy(error = "请先登录") }
            return@launch
        }

        val cur = _myState.value
        if (cur.isLoading || cur.endReached) return@launch
        _myState.update { it.copy(isLoading = true, error = null) }

        val offset = cur.items.size.toLong()
        runCatching {
            repo.listMyCarpool(
                myUserId = myUserId,
                offset = offset,
                pageSize = pageSize
            )
        }.onSuccess { more ->
            val merged = cur.items + more
            _myState.value = cur.copy(
                items = merged,
                isLoading = false,
                endReached = more.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _myState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    suspend fun publish(
        selectedDate: LocalDate,
        selectedTime: LocalTime,
        resortId: Long,
        origin: String,
        note: String
    ): Result<Unit> {
        val myUserId = supabase.currentUserIdOrNull() ?: return Result.failure(IllegalStateException("请先登录"))

        val departLdt = LocalDateTime.of(selectedDate, selectedTime)
        val departOdt = departLdt.atZone(zone).toOffsetDateTime()

        val departAt = departOdt.format(isoOffset)
        val departDateUtc = departOdt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate().toString()
        val expiresAt = departOdt.plusDays(1).format(isoOffset)

        val payload = CarpoolInsert(
            userId = myUserId,
            resortId = resortId,
            departAt = departAt,
            originText = origin,
            note = note,
            expiresAt = expiresAt,
            departDateUtc = departDateUtc
        )

        return runCatching {
            repo.publishCarpool(payload)
        }
    }

    suspend fun deleteMy(id: String): Result<Unit> {
        val myUserId = supabase.currentUserIdOrNull() ?: return Result.failure(IllegalStateException("请先登录"))
        return runCatching {
            repo.deleteCarpool(id, myUserId)
            _myState.update { it.copy(items = it.items.filterNot { x -> x.id == id }) }
        }
    }

    private suspend fun loadResortsIfNeeded() {
        if (_resorts.value.isNotEmpty()) return
        runCatching { repo.fetchResorts() }
            .onSuccess { _resorts.value = it }
            .onFailure { /* ignore */ }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val client = SupabaseClientProvider.supabaseClient
                return CarpoolVM(
                    supabase = client,
                    repo = DiscoverRepository(client)
                ) as T
            }
        }
    }
}

/* =========================
 * Roommate VM
 * ========================= */
class RoommateVM(
    private val supabase: SupabaseClient,
    private val repo: DiscoverRepository,
    private val pageSize: Long = DEFAULT_PAGE_SIZE.toLong()
) : ViewModel() {

    private val _resorts = MutableStateFlow<List<ResortRef>>(emptyList())
    val resorts: StateFlow<List<ResortRef>> = _resorts.asStateFlow()

    private val _selectedResort = MutableStateFlow<ResortRef?>(null)
    val selectedResort: StateFlow<ResortRef?> = _selectedResort.asStateFlow()

    private val _publicState = MutableStateFlow(PagedUiState<RoommateItem>())
    val publicState: StateFlow<PagedUiState<RoommateItem>> = _publicState.asStateFlow()

    private val _myState = MutableStateFlow(PagedUiState<RoommateItem>())
    val myState: StateFlow<PagedUiState<RoommateItem>> = _myState.asStateFlow()

    init {
        viewModelScope.launch {
            loadResortsIfNeeded()
            refreshPublic()
        }
    }

    fun selectResort(resort: ResortRef?) {
        _selectedResort.value = resort
        refreshPublic()
    }

    fun refreshPublic() = viewModelScope.launch {
        _publicState.update { it.copy(isLoading = true, error = null) }

        runCatching {
            repo.listRoommate(
                resortId = _selectedResort.value?.id,
                offset = 0,
                pageSize = pageSize
            )
        }.onSuccess { list ->
            _publicState.value = PagedUiState(
                items = list,
                isLoading = false,
                endReached = list.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _publicState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    fun loadMorePublic() = viewModelScope.launch {
        val cur = _publicState.value
        if (cur.isLoading || cur.endReached) return@launch
        _publicState.update { it.copy(isLoading = true, error = null) }

        val offset = cur.items.size.toLong()
        runCatching {
            repo.listRoommate(
                resortId = _selectedResort.value?.id,
                offset = offset,
                pageSize = pageSize
            )
        }.onSuccess { more ->
            val merged = cur.items + more
            _publicState.value = cur.copy(
                items = merged,
                isLoading = false,
                endReached = more.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _publicState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    fun refreshMy() = viewModelScope.launch {
        val myUserId = supabase.currentUserIdOrNull()
        if (myUserId == null) {
            _myState.value = PagedUiState(items = emptyList(), isLoading = false, endReached = true, error = "请先登录")
            return@launch
        }

        _myState.update { it.copy(isLoading = true, error = null) }
        runCatching {
            repo.listMyRoommate(
                myUserId = myUserId,
                offset = 0,
                pageSize = pageSize
            )
        }.onSuccess { list ->
            _myState.value = PagedUiState(
                items = list,
                isLoading = false,
                endReached = list.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _myState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    fun loadMoreMy() = viewModelScope.launch {
        val myUserId = supabase.currentUserIdOrNull() ?: run {
            _myState.update { it.copy(error = "请先登录") }
            return@launch
        }

        val cur = _myState.value
        if (cur.isLoading || cur.endReached) return@launch
        _myState.update { it.copy(isLoading = true, error = null) }

        val offset = cur.items.size.toLong()
        runCatching {
            repo.listMyRoommate(
                myUserId = myUserId,
                offset = offset,
                pageSize = pageSize
            )
        }.onSuccess { more ->
            val merged = cur.items + more
            _myState.value = cur.copy(
                items = merged,
                isLoading = false,
                endReached = more.size < pageSize.toInt(),
                error = null
            )
        }.onFailure { e ->
            _myState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
        }
    }

    suspend fun publish(resortId: Long, content: String): Result<Unit> {
        val myUserId = supabase.currentUserIdOrNull() ?: return Result.failure(IllegalStateException("请先登录"))
        val payload = RoommateInsert(
            userId = myUserId,
            resortId = resortId,
            content = content
        )
        return runCatching { repo.publishRoommate(payload) }
    }

    suspend fun deleteMy(id: String): Result<Unit> {
        val myUserId = supabase.currentUserIdOrNull() ?: return Result.failure(IllegalStateException("请先登录"))
        return runCatching {
            repo.deleteRoommate(id, myUserId)
            _myState.update { it.copy(items = it.items.filterNot { x -> x.id == id }) }
        }
    }

    private suspend fun loadResortsIfNeeded() {
        if (_resorts.value.isNotEmpty()) return
        runCatching { repo.fetchResorts() }
            .onSuccess { _resorts.value = it }
            .onFailure { /* ignore */ }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val client = SupabaseClientProvider.supabaseClient
                return RoommateVM(
                    supabase = client,
                    repo = DiscoverRepository(client)
                ) as T
            }
        }
    }
}


