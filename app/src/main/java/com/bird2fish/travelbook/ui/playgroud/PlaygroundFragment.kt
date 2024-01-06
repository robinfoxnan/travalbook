package com.bird2fish.travelbook.ui.playgroud

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.HttpService
import com.bird2fish.travelbook.core.News
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.ui.playgroud.NewsCoverAdapter.space_item
import java.util.LinkedList

import com.bird2fish.travelbook.ui.data.model.CurrentUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.bird2fish.travelbook.databinding.FragmentPlaygroundBinding

class PlaygroundFragment : Fragment() {

    private var _binding: FragmentPlaygroundBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    private var _changed : MutableLiveData<Long> = MutableLiveData(DateTimeHelper.getTimestamp())
    var changed : LiveData<Long> =  _changed

    var homeAdapter:NewsCoverAdapter? = null
    //var dataList:LinkedList<News>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(PlaygroundViewModel::class.java)

        _binding = FragmentPlaygroundBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 初始化控件
        initContent()

        this.changed.observe(requireActivity(), Observer {
            homeAdapter!!.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        })

        if (GlobalData.newList.isEmpty())
            performNewFindTask()

        return root
    }

    override fun onResume() {
        super.onResume()

        if (homeAdapter != null){
            homeAdapter!!.notifyDataSetChanged()
        }

    }

    fun initContent(){
        this.homeAdapter = NewsCoverAdapter(GlobalData.newList) //创建适配器对象
        this.homeAdapter!!.setView(this)
        this.recyclerView = binding.newContainer

        //设置为表格布局，列数为2（这个是最主要的，就是这个来展示陈列式布局）
        //recyclerView.layoutManager = GridLayoutManager(requireActivity(), 2)

        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)


        //recyclerView.addItemDecoration(space_item(5)) //给recycleView添加item的间距
        recyclerView.adapter = homeAdapter //将适配器添加到recyclerView

        this.swipeRefreshLayout = binding.swipeRefreshLayout
        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener {

            if (GlobalData.newList.size < 20){
                UiHelper.showCenterMessage(requireActivity(), "没有更多的帖子了")
                swipeRefreshLayout.isRefreshing = false
            }else{
                GlobalData.currentPageIndex += 1
                performNewFindTask()
            }

        }
    }

    fun performNewFindTask() {
        CoroutineScope(Dispatchers.IO).launch {
            // 在后台执行异步任务
            val result = doBackgroundFindWork()
            _changed.postValue(DateTimeHelper.getTimestamp())
        }
    }

    fun doBackgroundFindWork(){
        val user = CurrentUser.getUser() ?: return
        val dataList = GlobalData.getHttpServ().getNewsRecent(user.uid, user.sid, GlobalData.currentPageIndex, 20)
        GlobalData.setNewsList(dataList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 当点击了某个帖子的图片，执行跳转
    fun onClickItem(pos: Int){
        // 获取 NavController
        val navController = findNavController()

        val news = GlobalData.newList[pos]
        val bundle = Bundle()
        bundle.putParcelable("news", news)
        navController.navigate(R.id.action_nav_playground_to_newsFragment, bundle)

        //UiHelper.showCenterMessage(requireActivity(), "点击了条目${pos}")
    }
}