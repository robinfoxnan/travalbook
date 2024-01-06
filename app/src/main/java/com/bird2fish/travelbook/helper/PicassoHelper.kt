package com.bird2fish.travelbook.helper

import android.content.Context
import com.bird2fish.travelbook.core.HttpsUtil
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

object PicassoHelper {
    private var sPicasso: Picasso? = null
    private val password = "your_keystore_password" // 替换为你的密钥库密码

    fun getInstance(context: Context): Picasso {
        if (sPicasso == null) {

            // 创建 Picasso.Builder 实例
            val builder = Picasso.Builder(context)

            try {
                // 初始化 TLS 协议的 SSL 上下文
                val okHttpClient = HttpsUtil.getClient(context)

                // 创建 OkHttpDownloader，并将其设置为 Picasso 的下载器
                val okHttpDownloader = OkHttp3Downloader(okHttpClient)
                builder.downloader(okHttpDownloader)

                // 构建最终的 Picasso 实例
                sPicasso = builder.build()
            } catch (e: NoSuchAlgorithmException) {
                // 处理异常，通常是因为 TLS 不可用
                throw IllegalStateException("初始化默认 SSL 上下文失败", e)
            } catch (e: KeyManagementException) {
                // 处理异常，通常是因为密钥管理问题
                throw IllegalStateException("初始化默认 SSL 上下文失败", e)
            } catch (e: GeneralSecurityException) {
                // 处理其他安全性异常
                e.printStackTrace()
            }
        }

        return sPicasso!!
    }
}
