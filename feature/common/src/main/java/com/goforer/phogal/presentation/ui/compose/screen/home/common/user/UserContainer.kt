package com.goforer.phogal.presentation.ui.compose.screen.home.common.user

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.size.Size
import com.goforer.designsystem.component.IconButton
import com.goforer.designsystem.component.IconContainer
import com.goforer.designsystem.component.ImageCrossFade
import com.goforer.designsystem.component.loadImagePainter
import com.goforer.designsystem.component.snsShimmer
import com.goforer.base.extension.toUser
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.setting.follow.FollowViewModel
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.UserContainerUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.rememberUserContainerUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.follow.ShowFollowButton
import com.goforer.designsystem.theme.Black
import com.goforer.designsystem.theme.Blue50
import com.goforer.designsystem.theme.DarkGreen60
import com.goforer.designsystem.theme.DarkGreenGray99
import com.goforer.designsystem.theme.PhogalTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserContainer(
    modifier: Modifier = Modifier,
    state: UserContainerUiState = rememberUserContainerUiState(),
    followViewModel: FollowViewModel?,
    onShowUserInfo: (User) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit
) {
    if (state.user.isEmpty()) return

    val user = remember(state.user) { state.user.toUser() }

    UserContainerContent(
        modifier = modifier,
        state = state,
        user = user,
        isFollowed = followViewModel?.isUserFollowed(user) ?: false,
        onFollowClick = { followViewModel?.setUserFollow(user) },
        onShowUserInfo = onShowUserInfo,
        onViewPhotos = onViewPhotos
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserContainerContent(
    modifier: Modifier = Modifier,
    state: UserContainerUiState = rememberUserContainerUiState(),
    user: User,
    isFollowed: Boolean,
    onFollowClick: (User) -> Unit,
    onShowUserInfo: (User) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val lastName = user.lastName ?: stringResource(id = R.string.picture_no_last_name)

    Column(
        modifier = modifier.background(state.colors[2]),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 4.dp, end = 4.dp)
                .background(Color.Transparent)
                .wrapContentHeight(Alignment.CenterVertically)
                .fillMaxWidth()
                .heightIn(76.dp, 114.dp)
                .clickable {
                    state.baseUiState.keyboardController?.hide()
                    focusManager.clearFocus()
                    onShowUserInfo(user)
                },
        ) {
            ShowProfileImage(
                profileImageSize = state.profileSize.dp,
                user = user,
                lastName = lastName,
                visibleViewPhotosButton = state.visibleViewButton,
                onViewPhotos = onViewPhotos
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier
                .height(IntrinsicSize.Min)
                .widthIn(186.dp)
            ) {
                Text(
                    text = user.name,
                    color = state.colors[0],
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Normal,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${user.totalLikes}${" "}" +
                            "${stringResource(id = R.string.picture_likes)}${" "}${user.totalCollections}${" "}" +
                            "${stringResource(id = R.string.picture_collections)}${" "}",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Normal,
                    color = state.colors[1],
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${stringResource(id = R.string.user_updated_at)}${" "}${user.updatedAt}",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Normal,
                    color = state.colors[1],
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.width(10.dp))
            ShowFollowButton(
                modifier = Modifier,
                followColor = state.colors[4],
                isFollowed
            ) {
                onFollowClick(user)
            }
        }

        if (state.visibleViewButton) {
            Text(
                text = "${stringResource(id = R.string.picture_view_photos)} ${user.totalPhotos} ${stringResource(id = R.string.picture_photos, user.name)}",
                modifier = Modifier
                    .padding(
                        start = if (state.fromItem)
                            56.dp
                        else
                            66.dp
                    )
                    .clickable {
                        user.username?.let { username ->
                            onViewPhotos(
                                username,
                                user.firstName,
                                lastName,
                                username
                            )
                        }
                    },
                color = if (state.fromItem)
                    Color.White
                else
                    DarkGreen60,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }


}

@Composable
fun ShowProfileImage(
    profileImageSize: Dp,
    user: User,
    lastName: String,
    visibleViewPhotosButton: Boolean,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
) {
    IconContainer(profileImageSize) {
        Box {
            val painter = loadImagePainter(
                data = user.profileImage.small,
                size = Size.ORIGINAL
            )

            ImageCrossFade(painter = painter, durationMillis = null)
            Image(
                painter = painter,
                contentDescription = "Profile",
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(0.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                    .clickable {
                        if (visibleViewPhotosButton)
                            user.username?.let { username ->
                                onViewPhotos(
                                    username,
                                    user.firstName,
                                    lastName,
                                    username
                                )
                            }
                    },
                Alignment.CenterStart,
                contentScale = ContentScale.Crop
            )

            if (painter.state is AsyncImagePainter.State.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .snsShimmer()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowPortfolioButton(
    scope: CoroutineScope,
    bottomSheetState: SheetState,
    firstName: String,
    onDismissedRequest: (Boolean) -> Unit
) {
    val onClick = {
        scope.launch {
            bottomSheetState.hide()
            onDismissedRequest(true)
        }
        Unit
    }

    IconButton(
        modifier = Modifier.padding(horizontal = 2.dp),
        height = 32.dp,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.user_info_portfolio, firstName),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable {},
                color = DarkGreenGray99,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                fontStyle = FontStyle.Normal,
                style = MaterialTheme.typography.titleMedium
            )
        }
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
fun UserContainerPreview() {
    PhogalTheme {
        Column(
            modifier = Modifier.background(DarkGreen60),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .background(Color.Transparent)
                    .wrapContentHeight(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .heightIn(76.dp, 114.dp)
                    .clickable {},
            ) {
                IconContainer(36.dp) {
                    Box {
                        val painter = loadImagePainter(
                            data = "https://avatars.githubusercontent.com/u/18302717?v=4",
                            size = Size.ORIGINAL
                        )

                        ImageCrossFade(painter = painter, durationMillis = null)
                        Image(
                            painter = painter,
                            contentDescription = "ComposeTest",
                            modifier = Modifier
                                .padding(1.dp)
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(0.5.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                            Alignment.CenterStart,
                            contentScale = ContentScale.Crop
                        )

                        if (painter.state is AsyncImagePainter.State.Loading) {
                            val preloadPainter = loadImagePainter(
                                data = R.drawable.ic_profile_logo,
                                size = Size.ORIGINAL
                            )

                            Image(
                                painter = preloadPainter,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.Center),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .widthIn(186.dp)
                ) {
                    Text(
                        text = "Lukoh",
                        color = Black,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Normal,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${39}${" "}" +
                                "${stringResource(id = R.string.picture_likes)}${" "}${30}${" "}" +
                                "${stringResource(id = R.string.picture_collections)}${" "}",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Normal,
                        color = Black,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${stringResource(id = R.string.user_updated_at)}${" "}${"2023-06-12"}",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Normal,
                        color = Black,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {},
                    modifier = Modifier
                        .widthIn(88.dp)
                        .heightIn(42.dp)
                        .indication(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false)
                        ),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Transparent
                    ),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            modifier = Modifier.size(width = 28.dp, height = 28.dp),
                            imageVector = Icons.Filled.Add,
                            contentDescription = "F",
                            tint = Blue50
                        )
                        Spacer(modifier = Modifier.width(width = 4.dp))
                        Text(
                            text = "F",
                            color = Blue50,
                            fontStyle = FontStyle.Normal,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Text(
                "${stringResource(id = R.string.picture_view_photos)}${" "}${77}${" "}${stringResource(id = R.string.picture_photos, "Lukoh")}",
                modifier = Modifier
                    .padding(start = 56.dp)
                    .clickable {},
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}