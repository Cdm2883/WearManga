package vip.cdms.wearmanga.ui.pages.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ColumnItemType.Companion.IconButton
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Button
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HomePage(
    viewModel: HomePageModel = viewModel()
) {
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
                    Text(text = "推荐")
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .height(52.dp),
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp, CenterHorizontally),
                ) {
                    Button(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        onClick = { },
                        modifier = Modifier.weight(weight = 0.3F, fill = false),
                        colors = ButtonDefaults.secondaryButtonColors(),
                    )
                    Button(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "排行",
                        onClick = { },
                        modifier = Modifier.weight(weight = 0.3F, fill = false),
                    )
                    Button(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "分类",
                        onClick = { },
                        modifier = Modifier.weight(weight = 0.3F, fill = false),
                    )
                }
            }
            item {
                Chip(label = "Message 1", onClick = { })
            }
            item {
                Chip(label = "Message 2", onClick = { })
            }
        }
    }
}
