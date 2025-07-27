package com.aditya.daysleft.presentation.eventlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditya.daysleft.databinding.ItemEventBinding
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.utils.DaysLeftUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(
    private val onEdit: (Event) -> Unit, private val onDelete: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var events: List<Event> = emptyList()

    fun submitList(list: List<Event>) {
        events = list
        notifyDataSetChanged()
    }

    inner class EventViewHolder(val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.textTitle.text = event.title
            binding.textDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                Date(
                    event.dateMillis
                )
            )
            binding.textDaysLeft.text = "Days left: ${DaysLeftUtil.daysLeft(event.dateMillis)}"
            binding.btnEdit.setOnClickListener { onEdit(event) }
            binding.btnDelete.setOnClickListener { onDelete(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size
}