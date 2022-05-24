package com.kdapps.productscanner.ui.database

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kdapps.productscanner.data.DataRepository

class SwipeToDeleteCallback(private val adapter: SwipableAdapter, val context: Context?) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    interface SwipableAdapter{
        fun onSwipe(adapterPosition: Int)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onSwipe(viewHolder.adapterPosition)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && context != null && DataRepository(context).isHapticsEnabled()){
            viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
    }

}