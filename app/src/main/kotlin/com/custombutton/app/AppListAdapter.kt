package com.custombutton.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.custombutton.app.databinding.ItemAppBinding

class AppListAdapter(
    private val apps: List<AppInfo>,
    initialSelectedPackage: String?,
    private val onAppSelected: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private var selectedPackage: String? = initialSelectedPackage

    inner class AppViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.binding.appIcon.setImageDrawable(app.icon)
        holder.binding.appName.text = app.label
        holder.binding.checkIcon.visibility =
            if (app.packageName == selectedPackage) View.VISIBLE else View.INVISIBLE

        holder.binding.root.setOnClickListener {
            val previousIndex = apps.indexOfFirst { it.packageName == selectedPackage }
            selectedPackage = app.packageName
            onAppSelected(app)

            if (previousIndex >= 0) notifyItemChanged(previousIndex)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = apps.size
}
