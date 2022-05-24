package com.kdapps.productscanner.ui.database

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kdapps.productscanner.GlideApp
import com.kdapps.productscanner.ImageViewerActivity
import com.kdapps.productscanner.R
import com.kdapps.productscanner.data.DataRepository
import com.kdapps.productscanner.data.Product
import com.kdapps.productscanner.databinding.ListItemBinding
import java.util.ArrayList

class FilterListAdapter(val context: Context) : RecyclerView.Adapter<FilterListAdapter.FilterListViewHolder>(), SwipeToDeleteCallback.SwipableAdapter {


    private var items : ArrayList<Product> = ArrayList<Product>()

    inner class FilterListViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val storageReference = Firebase.storage("gs://product-scanner-7093a.appspot.com")
    private var expandedHolder = -1
    private lateinit var recyclerView : RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FilterListViewHolder(
        ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: FilterListAdapter.FilterListViewHolder, position: Int) {
        val product : Product = items[holder.adapterPosition]

        if(expandedHolder == holder.bindingAdapterPosition){
            holder.binding.imageView.visibility = View.VISIBLE
            GlideApp.with(context)
                .load(storageReference.reference.child(Firebase.auth.currentUser!!.uid).child(product.image!!))
                .apply(RequestOptions.bitmapTransform(RoundedCorners(80)))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.imageView)
        }else{
            holder.binding.imageView.visibility = View.GONE
        }

        holder.binding.codeView.text        = product.product_code
        holder.binding.quantityView.text    = context.resources.getString(R.string.quantity_format, product.quantity)

        holder.binding.root.setOnClickListener {
            if(holder.binding.imageView.visibility == View.GONE){


                if(product.image != null && Firebase.auth.currentUser != null){
                    holder.binding.imageView.visibility = View.VISIBLE

                    recyclerView.smoothSnapToPosition(holder.bindingAdapterPosition)

                    if(expandedHolder !=-1){
                        val holderToNotify = expandedHolder
                        expandedHolder = holder.bindingAdapterPosition
                        notifyItemChanged(holderToNotify)
                    }else{
                        expandedHolder = holder.bindingAdapterPosition
                    }

                    val circularProgressDrawable = CircularProgressDrawable(context)
                    circularProgressDrawable.strokeWidth = 5f
                    circularProgressDrawable.centerRadius = 30f
                    circularProgressDrawable.setColorSchemeColors(Color.WHITE)
                    circularProgressDrawable.setStyle(CircularProgressDrawable.LARGE)
                    circularProgressDrawable.centerRadius = 100f
                    circularProgressDrawable.start()

                    GlideApp.with(context)
                        .load(storageReference.reference.child(Firebase.auth.currentUser!!.uid).child(product.image!!))
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(80)))
                        .placeholder(circularProgressDrawable)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(holder.binding.imageView)
                }

            }else{
                holder.binding.imageView.visibility = View.GONE
                expandedHolder = -1
            }
        }

        if(product.image != null && Firebase.auth.currentUser != null) {
            holder.binding.imageView.setOnClickListener {
                context.startActivity(Intent(context, ImageViewerActivity::class.java).putExtra("image_uri", product.image))
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setData(items : ArrayList<Product>?){
        if(items != null){
            this.items = items
            notifyDataSetChanged()
        }
    }

    override fun onSwipe(adapterPosition: Int) {
        if(adapterPosition < expandedHolder){
            expandedHolder--
        }else if(adapterPosition == expandedHolder){
            expandedHolder = -1
        }

        DataRepository(context).deleteProduct(items[adapterPosition].product_code)
        items.remove(items[adapterPosition])
        notifyItemRemoved(adapterPosition)
    }

    private fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }

}