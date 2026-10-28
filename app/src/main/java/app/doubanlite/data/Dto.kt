package app.doubanlite.data

import kotlinx.serialization.Serializable

@Serializable
data class DtoPic(
    val large: String? = null,
    val normal: String? = null,
    val small: String? = null,
)

@Serializable
data class DtoRating(
    val value: Float? = null,
    val count: Int? = null,
    val max: Float? = null,
    val star_count: Float? = null,
)

@Serializable
data class DtoCover(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val shape: String? = null,
)

@Serializable
data class DtoCollectionItem(
    val id: String? = null,
    val title: String? = null,
    val type: String? = null,
    val subtype: String? = null,
    val card_subtitle: String? = null,
    val info: String? = null,
    val description: String? = null,
    val cover: DtoCover? = null,
    val pic: DtoPic? = null,
    val rating: DtoRating? = null,
    val year: String? = null,
    val uri: String? = null,
    val item_type: String? = null,
    val is_playable: Boolean? = null,
    val actions: List<String>? = null,
)

@Serializable
data class DtoCollectionResponse(
    val start: Int = 0,
    val count: Int = 0,
    val total: Int = 0,
    val title: String? = null,
    val subject_collection_items: List<DtoCollectionItem>? = null,
)

@Serializable
data class DtoSort(
    val text: String? = null,
    val name: String? = null,
    val checked: Boolean? = null,
)

@Serializable
data class DtoRecommendResponse(
    val start: Int = 0,
    val count: Int = 0,
    val total: Int = 0,
    val sorts: List<DtoSort>? = null,
    val items: List<DtoCollectionItem>? = null,
)

@Serializable
data class DtoPerson(
    val id: String? = null,
    val name: String? = null,
    val avatar: DtoPic? = null,
)

@Serializable
data class DtoDetail(
    val id: String? = null,
    val title: String? = null,
    val original_title: String? = null,
    val intro: String? = null,
    val year: String? = null,
    val card_subtitle: String? = null,
    val pic: DtoPic? = null,
    val cover_url: String? = null,
    val rating: DtoRating? = null,
    val genres: List<String>? = null,
    val countries: List<String>? = null,
    val durations: List<String>? = null,
    val pubdate: List<String>? = null,
    val directors: List<DtoPerson>? = null,
    val actors: List<DtoPerson>? = null,
    val episodes_count: Int? = null,
    val comment_count: Int? = null,
    val review_count: Int? = null,
    val type: String? = null,
)

@Serializable
data class DtoCredit(
    val id: String? = null,
    val name: String? = null,
    val latin_name: String? = null,
    val character: String? = null,
    val avatar: DtoPic? = null,
    val roles: List<String>? = null,
)

@Serializable
data class DtoCreditsResponse(
    val items: List<DtoCredit>? = null,
)

@Serializable
data class DtoImage(
    val large: String? = null,
    val normal: String? = null,
    val small: String? = null,
)

@Serializable
data class DtoPhoto(
    val id: String? = null,
    val image: DtoImage? = null,
)

@Serializable
data class DtoPhotosResponse(
    val total: Int = 0,
    val photos: List<DtoPhoto>? = null,
)

@Serializable
data class DtoSuggest(
    val id: String? = null,
    val title: String? = null,
    val year: String? = null,
    val type: String? = null,
    val img: String? = null,
    val url: String? = null,
    val episode: String? = null,
)

/** https://movie.douban.com/j/new_search_subjects —— 找电影/找电视的主力数据源，字段干净统一 */
@Serializable
data class DtoSearchSubjectsResponse(
    val data: List<DtoSearchSubject>? = null,
)

@Serializable
data class DtoSearchSubject(
    val id: String? = null,
    val title: String? = null,
    val rate: String? = null,
    val star: String? = null,
    val cover: String? = null,
    val url: String? = null,
    val directors: List<String>? = null,
    val casts: List<String>? = null,
)
