package com.goforer.phogal.presentation.ui.compose.screen.home.setting.following

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goforer.designsystem.animation.animateIconScale
import com.goforer.base.extension.toUser
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.ProfileImage
import com.goforer.phogal.data.model.remote.response.gallery.common.Social
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.common.user.UserLinks
import com.goforer.phogal.presentation.stateholder.uistate.home.setting.following.FollowingUserItemUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.setting.following.rememberFollowingUserItemUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.follow.ShowFollowButton
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.ProfileItem
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserInfoItem
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.getProfileInfoItems
import com.goforer.designsystem.theme.Blue50

@Composable
fun FollowingUsersItem(
    modifier: Modifier = Modifier,
    followingUserItemUiState: FollowingUserItemUiState = rememberFollowingUserItemUiState(),
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String?) -> Unit,
    onFollow: (userUiState: User) -> Unit
) {
    if (followingUserItemUiState.user.isEmpty()) return

    val user = followingUserItemUiState.user.toUser()
    val topPadding = if (followingUserItemUiState.index == 0) 16.dp else 8.dp
    val bottomPadding = 8.dp

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding)
            .clickable {
                user.username?.let {
                    onViewPhotos(it, user.firstName, user.lastName ?: "", it)
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (followingUserItemUiState.clicked)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ProfileItem(
                        image = user.profileImage.medium,
                        name = user.name,
                        nameColor = MaterialTheme.colorScheme.onSurface,
                        position = 9,
                        onClicked = {
                            user.username?.let {
                                onViewPhotos(it, user.firstName, user.lastName ?: "", it)
                            }
                        }
                    )
                }

                ShowFollowButton(
                    modifier = modifier,
                    followColor = Blue50,
                    isUserFollowed = followingUserItemUiState.followed
                ) {
                    onFollow(user)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))
            getProfileInfoItems(user).forEach { item ->
                UserInfoItem(
                    text = item.text,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconResId = item.iconResId,
                    position = item.position
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val animationIconScale = animateIconScale(inputScale = 0.6F, position = 1, delay = 150L)

                Image(
                    painter = painterResource(id = R.drawable.ic_portfolio),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = animationIconScale
                            scaleY = animationIconScale
                        }
                )
                Spacer(modifier = Modifier.width(8.dp))

                ShowPortfolioButton(
                    firstName = user.firstName,
                    onOpenWebView = { onOpenWebView(user.firstName, user.portfolioUrl) }
                )
            }
        }
    }
}

@Composable
fun ShowPortfolioButton(
    firstName: String,
    onOpenWebView: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onOpenWebView,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(id = R.string.user_info_portfolio, firstName),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun FollowingUsersItemPreviewContent() {
    val links = UserLinks(
        html = "https://unsplash.com/fr/@an_ku_sh",
        self = "https://api.unsplash.com/users/an_ku_sh",
        photos = "https://api.unsplash.com/photos/LBI7cgq3pbM/download"
    )

    val dummyUser = User(
        acceptedTos = true,
        bio = "Professional Photographer based in India.",
        firstName = "Ankush",
        forHire = true,
        id = "Ebx2G7C0GBo",
        instagramUsername = "An.ku.sh",
        lastName = "Minda",
        links = links,
        location = "India",
        name = "Ankush Minda",
        portfolioUrl = "http://ankushminda.com",
        profileImage = ProfileImage(
            large = "https://images.unsplash.com/profile-1539269396658-18762f46fa72?w=128",
            medium = "https://images.unsplash.com/profile-1539269396658-18762f46fa72?w=64",
            small = "https://images.unsplash.com/profile-1539269396658-18762f46fa72?w=32"
        ),
        social = Social(
            instagramUsername = "An.ku.sh",
            paypalEmail = null,
            portfolioUrl = "http://ankushminda.com",
            twitterUsername = "AnkushMinda"
        ),
        totalCollections = 0,
        totalLikes = 132,
        totalPhotos = 101,
        twitterUsername = "AnkushMinda",
        updatedAt = "2023-06-14T12:23:44Z",
        username = "an_ku_sh"
    )

    val mockState = rememberFollowingUserItemUiState(
        index = remember { mutableIntStateOf(0) }, // index = 0
        user = remember { mutableStateOf(dummyUser.toString()) },
        visibleViewButton = remember { mutableStateOf(true) },
        clicked = remember { mutableStateOf(false) },
        followed = remember { mutableStateOf(true) }
    )

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) + fadeIn(),
                    exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) + fadeOut()
                ) {
                    FollowingUsersItem(
                        followingUserItemUiState = mockState,
                        onViewPhotos = { _, _, _, _ -> },
                        onOpenWebView = { _, _ -> },
                        onFollow = {}
                    )
                }
            }
        }
    }
}

@Preview(
    name = "1. Light Mode - User Card",
    showBackground = true,
    device = "spec:width=360dp,height=300dp,dpi=420"
)
@Composable
fun FollowingUsersItemLightPreview() {
    FollowingUsersItemPreviewContent()
}

@Preview(
    name = "2. Dark Mode - User Card",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:width=360dp,height=300dp,dpi=420"
)
@Composable
fun FollowingUsersItemDarkPreview() {
    FollowingUsersItemPreviewContent()
}