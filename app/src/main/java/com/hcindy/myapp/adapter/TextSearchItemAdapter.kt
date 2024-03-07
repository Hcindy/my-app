package com.hcindy.myapp.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.hcindy.myapp.R

class TextSearchItemAdapter(
    private val ctx: Context,
    private val window: Boolean,
    private val dataset: List<String>
) : RecyclerView.Adapter<TextSearchItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.item_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater
            .from(parent.context)
            .inflate(
                if (window) R.layout.window_text_search_list_item
                else R.layout.activity_text_search_list_item,
                parent,
                false
            )

        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.textView.text = item
        holder.textView.setOnLongClickListener {
            val manager = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            manager.setPrimaryClip(
                ClipData.newPlainText("Res", (it as TextView).text)
            )
            Toast.makeText(ctx, "已复制", Toast.LENGTH_SHORT).show()
            true
        }
        if (window) return
        if (position % 2 == 0) holder.textView.setBackgroundColor(Color.parseColor("#efefef"))
        else holder.textView.setBackgroundColor(Color.parseColor("#ffffff"))
    }

    override fun getItemCount() = dataset.size

}