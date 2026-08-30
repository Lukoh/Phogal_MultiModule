package com.goforer.phogal.presentation.ui.compose.screen.home.common.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.goforer.designsystem.animation.animateIconScale
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.UserInfoUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.rememberUserInfoUiState
import com.goforer.designsystem.theme.DarkGreenGray10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoBottomSheet(
    userInfoUiState: UserInfoUiState = rememberUserInfoUiState(),
    user: User,
    showUserInfoBottomSheet: Boolean,
    onDismissedRequest: (Boolean) -> Unit
) {
    // Note: SearchSection text input is now hoisted into rememberSearchSectionUiState
    // alongside the screen, so the chip-tap path goes through the same channel as
    // typed input. This collapses two state mutation paths into one.
    val onDismissRequest = { onDismissedRequest(false) }

    if (showUserInfoBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = userInfoUiState.bottomSheetState,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.wrapContentHeight(),
                horizontalAlignment = Alignment.Start,
            ) {
                ProfileItem(
                    image = user.profileImage.medium,
                    name = user.name,
                    nameColor = DarkGreenGray10,
                    position = 9,
                    onClicked = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
                getProfileInfoItems(user).forEachIndexed { _, item ->
                    UserInfoItem(
                        text = item.text,
                        textColor = DarkGreenGray10,
                        iconResId = item.iconResId,
                        position = item.position
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val animationIconScale = animateIconScale(inputScale = 0.6F, position = 1, delay = 150L)

                    Image(
                        painter = painterResource(id = R.drawable.ic_portfolio),
                        contentDescription = "Following",
                        modifier = Modifier
                            .size(22.dp)
                            .padding(horizontal = 4.dp)
                            .graphicsLayer {
                                scaleX = animationIconScale
                                scaleY = animationIconScale
                            }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ShowPortfolioButton(
                        scope = userInfoUiState.scope,
                        bottomSheetState = userInfoUiState.bottomSheetState,
                        firstName = user.firstName,
                        onDismissedRequest = onDismissedRequest
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}