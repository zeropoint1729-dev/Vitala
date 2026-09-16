package com.faridul.vitala.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.faridul.vitala.R
import com.faridul.vitala.VitalaApplication
import com.faridul.vitala.data.model.NewsArticle
import com.faridul.vitala.ui.components.AnimatedInfoCard
import kotlinx.coroutines.launch

@Composable
fun NewsListScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as VitalaApplication
    val scope = rememberCoroutineScope()

    val articles by app.newsRepository.observeAll().collectAsState(initial = emptyList())
    val isRefreshing by app.newsRepository.isRefreshing.collectAsState()
    val lastSyncedAt by app.newsRepository.lastSyncedAt.collectAsState()
    val lastError by app.newsRepository.lastError.collectAsState()

    var query by remember { mutableStateOf("") }
    var showSavedOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        app.newsRepository.refresh()
    }

    val filtered = remember(articles, query, showSavedOnly) {
        articles
            .filter { !showSavedOnly || it.isBookmarked }
            .filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.source.contains(query, ignoreCase = true)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.news_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.cd_refresh),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { scope.launch { app.newsRepository.refresh() } }
                )
            }
        }

        SyncStatusLine(lastSyncedAt = lastSyncedAt, lastError = lastError, isRefreshing = isRefreshing)
        Spacer(Modifier.height(12.dp))

        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.news_search_placeholder)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterPill(
                label = stringResource(R.string.filter_all),
                selected = !showSavedOnly,
                onClick = { showSavedOnly = false }
            )
            FilterPill(
                label = stringResource(R.string.filter_saved),
                selected = showSavedOnly,
                onClick = { showSavedOnly = true }
            )
        }
        Spacer(Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            Text(
                text = stringResource(R.string.news_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        filtered.forEach { article ->
            NewsRow(article) { navController.navigate("news/${article.id}") }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SyncStatusLine(lastSyncedAt: Long?, lastError: String?, isRefreshing: Boolean) {
    val text = when {
        isRefreshing -> stringResource(R.string.news_refreshing)
        lastError != null && lastSyncedAt == null -> stringResource(R.string.news_refresh_error)
        lastSyncedAt != null -> stringResource(R.string.news_last_synced, relativeTime(lastSyncedAt))
        else -> null
    }
    if (text != null) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun relativeTime(timestamp: Long): String {
    val diffMinutes = (System.currentTimeMillis() - timestamp) / 60000
    return when {
        diffMinutes < 1 -> stringResource(R.string.time_just_now)
        diffMinutes < 60 -> stringResource(R.string.time_minutes_ago, diffMinutes.toInt())
        else -> stringResource(R.string.time_hours_ago, (diffMinutes / 60).toInt())
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
private fun NewsRow(article: NewsArticle, onClick: () -> Unit) {
    val isJournal = article.category == "journal"
    val accent = if (isJournal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val categoryLabel = if (isJournal) stringResource(R.string.category_journal) else stringResource(R.string.category_news)
    val thumbnailIcon = if (isJournal) Icons.Filled.Description else Icons.Filled.Article

    AnimatedInfoCard(
        title = article.title,
        subtitle = "$categoryLabel · ${article.source}",
        icon = thumbnailIcon,
        iconTint = accent,
        onClick = onClick,
        trailing = if (article.isBookmarked) {
            {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null
    )
}
