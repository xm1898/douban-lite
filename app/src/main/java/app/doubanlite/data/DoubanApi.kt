package app.doubanlite.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * 豆瓣移动端 rexxar 接口 + 电影频道 j 接口。
 * 无需登录、无需 apikey，但必须带移动端 UA 与对应 Referer。
 */
object DoubanApi {

    private const val REXXAR = "https://m.douban.com/rexxar/api/v2"

    private const val UA =
        "Mozilla/5.0 (Linux; Android 14; Pixel 7 Build/AP1A.240305.019.A1) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Mobile Safari/537.36"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = false
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 20_000
            }
        }
    }

    private fun HttpRequestBuilder.douban(referer: String) {
        header(HttpHeaders.UserAgent, UA)
        header(HttpHeaders.Referrer, referer)
        header(HttpHeaders.Accept, "application/json, text/plain, */*")
        header(HttpHeaders.AcceptLanguage, "zh-CN,zh;q=0.9")
    }

    /** 首页栏目：正在上映 / Top250 / 华语口碑剧集 … */
    suspend fun collection(collectionId: String, start: Int = 0, count: Int = 12): DtoCollectionResponse =
        client.get("$REXXAR/subject_collection/$collectionId/items") {
            douban("https://m.douban.com/movie/")
            parameter("start", start)
            parameter("count", count)
        }.body()

    /** 推荐流，同时也是「找电影/找电视」筛选接口（sort=T 综合 / S 评分 / R 首映时间 / U 热度） */
    suspend fun recommend(
        type: MediaType,
        sort: String = "T",
        start: Int = 0,
        count: Int = 21,
        tags: String? = null,
        scoreRange: String = "0,10",
    ): DtoRecommendResponse =
        client.get("$REXXAR/${type.apiValue}/recommend") {
            douban("https://movie.douban.com/explore")
            parameter("refresh", 0)
            parameter("start", start)
            parameter("count", count)
            parameter("sort", sort)
            parameter("selected_categories", "{}")
            parameter("uncollect", "false")
            parameter("score_range", scoreRange)
            if (!tags.isNullOrBlank()) parameter("tags", tags)
        }.body()

    suspend fun detail(type: MediaType, id: String): DtoDetail =
        client.get("$REXXAR/${type.apiValue}/$id") {
            douban(type.referer)
        }.body()

    suspend fun credits(type: MediaType, id: String): DtoCreditsResponse =
        client.get("$REXXAR/${type.apiValue}/$id/credits") {
            douban(type.referer)
        }.body()

    suspend fun photos(type: MediaType, id: String, count: Int = 12): DtoPhotosResponse =
        client.get("$REXXAR/${type.apiValue}/$id/photos") {
            douban(type.referer)
            parameter("start", 0)
            parameter("count", count)
        }.body()

    /**
     * 找电影/找电视主数据源：字段统一、稳定，支持 sort(U 综合 / T 近期热门 / S 评分最高 / R 首映时间)、
     * range(min,max)、tags（逗号拼接，首项固定「电影」或「电视剧」）。
     */
    suspend fun searchSubjects(
        sort: String,
        range: String,
        tags: String,
        start: Int,
        limit: Int,
    ): DtoSearchSubjectsResponse =
        client.get("https://movie.douban.com/j/new_search_subjects") {
            douban("https://movie.douban.com/explore")
            parameter("sort", sort)
            parameter("range", range)
            parameter("tags", tags)
            parameter("start", start)
            parameter("limit", limit)
        }.body()

    suspend fun suggest(keyword: String): List<DtoSuggest> =
        client.get("https://movie.douban.com/j/subject_suggest") {
            douban("https://movie.douban.com/")
            parameter("q", keyword)
        }.body()
}
