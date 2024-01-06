package com.bird2fish.travelbook.widgets

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout

// https://cloud.tencent.com/developer/article/1741756?areaSource=102001.10&traceId=O0xu7qvkAurw0k6By7b72
class SoftHideKeyBoardUtil(activity: Activity) {

    private val mChildOfContent: View
    private var usableHeightPrevious = 0
    private val frameLayoutParams: FrameLayout.LayoutParams
    private var contentHeight = 0
    public var isfirst = true
    private var statusBarHeight = 0

    init {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        mChildOfContent = content.getChildAt(0)

        mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (isfirst) {
                    contentHeight = mChildOfContent.height
                    isfirst = false
                }
                possiblyResizeChildOfContent()
            }
        })

//        mChildOfContent.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
//            override fun onPreDraw(): Boolean {
//                if (isfirst) {
//                    contentHeight = mChildOfContent.height
//                    isfirst = false
//                }
//                possiblyResizeChildOfContent()
//                return true
//            }
//        })

        statusBarHeight = getStatusBarHeight(activity)
        frameLayoutParams = mChildOfContent.layoutParams as FrameLayout.LayoutParams
    }
    private fun getStatusBarHeight(activity: Activity): Int {
        var result = 0
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = activity.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun possiblyResizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard = mChildOfContent.rootView.height
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference + statusBarHeight
                } else {
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference
                }
            } else {
                frameLayoutParams.height = contentHeight
            }
            mChildOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    private fun computeUsableHeight(): Int {
//        val r = Rect()
//        mChildOfContent.getWindowVisibleDisplayFrame(r)
//        return (r.bottom - r.top)
        return mChildOfContent.height
    }

}
