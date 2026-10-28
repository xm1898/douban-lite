package app.doubanlite.ui.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.doubanlite.data.MediaType
import app.doubanlite.data.Subject
import app.doubanlite.data.UiState
import app.doubanlite.ui.components.PosterCard
import kotlinx.coroutines.flow.distinctUntilChanged

private enum class SortOption(val label: String, val value: String) {
    RECOMMEND("综合排序", "T"),
    HOT("近期热门", "U"),
    RATING("高分优先", "S"),
    TIME("首映时间", "R"),
}

private data class RatingOption(val label: String, val range: String)

private val RATING_OPTIONS = listOf(
    RatingOption("不限", "0,10"),
    RatingOption("9 分以上", "9,10"),
    RatingOption("8 分以上", "8,10"),
    RatingOption("7 分以上", "7,10"),
    RatingOption("6 分以上", "6,10"),
)

private val GENRES = listOf(
    "全部", "剧情", "喜剧", "爱情", "动作", "科幻", "动画", "悬疑", "惊悚",
    "恐怖", "犯罪", "纪录片", "战争", "奇幻", "冒险", "家庭", "古装", "历史",
)

private val REGIONS = listOf(
    "全部", "中国大陆", "美国", "日本", "韩国", "英国", "法国", "德国", "印度", "泰国", "中国香港", "中国台湾",
)

private val YEARS = listOf(
    "全部", "2026", "2025", "2024", "2023", "2022", "2021", "2020", "2019", "2018", "2017", "2016",
)

@Composable
fun ExploreScreen(
    media: MediaType,
    onOpen: (Subject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ExploreViewModel = viewModel()

    var sort by remember { mutableStateOf(SortOption.RECOMMEND) }
    var rating by remember { mutableStateOf(RATING_OPTIONS.first()) }
    var genre by remember { mutableStateOf("全部") }
    var region by remember { mutableStateOf("全部") }
    var year by remember { mutableStateOf("全部") }
    var expanded by remember { mutableStateOf(false) }

    val tags = remember(genre, region, year) {
        listOf(genre, region, year)
            .filter { it != "全部" }
            .joinToString(",")
            .ifBlank { null }
    }

    LaunchedEffect(media, sort, tags, rating) {
        viewModel.reload(media, sort.value, tags, rating.range)
    }

    val items = viewModel.items.value
    val state = viewModel.state.value
    val loadingMore = viewModel.loadingMore.value
    val end = viewModel.end.value

    val gridState = rememberLazyGridState()
    LaunchedEffect(gridState, items.size, end) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { last ->
                if (items.isNotEmpty() && last >= items.size - 6) viewModel.loadMore()
            }
    }

    Column(modifier.fillMaxSize()) {
        FilterPanel(
            sort = sort,
            onSort = { sort = it },
            rating = rating,
            onRating = { rating = it },
            expanded = expanded,
            onToggle = { expanded = !expanded },
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(tween(200)),
            exit = shrinkVertically(animationSpec = tween(240, easing = FastOutSlowInEasing)) + fadeOut(tween(160)),
        ) {
            ExpandedFilters(
                genre = genre,
                onGenre = { genre = it },
                region = region,
                onRegion = { region = it },
                year = year,
                onYear = { year = it },
            )
        }

        when {
            state is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.reload(media, sort.value, tags, rating.range) }) {
                        Text("重试")
                    }
                }
            }

            items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "没有符合条件的结果，换个筛选试试",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("explore_grid"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                itemsIndexed(items, key = { index, item -> "$index:${item.id}" }) { _, subject ->
                    PosterCard(
                        subject = subject,
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        onClick = { onOpen(subject) },
                    )
                }
                if (loadingMore || !end) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPanel(
    sort: SortOption,
    onSort: (SortOption) -> Unit,
    rating: RatingOption,
    onRating: (RatingOption) -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Column {
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(SortOption.entries.size) { index ->
                val option = SortOption.entries[index]
                FilterPill(option.label, sort == option) { onSort(option) }
            }
            item {
                Row(
                    modifier = Modifier
                        .testTag("filter_toggle")
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onToggle)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("筛选", style = MaterialTheme.typography.labelLarge)
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(RATING_OPTIONS.size) { index ->
                val option = RATING_OPTIONS[index]
                FilterPill(option.label, rating == option) { onRating(option) }
            }
        }
    }
}

@Composable
private fun ExpandedFilters(
    genre: String,
    onGenre: (String) -> Unit,
    region: String,
    onRegion: (String) -> Unit,
    year: String,
    onYear: (String) -> Unit,
) {
    Column(Modifier.padding(top = 6.dp, bottom = 4.dp)) {
        FilterGroup("类型", GENRES, genre, onGenre)
        FilterGroup("地区", REGIONS, region, onRegion)
        FilterGroup("年份", YEARS, year, onYear)
    }
}

@Composable
private fun FilterGroup(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 14.dp, end = 6.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(options.size) { index ->
                val option = options[index]
                FilterPill(option, selected == option) { onSelect(option) }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bgTarget = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fgTarget = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val bg by animateColorAsState(bgTarget, tween(180), label = "pillBg")
    val fg by animateColorAsState(fgTarget, tween(180), label = "pillFg")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = fg,
        )
    }
}
