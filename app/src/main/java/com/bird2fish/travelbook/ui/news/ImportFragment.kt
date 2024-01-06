package com.bird2fish.travelbook.ui.news

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.*
import com.bird2fish.travelbook.databinding.FragmentImportBinding
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.helper.FileHelper
import com.bird2fish.travelbook.helper.TrackHelper
import com.google.gson.Gson

class ImportFragment : Fragment() {

    private var _binding:FragmentImportBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentImportBinding.inflate(inflater, container, false )
        //return inflater.inflate(R.layout.fragment_import, container, false)\
        binding.tvNewsImport.setOnClickListener{
            importDataFromclipboard()
        }
        binding.tvNewsSave.setOnClickListener{
            // 保存数据
            saveData()
        }


        // 获取 TextView
        val textView = binding.tvNewsEditDesV

        // 添加 TextWatcher
        textView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 在文本值变化之前执行的操作
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 在文本值变化过程中执行的操作
            }

            override fun afterTextChanged(s: Editable?) {
                // 在文本值变化之后执行的操作
                val newText = s.toString()
                parseData(newText)
            }
        })

        return binding.root
    }

    fun parseData(txt:String){
        val gSon = Gson()
        val shareData = gSon.fromJson(txt, ShareData::class.java)
        if (shareData == null || shareData.points.isEmpty()){
            binding.tvNewsEditTitleV.setText("")
            binding.tvNewsEditInfoV.setText("解析有误")
        }else{
            binding.tvNewsEditTitleV.setText(shareData.title)
            val user = "${shareData.nick}(${shareData.uid}) 分享"
            var t = "数据点"
            var info = ""
            if (shareData.points.size > 1){
                t = "轨迹"
                info = " ${shareData.points.size} 个点"
            }else{
                val p = shareData.points[0]
                if (p.size > 1)
                {
                    val lat = p[0]
                    val lon = p[1]
                    info = "($lat, $lon)"
                }
            }

            binding.tvNewsEditInfoV.setText(user + t + info)
        }
    }


    fun importDataFromclipboard(){
        val str = UiHelper.getClipboardText(requireActivity())
        if (str != null){
            binding.tvNewsEditDesV.setText(str)
        }
    }

    private fun saveData(){
        val gSon = Gson()
        val txt = binding.tvNewsEditDesV.text.toString()
        val shareData = gSon.fromJson(txt, ShareData::class.java)
        if (shareData == null || shareData.points.isEmpty()){
            binding.tvNewsEditTitleV.setText("")
            binding.tvNewsEditInfoV.setText("解析有误")
            return
        }

            if (shareData.points.size == 1){

                val p = shareData.points[0]
                val lat = p[0]
                val lon = p[1]
                val favLoc = FavLocation()
                favLoc.uId = shareData.uid
                favLoc.uNick = shareData.nick
                favLoc.uIcon = shareData.icon
                favLoc.lat = p[0]
                favLoc.lon =p[1]
                favLoc.alt = 0.0
                favLoc.des = ""
                favLoc.title = shareData.title
                favLoc.favId = DateTimeHelper.getTimestamp()
                favLoc.tm = favLoc.favId
                favLoc.tmStr = DateTimeHelper.getTimeStampLongString()

                GlobalData.addFavLocation(requireActivity(), favLoc)

                binding.tvNewsEditDesV.setText("")
                UiHelper.showCenterMessage(requireActivity(), "收藏点导入完毕，请在相关页面查看")

            }else{
                ShareData2Track(shareData)
            }


    }

    private fun ShareData2Track(shareData: ShareData):Track{
        val track = Track()
        for (p in shareData.points){
            val lat = p[0]
            val lon = p[1]
            TrackHelper.addWayPointToTrack(track, lat, lon)
        }
        var title = binding.tvNewsEditTitleV.text.toString()
        if (title == null && title == ""){
            title = shareData.title
        }
        GlobalData.addTrack(requireActivity(), track, title)
        binding.tvNewsEditDesV.setText("")
        UiHelper.showCenterMessage(requireActivity(), "收藏点导入完毕，请在相关页面查看")

        return track
    }


}