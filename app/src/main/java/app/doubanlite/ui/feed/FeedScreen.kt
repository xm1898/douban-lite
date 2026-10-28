package app.doubanlite.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.doubanlite.data.FeedSection
import app.doubanlite.data.MediaType
import app.doubanlite.data.Subject
import app.doubanlite.data.UiState
import app.doubanlite.ui.components.PosterCard

@Composable
fun FeedScreen(
    media: MediaType,
    onOpen: (Subject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: FeedViewModel = viewModel()
    LaunchedEffect(media) { viewModel.load(media) }

    val state = viewModel.state.value
    Crossfade(targetState = state, label = "feedState", animationSpec = tween(260)) { s ->
        when (s) {
            is UiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            is UiState.Error -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.load(media, force = true) }) { Text("重试") }
                }
            }

            is UiState.Success -> LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                itemsIndexed(s.data, key = { index, item -> "$index:${item.title}" }) { index, section ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(320)) +
                            slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 2 },
                        modifier = Modifier.animateItem(),
                    ) {
                        FeedRow(section, onOpen)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedRow(section: FeedSection, onOpen: (Subject) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(section.subjects, key = { index, item -> "$index:${item.id}" }) { _, subject ->
                PosterCard(subject = subject, width = 108.dp, onClick = { onOpen(subject) })
            }
        }
    }
}