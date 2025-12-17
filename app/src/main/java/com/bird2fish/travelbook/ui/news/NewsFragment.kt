package com.bird2fish.travelbook.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.FavLocation
import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.News
import com.bird2fish.travelbook.core.NewsFav
import com.bird2fish.travelbook.core.Track
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.databinding.FragmentNewsBinding
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.helper.FileHelper
import com.bird2fish.travelbook.helper.PicassoHelper
import com.bird2fish.travelbook.helper.TrackHelper
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import com.bird2fish.travelbook.widgets.MyDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private  var  imgplayer: Carousel? = null
    private var newsContent:News? = null
    private var newsFav : NewsFav?= null

    // 这里开始是空的，
    private var track :Track? = null

    private var _changed : MutableLiveData<Long> = MutableLiveData(DateTimeHelper.getTimestamp())
    var changed : LiveData<Long> =  _changed


    inner class ImagePagerAdapter(private val imageList: List<String>) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.viewpage2_holder, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            // 使用 Picasso 加载网络图片

            val url = GlobalData.getHttpServ().getImageUrl(imageList[position])
            PicassoHelper.getInstance(this@NewsFragment.requireActivity())
                .load(url)
                .into(holder.imageView)
        }

        override fun getItemCount(): Int {
            return imageList.size
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 加载数据结束后
        this.changed.observe(requireActivity(), Observer {
            updateStar()

            // 轨迹已经解析完毕了
            if (this.track != null){
                trySaveTrack()
            }
        })

        // 点击位置图标
        binding.newsItemPointImg.setOnClickListener{
            showNewsPointInMap()
        }

        // 点击文字
        binding.newsItemPointInfo.setOnClickListener{
            showNewsPointInMap()
        }

        // 喜欢
        binding.imgNewsLikes.setOnClickListener{
            if (newsFav!=null){
                newsFav!!.like = !newsFav!!.like

                if(newsFav!!.like){
                    newsContent!!.likes += 1
                }else{
                    newsContent!!.likes -= 1
                }

                updateStar()
                updateLike()
            }
        }

        // 收藏
        binding.imgNewFavs.setOnClickListener{
            if (newsFav!=null){
                newsFav!!.fav = !newsFav!!.fav

                if (newsFav!!.fav){
                    newsContent!!.favs += 1
                }else{
                    newsContent!!.favs -= 1
                }
                if (newsFav!!.fav){
                    addNewsToFoler()
                }
                updateStar()
                updateFav()
            }
        }

        // 删除
        binding.newsItemDelImg.setOnClickListener{
            delNews()
        }


        binding.tvNewInput.setOnFocusChangeListener(View.OnFocusChangeListener { view, hasFocus ->
//            if (hasFocus) {
//                binding.tvNewsItemLikes.visibility =  View.GONE
//                binding.tvNewsItemFavs.visibility = View.GONE
//                binding.imgNewFavs.visibility =  View.GONE
//                binding.imgNewsLikes.visibility =  View.GONE
//            } else {
//                binding.tvNewsItemLikes.visibility = View.VISIBLE
//                binding.tvNewsItemFavs.visibility = View.VISIBLE
//                binding.imgNewFavs.visibility = View.VISIBLE
//                binding.imgNewsLikes.visibility = View.VISIBLE
//            }
        })


        return root
    }

    private fun updateStar(){
        if (newsFav != null){
            if (newsFav!!.like){
                binding.imgNewsLikes.setImageResource(R.drawable.heart72_red)
            }else{
                binding.imgNewsLikes.setImageResource(R.drawable.heart72_grey)
            }
            binding.tvNewsItemLikes.setText(newsContent!!.likes.toString())

            if (newsFav!!.fav){
                binding.imgNewFavs.setImageResource(R.drawable.star72_y)
            }else{
                binding.imgNewFavs.setImageResource(R.drawable.star72_grey)
            }
            binding.tvNewsItemFavs.setText(newsContent!!.favs.toString())
        }
    }
    private fun showNewsPointInMap(){
        val navController = findNavController()

        if (newsContent != null){
            val bundle = Bundle()
            bundle.putParcelable("news", newsContent)
            navController.navigate(R.id.action_newsFragment_to_newsMapFragment, bundle)
        }
    }

    fun updateData () {
        if (newsContent == null)
            return

        // 图片显示与那个点点一起捆绑，
        imgplayer = Carousel(
            requireActivity(),
            binding.indexDot,
            binding.viewPager,
            binding.tvNewsPages
        )

        val adapter = ImagePagerAdapter(newsContent!!.images)
        //binding.viewPager.adapter = adapter

        // 图片
        imgplayer!!.initViews(newsContent!!.images, adapter)

        // 标题和文本TAG
        binding.titleTextView.text = newsContent!!.title
        binding.contentTextView.text = newsContent!!.content

        var tagBuf = StringBuffer()
        for (str in newsContent!!.tags){
            tagBuf.append(str)
            tagBuf.append(" ")
        }
        binding.tagsContentTextView.text = tagBuf.toString()
        // 位置信息
        if (newsContent!!.type == "point"){
            binding.newsItemPointImg.setImageResource(R.drawable.mark_red)
            val info = "点击查看 位置(${newsContent!!.lat}, ${newsContent!!.log})"
            binding.newsItemPointInfo.setText(info)
        }else{
            binding.newsItemPointImg.setImageResource(R.drawable.mark_track)
            val info = "点击查看 轨迹"
            binding.newsItemPointInfo.setText(info)
        }

        // 时间
        var tmStr = "发布于 " + DateTimeHelper.convertTimestampToDateString(newsContent!!.tm)
        binding.newsItemTm.setText(tmStr)

        // 用户图标与昵称
        val id = UiHelper.getIconResId(newsContent!!.icon)
        binding.newsItemIcon.setImageResource(id)
        binding.newsItemUsername.setText(newsContent!!.nick)

        // 输入框右侧的按钮
        val likeCount = newsContent!!.likes.toString()
        var favCount = newsContent!!.favs.toString()
        binding.tvNewsItemLikes.setText(likeCount)
        binding.tvNewsItemFavs.setText(favCount)

        // 不是自己的数据，不能删除
        val user = CurrentUser.getUser()
        if (user!!.uid != newsContent!!.uid){
            binding.newsItemDelImg.visibility = View.GONE
        }

        doGetFav()
    }

    // 获取用户的喜欢与收藏
    private  fun doGetFav(){
        val user = CurrentUser.getUser()
        CoroutineScope(Dispatchers.IO).launch {
            newsFav =  GlobalData.getHttpServ().getUserFav(user!!.uid, newsContent!!.nid, user!!.sid)
            _changed.postValue(DateTimeHelper.getTimestamp())
        }
    }

    private fun updateFav(){
        val user = CurrentUser.getUser()
        var opt = ""

        if (newsFav!!.fav){
            opt = "inc"
        }else{
            opt = "dec"
        }
        CoroutineScope(Dispatchers.IO).launch {
            GlobalData.getHttpServ().updateUserFav(user!!.uid, newsContent!!.nid, user!!.sid, opt)
        }
    }

    private fun updateLike(){
        val user = CurrentUser.getUser()

        var opt = ""

        if (newsFav!!.like){
            opt = "inc"
        }else{
            opt = "dec"
        }
        CoroutineScope(Dispatchers.IO).launch {
            GlobalData.getHttpServ().updateUserLike(user!!.uid, newsContent!!.nid, user!!.sid, opt)
        }
    }

    private  fun doDelete(){
        val user = CurrentUser.getUser()
        if (user!!.uid != newsContent!!.uid){
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            GlobalData.getHttpServ().removeNews(user.uid, user.sid, newsContent!!.nid)
        }

        GlobalData.delNewsInlist(newsContent!!)

        // 返回封面页
        val navController = findNavController()
        navController.navigate(R.id.action_news_to_coverpage)
    }

    fun delNews(){
        MyDialogFragment.showConfirmationDialog(
            context,
            "确认",
            "您确定删除这个帖子吗？"
        ) { dialog, which ->
            // 用户点击了确认按钮，执行相应操作
            doDelete()
        }
    }

    // 把收藏的东西放到本地的文件夹
    fun addNewsToFoler(){
        if (newsContent!!.type == "point"){
            addNewsPoint()
        }else{
            addNewsTrack()
        }
    }

    // 添加收藏的点
    fun addNewsPoint(){
        if (newsContent != null){
            val favLoc = FavLocation()

            favLoc.uId = newsContent!!.uid
            favLoc.uNick = newsContent!!.nick
            favLoc.uIcon = newsContent!!.icon
            favLoc.lat = newsContent!!.lat
            favLoc.lon = newsContent!!.log
            favLoc.alt = newsContent!!.alt
            favLoc.des = newsContent!!.content
            favLoc.title = newsContent!!.title
            favLoc.favId = newsContent!!.nid.toLong()
            favLoc.tm = DateTimeHelper.getTimestamp()
            favLoc.tmStr = DateTimeHelper.getTimeStampLongString()
            GlobalData.addFavLocation(requireActivity(), favLoc)

            UiHelper.showCenterMessage(requireActivity(), "收藏点导入完毕，请在地点页面查看")
        }
    }


    // 下载文件到内存
    fun doGetTrackFile(){
        this.track = GlobalData.getHttpServ().getGpxFile(newsContent!!.trackFile)
    }

    // 添加轨迹
    fun addNewsTrack(){
        if (newsContent == null){
            return
        }

        if (newsContent!!.type == "track"){
            // 异步加载路径
            CoroutineScope(Dispatchers.IO).launch {
                // 在后台执行异步任务
                val result = doGetTrackFile()
                _changed.postValue(DateTimeHelper.getTimestamp())
            }
        }else{  // localtrack
            track = FileHelper.readTrack(requireActivity(), newsContent!!.trackFile)
            _changed.postValue(DateTimeHelper.getTimestamp())
        }
    }

    // 尝试保存轨迹
    fun trySaveTrack(){
        if (this.track != null && this.newsContent != null){
            GlobalData.addTrack(requireActivity(), this.track!!, this.newsContent!!.title)
            UiHelper.showCenterMessage(requireActivity(), "收藏轨迹导入完毕，请在相关页面查看")
        }

    }

}