package org.mifos.mobile.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class SelfServiceOkHttpClient(private val tenant: String?, private val authToken: String?) {
    // Create a trust manager that does not validate certificate chains
    val mifosOkHttpClient: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()

            //Enable Full Body Logging
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY

            //Setting Timeout 30 Seconds
            builder.connectTimeout(60, TimeUnit.SECONDS)
            builder.readTimeout(60, TimeUnit.SECONDS)

            //Interceptor :> Full Body Logger and ApiRequest Header
            builder.addInterceptor(logger)
            builder.addInterceptor(SelfServiceInterceptor(tenant, authToken))
            return builder.build()
        }
}