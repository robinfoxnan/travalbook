package com.bird2fish.travelbook.ui.tracks

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.*
import com.bird2fish.travelbook.databinding.FragmentMaptrackBinding
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.helper.FileHelper
import com.bird2fish.travelbook.ui.SlideRecyclerView
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import com.google.gson.Gson
import java.util.*

class maptrack : Fragment() {

    companion object {
        fun newInstance() = maptrack()
    }

    private lateinit var viewModel: MaptrackViewModel

    private var _binding: FragmentMaptrackBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: SlideRecyclerView
    private lateinit var trackAdapter: TrackItemAdapter // 请替换为你自己的适配器类

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentMaptrackBinding.inflate(inflater, container, false)
        //return inflater.inflate(R.layout.fragment_maptrack, container, false)
        val root: View = binding.root

        initData()
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MaptrackViewModel::class.java)
        // TODO: Use the ViewModel

    }

    var data :LinkedList<TracklistElement> = LinkedList<TracklistElement>()

    private fun testData() {
        data += TracklistElement(
            "test2",
            title = "",
            "",
            Date(DateTimeHelper.getTimestamp()),
            "2023-12-12",
            "",
            "23min",
            5.6f,
            3,
            "",
            "",
            true
        )
    }


    private fun initData() {

        //testData()

        // 获取列表控件
        this.recyclerView = binding.trackListView
        this.recyclerView.layoutManager = LinearLayoutManager(context)

        if (GlobalData.trackList == null){
            this.trackAdapter = TrackItemAdapter(data)
        }else{
            this.trackAdapter = TrackItemAdapter(GlobalData.trackList!!.tracklistElements)
        }

        this.trackAdapter.setView(this)

        this.recyclerView.adapter = this.trackAdapter


        // 原文链接：https://blog.csdn.net/sinat_38184748/article/details/96422266
    }


    // 点击了条目的星星
    fun onClickItemStar(pos: Int){
        if (GlobalData.trackList!= null){
            val item = GlobalData.trackList!!.tracklistElements[pos]
            item.starred = !item.starred
            GlobalData.saveTrackList(this.requireActivity())
            trackAdapter.notifyDataSetChanged()
        }
    }

    // 删除
    fun onClickItemDelete(pos: Int){
        //UiHelper.showCenterMessage(requireActivity(), "delete")

        this.recyclerView.closeMenu()
        if (GlobalData.trackList!= null){
            GlobalData.removeTrack(this.requireActivity(), pos);
        }

        trackAdapter.notifyDataSetChanged()

    }

    fun loadTrack(item: TracklistElement): Track{
        return FileHelper.readTrack(requireActivity(), item.trackUriString)
    }

    // 点击分享
    fun onClickItemShare(pos: Int){
        this.recyclerView.closeMenu()

        if (GlobalData.usePublish)
        {
            jumpToPublish(pos)
        }else{
            copyToClipboard(pos)
        }

    }

    private fun copyToClipboard(pos: Int){
        val trackList = GlobalData.trackList ?: return

        val item = trackList.tracklistElements[pos]
        val track = loadTrack(item)
        if (track.wayPoints == null || track.wayPoints.size < 1){
            UiHelper.showCenterMessage(requireActivity(), "无法加载轨迹数据，或者无数据点")
            return
        }

        var user = CurrentUser.getUser()
        var data = ShareData(user!!.uid, user!!.nickName)
        data.icon = user!!.icon

        if (item.title != null){
            data.title = item.title
        }else{
            data.title = item.name
        }

        for (p in track.wayPoints){
            val pair = mutableListOf<Double>()
            pair.add(p.latitude)
            pair.add(p.longitude)
            data.points.add(pair)
        }
        val gson = Gson()
        val str = gson.toJson(data)

        UiHelper.copyToClipboard(requireActivity(), str)
        UiHelper.showCenterMessage(requireActivity(), "数据已经拷贝到剪切板")
    }


    private fun getNews(pos: Int, show: Boolean) : News?{
        val user = CurrentUser.getUser()

        val trackList = GlobalData.trackList ?: return null

        val item = trackList.tracklistElements[pos]
        val track = loadTrack(item)
        if (track.wayPoints == null || track.wayPoints.size < 1){
            UiHelper.showCenterMessage(requireActivity(), "无法加载轨迹数据，或者无数据点")
            return null
        }

        var title = ""
        if (item.title != null){
            title = item.title
        }
        var content = ""
        if (item.content != null){
            content  = item.content
        }

        val news =  News("", user!!.uid, user.nickName, user.icon,
            track.wayPoints[0].latitude,
            track.wayPoints[0].longitude,
            track.wayPoints[0].altitude,
            DateTimeHelper.getTimestamp(),
            title,
            content,
            ArrayList<String>(),
            ArrayList<String>(),
            "track",
            item.trackUriString,
            0, 0, false, 0)

        if (show){
            news.type = "localtrack"
        }
        return news
    }

    private fun jumpToPublish(pos: Int){
        val navController = findNavController()

        val news = getNews(pos, false) ?: return

        val bundle = Bundle()
        bundle.putParcelable("news", news)
        navController.navigate(R.id.action_nav_track_to_publishImageNewsFragment, bundle)
    }

    fun onClickItem(pos:Int){
        val navController = findNavController()

        val news = getNews(pos, true) ?: return

        val bundle = Bundle()
        bundle.putParcelable("news", news)
        navController.navigate(R.id.action_nav_track_to_newsMapFragment, bundle)
    }

    // 重新设置备注
    fun onClickMemo(pos: Int){
        this.recyclerView.closeMenu()
        val trackList = GlobalData.trackList ?: return
        val item = trackList.tracklistElements[pos]
        val window = FavEditWindow(this.requireActivity(), R.layout.fav_edit_info, "")
        window.setTrack(item)
        // 设置PopupWindow的结束回调
        window.setOnCloseListener{
            // 在这里添加处理 PopupWindow 关闭时的逻辑
            this.trackAdapter.notifyDataSetChanged()
        }

        window.showPopupWindow()

    }

}