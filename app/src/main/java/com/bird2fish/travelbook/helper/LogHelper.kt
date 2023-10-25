package com.bird2fish.travelbook.helper

import android.util.Log

// 这个是动态生成的，不是自己写的
import com.bird2fish.travelbook.BuildConfig


/*
 * LogHelper object
 */
object LogHelper {

    private const val TESTING: Boolean = true // set to "false"
    private const val LOG_PREFIX: String = "travelbook_"
    private const val MAX_LOG_TAG_LENGTH: Int = 64
    private const val LOG_PREFIX_LENGTH: Int = LOG_PREFIX.length

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
        val message: String
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

//        if (Log.isLoggable(tag, level)) {
//            val message: String
//            if (t == null && messages != null && messages.size == 1) {
//                // handle this common case without the extra cost of creating a stringbuffer:
//                message = messages[0].toString()
//            } else {
//                val sb = StringBuilder()
//                if (messages != null)
//                    for (m in messages) {
//                        sb.append(m)
//                    }
//                if (t != null) {
//                    sb.append("\n").append(Log.getStackTraceString(t))
//                }
//                message = sb.toString()
//            }
//            Log.println(level, tag, message)
//        }
    }
}