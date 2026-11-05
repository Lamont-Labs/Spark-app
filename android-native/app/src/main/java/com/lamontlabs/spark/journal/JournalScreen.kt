package com.lamontlabs.spark.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.ContentScale
import com.lamontlabs.spark.media.PrivatePhotoStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.spark.data.ReflectionRepository

@Composable
fun JournalScreen(repository: ReflectionRepository, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val photoStore = remember(context) { PrivatePhotoStore(context) }
    val stream = remember(repository) { repository.observeAll() }
    val reflections by stream.collectAsState(initial = emptyList())
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Your journal", style = MaterialTheme.typography.headlineLarge)
        }
        if (reflections.isEmpty()) {
            item {
                Text(
                    "Your gratitude and daily wins will appear here once you save them.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        items(reflections, key = { it.id }) { reflection ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(reflection.reflectionDate, style = MaterialTheme.typography.labelMedium)
                    Text(
                        if (reflection.kind == "GRATITUDE") "Gratitude" else "Daily Win",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (reflection.body.isNotBlank()) {
                        Text(reflection.body, style = MaterialTheme.typography.bodyLarge)
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
                            Image(bitmap.asImageBitmap(), contentDescription = "Attached reflection photo", modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop)
                        } else {
                            Text("Attached photo unavailable", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    reflection.mood?.let { selectedMood ->
                        Text("Felt: $selectedMood", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
