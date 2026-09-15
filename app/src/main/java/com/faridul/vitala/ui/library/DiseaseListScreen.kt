package com.faridul.vitala.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DiseaseListScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Disease library", style = MaterialTheme.typography.titleLarge)
        // TODO: searchable list of Disease entries loaded from bundled JSON via Room
    }
}

@Composable
fun DiseaseDetailScreen(navController: NavController, diseaseId: String) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Disease $diseaseId", style = MaterialTheme.typography.titleLarge)
        // TODO: overview, symptoms list, treatment section, source citation + disclaimer
    }
}
