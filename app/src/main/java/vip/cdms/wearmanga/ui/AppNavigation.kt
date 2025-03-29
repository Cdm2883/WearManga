package vip.cdms.wearmanga.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeTextDefaults
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.*
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader

class AppNavigations { companion object }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    AppScaffold(
        timeText = {
            ResponsiveTimeText(
                timeTextStyle = TimeTextDefaults.timeTextStyle().copy(color = MaterialTheme.colors.onSurface)
            )
        }
    ) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = AppNavigations.StartDestination,
                modifier = Modifier.background(MaterialTheme.colors.background),
                enterTransition = { fadeIn() + slideIn { IntOffset(it.width, 0) } },
                exitTransition = { fadeOut() + slideOut { IntOffset(it.width, 0) } },
            ) {
                appNavGraph(navController, this@SharedTransitionLayout)
            }
        }
    }
}

@Composable
fun MessageDetail(id: String) {
    // .. Screen level content goes here
    val scrollState = rememberScrollState()

    ScreenScaffold(scrollState = scrollState) {
        // Screen content goes here
        // [END android_wear_navigation]
        val padding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Text
        )()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
//                .rotaryWithScroll(scrollState)
                .padding(padding),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = id,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MessageList(onMessageClick: (String) -> Unit) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(text = "Test".repeat(100))
                }
            }
            item {
                Chip(label = "Message 1", onClick = { onMessageClick("message1".repeat(100)) })
            }
            item {
                Chip(label = "Message 2", onClick = { onMessageClick("message2") })
            }
        }
    }
}
