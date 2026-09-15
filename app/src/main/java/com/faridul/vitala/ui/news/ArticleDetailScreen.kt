package com.faridul.vitala.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.faridul.vitala.R
import com.faridul.vitala.VitalaApplication
import kotlinx.coroutines.launch

@Composable
fun ArticleDetailScreen(navController: NavController, articleId: String) {
    val context = LocalContext.current
    val app = context.applicationContext as VitalaApplication
    val article by app.newsRepository.observeById(articleId).collectAsState(initial = null)
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = article?.title ?: stringResource(R.string.loading),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            article?.let { current ->
                Icon(
                    imageVector = if (current.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = stringResource(
                        if (current.isBookmarked) R.string.article_unsave else R.string.article_save
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        scope.launch { app.newsRepository.toggleBookmark(current.id, !current.isBookmarked) }
                    }
                )
            }
        }

        article?.let { current ->
            val isJournal = current.category == "journal"
            val accent = if (isJournal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            val categoryLabel = if (isJournal) stringResource(R.string.category_journal) else stringResource(R.string.category_news)

            Spacer(Modifier.height(14.dp))
            Text(
                text = "$categoryLabel · ${current.source}",
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = current.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { uriHandler.openUri(current.url) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.article_read_full),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
