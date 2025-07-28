package com.aditya.daysleft.presentation.eventlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditya.daysleft.databinding.ItemEventBinding
import com.aditya.daysleft.databinding.ItemSectionHeaderBinding
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.utils.DaysLeftUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(
    private val onEdit: (Event) -> Unit, 
    private val onDelete: (Event) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listItems: List<EventListItem> = emptyList()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_EVENT = 1
    }

    fun submitList(events: List<Event>) {
        listItems = buildListItems(events)
        notifyDataSetChanged()
    }

    private fun buildListItems(events: List<Event>): List<EventListItem> {
        if (events.isEmpty()) return emptyList()
        
        val sortedEvents = events.sortedBy { it.dateMillis }
        val upcomingEvents = sortedEvents.filter { DaysLeftUtil.isUpcomingEvent(it.dateMillis) }
        val pastEvents = sortedEvents.filter { DaysLeftUtil.isPastEvent(it.dateMillis) }
        
        val listItems = mutableListOf<EventListItem>()
        
        // Add upcoming events section
        if (upcomingEvents.isNotEmpty()) {
            listItems.add(EventListItem.SectionHeader(EventSection.UPCOMING.title))
            upcomingEvents.forEach { event ->
                listItems.add(EventListItem.EventItem(event))
            }
        }
        
        // Add past events section
        if (pastEvents.isNotEmpty()) {
            listItems.add(EventListItem.SectionHeader(EventSection.PAST.title))
            pastEvents.reversed().forEach { event -> // Most recent past events first
                listItems.add(EventListItem.EventItem(event))
            }
        }
        
        return listItems
    }

    inner class EventViewHolder(val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.textTitle.text = event.title
            binding.textDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                Date(event.dateMillis)
            )
            
            // Update days text based on whether event is past or upcoming
            val daysSinceOrUntil = DaysLeftUtil.daysSinceOrUntil(event.dateMillis)
            binding.textDaysLeft.text = if (DaysLeftUtil.isPastEvent(event.dateMillis)) {
                if (daysSinceOrUntil == 0) "Today" else "$daysSinceOrUntil days ago"
            } else {
                if (daysSinceOrUntil == 0) "Today" else "$daysSinceOrUntil days left"
            }
            
            binding.btnEdit.setOnClickListener { onEdit(event) }
            binding.btnDelete.setOnClickListener { onDelete(event) }
        }
    }
    
    inner class SectionHeaderViewHolder(val binding: ItemSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.textSectionHeader.text = title
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (listItems[position]) {
            is EventListItem.SectionHeader -> VIEW_TYPE_HEADER
            is EventListItem.EventItem -> VIEW_TYPE_EVENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemSectionHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SectionHeaderViewHolder(binding)
            }
            VIEW_TYPE_EVENT -> {
                val binding = ItemEventBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                EventViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItems[position]) {
            is EventListItem.SectionHeader -> {
                (holder as SectionHeaderViewHolder).bind(item.title)
            }
            is EventListItem.EventItem -> {
                (holder as EventViewHolder).bind(item.event)
            }
        }
    }

    override fun getItemCount(): Int = listItems.size
}