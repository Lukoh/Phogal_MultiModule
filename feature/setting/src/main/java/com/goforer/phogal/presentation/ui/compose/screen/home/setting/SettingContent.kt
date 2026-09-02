package com.goforer.phogal.presentation.ui.compose.screen.home.setting

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goforer.designsystem.theme.PhogalTheme
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.local.home.setting.SettingItem
import com.goforer.phogal.data.model.remote.response.setting.Profile

@Composable
fun SettingContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    onItemClicked: (index: Int) -> Unit
) {
    val accountItems = listOf(
        SettingItem(stringResource(id = R.string.setting_bookmark), R.drawable.ic_bookmark),
        SettingItem(stringResource(id = R.string.setting_follower), R.drawable.ic_followers),
        SettingItem(stringResource(id = R.string.setting_alarm), R.drawable.ic_notification)
    )
    val appItems = listOf(
        SettingItem(stringResource(id = R.string.setting_privacy_policy), R.drawable.ic_privacy),
        SettingItem(stringResource(id = R.string.setting_app_info), R.drawable.ic_information)
    )
    val supportItems = listOf(
        SettingItem(stringResource(id = R.string.setting_send_feedback), R.drawable.ic_bookmark),
        SettingItem(stringResource(id = R.string.setting_give_start), R.drawable.ic_rating_start),
        SettingItem(stringResource(id = R.string.setting_homepage), R.drawable.ic_homepage)
    )

    val profileUiState = Profile(
        id = 0,
        name = "Lukoh",
        sex = "남성",
        favor = true,
        followed = true,
        email = "lukoh.nam@gmail.com",
        profileImage = "https://avatars.githubusercontent.com/u/18302717?v=4",
        personality = "sociable & gregarious",
        cellphone = "+820101111-1111",
        address = "",
        birthday = "Mar, 04, 1999",
        reputation = "Professional mobile SW engineer...",
        deleted = false
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = contentPadding.calculateTopPadding())
            .verticalScroll(rememberScrollState())
    ) {
        SetProfileItem(
            profileUiState = profileUiState,
            onItemClicked = {}
        )

        HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

        SettingSectionHeader(title = stringResource(id = R.string.setting_account_section))
        accountItems.forEachIndexed { index, item ->
            SetItem(
                index = index,
                text = item.text,
                drawable = item.drawable,
                onItemClicked = onItemClicked
            )
            if (index < accountItems.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
        }

        SettingSectionHeader(title = stringResource(id = R.string.setting_app_section))
        appItems.forEachIndexed { index, item ->
            SetItem(
                index = index + accountItems.size,
                text = item.text,
                drawable = item.drawable,
                onItemClicked = onItemClicked
            )
            if (index < appItems.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
        }

        SettingSectionHeader(title = stringResource(id = R.string.setting_support_section))
        supportItems.forEachIndexed { index, item ->
            SetItem(
                index = index + accountItems.size + appItems.size,
                text = item.text,
                drawable = item.drawable,
                onItemClicked = onItemClicked
            )
            if (index < supportItems.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            MetaLinkItem(text = stringResource(id = R.string.setting_terms_and_conditions))
            MetaLinkItem(text = stringResource(id = R.string.setting_view_contact))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(id = R.string.app_name) + " 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun MetaLinkItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle link */ }
            .padding(vertical = 10.dp)
    )
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@Composable
fun SettingContentPreview() {
    PhogalTheme {
        SettingContent(onItemClicked = {})
    }
}

