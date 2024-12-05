package com.godongijo.ecotainment.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R

class SkeletonAdapter(
    private val itemCount: Int,
    private val layoutType: Int
) : RecyclerView.Adapter<SkeletonAdapter.SkeletonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkeletonViewHolder {
        val layoutRes = when (layoutType) {
            1 -> R.layout.skeleton_view_product // Skeleton Product
            2 -> R.layout.skeleton_view_cart
            3 -> R.layout.skeleton_order_status
            else -> R.layout.skeleton_view_product
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return SkeletonViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkeletonViewHolder, position: Int) {
        // Tidak ada data binding untuk skeleton
    }

    override fun getItemCount(): Int = itemCount

    class SkeletonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
