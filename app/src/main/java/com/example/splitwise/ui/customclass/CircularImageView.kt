package com.example.splitwise.ui.customclass

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.splitwise.R
import com.example.splitwise.util.dpToPx

class CircularImageView : androidx.appcompat.widget.AppCompatImageView {
    private lateinit var mPaint: Paint
    private lateinit var mPath: Path
    private lateinit var mBitmap: Bitmap
    private lateinit var mMatrix: Matrix
    private var mRadius: Float = 0.dpToPx(resources.displayMetrics).toFloat()
    private var mFillColor = DEFAULT_COLOR
    private var mWidth = 0
    private var mHeight = 0
    private var mDrawable: Drawable? = null
    //private lateinit var mCanvas : Canvas

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    override fun hasOverlappingRendering(): Boolean {
        Log.d(TAG, "hasOverlappingRendering: ${super.hasOverlappingRendering()}  tesstt")
        return super.hasOverlappingRendering()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        Log.d(TAG, "drawableStateChanged: tesstt")
    }

    private fun setupAttributes(attributeSet: AttributeSet) {
        val typedArray =
            context.theme.obtainStyledAttributes(attributeSet, R.styleable.CircularImageView, 0, 0)

//        val radius =  typedArray.getDimension(R.styleable.CircularImageView_radius, DEFAULT_RADIUS).toInt()
//
//        Log.d(TAG, "setupAttributes: radius is ${radius}")

        mRadius = typedArray.getDimension(R.styleable.CircularImageView_radius, DEFAULT_RADIUS)
        mFillColor = typedArray.getColor(R.styleable.CircularImageView_fill_color, DEFAULT_COLOR)
        // by default dp is converted to px

        typedArray.recycle()
    }

    private fun init(attrs: AttributeSet?) {
        attrs?.let {
            setupAttributes(attrs)
        }

        mPaint = Paint()
        mPaint.color = mFillColor//resources.getColor(R.color.new_view_color)
        mPath = Path()

    }

//    override fun setImageDrawable(drawable: Drawable?) {
//        Log.d(TAG, "setImageDrawable: setImageDrawable called")
//        mDrawable = drawable
//        if (drawable == null) {
//            Log.d(TAG, "setImageDrawable: drawable null")
//            return
//        }
//        mBitmap = drawableToBitmap(drawable)
//
//        Log.d(TAG, "setImageDrawable: setImageDrawable drawable to bitmap success")
//
//        val bDIWidth = mBitmap.width
//        val bDIHeight = mBitmap.height
//
//        //Fit to screen.
//        val scale: Float = if (mHeight / bDIHeight.toFloat() >= mWidth / bDIWidth.toFloat()) {
//            mHeight / bDIHeight.toFloat()
//        } else {
//            mWidth / bDIWidth.toFloat()
//        }
//        val borderLeft = (mWidth - bDIWidth * scale) / 2
//        val borderTop = (mHeight - bDIHeight * scale) / 2
//        mMatrix = imageMatrix
//        val drawableRect = RectF(0f, 0f, bDIWidth.toFloat(), bDIHeight.toFloat())
//        val viewRect = RectF(
//            borderLeft,
//            borderTop,
//            bDIWidth * scale + borderLeft,
//            bDIHeight * scale + borderTop
//        )
//        mMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER)
//
//        Log.d(TAG, "setImageDrawable: set rect to rect")
//        invalidate()
//        Log.d(TAG, "setImageDrawable: invalidated")
//    }

//    private fun drawableToBitmap(drawable: Drawable): Bitmap {
//        Log.d(TAG, "drawableToBitmap: called")
//        if (drawable is BitmapDrawable) {
//            if (drawable.bitmap != null) {
//                Log.d(TAG, "drawableToBitmap: bitmap not null")
//                return drawable.bitmap
//            }
//        }
//        val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
//            Bitmap.createBitmap(
//                1,
//                1,
//                Bitmap.Config.ARGB_8888
//            ) // Single color bitmap will be created of 1x1 pixel
//        } else {
//            Bitmap.createBitmap(
//                drawable.intrinsicWidth,
//                drawable.intrinsicHeight,
//                Bitmap.Config.ARGB_8888
//            )
//        }
//        val canvas = Canvas(bitmap)
//        drawable.setBounds(0, 0, canvas.width, canvas.height)
//        drawable.draw(canvas)
//        return bitmap
//    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
//        if (mDrawable != null && mHeight > 0 && mWidth > 0) {
//            setImageDrawable(mDrawable)
//        }
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)
        //canvas.drawColor(Color.TRANSPARENT)
        mPath.reset()

        Log.d(TAG, "onDraw: circle radius ${mRadius}")

        mPath.addCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            mRadius,
            Path.Direction.CW
        )
//        mPath.moveTo(0f, mRadius.toFloat())
//        mPath.lineTo(0f, height.toFloat())
//        mPath.lineTo(width.toFloat(), height.toFloat())
//        mPath.lineTo(width.toFloat(), mRadius.toFloat())
//
//        // top right cut
//        mPath.quadTo(width.toFloat(), 0f, width.toFloat() - mRadius, 0f)
//
//        mPath.lineTo(mRadius.toFloat(), 0f)
//
//        // top left cut
//        mPath.quadTo(0f, 0f, 0f, mRadius.toFloat())
//
//        mPath.lineTo(0f, mRadius.toFloat())
//
//        // bottom left cut
//        mPath.quadTo(0f, height.toFloat(), width.toFloat() - mRadius, height.toFloat())

        canvas.drawPath(mPath, mPaint)
        canvas.clipPath(mPath)

//        canvas.drawBitmap(mBitmap, mMatrix, mPaint)

    //    mCanvas = canvas // not in actual code, experimented
    }

//    fun reDraw(){
//        mPath.reset()
//
//        Log.d(TAG, "onDraw: circle radius ${mRadius}")
//
//        mPath.reset()
//        mPath.addCircle(
//            (width / 2).toFloat(),
//            (height / 2).toFloat(),
//            mRadius,
//            Path.Direction.CW
//        )
//
//        mCanvas.drawPath(mPath, mPaint)
//        mCanvas.clipPath(mPath)
//
//    }

    companion object {
        const val DEFAULT_RADIUS = 0F
        const val DEFAULT_COLOR = Color.TRANSPARENT
    }
}