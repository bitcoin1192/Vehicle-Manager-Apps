package com.sisalma.vehicleandusermanagement.model.API

import okhttp3.Interceptor
import okhttp3.Response

class InternetCheck: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val addedHeader = originRequest.newBuilder()
            .addHeader("Cookies","abc")
            .build()
        return chain.proceed(addedHeader)
    }
}