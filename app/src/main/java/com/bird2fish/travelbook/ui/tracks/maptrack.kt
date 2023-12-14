package com.bird2fish.travelbook.ui.tracks

import android.graphics.Canvas
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.TracklistElement
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.databinding.FragmentContractBinding
import com.bird2fish.travelbook.databinding.FragmentMaptrackBinding
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.ui.SlideRecyclerView
import com.bird2fish.travelbook.ui.contact.ContactItemAdapter
import java.lang.Integer.min
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

    private fun testData(){
        data += TracklistElement(
            "test1",
            Date(DateTimeHelper.getTimestamp()),
            "2023-12-12",
            "23min",
            5.6f,
            "",
            "",
            false
        )
        data += TracklistElement(
            "test2",
            Date(DateTimeHelper.getTimestamp()),
            "2023-12-12",
            "23min",
            5.6f,
            "",
            "",
            true
        )

        data += TracklistElement(
            "test3",
            Date(DateTimeHelper.getTimestamp()),
            "2023-12-12",
            "23min",
            5.6f,
            "",
            "",
            true
        )

        data += TracklistElement(
            "test4",
            Date(DateTimeHelper.getTimestamp()),
            "2023-12-12",
            "23min",
            5.6f,
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
            trackAdapter.notifyDataSetChanged()
        }
    }

    // 删除
    fun onClickItemDelete(pos: Int){
        //UiHelper.showCenterMessage(requireActivity(), "delete")

        this.recyclerView.closeMenu()
        if (GlobalData.trackList!= null){
            //this.data.removeAt(pos)
        }

        trackAdapter.notifyDataSetChanged()

    }

}