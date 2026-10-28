package app.doubanlite.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.doubanlite.data.MediaType
import app.doubanlite.data.Subject
import app.doubanlite.ui.detail.DetailScreen
import app.doubanlite.ui.explore.ExploreScreen
import app.doubanlite.ui.feed.FeedScreen
import app.doubanlite.ui.search.SearchScreen

private enum class HomeTab(val label: String) {
    RECOMMEND("推荐"),
    EXPLORE("找片"),
}

@Composable
fun DoubanLiteApp() {
    var media by remember { mutableStateOf(MediaType.MOVIE) }
    var tab by remember { mutableStateOf(HomeTab.RECOMMEND) }
    var detail by remember { mutableStateOf<Subject?>(null) }
    var searchOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = detail != null || searchOpen) {
        if (searchOpen) searchOpen = false else detail = null
    }

    val opened = detail
    if (opened != null) {
        DetailScreen(type = opened.type, id = opened.id, onBack = { detail = null })
        return
    }
    if (searchOpen) {
        SearchScreen(onBack = { searchOpen = false }, onOpen = { detail = it })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
    ) {
        Column(Modifier.statusBarsPadding()) {
            TopBar(
                media = media,
                onMediaChange = {
                    if (it != media) {
                        media = it
                        tab = HomeTab.RECOMMEND
                    }
                },
                onSearch = { searchOpen = true },
            )
            TabStrip(tab = tab, onTab = { tab = it })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Box(Modifier.weight(1f)) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    (slideInVertically(tween(260)) { it / 8 } + fadeIn(tween(260)))
                        .togetherWith(slideOutVertically(tween(260)) { -it / 8 } + fadeOut(tween(200)))
                },
                label = "tabContent",
            ) { active ->
                when (active) {
                    HomeTab.RECOMMEND -> FeedScreen(media = media, onOpen = { detail = it })
                    HomeTab.EXPLORE -> ExploreScreen(media = media, onOpen = { detail = it })
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    media: MediaType,
    onMediaChange: (MediaType) -> Unit,
    onSearch: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MediaPill("电影", media == MediaType.MOVIE) { onMediaChange(MediaType.MOVIE) }
        Spacer(Modifier.width(8.dp))
        MediaPill("电视", media == MediaType.TV) { onMediaChange(MediaType.TV) }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSearch) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "搜索",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun MediaPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = fg,
        )
    }
}

@Composable
private fun TabStrip(tab: HomeTab, onTab: (HomeTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HomeTab.entries.forEach { entry ->
            val selected = entry == tab
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTab(entry) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = entry.label,
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(Modifier.height(3.dp))
                val indicatorWidth by animateDpAsState(if (selected) 18.dp else 0.dp, label = "indicator")
                Box(
                    Modifier
                        .height(3.dp)
                        .width(indicatorWidth)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}
