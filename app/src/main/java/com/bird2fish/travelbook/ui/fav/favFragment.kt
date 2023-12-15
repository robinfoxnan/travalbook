package com.bird2fish.travelbook.ui.fav

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.FavLocation
import com.bird2fish.travelbook.core.TracklistElement
import com.bird2fish.travelbook.databinding.FragmentFavBinding
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.ui.SlideRecyclerView
import com.bird2fish.travelbook.ui.tracks.TrackItemAdapter
import java.util.*

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
    private fun initData(){

        data += FavLocation(1, "1001", "飞鸟", "sys:1", 39.0, 116.0, 12.0, System.currentTimeMillis(), "2023-12-12", "颐和园", "发现一颗好看的树, 在下雪的日子里，显得格外的好看，")
        data += FavLocation(1, "1001", "小鱼儿", "sys:2", 39.0, 116.0, 12.0, System.currentTimeMillis(), "2023-12-12", "故宫", "落日")


        this.favAdapter = FavItemAdapter(data)
        this.favAdapter.setView(this)

        this.recyclerView = binding.favListView
        this.recyclerView.layoutManager = LinearLayoutManager(context)
        // 第三步：给listview设置适配器（view）
        this.recyclerView.setAdapter(this.favAdapter);
    }

    // 删除
    fun onClickItemDelete(pos: Int){
        //UiHelper.showCenterMessage(requireActivity(), "delete")

        this.recyclerView.closeMenu()
        this.data.removeAt(pos)
        this.favAdapter.notifyDataSetChanged()

    }

}