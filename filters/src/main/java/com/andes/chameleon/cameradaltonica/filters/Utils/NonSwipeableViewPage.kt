package com.andes.chameleon.cameradaltonica.filters.Utils

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import java.lang.Exception
import java.lang.reflect.Field

class NonSwipeableViewPage : ViewPager {
    constructor(context: Context):super(context){
        setMyScroller()
    }
    constructor(context:Context,attributeSet: AttributeSet):super(context,attributeSet){
        setMyScroller()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    private fun setMyScroller() {
        try {
            val viewPager : Class<ViewPager> = ViewPager::class.java
            val scroller = viewPager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(this, MyScroller(context))
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}

class MyScroller(context:Context): Scroller(context, DecelerateInterpolator()) {
    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, 400)
    }
}