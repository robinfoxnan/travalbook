package com.bird2fish.travelbook.ui.news

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bird2fish.Carousel
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.databinding.FragmentNewsBinding
import com.bird2fish.travelbook.helper.PicassoHelper


class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private  var  imgplayer: Carousel? = null


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

            PicassoHelper.getInstance(this@NewsFragment.requireActivity())
                .load(imageList[position])
                .into(holder.imageView)
        }

        override fun getItemCount(): Int {
            return imageList.size
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val imageList = listOf(
            "https://8.140.203.92:7817/file/7807018625998524416.png",
            "https://8.140.203.92:7817/file/14594012524371251200.png"
        )
        val adapter = ImagePagerAdapter(imageList)

        imgplayer = Carousel(requireActivity(), binding.indexDot, binding.viewPager)
        //binding.viewPager.adapter = adapter
        imgplayer!!.initViews(imageList, adapter)

        binding.titleTextView.text = "颐和园雪后"
        binding.contentTextView.text = "雪后与湖面融合 若遇上日落更是散发着烂漫的金光 谐趣园 颐和园东北角 是园中zui静谧的一处园中园 宫门西开 轩堂亭榭一应俱全 在冬天在那三步一曲 五步一折的游廊里看雪景 甚是有.."
        binding.tagsTextView.text = "#颐和园 #大雪"
        return root
    }

    fun InitData () {

    }

}