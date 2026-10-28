package app.doubanlite

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/**
 * 渲染冒烟测试：顶栏只有「电影 / 电视 + 搜索」，页面只有「推荐 / 找片」，
 * 没有登录、没有读书/音乐、没有左上角人物按钮，并且能真的把豆瓣数据渲染出来。
 * 顺手把界面截图落到 app/build/screenshots/。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppSmokeTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun chromeHasOnlyMovieTvAndNoLegacyEntries() {
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()

        assertTrue(rule.onAllNodesWithText("电影").fetchSemanticsNodes().isNotEmpty())
        assertTrue(rule.onAllNodesWithText("电视").fetchSemanticsNodes().isNotEmpty())
        assertTrue(rule.onAllNodesWithText("推荐").fetchSemanticsNodes().isNotEmpty())
        assertTrue(rule.onAllNodesWithText("找片").fetchSemanticsNodes().isNotEmpty())

        listOf("读书", "音乐", "登录", "我的", "小组", "动态").forEach { legacy ->
            assertEquals(
                "不该出现「$legacy」",
                0,
                rule.onAllNodesWithText(legacy).fetchSemanticsNodes().size,
            )
        }
        saveScreenshot("chrome")
    }

    /** 推荐信息流：等真实豆瓣数据回来。 */
    @Test
    fun feedLoadsRealDataFromDouban() {
        rule.mainClock.autoAdvance = false
        val loaded = waitUntilText("正在上映", 45_000)
        if (loaded) {
            rule.mainClock.advanceTimeBy(600)
            saveScreenshot("feed_movie")
        }
        assertTrue("推荐流没有加载出「正在上映」", loaded)
    }

    /** 切到电视。 */
    @Test
    fun tvFeedLoads() {
        rule.mainClock.autoAdvance = false
        rule.onAllNodesWithText("电视").onFirst().performClick()
        val loaded = waitUntilText("华语口碑剧集", 45_000)
        if (loaded) {
            rule.mainClock.advanceTimeBy(600)
            saveScreenshot("feed_tv")
        }
        assertTrue("电视推荐流没有加载出栏目", loaded)
    }

    /** 找片：筛选栏 + 结果网格 + 进详情。 */
    @Test
    fun exploreAndDetail() {
        rule.mainClock.autoAdvance = false
        rule.onAllNodesWithText("找片").onFirst().performClick()
        assertTrue("找片页没有出排序栏", waitUntilText("综合排序", 20_000))

        val cards = waitUntilCards(6, 45_000)
        assertTrue("找片网格没有数据，卡片数=$cards", cards >= 6)
        saveScreenshot("explore")

        // 展开筛选面板
        rule.onAllNodesWithTag("filter_toggle").onFirst().performClick()
        rule.mainClock.advanceTimeBy(800)
        assertTrue(waitUntilText("类型", 10_000))
        saveScreenshot("explore_filters")

        // 进详情
        rule.onAllNodes(hasTestTag("subject_card") and hasAnyAncestor(hasTestTag("explore_grid")))
            .onFirst()
            .performClick()
        assertTrue("详情页没有渲染出来", waitUntilTag("detail_screen", 20_000))
        // 「演职人员」在 LazyColumn 深处，先等待网络数据回来再往下滚
        val introSeen = waitUntilText("简介", 30_000)
        assertTrue("详情没有等到内容, 可见=${visibleTextsAll().take(25)}", introSeen)
        rule.mainClock.advanceTimeBy(600)
        saveScreenshot("detail")
        assertTrue("详情页滚不到演职人员", scrollUntilText("演职人员", maxSwipes = 12))
        // 再往下滚几屏，截一张下半部分的图
        repeat(3) {
            rule.onNodeWithTag("detail_list").performTouchInput { swipeUp() }
            repeat(5) { rule.mainClock.advanceTimeBy(50) }
            Thread.sleep(200)
        }
        saveScreenshot("detail_scrolled")
    }

    /** 顶栏搜索：点搜索图标 → 输入关键词 → 出结果。 */
    @Test
    fun searchReturnsResults() {
        rule.mainClock.autoAdvance = false
        rule.onNodeWithContentDescription("搜索").performClick()
        assertTrue("搜索页没有打开", waitUntilTag("search_field", 15_000))

        rule.onNodeWithTag("search_field").performClick()
        rule.onNodeWithTag("search_field").performTextInput("霸王别姬")

        val found = waitUntilText("霸王别姬", 40_000)
        assertTrue("搜索没有返回结果", found)
        rule.mainClock.advanceTimeBy(400)
        saveScreenshot("search")
    }

    private fun waitUntilText(text: String, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            rule.mainClock.advanceTimeBy(32)
            if (rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()) return true
            Thread.sleep(250)
        }
        println("[warn] 等待文本超时: $text，可见文本=${visibleTexts().take(25)}")
        return false
    }

    private fun waitUntilTag(tag: String, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            rule.mainClock.advanceTimeBy(32)
            if (rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()) return true
            Thread.sleep(250)
        }
        return false
    }

    /** LazyColumn 深处的内容，反复上滑直到出现目标文本。 */
    private fun scrollUntilText(text: String, maxSwipes: Int): Boolean {
        repeat(maxSwipes) {
            rule.mainClock.advanceTimeBy(32)
            if (rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()) return true
            rule.onNodeWithTag("detail_list").performTouchInput { swipeUp() }
            repeat(6) { rule.mainClock.advanceTimeBy(50) }
            Thread.sleep(200)
        }
        return rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    private fun visibleTextsAll(): List<String> =
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .fetchSemanticsNodes()
            .flatMap { it.config.getOrNull(SemanticsProperties.Text).orEmpty().map { t -> t.text } }

    private fun waitUntilCards(min: Int, timeoutMs: Long): Int {
        val deadline = System.currentTimeMillis() + timeoutMs
        var last = 0
        while (System.currentTimeMillis() < deadline) {
            rule.mainClock.advanceTimeBy(32)
            last = rule.onAllNodes(
                hasTestTag("subject_card") and hasAnyAncestor(hasTestTag("explore_grid")),
            ).fetchSemanticsNodes().size
            if (last >= min) return last
            Thread.sleep(250)
        }
        return last
    }

    private fun visibleTexts(): List<String> =
        rule.onAllNodes(hasTestTag("explore_grid")).fetchSemanticsNodes()
            .flatMap { node -> node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text } }

    private fun saveScreenshot(name: String) {
        val decor = rule.activity.window.decorView
        val width = if (decor.width > 0) decor.width else 1080
        val height = if (decor.height > 0) decor.height else 2160
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        decor.draw(Canvas(bitmap))
        val dir = File("build/screenshots").apply { mkdirs() }
        FileOutputStream(File(dir, "$name.png")).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
