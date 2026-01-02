import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.github.bumblebee202111.minusonecloudmusic.model.DiscoverBlock
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme

@Composable
fun DiscoverList(
    items: List<DiscoverBlock>,
    contentPadding: PaddingValues,
    onUrlClick: (String) -> Unit,
) {
    LazyColumn(contentPadding = contentPadding) {
        items(items = items) { block ->
            if (!block.title.isNullOrBlank()) {
                Text(
                    text = block.title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = DolphinTheme.colors.text1,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(block.items) { item ->
                    Column(
                        modifier = Modifier
                            .width(141.45.dp)
                            .clickable(
                                onClick = {
                                    onUrlClick(item.actionUrl)
                                }
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(141.45.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            AsyncImage(
                                model = item.coverImg,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop,
                                contentDescription = null
                            )
                            if (!block.showItemTitles)
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.2f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                        .padding(
                                            start = 8.dp,
                                            top = 8.dp,
                                            end = 8.dp,
                                            bottom = 33.dp
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!item.icon.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = item.icon,
                                            modifier = Modifier.size(25.dp),
                                            contentDescription = null
                                        )
                                    }

                                    if (!item.subTitle.isNullOrBlank()) {
                                        Text(
                                            text = item.subTitle,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            lineHeight = 25.sp,
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 1
                                        )
                                    }
                                }


                            item.coverText?.let { coverText ->
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.2f)
                                                )
                                            )
                                        )
                                        .padding(
                                            start = 8.dp,
                                            bottom = 8.dp,
                                            top = 24.dp,
                                            end = 8.dp
                                        )
                                ) {
                                    coverText.forEach { line ->
                                        Text(
                                            text = line,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                        }

                        if (block.showItemTitles) {
                            val displayText = item.subTitle?.takeIf { it.isNotBlank() }
                                ?: item.mainTitle

                            if (!displayText.isNullOrBlank()) {
                                Text(
                                    text = displayText,
                                    modifier = Modifier.padding(top = 6.dp),
                                    color = DolphinTheme.colors.text2,
                                    fontSize = 14.5.sp,
                                    lineHeight = 17.5.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}