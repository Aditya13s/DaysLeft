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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        observeEvents()
        setupListeners()
    }

    private fun observeEvents() {
        eventViewModel.events.observe(this) { events ->
            adapter.submitList(events, FilterOption.UPCOMING_ONLY)
            updateEmptyState(events)
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
        
        // Set to show upcoming events only by default
        eventViewModel.setSortOption(SortOption.DAYS_LEFT)
        eventViewModel.setFilterOption(FilterOption.UPCOMING_ONLY)
    }

    private fun setupListeners() {
        binding.addEvent.setOnClickListener {
            AddEditEventBottomSheet.newInstance()
                .show(supportFragmentManager, "AddEventBottomSheet")
        }
        
        binding.btnMenu.setOnClickListener {
            showFilterMenu()
        }
    }
    
    private fun showFilterMenu() {
        val options = arrayOf(
            "Upcoming Events",
            "Past Events",
            "All Events"
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle("View Events")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        binding.textMainTitle.text = "Upcoming Events"
                        eventViewModel.setFilterOption(FilterOption.UPCOMING_ONLY)
                    }
                    1 -> {
                        binding.textMainTitle.text = "Past Events"
                        eventViewModel.setFilterOption(FilterOption.PAST)
                    }
                    2 -> {
                        binding.textMainTitle.text = "All Events"
                        eventViewModel.setFilterOption(FilterOption.ALL)
                    }
                }
                // Re-observe to update the display
                eventViewModel.events.observe(this) { events ->
                    val currentFilter = eventViewModel.filterOption.value ?: FilterOption.UPCOMING_ONLY
                    adapter.submitList(events, currentFilter)
                    updateEmptyState(events)
                }
            }
            .show()
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
        Snackbar.make(binding.root, "Event deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                recentlyDeletedEvent?.let { eventViewModel.addEvent(it) }
            }.show()
    }

    private fun launchEditEvent(event: Event) {
        AddEditEventBottomSheet.newInstance(event)
            .show(supportFragmentManager, "EditEventBottomSheet")
    }

    private fun updateEmptyState(events: List<Event>) {
        val isEmpty = events.isEmpty()
        binding.emptyStateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.eventRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}