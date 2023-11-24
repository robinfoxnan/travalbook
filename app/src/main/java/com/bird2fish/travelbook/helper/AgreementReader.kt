package com.bird2fish.travelbook.helper

import android.content.res.Resources

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import com.bird2fish.travelbook.R

class AgreementReader(private val resources: Resources) {

    fun readPrivacyPolicy(): String {
        return readFile(R.raw.privacy)
    }

    fun readUserAgreement(): String {
        return readFile(R.raw.user_agreement)
    }

    private fun readFile(resourceId: Int): String {
        val inputStream: InputStream = resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line).append('\n')
        }

        reader.close()
        inputStream.close()

        return stringBuilder.toString()
    }
}