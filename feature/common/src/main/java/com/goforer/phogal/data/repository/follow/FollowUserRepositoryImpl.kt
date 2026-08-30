package com.goforer.phogal.data.repository.follow

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.goforer.phogal.data.datasource.local.LocalDataSource
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.repository.follow.paging.FollowUserPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowUserRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : FollowUserRepository {
    override fun getFollowingUsers(): Flow<List<User>> = localDataSource.followingUsersFlow

    override fun followedUsers(followedUsers: List<User>, pageSize: Int): Flow<PagingData<User>> {
        val followUsersPagingFlow = Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize * 2,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { FollowUserPagingSource(followedUsers) }
        ).flow

        return followUsersPagingFlow
    }

    override suspend fun toggleFollowingUser(user: User) = localDataSource.toggleFollowingUser(user)

    override fun isUserFollowedFlow(user: User): Flow<Boolean> = localDataSource.isUserFollowedFlow(user)
}