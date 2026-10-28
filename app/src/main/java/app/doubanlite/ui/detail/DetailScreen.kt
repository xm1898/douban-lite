package app.doubanlite.ui.detail

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.doubanlite.data.MediaType
import app.doubanlite.data.Person
import app.doubanlite.data.SubjectDetail
import app.doubanlite.data.UiState
import app.doubanlite.ui.components.formatRating
import coil3.compose.AsyncImage

@Composable
fun DetailScreen(
    type: MediaType,
    id: String,
    onBack: () -> Unit,
) {
    val viewModel: DetailViewModel = viewModel()
    LaunchedEffect(type, id) { viewModel.load(type, id) }

    Box(Modifier.fillMaxSize().testTag("detail_screen")) {
        val state = viewModel.state.value
        Crossfade(targetState = state, label = "detailState", animationSpec = tween(280)) { s ->
            when (s) {
                is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.load(type, id) }) { Text("重试") }
                    }
                }

                is UiState.Success -> DetailContent(s.data)
            }
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
            modifier = Modifier
                .padding(12.dp)
                .size(38.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
            }
        }
    }
}

@Composable
private fun DetailContent(detail: SubjectDetail) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("detail_list"),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 58.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(104.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    AsyncImage(
                        model = detail.imageUrl,
                        contentDescription = detail.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Column(Modifier.padding(start = 14.dp)) {
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!detail.originalTitle.isNullOrBlank() && detail.originalTitle != detail.title) {
                        Text(
                            text = detail.originalTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        val rating = detail.rating
                        if (rating != null && rating > 0f) {
                            val animated by animateFloatAsState(rating, tween(650), label = "rating")
                            Text(
                                text = formatRating(animated),
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            val count = detail.ratingCount
                            if (count != null && count > 0) {
                                Text(
                                    text = "  ${count}人评",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                )
                            }
                        } else {
                            Text(
                                text = "暂无评分",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = metaLine(detail),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (!detail.intro.isNullOrBlank()) {
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text("简介", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = detail.intro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val people = detail.credits.ifEmpty { detail.directors + detail.actors }
        if (people.isNotEmpty()) {
            item {
                Column {
                    SectionTitle("演职人员")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(people, key = { index, p -> "$index:${p.id}:${p.name}" }) { _, person ->
                            CreditCell(person)
                        }
                    }
                }
            }
        }

        if (detail.photos.isNotEmpty()) {
            item {
                Column {
                    SectionTitle("剧照")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(detail.photos, key = { index, url -> "$index:$url" }) { _, url ->
                            Box(
                                modifier = Modifier
                                    .width(150.dp)
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(
                Modifier.padding(top = 18.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = "数据来自豆瓣公开接口，仅供浏览",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 10.dp),
    )
}

@Composable
private fun CreditCell(person: Person) {
    Column(modifier = Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AsyncImage(
                model = person.avatarUrl,
                contentDescription = person.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = person.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val role = person.role
        if (!role.isNullOrBlank()) {
            Text(
                text = role,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun metaLine(detail: SubjectDetail): String {
    val parts = mutableListOf<String>()
    detail.year?.takeIf { it.isNotBlank() }?.let { parts += it }
    detail.countries.take(3).joinToString(" ").takeIf { it.isNotBlank() }?.let { parts += it }
    detail.genres.take(3).joinToString(" ").takeIf { it.isNotBlank() }?.let { parts += it }
    detail.durations.firstOrNull()?.let { parts += it }
    detail.episodes?.takeIf { it > 0 }?.let { parts += "${it}集" }
    return parts.joinToString(" / ")
}
