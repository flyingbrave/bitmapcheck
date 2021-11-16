package com.yxy.checkbitmap

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.lang.Exception

object LargeBitmapCheck {

    lateinit var context: Application
    var ratio: Float = 1.2f

    fun init(cxt: Application) {
        Log.i("LargeBitmapCheck", "LargeBitmapCheck init")
        if (Looper.getMainLooper().thread !== Thread.currentThread()) {
            return
        }
        context = cxt
        dealActivityLifecycleCallback(context)
    }

    private fun dealActivityLifecycleCallback(application: Application) {
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
            }

            override fun onActivityStarted(p0: Activity) {
            }

            override fun onActivityResumed(p0: Activity) {
            }

            override fun onActivityPaused(p0: Activity) {
            }

            override fun onActivityStopped(p0: Activity) {
                Log.i("LargeBitmapCheck", "onActivityStopped  "+p0::class.java)
                val fragments: List<Fragment>? = getFragment(p0)
                fragments?.let {
                    for (fragment in it) {
                        val childViews: List<View> = getAllChildViews(fragment.view!!)
                        checkBitmapIsTooBig(childViews, fragment::class.java.name)
                    }
                }
                val childViews: List<View> = getAllChildViews(p0.window.decorView)
                checkBitmapIsTooBig(childViews)
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
            }

            override fun onActivityDestroyed(p0: Activity) {
            }
        })
    }

    private fun getFragment(activity: Activity): List<Fragment>? {
        if (activity is FragmentActivity) {
            return activity.supportFragmentManager.fragments
        }
        return null
    }

    private fun getAllChildViews(view: View): List<View> {
        val allChildren = ArrayList<View>()
        if (view is ViewGroup) {
            for (i in 0..view.childCount) {
                val viewChild: View = view.getChildAt(i) ?: return allChildren
                allChildren.add(viewChild)
                allChildren.addAll(getAllChildViews(viewChild))
            }
        }
        return allChildren
    }

    /**
     * 检查图片是否过大
     */
    private fun checkBitmapIsTooBig(views: List<View>, containerName: String? = null) {
        for (view in views) {
            isTooBig(view, containerName)
        }
    }

    private fun isTooBig(view: View, containerName: String? = null): Boolean {
        val viewName = findViewIDNameByView(context, view)
        val activityName =
            if (TextUtils.isEmpty(containerName)) findActivityNameByView(view) else containerName
        if (view is ImageView) {
            if (view.drawable !is BitmapDrawable) {
                return false
            }
            val bmDrawable: BitmapDrawable? = view.drawable as BitmapDrawable?
            val bm: Bitmap? = bmDrawable?.bitmap
            bm?.let {
                if (it.width > ratio * view.width || it.height > ratio * view.height) {
                    logWarn(
                        activityName,
                        viewName,
                        view::class.java.simpleName,
                        it.width,
                        it.height,
                        view.width,
                        view.height
                    )
                    return true
                }
            }
        }
        //取背景图片
        val drawable = view.background
        if (drawable is BitmapDrawable) {
            val bm: Bitmap? = drawable.bitmap
            bm?.let {
                if (it.width > ratio * view.width || it.height > ratio * view.height) {
                    logWarn(
                        activityName,
                        viewName,
                        view::class.java.simpleName,
                        it.width,
                        it.height,
                        view.width,
                        view.height
                    )
                    return true
                }
            }
        }
        return false
    }

    private fun logWarn(activityName: String?, viewName: String?, viewClassName: String?,
                        realWidth: Int, realHeight: Int, desiredWidth: Int, desiredHeight: Int) {
        val warnInfo = StringBuilder("Bitmap size too large!!! ")
            .append("\n Activity Name:($activityName)")
            .append("\n View Id Name:($viewName)")
            .append("\n View Class Name:($viewClassName)")
            .append("\n real width: ($realWidth)")
            .append("\n real height: ($realHeight)")
            .append("\n desired width: ($desiredWidth)")
            .append("\n desired height: ($desiredHeight)")
            .toString()
        Log.i("LargeBitmapCheck", warnInfo)
    }

    /**
     * 根据view id反查view id name
     */
    private fun findViewIDNameByView(context: Context, view: View): String? {
        if (view.id == View.NO_ID) {
            return null
        }
        var viewName: String? = null
        try {
            viewName = context.resources.getResourceEntryName(view.id)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return viewName
    }

    /**
     * 根据view获取activity name
     */
    private fun findActivityNameByView(view: View): String {
        return view.context::class.java.name
    }
}