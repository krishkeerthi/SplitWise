package com.example.splitwise.ui.customclass

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory

class CustomImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onDraw(canvas: Canvas) {
        val radius = 50.0f
        Log.d(TAG, "onDraw: custom image view on draw")
        @SuppressLint("DrawAllocation") val clipPath = Path()
        @SuppressLint("DrawAllocation") val rect = RectF(0f, 0f, this.width.toFloat(), this.width.toFloat())
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW)
        canvas.clipPath(clipPath)
        super.onDraw(canvas)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        Log.d(TAG, "onDraw: custom image view set image drawable")
        super.setImageDrawable(drawable)
        val radius = 100f
        val bitmap = (drawable as BitmapDrawable).bitmap
        val resourceId = RoundedBitmapDrawableFactory.create(resources, bitmap)
        resourceId.cornerRadius = bitmap.width * radius
        super.setImageDrawable(resourceId)
    }

    companion object {
        var radius = 100.0f
    }
}