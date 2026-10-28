package app.doubanlite.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

object SubjectRepository {

    private data class SectionSpec(val title: String, val collectionId: String)

    private val MOVIE_SECTIONS = listOf(
        SectionSpec("正在上映", "movie_showing"),
        SectionSpec("即将上映", "movie_soon"),
        SectionSpec("豆瓣 Top 250", "movie_top250"),
        SectionSpec("实时热门", "movie_real_time_hotest"),
        SectionSpec("一周口碑榜", "movie_weekly_best"),
    )

    private val TV_SECTIONS = listOf(
        SectionSpec("华语口碑剧集", "tv_chinese_best_weekly"),
        SectionSpec("全球口碑剧集", "tv_global_best_weekly"),
        SectionSpec("国产剧", "tv_domestic"),
        SectionSpec("美剧", "tv_american"),
        SectionSpec("韩剧", "tv_korean"),
        SectionSpec("日剧", "tv_japanese"),
        SectionSpec("动画", "tv_animation"),
        SectionSpec("综艺", "tv_variety_show"),
    )

    fun sectionsOf(type: MediaType): List<String> =
        (if (type == MediaType.MOVIE) MOVIE_SECTIONS else TV_SECTIONS).map { it.title }

    suspend fun feed(type: MediaType): Result<List<FeedSection>> = runCatching {
        val specs = if (type == MediaType.MOVIE) MOVIE_SECTIONS else TV_SECTIONS
        val out = ArrayList<FeedSection>(specs.size)
        specs.forEachIndexed { index, spec ->
            if (index > 0) delay(120)
            val subjects = runCatching {
                DoubanApi.collection(spec.collectionId, 0, 12)
                    .subject_collection_items
                    .orEmpty()
                    .mapNotNull { it.toSubject(type) }
                    .distinctBy { it.id }
            }.getOrDefault(emptyList())
            if (subjects.isNotEmpty()) out += FeedSection(spec.title, subjects)
        }
        if (out.isEmpty()) error("豆瓣接口暂时没有返回数据")
        out
    }

    suspend fun explore(
        type: MediaType,
        sort: String,
        start: Int,
        count: Int,
        tags: String?,
        scoreRange: String,
    ): Result<List<Subject>> = runCatching {
        val allTags = buildList {
            add(type.exploreTag)
            if (!tags.isNullOrBlank()) tags.split(",").forEach { t -> if (t.isNotBlank()) add(t) }
        }.joinToString(",")
        DoubanApi.searchSubjects(sort, scoreRange, allTags, start, count).data.orEmpty().mapNotNull { dto ->
            val id = dto.id?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val title = dto.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val cast = (dto.directors ?: dto.casts).orEmpty().take(2).joinToString(" / ")
            Subject(
                id = id,
                title = title,
                subtitle = cast.ifBlank { null },
                imageUrl = dto.cover?.takeIf { it.isNotBlank() },
                rating = dto.rate?.takeIf { it.isNotBlank() }?.toFloatOrNull(),
                type = type,
            )
        }
    }

    suspend fun exploreRaw(
        type: MediaType,
        sort: String,
        start: Int,
        count: Int,
        tags: String?,
        scoreRange: String,
    ): Result<Int> = runCatching {
        DoubanApi.recommend(type, sort, start, count, tags, scoreRange).items.orEmpty().size
    }

    suspend fun detail(type: MediaType, id: String): Result<SubjectDetail> = runCatching {
        coroutineScope {
            val detailDeferred = async { DoubanApi.detail(type, id) }
            val creditsDeferred = async { runCatching { DoubanApi.credits(type, id) }.getOrNull() }
            val photosDeferred = async { runCatching { DoubanApi.photos(type, id) }.getOrNull() }

            val dto = detailDeferred.await()
            val credits = creditsDeferred.await()?.items.orEmpty().mapNotNull { credit ->
                val name = credit.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                Person(
                    id = credit.id,
                    name = name,
                    avatarUrl = credit.avatar?.normal ?: credit.avatar?.large,
                    role = credit.character?.takeIf { it.isNotBlank() } ?: credit.roles?.firstOrNull(),
                )
            }
            val photos = photosDeferred.await()?.photos.orEmpty()
                .mapNotNull { it.image?.large ?: it.image?.normal }

            val title = dto.title?.takeIf { it.isNotBlank() } ?: id
            SubjectDetail(
                id = dto.id ?: id,
                type = MediaType.fromApi(dto.type) ?: type,
                title = title,
                originalTitle = dto.original_title?.takeIf { it.isNotBlank() },
                imageUrl = dto.pic?.large ?: dto.pic?.normal ?: dto.cover_url,
                rating = dto.rating?.value,
                ratingCount = dto.rating?.count,
                year = dto.year,
                genres = dto.genres.orEmpty(),
                countries = dto.countries.orEmpty(),
                durations = dto.durations.orEmpty(),
                pubdate = dto.pubdate?.firstOrNull(),
                episodes = dto.episodes_count,
                intro = dto.intro?.takeIf { it.isNotBlank() },
                directors = dto.directors.orEmpty().mapNotNull { Person(it.id, it.name ?: return@mapNotNull null, it.avatar?.normal, "导演") },
                actors = dto.actors.orEmpty().mapNotNull { Person(it.id, it.name ?: return@mapNotNull null, it.avatar?.normal, "演员") },
                credits = credits,
                photos = photos,
            )
        }
    }

    suspend fun search(keyword: String): Result<List<Subject>> = runCatching {
        DoubanApi.suggest(keyword).mapNotNull { dto ->
            val sid = dto.id?.takeIf { it.isNotBlank() }
                ?: Regex("/subject/(\\d+)").find(dto.url.orEmpty())?.groupValues?.getOrNull(1)
                ?: return@mapNotNull null
            val title = dto.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val type = MediaType.fromApi(dto.type) ?: MediaType.MOVIE
            val sub = listOfNotNull(dto.year?.takeIf { it.isNotBlank() }, dto.episode?.takeIf { it.isNotBlank() })
                .joinToString(" · ")
                .takeIf { it.isNotBlank() }
            Subject(sid, title, sub, dto.img, null, type)
        }
    }

    private fun DtoCollectionItem.toSubject(fallback: MediaType): Subject? {
        val sid = id?.takeIf { it.isNotBlank() } ?: return null
        val name = title?.takeIf { it.isNotBlank() } ?: return null
        val resolvedType = MediaType.fromApi(type) ?: MediaType.fromApi(item_type) ?: fallback
        val image = pic?.large ?: pic?.normal ?: cover?.url
        val sub = (card_subtitle ?: info ?: description)?.takeIf { it.isNotBlank() }
        return Subject(sid, name, sub, image, rating?.value, resolvedType)
    }
}
