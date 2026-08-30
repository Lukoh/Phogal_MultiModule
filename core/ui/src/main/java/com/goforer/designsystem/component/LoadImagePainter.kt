package com.goforer.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.DataSource
import coil.decode.Decoder
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size

@Composable
fun loadImagePainter(
    data: Any,
    factory: Decoder.Factory? = null,
    size: Size
): AsyncImagePainter {
    return if (factory != null)
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .decoderFactory(factory)
                .size(size)
                .build()
        )
    else
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .size(size)
                .build()
        )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun loadImagePainter(
    data: String,
    factory: Decoder.Factory? = null,
    size: Size
): AsyncImagePainter {
    val context = LocalContext.current

    return if (factory != null) {
        val imageRequest = remember(data, size) {
            ImageRequest.Builder(context)
                .data(data)
                .decoderFactory(factory)
                .size(size)
                .memoryCacheKey(data)
                .diskCacheKey(data)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build()
        }

        rememberAsyncImagePainter(model = imageRequest)
    }
    else {
        val imageRequest = remember(data, size) {
            ImageRequest.Builder(context)
                .data(data)
                .size(size)
                .memoryCacheKey(data)
                .diskCacheKey(data)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build()
        }
        rememberAsyncImagePainter(model = imageRequest)
    }
}

@Composable
fun ImageCrossFade(painter: AsyncImagePainter, durationMillis: Int?) {
    val painterState = painter.state

    if (painterState is AsyncImagePainter.State.Success && painterState.result.dataSource != DataSource.MEMORY_CACHE) {
        if (durationMillis != null && durationMillis > 0)
            painter.imageLoader.newBuilder().crossfade(durationMillis)
        else
            painter.imageLoader.newBuilder().crossfade(true)
    }
}