package com.godongijo.ecotainment.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.SkeletonLayoutType

class SkeletonAdapter(
    private val itemCount: Int,
    private val layoutType: SkeletonLayoutType
) : RecyclerView.Adapter<SkeletonAdapter.SkeletonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkeletonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutType.layoutResId, parent, false)
        return SkeletonViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkeletonViewHolder, position: Int) {
        // Tidak ada data binding untuk skeleton
    }

    override fun getItemCount(): Int = itemCount

    class SkeletonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
