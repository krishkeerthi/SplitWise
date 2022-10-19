package com.example.splitwise.ui.fragment.groups

import android.content.ContentValues.TAG
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.splitwise.R
import com.example.splitwise.ui.fragment.adapter.ExpenseEmptyViewHolder
import com.example.splitwise.ui.fragment.adapter.GroupEmptyViewHolder
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.pxToDp
import kotlin.math.absoluteValue

abstract class SwipeToDeleteCallback : ItemTouchHelper.Callback() {

    val paint = Paint()
    val mClearPaint = Paint()
    val textPaint = Paint().apply {
        color = Color.WHITE
    }

    var swipeThreshold = 0.5f
    var escapeVelocity: Float = 1f
    var animationSpeed: Long = 0

    var leftSwipeLimitCrossed: Boolean = false
    var rightSwipeLimitCrossed: Boolean = false
    var rightSwiped: Boolean = false
    var leftSwiped: Boolean = false

    open fun leftSwipeCallback(viewHolder: ViewHolder){
        
    }
    
    open fun rightSwipeCallback(viewHolder: ViewHolder){
        Log.d(TAG, "rightSwipeCallback: called")
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return super.getSwipeVelocityThreshold(defaultValue)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder
    ): Int {
        val swipeFlag = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

        // turning off swipe for empty record at end
        if(viewHolder is GroupEmptyViewHolder || viewHolder is ExpenseEmptyViewHolder)
            return makeMovementFlags(0, 0) //ItemTouchHelper.ACTION_STATE_IDLE 0

        return makeMovementFlags(0, swipeFlag)
    }


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        return false
    }



    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        viewHolder.itemView.setBackgroundColor(viewHolder.itemView.resources.getColor(R.color.background))
        if(dX == 0f && !isCurrentlyActive){
            if(rightSwiped){
                rightSwiped = false
                if(rightSwipeLimitCrossed){
                    rightSwipeLimitCrossed = false
                    rightSwipeCallback(viewHolder)
                }
            }
            else{
                leftSwiped = false
                if(leftSwipeLimitCrossed){
                    leftSwipeLimitCrossed = false
                    leftSwipeCallback(viewHolder)
                }
            }
        }

        Log.d(TAG, "onChildDraw: child draw state is ${actionState}")

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            Log.d(TAG, "onChildDraw: action state swipe")
            val itemView = viewHolder.itemView

            Log.d(TAG, "onChildDraw: dX value $dX")
            if (dX > 0) { // left to right side movement
                rightSwiped = true

                // tracking user swipe action
                if(dX.absoluteValue/6 >= 50f){
                    rightSwipeLimitCrossed = true
                }
                else{
                    if(isCurrentlyActive){
                        rightSwipeLimitCrossed = false
                        //Log.d(TAG, "onChildDraw: user is touching ")
                    }
                }

                // setting velocity and threshold
                swipeThreshold = 1f
                escapeVelocity = 100000f
                animationSpeed = 250//500

                paint.color = recyclerView.context.resources.getColor(R.color.gray)
                c.drawRect(RectF(itemView.left.toFloat(), itemView.top.toFloat(), itemView.left + dX/6, itemView.bottom.toFloat())
                    , paint)

                // set no action
                if(setNoAction(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)) {
                //    viewHolder.bindingAdapter?.notifyItemChanged(viewHolder.absoluteAdapterPosition)
                //     this.onSwiped(viewHolder, ItemTouchHelper.RIGHT)
                //    this.swi
        //            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, 0, false)
//                    clearView(recyclerView, viewHolder)
//                    viewHolder.bindingAdapter?.notifyItemChanged(viewHolder.absoluteAdapterPosition)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX/6, dY, actionState, isCurrentlyActive)


            }
            else{ // right to left side movement
                leftSwiped = true
                Log.d(TAG, "onChildDraw: dX value on left swipe is $dX")

                // tracking user swipe option
                if(dX.absoluteValue >= 140f){
                    leftSwipeLimitCrossed = true
                }
                else{
                    if(isCurrentlyActive){
                        leftSwipeLimitCrossed = false
                        //Log.d(TAG, "onChildDraw: user is touching ")
                    }
                }

                // setting velocity and threshold
                swipeThreshold = 1f
                escapeVelocity = 100000f
                animationSpeed = 250

                paint.color = recyclerView.context.resources.getColor(R.color.light_red)
                c.drawRect(RectF(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                , paint)

                // draw delete icon
                setTrashIcon(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }


        }
        else if(actionState == ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL){
            Log.d(TAG, "onChildDraw: action state cancel")
        }
        else{
            Log.d(TAG, "onChildDraw: action state unknown")
        }

    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        Log.d(TAG, "onChildDrawOver: child drawed")
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

//    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        animationSpeed = 0
//    }

    private fun setTrashIcon(c: Canvas,
                             recyclerView: RecyclerView,
                             viewHolder: ViewHolder,
                             dX: Float,
                             dY: Float,
                             actionState: Int,
                             isCurrentlyActive: Boolean){


        Log.d(TAG, "setTrashIcon: onChildDraw")
        //mClearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        val deleteDrawable = ContextCompat.getDrawable(recyclerView.rootView.context , R.drawable.ic_baseline_delete_24)!!
        val intrinsicWidth = deleteDrawable.intrinsicWidth
        val intrinsicHeight = deleteDrawable.intrinsicHeight

        val itemView = viewHolder.itemView
        val itemHeight = itemView.height

        // swipe cancelled
        val isCancelled = dX == 0f && !isCurrentlyActive

        if(isCancelled){
            Log.d(TAG, "setTrashIcon: onChildDraw isCancelled called")
//            c.drawRect(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(),
//            mClearPaint)
//
//            recyclerView.invalidate()
        }
        // delete icon rect
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconTop = itemView.top + deleteIconMargin
        val deleteIconLeft = itemView.right - intrinsicWidth - deleteIconMargin
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight


        Log.d(TAG, "setTrashIcon: onChildDraw dx dx abs ${dX.absoluteValue} delete left ${deleteIconRight
            .pxToDp(recyclerView.context.resources.displayMetrics).toFloat()}")
        //deleteIconRight.pxToDp(recyclerView.context.resources.displayMetrics).toFloat()
     //   if(dX.absoluteValue > (intrinsicWidth * 2)) {
            // drawing delete
            deleteDrawable.setBounds(
                deleteIconLeft,
                deleteIconTop,
                deleteIconRight,
                deleteIconBottom
            )
            //c.drawPaint(whitePaint)
            //deleteDrawable.setColorFilter(R.color.red)
            deleteDrawable.draw(c)

     //   }
        val textWidth = paint.measureText("Delete").toInt().dpToPx(recyclerView.resources.displayMetrics).toFloat()

      //  if(dX.absoluteValue > (itemView.right - textWidth * 3).toInt().pxToDp(recyclerView.resources.displayMetrics)){
            textPaint.textSize = 16.dpToPx(recyclerView.resources.displayMetrics).toFloat()


            val textY = (itemView.bottom - itemView.top) / 2
            val text = recyclerView.resources.getString(R.string.delete)
            c.drawText(text, (itemView.right - textWidth * 3), (itemView.top + (textY * 1.15f)), textPaint)
            //0, 6, previously text index was set, but it wont work translated s    trings

      //  }
    }

    private fun setNoAction(c: Canvas,
                            recyclerView: RecyclerView,
                            viewHolder: ViewHolder,
                            dX: Float,
                            dY: Float,
                            actionState: Int,
                            isCurrentlyActive: Boolean): Boolean{
        
        if(dX > 100){

          //  super.onChildDraw(c, recyclerView, viewHolder, dX/4, dY, actionState, isCurrentlyActive)
//            Log.d(TAG, "setNoAction: dx value is ${dX}")
//            clearView(recyclerView, viewHolder)
//            viewHolder.bindingAdapter?.notifyItemChanged(viewHolder.absoluteAdapterPosition)
//            recyclerView.invalidate()
            
//            val listener = object: RecyclerView.OnItemTouchListener{
//                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//                    Log.d(TAG, "onInterceptTouchEvent: item touch listener")
//                    return false
//                }
//
//                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
//                    Log.d(TAG, "onTouchEvent: item touch listener")
//                }
//
//                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
//                    Log.d(TAG, "onRequestDisallowInterceptTouchEvent: item touch listener")
//                }
//
//            }
//
//            recyclerView.addOnItemTouchListener(listener)
//
//            return true

        }


        return false
    }

    override fun getSwipeThreshold(viewHolder: ViewHolder): Float {
       // super.getSwipeThreshold(viewHolder)
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return if(escapeVelocity == 100000f) super.getSwipeEscapeVelocity(defaultValue) * escapeVelocity
        else super.getSwipeEscapeVelocity(defaultValue)

    }

    override fun getAnimationDuration(
        recyclerView: RecyclerView,
        animationType: Int,
        animateDx: Float,
        animateDy: Float
    ): Long {

        return animationSpeed//super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
    }

    

    //    override fun canDropOver(
//        recyclerView: RecyclerView,
//        current: RecyclerView.ViewHolder,
//        target: RecyclerView.ViewHolder
//    ): Boolean {
//        return super.canDropOver(recyclerView, current, target)
//    }
}