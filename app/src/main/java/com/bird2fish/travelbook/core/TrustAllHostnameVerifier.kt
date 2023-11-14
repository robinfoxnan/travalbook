package com.bird2fish.travelbook.core

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class TrustAllHostnameVerifier : HostnameVerifier {
    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        return true
    }
}
