package com.bird2fish.travelbook.ui.contact

import android.util.SparseArray
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * 作为公共的类.对应ListView里面的item控件，提高控件的查询效率
 */
class CommonViewHolder(
    context: Context?,
    parent: ViewGroup?,  //此视图最终将附加到的父级，用于加载XML布局
    layoutId: Int,       //单个item的布局
    private var mPosition: Int
) {
    private val mViews: SparseArray<View?>
    private val mConvertView: View

    init { //当前item的位置
        mViews = SparseArray<View?>()
        mConvertView = LayoutInflater.from(context).inflate(layoutId, parent, false)
        mConvertView.setTag(this)
    }

    /*
    通过viewId获取控件
     */
    //使用的是泛型T,返回的是View的子类
    fun <T : View?> getView(viewId: Int): T {
        var view: View? = mViews[viewId]
        if (view == null) {
            view = mConvertView.findViewById(viewId)
            mViews.put(viewId, view)
        }
        return view as T
    }

    fun getConvertView(): View
    {
        return mConvertView
    }


    companion object {
        operator fun get(
            context: Context?,
            convertView: View?,  //用于复用旧视图
            parent: ViewGroup?,  //此视图最终将附加到的父级，用于加载XML布局
            layoutId: Int,  //单个item的布局
            position: Int
        ):  CommonViewHolder { //当前item的位置
            return if (convertView == null) {
                CommonViewHolder(context, parent, layoutId, position)
            } else {
                val holder = convertView.getTag() as  CommonViewHolder
                holder.mPosition = position //即使ViewHolder是复用的，但是position记得更新一下
                holder
            }
        }
    }


}