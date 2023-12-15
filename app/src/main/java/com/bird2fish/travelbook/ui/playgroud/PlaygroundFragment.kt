package com.bird2fish.travelbook.ui.playgroud

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bird2fish.travelbook.core.News
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.ui.playgroud.NewsCoverAdapter.space_item
import java.util.LinkedList
import com.bird2fish.travelbook.databinding.FragmentPlaygroundBinding

class PlaygroundFragment : Fragment() {

    private var _binding: FragmentPlaygroundBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(PlaygroundViewModel::class.java)

        _binding = FragmentPlaygroundBinding.inflate(inflater, container, false)
        val root: View = binding.root

        testData()
        initContent()

        return root
    }

    val datalist = LinkedList<News>()

    private  fun testData(){
        datalist += News(
            com.bird2fish.travelbook.R.drawable.news1, "1001", "飞鸟真人", "sys:5", DateTimeHelper.getTimestamp(),
        DateTimeHelper.getTimeStampString(),
        "颐和园的雪花是最好的", "")

        datalist += News(
            com.bird2fish.travelbook.R.drawable.news2, "1002", "天涯刀客", "sys:6", DateTimeHelper.getTimestamp(),
            DateTimeHelper.getTimeStampString(),
            "颐和园的雪花是最好的", "")

        datalist += News(
            com.bird2fish.travelbook.R.drawable.news3, "1002", "梦里化开", "sys:7", DateTimeHelper.getTimestamp(),
            DateTimeHelper.getTimeStampString(),
            "颐和园的雪花是最好的", "")

        datalist += News(
            com.bird2fish.travelbook.R.drawable.news4, "1002", "梦里化开", "sys:7", DateTimeHelper.getTimestamp(),
            DateTimeHelper.getTimeStampString(),
            "颐和园的雪花是最好的", "")

        datalist += News(
            com.bird2fish.travelbook.R.drawable.news5, "1002", "梦里化开", "sys:7", DateTimeHelper.getTimestamp(),
            DateTimeHelper.getTimeStampString(),
            "颐和园的雪花是最好的", "")

        datalist += News(
            com.bird2fish.travelbook.R.drawable.news5, "1002", "梦里化开", "sys:7", DateTimeHelper.getTimestamp(),
            DateTimeHelper.getTimeStampString(),
            "颐和园的雪花是最好的", "")
    }

    fun initContent(){
        val homeAdapter = NewsCoverAdapter(datalist) //创建适配器对象
        homeAdapter.setView(this)
        this.recyclerView = binding.newContainer

        //设置为表格布局，列数为2（这个是最主要的，就是这个来展示陈列式布局）
        //recyclerView.layoutManager = GridLayoutManager(requireActivity(), 2)

        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)


        //recyclerView.addItemDecoration(space_item(5)) //给recycleView添加item的间距
        recyclerView.adapter = homeAdapter //将适配器添加到recyclerView

        this.swipeRefreshLayout = binding.swipeRefreshLayout
        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener {
            // 在这里执行刷新数据的操作
            // 通常是异步的网络请求或其他刷新逻辑

            // 刷新完成后，调用 setRefreshing(false) 结束刷新动画
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 当点击了某个按钮
    fun onClickItem(pos: Int){
        UiHelper.showCenterMessage(requireActivity(), "点击了条目")
    }
}