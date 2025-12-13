package com.gosnow.app.datasupabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
//import io.github.jan.supabase.postgrest.result.decodeList
import java.time.*
import java.time.format.DateTimeFormatter

// ============== DTO（对应数据库返回）==============

@kotlinx.serialization.Serializable
data class ResortRow(
    val id: Long,
    @kotlinx.serialization.SerialName("name_resort") val name: String? = null
)

@kotlinx.serialization.Serializable
data class LostAndFoundRow(
    val id: Long,
    @kotlinx.serialization.SerialName("created_at") val createdAt: String,
    @kotlinx.serialization.SerialName("resort_id") val resortId: Long? = null,
    @kotlinx.serialization.SerialName("item_description") val itemDescription: String? = null,
    @kotlinx.serialization.SerialName("contact_info") val contactInfo: String? = null,
    val type: String? = null,
    @kotlinx.serialization.SerialName("user_id") val userId: String? = null,
    @kotlinx.serialization.SerialName("image_url") val imageUrl: String? = null,
    @kotlinx.serialization.SerialName("Resorts_data") val resort: ResortRow? = null
)

@kotlinx.serialization.Serializable
data class CarpoolRow(
    val id: String, // uuid
    @kotlinx.serialization.SerialName("created_at") val createdAt: String,
    @kotlinx.serialization.SerialName("user_id") val userId: String? = null,
    @kotlinx.serialization.SerialName("resort_id") val resortId: Long? = null,
    @kotlinx.serialization.SerialName("depart_at") val departAt: String? = null,
    @kotlinx.serialization.SerialName("origin_text") val originText: String? = null,
    val note: String? = null,
    @kotlinx.serialization.SerialName("is_hidden") val isHidden: Boolean = false,
    @kotlinx.serialization.SerialName("canceled_at") val canceledAt: String? = null,
    @kotlinx.serialization.SerialName("Resorts_data") val resort: ResortRow? = null
)

@kotlinx.serialization.Serializable
data class RoommateRow(
    val id: String, // uuid
    @kotlinx.serialization.SerialName("created_at") val createdAt: String,
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("resort_id") val resortId: Long,
    val content: String,
    @kotlinx.serialization.SerialName("is_hidden") val isHidden: Boolean = false,
    @kotlinx.serialization.SerialName("canceled_at") val canceledAt: String? = null,
    @kotlinx.serialization.SerialName("Resorts_data") val resort: ResortRow? = null
)

// ============== Insert Payload（写入数据库）==============

@kotlinx.serialization.Serializable
data class LostAndFoundInsert(
    @kotlinx.serialization.SerialName("resort_id") val resortId: Long?,
    @kotlinx.serialization.SerialName("item_description") val itemDescription: String,
    @kotlinx.serialization.SerialName("contact_info") val contactInfo: String,
    val type: String,
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("image_url") val imageUrl: String? = null,
)

@kotlinx.serialization.Serializable
data class CarpoolInsert(
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("resort_id") val resortId: Long,
    @kotlinx.serialization.SerialName("depart_at") val departAt: String,
    @kotlinx.serialization.SerialName("origin_text") val originText: String,
    val note: String,
    @kotlinx.serialization.SerialName("expires_at") val expiresAt: String,
    @kotlinx.serialization.SerialName("depart_date_utc") val departDateUtc: String,
)

@kotlinx.serialization.Serializable
data class RoommateInsert(
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("resort_id") val resortId: Long,
    val content: String,
)

// ============== Domain（给 UI 用）==============

data class ResortRef(val id: Long, val name: String)

enum class LostFoundType { LOST, FOUND }

data class LostAndFoundItem(
    val id: Long,
    val resort: ResortRef?,
    val type: LostFoundType,
    val description: String,
    val contact: String,
    val createdAt: LocalDateTime
)

data class CarpoolItem(
    val id: String,
    val resort: ResortRef?,
    val departAt: LocalDateTime,
    val origin: String,
    val note: String,
    val isCanceled: Boolean
)

data class RoommateItem(
    val id: String,
    val resort: ResortRef?,
    val content: String,
    val createdAt: LocalDateTime,
    val isCanceled: Boolean
)

// ============== Repository ==============

class DiscoverRepository(
    private val supabase: SupabaseClient
) {
    private val isoOffset = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    suspend fun fetchResorts(): List<ResortRef> {
        val rows = supabase.from("Resorts_data")
            .select(columns = Columns.list("id", "name_resort")) {
                order(column = "id", order = Order.ASCENDING)
            }
            .decodeList<ResortRow>()

        return rows.map { ResortRef(it.id, it.name.orEmpty()) }
    }

    // ---------- Lost & Found（公共列表）----------
    suspend fun listLostAndFound(
        resortId: Long?,
        date: LocalDate?,
        keyword: String,
        offset: Long,
        pageSize: Long
    ): List<LostAndFoundItem> {
        val columns = Columns.raw(
            """
            id, created_at, resort_id, item_description, contact_info, type, user_id, image_url,
            Resorts_data ( id, name_resort )
            """.trimIndent()
        )

        val from = offset
        val to = offset + pageSize - 1

        val rows = supabase.from("LostAndFoundItems")
            .select(columns = columns) {
                order(column = "created_at", order = Order.DESCENDING)
                range(from..to)

                filter {
                    resortId?.let { eq("resort_id", it) }

                    if (date != null) {
                        val (startIso, endIso) = dayRangeIso(date)
                        gte("created_at", startIso)
                        lt("created_at", endIso)
                    }

                    if (keyword.isNotBlank()) {
                        ilike("item_description", "%$keyword%")
                    }
                }
            }
            .decodeList<LostAndFoundRow>()

        return rows.map { it.toDomainLostAndFound() }
    }

    // ---------- Lost & Found（我的）----------
    suspend fun listMyLostAndFound(
        myUserId: String,
        offset: Long,
        pageSize: Long
    ): List<LostAndFoundItem> {
        val columns = Columns.raw(
            """
            id, created_at, resort_id, item_description, contact_info, type, user_id, image_url,
            Resorts_data ( id, name_resort )
            """.trimIndent()
        )

        val from = offset
        val to = offset + pageSize - 1

        val rows = supabase.from("LostAndFoundItems")
            .select(columns = columns) {
                order(column = "created_at", order = Order.DESCENDING)
                range(from..to)

                filter {
                    eq("user_id", myUserId)
                }
            }
            .decodeList<LostAndFoundRow>()

        return rows.map { it.toDomainLostAndFound() }
    }

    suspend fun publishLostAndFound(payload: LostAndFoundInsert) {
        supabase.from("LostAndFoundItems").insert(payload)
    }

    suspend fun deleteLostAndFound(id: Long, myUserId: String) {
        supabase.from("LostAndFoundItems").delete {
            filter {
                eq("id", id)
                eq("user_id", myUserId)
            }
        }
    }

    // ---------- Carpool（公共列表）----------
    suspend fun listCarpool(
        resortId: Long?,
        date: LocalDate?,
        offset: Long,
        pageSize: Long
    ): List<CarpoolItem> {
        val from = offset
        val to = offset + pageSize - 1

        // 你这里用显式 fk 名消歧义（保留你的写法）
        val columns = Columns.raw(
            """
            id, created_at, user_id, resort_id, depart_at, origin_text, note, is_hidden, canceled_at,
            Resorts_data!carpool_posts_resort_fkey ( id, name_resort )
            """.trimIndent()
        )

        val rows = supabase.from("carpool_posts")
            .select(columns = columns) {
                order(column = "depart_at", order = Order.ASCENDING)
                range(from..to)

                filter {
                    eq("is_hidden", false)
                    exact("canceled_at", null)

                    resortId?.let { eq("resort_id", it) }
                    date?.let { eq("depart_date_utc", it.toString()) }
                }
            }
            .decodeList<CarpoolRow>()

        return rows.mapNotNull { it.toDomainCarpoolOrNull() }
    }

    // ---------- Carpool（我的）----------
    suspend fun listMyCarpool(
        myUserId: String,
        offset: Long,
        pageSize: Long
    ): List<CarpoolItem> {
        val from = offset
        val to = offset + pageSize - 1

        val columns = Columns.raw(
            """
            id, created_at, user_id, resort_id, depart_at, origin_text, note, is_hidden, canceled_at,
            Resorts_data!carpool_posts_resort_fkey ( id, name_resort )
            """.trimIndent()
        )

        val rows = supabase.from("carpool_posts")
            .select(columns = columns) {
                order(column = "created_at", order = Order.DESCENDING)
                range(from..to)

                filter {
                    eq("user_id", myUserId)
                }
            }
            .decodeList<CarpoolRow>()

        return rows.mapNotNull { it.toDomainCarpoolOrNull() }
    }

    suspend fun publishCarpool(payload: CarpoolInsert) {
        supabase.from("carpool_posts").insert(payload)
    }

    suspend fun deleteCarpool(id: String, myUserId: String) {
        supabase.from("carpool_posts").delete {
            filter {
                eq("id", id)
                eq("user_id", myUserId)
            }
        }
    }

    // ---------- Roommate（公共列表）----------
    suspend fun listRoommate(
        resortId: Long?,
        offset: Long,
        pageSize: Long
    ): List<RoommateItem> {
        val from = offset
        val to = offset + pageSize - 1

        val columns = Columns.raw(
            """
            id, created_at, user_id, resort_id, content, is_hidden, canceled_at,
            Resorts_data ( id, name_resort )
            """.trimIndent()
        )

        val rows = supabase.from("roommate_posts")
            .select(columns = columns) {
                order(column = "created_at", order = Order.DESCENDING)
                range(from..to)

                filter {
                    eq("is_hidden", false)
                    exact("canceled_at", null)
                    resortId?.let { eq("resort_id", it) }
                }
            }
            .decodeList<RoommateRow>()

        return rows.map { it.toDomainRoommate() }
    }

    suspend fun listMyRoommate(
        myUserId: String,
        offset: Long,
        pageSize: Long
    ): List<RoommateItem> {
        val from = offset
        val to = offset + pageSize - 1

        val columns = Columns.raw(
            """
            id, created_at, user_id, resort_id, content, is_hidden, canceled_at,
            Resorts_data ( id, name_resort )
            """.trimIndent()
        )

        val rows = supabase.from("roommate_posts")
            .select(columns = columns) {
                order(column = "created_at", order = Order.DESCENDING)
                range(from..to)

                filter {
                    eq("user_id", myUserId)
                }
            }
            .decodeList<RoommateRow>()

        return rows.map { it.toDomainRoommate() }
    }

    suspend fun publishRoommate(payload: RoommateInsert) {
        supabase.from("roommate_posts").insert(payload)
    }

    suspend fun deleteRoommate(id: String, myUserId: String) {
        supabase.from("roommate_posts").delete {
            filter {
                eq("id", id)
                eq("user_id", myUserId)
            }
        }
    }

    // ----------- utils -----------

    private fun dayRangeIso(date: LocalDate): Pair<String, String> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toOffsetDateTime().format(isoOffset)
        val end = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime().format(isoOffset)
        return start to end
    }

    private fun parseToLocalDateTime(iso: String): LocalDateTime {
        return OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    }

    private fun LostAndFoundRow.toDomainLostAndFound(): LostAndFoundItem {
        val t = when (type?.lowercase()) {
            "found" -> LostFoundType.FOUND
            "lost" -> LostFoundType.LOST
            else -> LostFoundType.LOST
        }
        return LostAndFoundItem(
            id = id,
            resort = resort?.let { ResortRef(it.id, it.name.orEmpty()) },
            type = t,
            description = itemDescription.orEmpty(),
            contact = contactInfo.orEmpty(),
            createdAt = parseToLocalDateTime(createdAt)
        )
    }

    private fun CarpoolRow.toDomainCarpoolOrNull(): CarpoolItem? {
        val departIso = departAt ?: return null
        return CarpoolItem(
            id = id,
            resort = resort?.let { ResortRef(it.id, it.name.orEmpty()) },
            departAt = parseToLocalDateTime(departIso),
            origin = originText.orEmpty(),
            note = note.orEmpty(),
            isCanceled = canceledAt != null
        )
    }

    private fun RoommateRow.toDomainRoommate(): RoommateItem {
        return RoommateItem(
            id = id,
            resort = resort?.let { ResortRef(it.id, it.name.orEmpty()) },
            content = content,
            createdAt = parseToLocalDateTime(createdAt),
            isCanceled = canceledAt != null
        )
    }
}
