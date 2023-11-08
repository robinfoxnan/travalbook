package com.bird2fish.travelbook.ui.contact

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.databinding.FragmentContractBinding
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.ui.login.afterTextChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList
import android.view.inputmethod.EditorInfo


class ContactFragment : Fragment() {

    private var _binding: FragmentContractBinding? = null

    private lateinit var btn: ImageButton
    private lateinit var edit:EditText
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var _changed :MutableLiveData<Long> = MutableLiveData(DateTimeHelper.getTimestamp())
    var changed : LiveData<Long> =  _changed


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val galleryViewModel =
//            ViewModelProvider(this).get(ContractViewModel::class.java)

        _binding = FragmentContractBinding.inflate(inflater, container, false)
        val root: View = binding.root
        btn = binding.btnSearchFriend
        edit = binding.textSearchById

//        val textView: TextView = binding.textGallery
//        galleryViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        initFriend()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 在后台线程或协程中加载数据
        performGetTask()
    }


    // 获取关注列表
    fun performGetTask() {
        CoroutineScope(Dispatchers.IO).launch {
            // 在后台执行异步任务
            val result = doBackgroundGetWork()
            GlobalData.setFollowers(1, result)
            _changed.postValue(DateTimeHelper.getTimestamp())
        }
    }

    // 后台获取列表协程函数
    private suspend fun doBackgroundGetWork(): LinkedList<Friend> {
        // 模拟异步任务，这里可以执行任何需要后台处理的操作
        return GlobalData.getHttpServ().getFollowList()
    }



    // 执行查询
    fun performSearchTask() {
        var id = edit.text.toString()
        CoroutineScope(Dispatchers.IO).launch {
            // 在后台执行异步任务
            val result = doBackgroundSearchWork(id)
            GlobalData.setFollowers(0, result)
            _changed.postValue(DateTimeHelper.getTimestamp())
        }
    }



    private suspend fun doBackgroundSearchWork(id: String): LinkedList<Friend> {
        // 模拟异步任务，这里可以执行任何需要后台处理的操作
        return GlobalData.getHttpServ().searchForFriend(id)
    }

    private fun initFriend(){

        // 获取列表控件
        var lv = binding.listviewContact

//        data += Friend("robin", "icon1", "13810501000", true)
//        data += Friend("robin1", "icon2", "13810502000", false)

        val adapter = ContactItemAdapter(getActivity(), GlobalData.friendList)
        adapter.setView(this)
        // 第三步：给listview设置适配器（view）
        lv.setAdapter(adapter);

        this.changed.observe(requireActivity(), Observer {
            adapter.notifyDataSetChanged()

        })

        // 点击查询
        btn.setOnClickListener {

            performSearchTask()
            //UiHelper.showMessage(requireActivity(), edit.text.toString() )
        }

        // 没有字符串时候显示关注列表，
        edit.afterTextChanged{
            val str = edit.text.toString()
            if (str == null || str.isEmpty()){
                GlobalData.setFollowerUseType(1)
                adapter.notifyDataSetChanged()
            }else{
                GlobalData.setFollowerUseType(0)
                adapter.notifyDataSetChanged()
            }
        }

        edit.setOnEditorActionListener { v, actionId, event ->
            if (actionId == KeyEvent.KEYCODE_SEARCH || actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 在这里处理用户的操作
                performSearchTask()
                return@setOnEditorActionListener true  // 消费事件
            }
            false  // 返回false表示不消费事件
        }


//        val listView = binding.listviewContact
//        listView.setOnItemClickListener { parent, view, position, id ->
//
//                val selectedItem = listView.adapter.getItem(position) as String
//                // 例如，显示一个Toast通知
//                UiHelper.showCenterMessage(requireActivity(), "Clicked item: $selectedItem")
//            }

    }

    // 设置关注某个好友，关注成功则需要添加到好友列表，取消关注则需要从列表中移除
    public fun onFollowFriend(friend: Friend){
        UiHelper.showCenterMessage(requireActivity(), "点了关注按钮")
        var index = GlobalData.getFollowerUseType()
        if (friend.isFriend){
            CoroutineScope(Dispatchers.IO).launch {
                addFriend(friend)
            }
        }
        else  // 移除好友
        {
                CoroutineScope(Dispatchers.IO).launch {
                    removeFriend(friend)
                }
        }
    }

    suspend fun  addFriend(friend: Friend){
        val ret = GlobalData.httpServer.setFollowHim(friend)
        if (ret){
            GlobalData.addFollowrs(friend)
        }
        _changed.postValue(DateTimeHelper.getTimestamp())
    }

    suspend fun  removeFriend(friend: Friend){
        if (GlobalData.httpServer.removeFriend(friend))
        {
            GlobalData.removeFollowers(friend)
        }

        _changed.postValue(DateTimeHelper.getTimestamp())

    }

    // 设置是有显示某个好友位置，保存到远端
    public fun onShowFriend(friend: Friend){
        //UiHelper.showMessage(requireActivity(), "点了关显示按钮")
        CoroutineScope(Dispatchers.IO).launch {
            GlobalData.httpServer.setFollowUserInfo(friend)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}