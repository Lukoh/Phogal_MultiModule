package com.goforer.phogal.presentation.ui.compose.screen.home.setting.notification

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goforer.phogal.presentation.stateholder.business.home.setting.notification.NotificationSettingViewModel
import com.goforer.phogal.presentation.stateholder.business.home.setting.notification.NotificationSettingViewModel.NotificationChannel
import com.goforer.designsystem.theme.Black
import com.goforer.designsystem.theme.ColorBgSecondary
import com.goforer.designsystem.theme.DarkGreen60
import com.goforer.designsystem.theme.DarkGreenGray90
import com.goforer.designsystem.theme.PhogalTheme
import com.goforer.designsystem.theme.Red60
import com.goforer.designsystem.theme.Red80

@Composable
fun NotificationSettingContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    viewModel: NotificationSettingViewModel = hiltViewModel()
) {
    val followingEnabled by viewModel.followingEnabled.collectAsStateWithLifecycle()
    val latestEnabled by viewModel.latestEnabled.collectAsStateWithLifecycle()
    val communityEnabled by viewModel.communityEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBgSecondary)
            .padding(top = contentPadding.calculateTopPadding())
            .verticalScroll(rememberScrollState())
    ) {
        NotificationToggleItem(
            name = "Following Notification",
            isToggled = followingEnabled,
            onToggled = { viewModel.setEnabled(NotificationChannel.Following, it) }
        )

        HorizontalDivider(modifier = Modifier.height(0.5.dp))

        NotificationToggleItem(
            name = "Latest Notification",
            isToggled = latestEnabled,
            onToggled = { viewModel.setEnabled(NotificationChannel.Latest, it) }
        )

        HorizontalDivider(modifier = Modifier.height(0.5.dp))

        NotificationToggleItem(
            name = "Community Notification",
            isToggled = communityEnabled,
            onToggled = { viewModel.setEnabled(NotificationChannel.Community, it) }
        )

        HorizontalDivider(modifier = Modifier.height(0.5.dp))
    }
}

@Composable
fun NotificationToggleItem(
    modifier: Modifier = Modifier,
    name: String,
    isToggled: Boolean,
    onToggled: (toggled: Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            color = Black,
            fontFamily = FontFamily.SansSerif,
            fontSize = 16.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium
        )
        Switch(
            checked = isToggled,
            onCheckedChange = onToggled,
            modifier = Modifier.size(60.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Red80,
                uncheckedThumbColor = DarkGreen60,
                checkedTrackColor = Red60,
                uncheckedTrackColor = DarkGreenGray90
            )
        )
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@Composable
fun NotificationSettingContentPreview() = PhogalTheme {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBgSecondary)
            .verticalScroll(rememberScrollState())
    ) {
        NotificationToggleItem(
            name = "Following Notification",
            isToggled = true
        ) {}

        HorizontalDivider(modifier = Modifier.height(0.5.dp))
        NotificationToggleItem(
            name = "Latest Notification",
            isToggled = false
        ) {}

        HorizontalDivider(modifier = Modifier.height(0.5.dp))
        NotificationToggleItem(
            name = "Community Notification",
            isToggled = true
        ) {}

        HorizontalDivider(modifier = Modifier.height(0.5.dp))
    }
}
