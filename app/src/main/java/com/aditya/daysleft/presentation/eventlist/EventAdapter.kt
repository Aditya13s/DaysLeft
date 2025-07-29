package com.aditya.daysleft.presentation.eventlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditya.daysleft.R
import com.aditya.daysleft.databinding.ItemEventBinding
import com.aditya.daysleft.databinding.ItemSectionHeaderBinding
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.domain.model.FilterOption
import com.aditya.daysleft.utils.DaysLeftUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(
    private val onEdit: (Event) -> Unit, 
    private val onDelete: (Event) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listItems: List<EventListItem> = emptyList()
    private var currentFilter: FilterOption = FilterOption.ALL

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_EVENT = 1
    }

    fun submitList(events: List<Event>, filterOption: FilterOption = FilterOption.ALL) {
        currentFilter = filterOption
        listItems = buildListItems(events, filterOption)
        notifyDataSetChanged()
    }

    private fun buildListItems(events: List<Event>, filterOption: FilterOption): List<EventListItem> {
        if (events.isEmpty()) return emptyList()
        
        val sortedEvents = events.sortedBy { it.dateMillis }
        val listItems = mutableListOf<EventListItem>()
        
        // For specific filters, don't show sections, just show the events
        when (filterOption) {
            FilterOption.UPCOMING -> {
                // Show only upcoming events without section header
                sortedEvents.filter { DaysLeftUtil.isUpcomingButNotToday(it.dateMillis) }.forEach { event ->
                    listItems.add(EventListItem.EventItem(event))
                }
            }
            FilterOption.UPCOMING_ONLY -> {
                // Show today and upcoming events without section header
                sortedEvents.filter { !DaysLeftUtil.isPastEvent(it.dateMillis) }.forEach { event ->
                    listItems.add(EventListItem.EventItem(event))
                }
            }
            FilterOption.PAST -> {
                // Show only past events without section header, most recent first
                sortedEvents.filter { DaysLeftUtil.isPastEvent(it.dateMillis) }.reversed().forEach { event ->
                    listItems.add(EventListItem.EventItem(event))
                }
            }
            FilterOption.NEXT_7_DAYS -> {
                // Show events in next 7 days without section header
                sortedEvents.forEach { event ->
                    listItems.add(EventListItem.EventItem(event))
                }
            }
            FilterOption.ALL -> {
                // Show all events with sections
                val todayEvents = sortedEvents.filter { DaysLeftUtil.isTodayEvent(it.dateMillis) }
                val upcomingEvents = sortedEvents.filter { DaysLeftUtil.isUpcomingButNotToday(it.dateMillis) }
                val pastEvents = sortedEvents.filter { DaysLeftUtil.isPastEvent(it.dateMillis) }
                
                // Add today events section
                if (todayEvents.isNotEmpty()) {
                    listItems.add(EventListItem.SectionHeader(EventSection.TODAY.title))
                    todayEvents.forEach { event ->
                        listItems.add(EventListItem.EventItem(event))
                    }
                }
                
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
            }
        }
        
        return listItems
    }

    inner class EventViewHolder(val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.textTitle.text = event.title
            binding.textDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
                Date(event.dateMillis)
            )
            
            // Update days text for the new simple badge
            val daysText = DaysLeftUtil.getRelativeDateText(event.dateMillis)
            binding.textDaysLeft.text = daysText
            
            // Enhanced accessibility
            val eventDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(event.dateMillis))
            binding.textTitle.contentDescription = "Event: ${event.title}"
            binding.textDate.contentDescription = "Date: $eventDate"
            binding.textDaysLeft.contentDescription = "Time remaining: $daysText"
            
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