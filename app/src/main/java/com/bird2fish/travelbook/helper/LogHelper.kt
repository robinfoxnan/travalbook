package com.bird2fish.travelbook.helper

import android.os.Environment
import android.util.Log

// 这个是动态生成的，不是自己写的
import com.bird2fish.travelbook.BuildConfig
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/*
 * LogHelper object
 */
object LogHelper {

    private const val TESTING: Boolean = true // set to "false"
    private const val LOG_DIR: String = "travel_log"
    private const val MAX_LOG_TAG_LENGTH: Int = 64
    private const val LOG_PREFIX = "TRAVAL"
    private const val LOG_PREFIX_LENGTH: Int = LOG_PREFIX.length

    private const val LOG_TAG = "MyAppLog"
    private const val LOG_FILE_NAME = "travellog.txt"
    private var logDir = ""

    fun setLogDir(dir :String){
        logDir = dir
    }
    private fun log(message: String) {
        try {
            val logFile = getLogFile()

            // 格式化当前时间
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // 写入日志到文件
            FileWriter(logFile, true).use { writer ->
                writer.append("$currentTime - $LOG_TAG: $message\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getLogFile(): File {
        val logDir = File(logDir, LOG_DIR)
        // 创建日志目录
        try {
            if (!logDir.exists()) {
                val ret = logDir.mkdirs()
                System.out.println(ret)
            }
        }
        catch (e: IOException) {
            e.printStackTrace()
        }

        return File(logDir, LOG_FILE_NAME)
    }

    fun makeLogTag(str: String): String {
        return if (str.length > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1)
        } else LOG_PREFIX + str
    }

    fun makeLogTag(cls: Class<*>): String {
        // don't use this when obfuscating class names
        return makeLogTag(cls.simpleName)
    }

    fun v(tag: String, vararg messages: Any) {
        // Only log VERBOSE if build type is DEBUG or if TESTING is true
        if (BuildConfig.DEBUG || TESTING) {
            log(tag, Log.VERBOSE, null, *messages)
        }
    }

    fun d(tag: String, vararg messages: Any) {
        // Only log DEBUG if build type is DEBUG or if TESTING is true
        if (BuildConfig.DEBUG || TESTING) {
            log(tag, Log.DEBUG, null, *messages)
        }
    }

    fun i(tag: String, vararg messages: Any) {
        log(tag, Log.INFO, null, *messages)
    }

    fun w(tag: String, vararg messages: Any) {
        log(tag, Log.WARN, null, *messages)
    }

    fun w(tag: String, t: Throwable, vararg messages: Any) {
        log(tag, Log.WARN, t, *messages)
    }

    fun e(tag: String, vararg messages: Any) {
        log(tag, Log.ERROR, null, *messages)
    }

    fun e(tag: String, t: Throwable, vararg messages: Any) {
        log(tag, Log.ERROR, t, *messages)
    }

    private fun log(tag: String, level: Int, t: Throwable?, vararg messages: Any) {
        var message: String
        if (t == null && messages.size == 1) {
            // handle this common case without the extra cost of creating a stringbuffer:
            message = messages[0].toString()
        } else {
            val sb = StringBuilder()
            for (m in messages) {
                sb.append(m)
            }
            if (t != null) {
                sb.append("\n").append(Log.getStackTraceString(t))
            }
            message = sb.toString()
        }
        Log.println(level, tag, message)
        log(message)

    }
}