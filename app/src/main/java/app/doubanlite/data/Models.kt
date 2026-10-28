package app.doubanlite.data

enum class MediaType(val apiValue: String, val label: String) {
    MOVIE("movie", "电影"),
    TV("tv", "电视"),
    ;

    val referer: String get() = "https://m.douban.com/$apiValue/"

    /** new_search_subjects 用的 tags 首项：电影 / 电视剧 */
    val exploreTag: String get() = when (this) { MOVIE -> "电影"; TV -> "电视剧" }

    companion object {
        fun fromApi(value: String?): MediaType? = when (value) {
            "movie" -> MOVIE
            "tv" -> TV
            else -> null
        }
    }
}

data class Subject(
    val id: String,
    val title: String,
    val subtitle: String?,
    val imageUrl: String?,
    val rating: Float?,
    val type: MediaType,
)

data class FeedSection(
    val title: String,
    val subjects: List<Subject>,
)

data class Person(
    val id: String?,
    val name: String,
    val avatarUrl: String?,
    val role: String?,
)

data class SubjectDetail(
    val id: String,
    val type: MediaType,
    val title: String,
    val originalTitle: String?,
    val imageUrl: String?,
    val rating: Float?,
    val ratingCount: Int?,
    val year: String?,
    val genres: List<String>,
    val countries: List<String>,
    val durations: List<String>,
    val pubdate: String?,
    val episodes: Int?,
    val intro: String?,
    val directors: List<Person>,
    val actors: List<Person>,
    val credits: List<Person>,
    val photos: List<String>,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
