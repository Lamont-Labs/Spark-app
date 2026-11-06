package com.lamontlabs.spark.journal

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.spark.data.ReflectionRepository
import com.lamontlabs.spark.media.PrivatePhotoStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun JournalScreen(repository: ReflectionRepository, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val photoStore = remember(context) { PrivatePhotoStore(context) }
    val stream = remember(repository) { repository.observeAll() }
    val reflections by stream.collectAsState(initial = emptyList())
    val dateFormat = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d") }
    val grouped = remember(reflections) { reflections.groupBy { it.reflectionDate } }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 28.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("YOUR STORY  /  JOURNAL", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary, letterSpacing = 1.6.sp)
                Text("The good you've kept.", style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold)
                Text("Little moments become something meaningful over time.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (reflections.isNotEmpty()) {
                    Text("${reflections.size} moments saved", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        if (reflections.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("A beautiful beginning", style = MaterialTheme.typography.titleLarge)
                        Text("Your gratitude and daily wins will find a home here. Start with one small moment on Today.",
                            style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        grouped.forEach { (date, entries) ->
            item(key = "date:$date") {
                val readable = remember(date) {
                    runCatching { LocalDate.parse(date).format(dateFormat) }.getOrDefault(date)
                }
                Text(readable, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp))
            }
            items(entries, key = { it.id }) { reflection ->
                val gratitude = reflection.kind == "GRATITUDE"
                val accent = if (gratitude) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(21.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (gratitude) Icons.Filled.FavoriteBorder else Icons.Filled.StarOutline,
                                contentDescription = null, tint = accent
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(if (gratitude) "GRATITUDE" else "DAILY WIN",
                                style = MaterialTheme.typography.labelMedium, color = accent,
                                fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
                        }
                        if (reflection.body.isNotBlank()) {
                            Text(reflection.body, style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface, lineHeight = 25.sp)
                        }
                        reflection.photoId?.let { id ->
                            val photo = photoStore.fileFor(id)
                            val bitmap = remember(id) {
                                photo?.let { file ->
                                    BitmapFactory.Options().run {
                                        inSampleSize = 4
                                        BitmapFactory.decodeFile(file.absolutePath, this)
                                    }
                                }
                            }
                            if (bitmap != null) {
                                Image(bitmap.asImageBitmap(), contentDescription = "Attached reflection photo",
                                    modifier = Modifier.fillMaxWidth().height(190.dp),
                                    contentScale = ContentScale.Crop)
                            } else {
                                Text("Attached photo unavailable", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        reflection.mood?.let { mood ->
                            Text("FELT  ·  $mood", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
