package com.bird2fish.travelbook.ui.publish

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.*
import com.bird2fish.travelbook.databinding.FragmentPublishImageNewsBinding
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class PublishImageNewsFragment : Fragment() {
    private var _binding: FragmentPublishImageNewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private var newsContent :News? =  null

    private var _changed : MutableLiveData<Long> = MutableLiveData(DateTimeHelper.getTimestamp())
    var changed : LiveData<Long> =  _changed

    private var _result : MutableLiveData<Int> = MutableLiveData(0)
    var result : LiveData<Int> =  _result

    var homeAdapter: GalleryAdapter? = null
    var dataList = ArrayList<ImagePathPair>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this._binding = FragmentPublishImageNewsBinding.inflate(inflater, container, false)
        val root = binding.root

        initContent()

        this.changed.observe(requireActivity(), Observer {
            homeAdapter!!.notifyDataSetChanged()
        })

        this.result.observe(requireActivity(), Observer {

            if (result.value!! == 1) {
                UiHelper.showCenterMessage(this.requireActivity(), "发布成功")
                val navController = findNavController()
                navController.navigateUp()
            } else if (result.value!! == -1){
                UiHelper.showCenterMessage(this.requireActivity(), "发布失败，请重试")
            }
        })

        return root
    }





    fun initContent(){
        dataList.add(ImagePathPair("add", "add", ""))

        this.homeAdapter = GalleryAdapter(dataList) //创建适配器对象
        this.homeAdapter!!.setView(this)



        this.recyclerView = binding.imgContainer
        recyclerView.adapter = this.homeAdapter

       // val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // 设置 GridLayoutManager，参数表示列数
        // 设置 GridLayoutManager，参数表示列数
        val spanCount = 3 // 三列，即九宫格

        val layoutManager = GridLayoutManager(context, spanCount)


        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(GalleryAdapter.space_item(5))

        binding.tvNewsPublish.setOnClickListener{
            publish()
        }

        binding.tvNewsCancel.setOnClickListener{
            val navController = findNavController()
            navController.navigateUp()
        }

        binding.tvNewsEditTitleV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 在文本变化之前执行的操作
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 在文本变化过程中执行的操作
                val newText = s?.toString() ?: ""
                // 处理新文本

            }

            override fun afterTextChanged(s: Editable?) {
                // 在文本变化之后执行的操作
                if (newsContent != null){
                    newsContent!!.title = s.toString()
                }
            }
        })

        binding.tvNewsEditDesV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 在文本变化之前执行的操作
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 在文本变化过程中执行的操作
                val newText = s?.toString() ?: ""
                // 处理新文本

            }

            override fun afterTextChanged(s: Editable?) {
                // 在文本变化之后执行的操作
                if (newsContent != null){
                    newsContent!!.content = s.toString()
                }
            }
        })

    }

    fun updateData(){
        if (newsContent == null)
            return
        binding.tvNewsEditTitleV.setText(newsContent!!.title)
        binding.tvNewsEditDesV.setText(newsContent!!.content)
    }

    // 提取数据
    override fun onResume() {
        super.onResume()
        // 获取传递的 Intent
        val arg = getArguments()
        if (arg != null) {

            val obj = arg!!.getParcelable<News>("news")
            newsContent = obj
            //UiHelper.showCenterMessage(requireActivity(), str!!)
            updateData()
        }

    }

    fun onClickItemAdd(position: Int){
        getContent.launch("image/*");
    }


    private val getContent = registerForActivityResult<String, Uri>(
        ActivityResultContracts.GetContent()
    ) { result: Uri? ->
        if (result != null) {
            // 处理选择的图片
            val actualUri: Uri? = getActualPath(result)
            if (actualUri != null){
                var path = actualUri.toString()
                System.out.println(path)
                val index = this.dataList.size - 1
                this.dataList.add(index, ImagePathPair(path!!, "", ""))
                this.homeAdapter!!.notifyDataSetChanged()
            }

        }
    }

    private fun getActualPath(uri: Uri): Uri? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val documentId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority) {
                val id = documentId.split(":").toTypedArray()[1]
                val selection = MediaStore.Images.Media._ID + "=" + id
                return getImageContentUri(id.toLong())
            }
        }
        return uri
    }

    private fun getImageContentUri(imageId: Long): Uri? {
        val contentResolver: ContentResolver = requireActivity().getContentResolver()
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
            .appendPath(imageId.toString()).build()
    }

    // 显示marker的 PopupWindow 的方法
    fun showPopupMenu(position: Int) {
        // 创建布局
        val popupView: View = layoutInflater.inflate(R.layout.popup_menu, null)

        // 创建 PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 设置菜单项点击事件
        val deleteTextView = popupView.findViewById<TextView>(R.id.tv_delete_mark)
        deleteTextView.setOnClickListener {
            popupWindow.dismiss() // 关闭 菜单
            // 在这里执行删除标记的操作
            this.dataList.removeAt(position)
        }

        val editTextView = popupView.findViewById<TextView>(R.id.tv_mark_info)
        editTextView.setOnClickListener {
            popupWindow.dismiss() // 关闭 菜单

        }

        // 显示 PopupWindow
        // 显示 PopupWindow 在标记位置上方

// 现在，screenLocation 包含了 Marker 在屏幕上的坐标
        val screenX = 0
        val screenY = 0
        val offsetX = 50
        val offsetY = 50

        popupWindow.showAtLocation(
            recyclerView,
            Gravity.NO_GRAVITY,
            screenX + offsetX,
            screenY + offsetY
        )
    }

    // 开始发布数据
    fun publish(){
        if (newsContent == null)
            return

        newsContent!!.title = binding.tvNewsEditTitleV.text.toString()
        newsContent!!.content = binding.tvNewsEditDesV.text.toString()
        //newsContent!!.tags =

        GlobalScope.launch(Dispatchers.IO) {
            doSendWork()
        }
    }

    fun getRealPathFromUri(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun doSendWork(){
        for (i in 0 until dataList.size-1){
            if ( dataList[i].remoteName != ""){
                continue
            }

            val uri = getRealPathFromUri(requireActivity(), dataList[i].localPath.toUri())
            System.out.println(uri)
            if (uri == null){
                return
            }

            dataList[i].state = "uploading"
            _changed.postValue(DateTimeHelper.getTimeStamp())

            val newFilename = GlobalData.getHttpServ().uploadAndScaleImageFile(uri)

            if (newFilename != ""){
                dataList[i].state = "ok"
                dataList[i].remoteName = newFilename
            }else{
                dataList[i].state = "error"
            }
            _changed.postValue(DateTimeHelper.getTimeStamp())
        }

        for (i in 0 until dataList.size-1){
            val list = kotlin.collections.ArrayList<String>()
            list.add(dataList[i].remoteName)
            this.newsContent!!.images = list
        }

        // 尝试上传轨迹文件
        if (newsContent!!.trackFile != "") {
            if (newsContent!!.trackFile.indexOf('/') != -1){
                val tmpFilename = GlobalData.getHttpServ().uploadTextFile(newsContent!!.trackFile)
                if (tmpFilename != ""){
                    newsContent!!.trackFile = tmpFilename
                }else{
                    this._result.postValue(-1)
                    return
                }
            }
        }



        val user = CurrentUser.getUser()
        val ret = GlobalData.getHttpServ().postNews(newsContent!!, user!!.uid, user!!.sid)
        if (ret){
            this._result.postValue(1)
        }else {
            this._result.postValue(-1)
        }

    }

}