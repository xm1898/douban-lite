package app.doubanlite.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.doubanlite.data.Subject
import coil3.compose.AsyncImage

fun formatRating(value: Float): String = String.format(java.util.Locale.US, "%.1f", value)

/** 评分徽章：贴在封面右上角，评分高时用橙色强调。 */
@Composable
fun RatingBadge(rating: Float?, modifier: Modifier = Modifier) {
    val score = rating?.takeIf { it > 0f }
    val shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
    if (score != null) {
        val bg = if (score >= 8f) {
            Color(0xFFF5A623)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        }
        val fg = if (score >= 8f) Color.White else MaterialTheme.colorScheme.onSurface
        Surface(
            shape = shape,
            color = bg,
            contentColor = fg,
            modifier = modifier,
        ) {
            Text(
                text = formatRating(score),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            )
        }
    } else {
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        ) {
            Text(
                text = "暂无",
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
fun PosterCard(
    subject: Subject,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = (if (width != null) modifier.width(width) else modifier)
            .testTag("subject_card")
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
        ) {
            Box {
                AsyncImage(
                    model = subject.imageUrl,
                    contentDescription = subject.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                // 底部渐变，让片名叠在封面上更好读
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(72.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.75f),
                            ),
                        ),
                )
                Text(
                    text = subject.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                )
                RatingBadge(
                    rating = subject.rating,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
        }
        RatingLine(subject.rating)
    }
}

@Composable
fun RatingLine(rating: Float?) {
    if (rating != null && rating > 0f) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(top = 6.dp, start = 2.dp),
        ) {
            Box(
                Modifier
                    .width(6.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(MaterialTheme.colorScheme.secondary),
            )
            Text(
                text = formatRating(rating),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    } else {
        Text(
            text = "暂无评分",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 6.dp, start = 2.dp),
        )
    }
}