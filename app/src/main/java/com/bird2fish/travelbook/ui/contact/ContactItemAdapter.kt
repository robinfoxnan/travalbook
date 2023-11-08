package com.bird2fish.travelbook.ui.contact

import android.content.Context
import android.opengl.Visibility
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.*
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.UiHelper

//将Adapter和ViewHolder关联，加载到item布局，此处用于扩展
class ContactItemAdapter (context: Context?, datas: List<Friend?>?) :
    ListViewAdapter<Friend?>(context!!, datas!!, R.layout.contact_item) {

    private var fragment : ContactFragment? = null


    fun setView(view : ContactFragment?){
        this.fragment = view
    }

//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        //val view = super.getView(position, convertView, parent)
//
//        val holder: CommonViewHolder = CommonViewHolder.get(mContext, convertView, parent, R.layout.contact_item, position) //layoutId就是单个item的布局
//        convert(holder, getItem(position))
//
//        val view = holder.getConvertView()
//
//        // 设置CheckBox的点击事件监听器
//        val checkbox1 = (holder!!.getView(R.id.shareTv) as CheckBox)
//        checkbox1.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                // CheckBox被选中
//                // 获取点击的数据项的位置（position）
//                val clickedPosition = position
//                // 可以使用该位置来访问相关数据
//
//                // 执行你的操作
//            } else {
//                // CheckBox被取消选中
//                // 执行其他操作
//            }
//        }
//
//        return view
//    }


    override fun convert(holder: CommonViewHolder?, friend: Friend?) {
//        if (friend == null)
//        {
//            return
//        }

        val checkbox = (holder!!.getView(R.id.shareTv) as CheckBox)
        val switch = (holder!!.getView(R.id.switch_follow) as Switch)


        (holder!!.getView(R.id.nameTv) as TextView).setText(friend!!.nick)
        val id = UiHelper.getIconResId(friend.icon)
        (holder.getView(R.id.iconTv) as ImageView).setImageResource(id) // R.drawable.icon2
        (holder.getView(R.id.phoneTv) as TextView).setText(friend.uid)
        checkbox.isChecked = friend.show
        switch.isChecked = friend.isFriend

        checkbox.tag = friend
        switch.tag = friend

        //(holder.getView(R.id.shareTv) as CheckBox).bringToFront()
        if (friend.isFriend == false){
            checkbox.visibility = android.view.View.INVISIBLE
            //switch.visibility = android.view.View.VISIBLE
        }else
        {
            checkbox.visibility = android.view.View.VISIBLE
           // switch.visibility = android.view.View.INVISIBLE
        }

        // 选中了显示

        checkbox.setOnClickListener {
            var f = checkbox.tag as Friend
            f.show = checkbox.isChecked
            fragment?.onShowFriend(f)
        }
//
        switch.setOnClickListener{
            var f = switch.tag as Friend
            f.isFriend = switch.isChecked
           fragment?.onFollowFriend(f)
        }
//        (holder.getView(R.id.shareTv) as CheckBox).setOnCheckedChangeListener { buttonView, isChecked ->
//
//        }

        // 点击取消或者设置关注, 需要将该对象添加到好友列表
//        (holder.getView(R.id.switch_follow) as Switch).setOnCheckedChangeListener { buttonView, isChecked ->
//            var f = buttonView.tag as Friend
//            f.isFriend = isChecked
//
//            fragment?.onFollowFriend(f)
//
//        }
    }

}