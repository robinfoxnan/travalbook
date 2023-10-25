package com.bird2fish.travelbook.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileOutputStream
import com.google.gson.Gson

data class UserInfo(val name: String, val pwd:String, var sid:String)

object UserHelper {
    // 用户保存的文件名，封装起来 , 避免给调用者带来负担，
    // 也避免了写入 / 读入不一致导致的失败
    const val txtfileName = "seeku.txt"

    //注意：用SharedPreferences存储在xml文件不需要后缀名
    const val xmlfileName = "seeku"
    const val jsonfileName = "seeku.json"
    const val hostname = "http://localhost:7817"
    var  uid = ""

    // 保存账号和密码到data.txt文件中
    fun saveUserInfoinTXT(
        context: Context,  // 用户上下文
        number: String,  // 用户的账号
        password: String,
        sid: String
    ): Boolean { // 用户的密码
        return try {
            val fos: FileOutputStream = context.openFileOutput(txtfileName, Context.MODE_PRIVATE)
            // 账号和密码间采用的是【 : 】 , 这就是一个简单的【文件格式】！
            fos.write("$number:$password:$sid".toByteArray())
            fos.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    //从data.txt文件中获取存储的QQ账号和密码
    // 由于获取的是 账号和密码，因此应该用一个数据结构存下来，如 HashMap
    fun getUserInfoFromTXT(context: Context): Map<String, String>? {
        var content = ""
        return try {
            val fis: FileInputStream = context.openFileInput(txtfileName)
            val buffer = ByteArray(fis.available())
            fis.read(buffer)
            content = String(buffer)
            val userMap: MutableMap<String, String> = HashMap()
            // 根据文件格式解析数据内容
            val infos = content.split(":").toTypedArray() // 存入数据结构中
            userMap["name"] = infos[0]
            userMap["pwd"] = infos[1]
            userMap["sid"] = infos[2]
            Log.v("aa", "data.txt    name:  " + infos[0] + "   pwd:   " + infos[1])
            fis.close()
            userMap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 保存账号和密码到data.xml文件中
    fun saveUserInfosinXML(
        context: Context, number: String?,
        password: String?,
        sid: String?
    ): Boolean {
        val sp: SharedPreferences = context.getSharedPreferences(
            xmlfileName,
            Context.MODE_PRIVATE
        )
        val edit = sp.edit()
        edit.putString("name", number)
        edit.putString("pwd", password)
        edit.putString("sid", sid)
        edit.commit()
        return true
    }

    //从data.xml文件中获取存储的QQ账号和密码
    fun getUserInfoFromXML(context: Context): Map<String, String?> {
        val sp: SharedPreferences = context.getSharedPreferences(
            xmlfileName,
            Context.MODE_PRIVATE
        )
        val number = sp.getString("name", null)
        val password = sp.getString("pwd", null)
        var sid = sp.getString("sid", null)
        val userMap: MutableMap<String, String?> = HashMap()
        userMap["name"] = number
        userMap["pwd"] = password
        userMap["sid"] = sid
        Log.v("aa", "data.xml    name:  $number   pwd:   $password")
        return userMap
    }

    // 从data.json文件中取出保存的账号和密码
    //解析json文件返回信息的集合
    fun getUserInfosFromJson(context: Context): Map<String, String>? {
        try {
            val ifs: FileInputStream = context.openFileInput(jsonfileName)
            val buffer = ByteArray(ifs.available())
            ifs.read(buffer)
            val json = String(buffer, charset("UTF-8"))

            //使用gson库解析JSON数据
            val gson = Gson()
            val person: UserInfo = gson.fromJson(json, UserInfo::class.java)
            val map: MutableMap<String, String> = HashMap()
            map["name"] = person.name
            map["pwd"] = person.pwd
            map["sid"] = person.sid

            uid = person.name
            Log.v("aa",
                "data.json    name:  " + person.name
                    .toString() + "   pwd:   " + person.pwd
            )
            return map
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 保存账号和密码到data.json文件中
     * 封装JSON对象-带Key
     * {
     * "name":"liaoyanxia",
     * "pwd":123456,
     * }
     */
    fun saveUserInfosinJSON(
        context: Context,
        number: String?,
        password: String?,
        sid:String?
    ): Boolean {
        try {
            val fos: FileOutputStream = context.openFileOutput(jsonfileName,
                Context.MODE_PRIVATE)
            // json 对象

            val jsonObject = JSONObject()
            jsonObject.put("name", number)
            jsonObject.put("pwd", password)
            jsonObject.put("sid", sid)
            //            JSONArray jsonArray = new JSONArray();
//            jsonArray.put(0, jsonObject);
            fos.write(jsonObject.toString().toByteArray())
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }
}
