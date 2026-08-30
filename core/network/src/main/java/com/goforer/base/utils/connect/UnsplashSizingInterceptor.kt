package com.goforer.base.utils.connect

import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.size.Dimension
import okhttp3.HttpUrl.Companion.toHttpUrl

object UnsplashSizingInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val data = chain.request.data
        val size = chain.size
        if (data is String &&
            data.startsWith("https://images.unsplash.com/photo-")
        ) {
            val width = size.width
            val height = size.height
            
            // Only add sizing parameters if we have valid pixel dimensions.
            // Avoid adding them if using Size.ORIGINAL (Dimension.Undefined).
            if (width is Dimension.Pixels && height is Dimension.Pixels) {
                val url = data.toHttpUrl()
                    .newBuilder()
                    .setQueryParameter("w", width.px.toString())
                    .setQueryParameter("h", height.px.toString())
                    .build()
                val request = chain.request.newBuilder().data(url.toString()).build()
                return chain.proceed(request)
            }
        }
        return chain.proceed(chain.request)
    }
}
