package com.bird2fish.travelbook.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.HttpService
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.databinding.FragmentPersonalInfoBinding
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.helper.PreferencesHelper
import com.bird2fish.travelbook.ui.data.Result
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import com.bird2fish.travelbook.ui.data.model.LoggedInUser
import com.bird2fish.travelbook.ui.login.LoggedInUserView
import com.bird2fish.travelbook.ui.login.LoginResult
import androidx.appcompat.app.AppCompatActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PersonalInfo.newInstance] factory method to
 * create an instance of this fragment.
 */
class PersonalInfo : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private var _changed : MutableLiveData<Boolean> = MutableLiveData(false)
    var changed : LiveData<Boolean> =  _changed
    private var inited :Boolean = false

    private var  icon :String  = ""

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }

    override fun onResume() {
        super.onResume()
//        val iconId = UiHelper.getIconResId(this.icon)
//        //binding.imageViewIcon.setImageResource(iconId)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_personal_info, container, false)

        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)

        val user = CurrentUser.getUser()
        binding.tvName.setText("${user!!.userId} (${user!!.uid}) ")
        binding.tvNick.setText(user!!.nickName)
        binding.tvAge.setText(user!!.age)
        binding.tvGender.setText(user!!.gender)
        binding.tvPass.setText(user!!.pwd)
        binding.tvPhone.setText(user!!.phone)
        binding.tvEmail.setText(user!!.email)
        this.icon = user.icon


        // 保存基本信息
        binding.btnSave.setOnClickListener{
            saveInfo()
        }

        // 验证手机
        binding.btnCheckPhone.setOnClickListener{
            UiHelper.showCenterMessage(requireActivity(), "后端暂时未能发送验证码")
        }
        binding.btnCheckPhone.visibility = View.INVISIBLE

        // 验证邮件
        binding.btnCheckEmail.setOnClickListener{
            UiHelper.showCenterMessage(requireActivity(), "后端暂时未能发送验证码")
        }

        binding.btnCheckEmail.visibility = View.INVISIBLE

        // 检查验证码
        binding.btnSendCode.setOnClickListener{

        }

        binding.btnSendCode.visibility = View.INVISIBLE
        binding.tvCode.visibility = View.INVISIBLE
        binding.titleTvCode.visibility = View.INVISIBLE
        binding.iconSpinner.visibility = View.INVISIBLE

        // 结果
        this.changed.observe(requireActivity(), Observer {

           if (inited){
               if (changed.value == true){
                   UiHelper.showCenterMessage(requireActivity(), "更新成功")
               }else{
                   UiHelper.showCenterMessage(requireActivity(), "更新失败")
               }
               inited = false
           }

        })

        // 设置图标按钮点击事件
        binding.imageViewIcon.setOnClickListener {
            // 显示或隐藏Spinner下拉框
            //binding.iconSpinner.visibility  = View.VISIBLE
            binding.iconSpinner.performClick()
        }


        // 声明一个映射对象的队列，用于保存行星的图标与名称配对信息
        val list: MutableList<Map<String, Any>> = ArrayList()
        // iconArray是行星的图标数组，starArray是行星的名称数组
        for (i in UiHelper.iconIds.indices) {
            val item: MutableMap<String, Any> = HashMap()
            item["icon"] = UiHelper.iconIds[i]
            item["name"] = (i+1).toString()
            // 把一个行星图标与名称的配对映射添加到队列当中
            list.add(item)
        }
        // 声明一个下拉列表的简单适配器，其中指定了图标与文本两组数据
        val starAdapter = SimpleAdapter(
            requireActivity(),
            list,
            R.layout.spinner_drop_down,
            arrayOf("icon", "name"),
            intArrayOf(R.id.iv_icon, R.id.tv_name)
        )
        // 设置简单适配器的布局样式
        starAdapter.setDropDownViewResource(R.layout.spinner_drop_down)

        // 设置下拉框的标题
        binding.iconSpinner.prompt = "请选择图标"
        // 设置下拉框的简单适配器
        binding.iconSpinner.adapter = starAdapter
        binding.iconSpinner.setSelection(UiHelper.getIconResIndex(this.icon))


//        val iconArray = UiHelper.iconIds.toList()
//        val adapter  = IconAdapter(requireActivity(), R.layout.spinner_drop_down, iconArray)
//        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
//        binding.iconSpinner.adapter = adapter

        // 设置 Spinner 的选择监听器
        binding.iconSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 用户选择了某一项的回调函数
                val selectedIconId = UiHelper.iconIds[position]
                // 在这里可以执行相应的操作，例如更新界面或执行其他逻辑
                val index = position + 1
                this@PersonalInfo.icon = "sys:" + index.toString()

                val iconId = UiHelper.getIconResId(this@PersonalInfo.icon)
                binding.imageViewIcon.setImageResource(iconId)
                //binding.imageViewIcon.setImageResource(selectedIconId)

                binding.iconSpinner.visibility  = View.INVISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.iconSpinner.visibility  = View.INVISIBLE
            }
        }

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PersonalInfo.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PersonalInfo().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    // 保存昵称等基本信息
    fun saveInfo(){

        var user = LoggedInUser()
        val info = PreferencesHelper.getUserInfo()
        user.uid = info!!.uid
        user.sid = info!!.sid
        user.pwd = binding.tvPass.text.toString()
        user.userId = CurrentUser.getUser()!!.userId

        user.icon = this.icon
        user.nickName = binding.tvNick.text.toString()
        user.age =      binding.tvAge.text.toString()
        user.gender =   binding.tvGender.text.toString()
        user.phone =    binding.tvPhone.text.toString()
        user.email =    binding.tvEmail.text.toString()

        inited  = true
        object : Thread() {
            override fun run() {
                //网络操作连接的代码
                val server = HttpService()
                server.initServer()
                val ret = server.updateInfo(user)
                _changed.postValue(ret)

                if (ret){
                    PreferencesHelper.saveUserInfo(user.userId, user.pwd, user.uid, user.sid)
                    var u = CurrentUser.getUser()
                    u!!.isChanged = true
                    u!!.icon = user.icon
                    u!!.nickName = user.nickName
                    u!!.age = user.age
                    u!!.gender = user.gender
                    u!!.phone = user.phone
                    u!!.email = user.email
                }


            }
        }.start()
    }
}