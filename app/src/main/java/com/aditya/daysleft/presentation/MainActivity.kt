package com.aditya.daysleft.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditya.daysleft.R
import com.aditya.daysleft.data.local.AppDatabase
import com.aditya.daysleft.data.repository.EventRepositoryImpl
import com.aditya.daysleft.databinding.ActivityMainBinding
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.domain.model.SortOption
import com.aditya.daysleft.domain.model.FilterOption
import com.aditya.daysleft.domain.usecases.AddEvent
import com.aditya.daysleft.domain.usecases.DeleteEvent
import com.aditya.daysleft.domain.usecases.EventUseCases
import com.aditya.daysleft.domain.usecases.GetEvents
import com.aditya.daysleft.domain.usecases.UpdateEvent
import com.aditya.daysleft.presentation.addevent.AddEditEventBottomSheet
import com.aditya.daysleft.presentation.eventlist.EventAdapter
import com.aditya.daysleft.presentation.viewmodel.EventViewModel
import com.aditya.daysleft.presentation.viewmodel.EventViewModelFactory
import com.aditya.daysleft.utils.DaysLeftUtil
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var eventViewModel: EventViewModel
    private lateinit var adapter: EventAdapter
    private var recentlyDeletedEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViewModel()
        setupRecyclerView()
        setupFilter()
        setupDashboard()
        observeEvents()
        setupListeners()
    }

    private fun observeEvents() {
        eventViewModel.events.observe(this) { events ->
            updateDashboard(events)
            val currentFilter = eventViewModel.filterOption.value ?: FilterOption.ALL
            adapter.submitList(events, currentFilter)
            updateEmptyState(events, currentFilter)
        }
        
        // Also observe filter changes to update the adapter
        eventViewModel.filterOption.observe(this) { filterOption ->
            val currentEvents = eventViewModel.events.value ?: emptyList()
            adapter.submitList(currentEvents, filterOption)
            updateEmptyState(currentEvents, filterOption)
        }
    }

    private fun updateDashboard(events: List<Event>) {
        val todayCount = events.count { DaysLeftUtil.isTodayEvent(it.dateMillis) }
        val upcomingCount = events.count { DaysLeftUtil.isUpcomingButNotToday(it.dateMillis) }
        val pastCount = events.count { DaysLeftUtil.isPastEvent(it.dateMillis) }
        
        binding.textTodayCount.text = todayCount.toString()
        binding.textUpcomingCount.text = upcomingCount.toString()
        binding.textPastCount.text = pastCount.toString()
    }

    private fun setupDashboard() {
        // Add click listeners for dashboard cards
        binding.cardToday.setOnClickListener {
            binding.chipToday.isChecked = true
            eventViewModel.setFilterOption(FilterOption.TODAY)
        }
        
        binding.cardUpcoming.setOnClickListener {
            binding.chipUpcoming.isChecked = true
            eventViewModel.setFilterOption(FilterOption.UPCOMING)
        }
        
        binding.cardPast.setOnClickListener {
            binding.chipPast.isChecked = true
            eventViewModel.setFilterOption(FilterOption.PAST)
        }
    }

    private fun setupViewModel() {
        val dao = AppDatabase.getInstance(application).eventDao()
        val repository = EventRepositoryImpl(dao)
        val eventUseCases = EventUseCases(
            getEvents = GetEvents(repository),
            addEvent = AddEvent(repository),
            updateEvent = UpdateEvent(repository),
            deleteEvent = DeleteEvent(repository)
        )
        val factory = EventViewModelFactory(application, eventUseCases)
        eventViewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    private fun setupFilter() {
        // Always sort by Days Left
        eventViewModel.setSortOption(SortOption.DAYS_LEFT)
        
        // Add accessibility descriptions for filter chips
        binding.chipAllEvents.contentDescription = "Show all events with sections"
        binding.chipToday.contentDescription = "Show only today's events"
        binding.chipUpcoming.contentDescription = "Show only upcoming events"
        binding.chipPast.contentDescription = "Show only past events"
        
        // Set up chip selection listeners
        binding.filterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val filterOption = when {
                checkedIds.contains(R.id.chipToday) -> FilterOption.TODAY
                checkedIds.contains(R.id.chipUpcoming) -> FilterOption.UPCOMING
                checkedIds.contains(R.id.chipPast) -> FilterOption.PAST
                else -> FilterOption.ALL
            }
            eventViewModel.setFilterOption(filterOption)
        }
    }

    private fun setupListeners() {
        binding.addEvent.setOnClickListener {
            AddEditEventBottomSheet.newInstance()
                .show(supportFragmentManager, "AddEventBottomSheet")
        }
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter(
            onEdit = { event -> launchEditEvent(event) },
            onDelete = { event -> handleDeleteEvent(event) })
        binding.eventRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.eventRecyclerView.adapter = adapter
    }

    private fun handleDeleteEvent(event: Event) {
        recentlyDeletedEvent = event
        eventViewModel.deleteEvent(event)
        Snackbar.make(binding.root, getString(R.string.event_deleted), Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo)) {
                recentlyDeletedEvent?.let { eventViewModel.addEvent(it) }
            }.show()
    }

    private fun launchEditEvent(event: Event) {
        AddEditEventBottomSheet.newInstance(event)
            .show(supportFragmentManager, "EditEventBottomSheet")
    }

    private fun updateEmptyState(events: List<Event>, filterOption: FilterOption) {
        val filteredEvents = when (filterOption) {
            FilterOption.TODAY -> events.filter { DaysLeftUtil.isTodayEvent(it.dateMillis) }
            FilterOption.UPCOMING -> events.filter { DaysLeftUtil.isUpcomingButNotToday(it.dateMillis) }
            FilterOption.PAST -> events.filter { DaysLeftUtil.isPastEvent(it.dateMillis) }
            FilterOption.ALL -> events
            else -> events
        }
        
        val isEmpty = filteredEvents.isEmpty()
        binding.emptyStateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.eventRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        
        // Update empty state text based on filter
        if (isEmpty) {
            val emptyText = when (filterOption) {
                FilterOption.TODAY -> "No events scheduled for today"
                FilterOption.UPCOMING -> "No upcoming events"
                FilterOption.PAST -> "No past events"
                else -> getString(R.string.empty_events_title)
            }
            binding.textEmpty.text = emptyText
        }
    }
}