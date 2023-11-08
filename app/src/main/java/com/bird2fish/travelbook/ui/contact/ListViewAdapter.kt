package com.bird2fish.travelbook.ui.contact

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter


/**
 * 自定义的通用适配器，作为公共的Adapter类，继承自BaseAdapter。以后如果是自定义ListView的adapter，继承它就行了
 */
abstract class ListViewAdapter<T>(context: Context, datas: List<T>, layoutId: Int) :
    BaseAdapter() {
    //为了让子类访问，于是将属性设置为protected
    protected var mContext: Context
    protected var mDatas: List<T>
    protected var mInflater: LayoutInflater
    private val layoutId: Int  //不同的ListView的item布局肯能不同，所以要把布局单独提取出来

    override fun getCount(): Int {
        return mDatas.size
    }

    override fun getItem(position: Int): T {
        return mDatas[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,  //当前item的位置
        convertView: View?,  //用于复用旧视图
        parent: ViewGroup?
    ): View { //此视图最终将附加到的父级，用于加载XML布局
        //初始化ViewHolder,使用通用的ViewHolder，一行代码就搞定ViewHolder的初始化咯
        val holder: CommonViewHolder =
            CommonViewHolder.get(mContext, convertView, parent, layoutId, position) //layoutId就是单个item的布局
        convert(holder, getItem(position))
        return holder.getConvertView() //这一行的代码要注意了
    }

    //将convert方法公布出去
    abstract fun convert(holder: CommonViewHolder?, t: T)

    init {
        mContext = context
        mInflater = LayoutInflater.from(context)
        mDatas = datas
        this.layoutId = layoutId
    }
}