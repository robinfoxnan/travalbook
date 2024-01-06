package com.bird2fish.travelbook.ui.fav

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.*
import com.bird2fish.travelbook.databinding.FragmentFavBinding
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.ui.SlideRecyclerView
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import com.bird2fish.travelbook.ui.tracks.TrackItemAdapter
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

class favFragment : Fragment() {

    companion object {
        fun newInstance() = favFragment()
    }

    private lateinit var viewModel: FavViewModel

    private var _binding : FragmentFavBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: SlideRecyclerView
    private lateinit var favAdapter: FavItemAdapter // 请替换为你自己的适配器类

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavBinding.inflate(inflater, container, false)
        //return inflater.inflate(R.layout.fragment_fav, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FavViewModel::class.java)
        // TODO: Use the ViewModel
        initData()
    }

    var data :LinkedList<FavLocation> = LinkedList<FavLocation>()

    private  fun createTestData(){

        data += FavLocation(1, "1001", "飞鸟", "sys:1", 39.0, 116.0, 12.0, System.currentTimeMillis(), "2023-12-12", "颐和园", "发现一颗好看的树, 在下雪的日子里，显得格外的好看，")
        data += FavLocation(1, "1001", "小鱼儿", "sys:2", 39.0, 116.0, 12.0, System.currentTimeMillis(), "2023-12-12", "故宫", "落日")

    }

    private fun initData(){

        this.favAdapter = FavItemAdapter(GlobalData.getLocations())
        this.favAdapter.setView(this)

        this.recyclerView = binding.favListView
        this.recyclerView.layoutManager = LinearLayoutManager(context)
        // 第三步：给listview设置适配器（view）
        this.recyclerView.setAdapter(this.favAdapter);
    }

    // 点击了整行
    fun onClickItem(pos:Int){
        val favDataList = GlobalData.getLocations()
        val item = favDataList[pos]

        val navController = findNavController()
        val bundle = Bundle()
        val user = CurrentUser.getUser()
        val news =  News("", user!!.uid, user.nickName, user.icon,
            item.lat,
            item.lon,
            item.alt,
            DateTimeHelper.getTimestamp(),
            item.title,
            item.des,
            ArrayList<String>(),
            ArrayList<String>(),
            "point",
            "", 0, 0, false, 0)

        bundle.putParcelable("news", news)
        navController.navigate(R.id.action_nav_favourite_to_newsMapFragment, bundle)
    }

    // 分享共享点位置
    fun onClickItemShare(pos: Int){
        this.recyclerView.closeMenu()

        this.favAdapter.notifyDataSetChanged()
        if (GlobalData.usePublish)
        {
            jumpToPublish(pos)
        }else{
            val favDataList = GlobalData.getLocations()
            val item = favDataList[pos]
           val str = Fav2ShareData(item)
            UiHelper.copyToClipboard(requireActivity(), str)
            UiHelper.showCenterMessage(requireActivity(), "数据已经拷贝到剪切板")
        }
    }

    private fun Fav2ShareData(item: FavLocation): String {
        var data = ShareData(item.uId, item.uNick)
//        val pair: Pair<Double, Double> = Pair(item.lat, item.lon)
//        data.points.add(pair)

        val pair = mutableListOf<Double>()
        pair.add(item.lat)
        pair.add(item.lon)

        data.points.add(pair)
        data.title = item.title
        data.icon = item.uIcon
        val gson = Gson()
        val str = gson.toJson(data)
        return str
    }

    private fun jumpToPublish(pos: Int){
        val navController = findNavController()
        val bundle = Bundle()
        val user = CurrentUser.getUser()

        val favDataList = GlobalData.getLocations()
        val item = favDataList[pos]
        val news =  News("", user!!.uid, user.nickName, user.icon,
            item.lat,
            item.lon,
            item.alt,
            DateTimeHelper.getTimestamp(),
            item.title,
            item.des,
            ArrayList<String>(),
            ArrayList<String>(),
            "point",
            "", 0, 0, false, 0)
        bundle.putParcelable("news", news)
        navController.navigate(R.id.action_nav_favourite_to_publishImageNewsFragment, bundle)
    }


}