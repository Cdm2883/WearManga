package vip.cdms.wearmanga.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.wear.compose.foundation.pager.rememberPagerState
import com.google.android.horologist.compose.pager.PagerScreen
import kotlinx.serialization.Serializable
import vip.cdms.wearmanga.ui.pages.home.HomePage

val AppNavigations.Companion.StartDestination: Any
    get() = Routes.Main
private object Routes {
    @Serializable
    object Main

    @Serializable
    data class MessageDetail(val id: String)
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.appNavGraph(navController: NavHostController, sharedTransitionScope: SharedTransitionScope) {
    composable<Routes.Main> {
        val pagerState = rememberPagerState { 3 }
        PagerScreen(state = pagerState) { page ->
            when (page) {
                0 -> HomePage()
                1 -> MessageList(onMessageClick = { id ->
                    navController.navigate(Routes.MessageDetail(id))
                })
                2 -> MessageList(onMessageClick = { id ->
                    navController.navigate(Routes.MessageDetail(id))
                })
            }
        }
    }

    composable<Routes.MessageDetail> { backStackEntry ->
        val route: Routes.MessageDetail = backStackEntry.toRoute()
        MessageDetail(id = route.id)
    }
}
