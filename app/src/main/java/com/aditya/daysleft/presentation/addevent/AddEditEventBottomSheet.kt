package com.aditya.daysleft.presentation.addevent

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.aditya.daysleft.R
import com.aditya.daysleft.databinding.BottomSheetAddEditEventBinding
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.presentation.viewmodel.EventViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditEventBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddEditEventBinding? = null
    private val binding get() = _binding!!

    private val eventViewModel: EventViewModel by activityViewModels()

    private var eventId: Int = 0
    private var selectedDateMillis: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddEditEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        eventId = arguments?.getInt("event_id", 0) ?: 0
        val eventTitle = arguments?.getString("event_title") ?: ""
        val eventDate = arguments?.getLong("event_date", 0L) ?: 0L

        if (eventId != 0) {
            binding.editTextTitle.setText(eventTitle)
            selectedDateMillis = eventDate
            binding.buttonDate.text = formatDate(selectedDateMillis)
            binding.buttonSave.text = getString(R.string.update)
        }

        // Automatically show keyboard for the title field
        binding.editTextTitle.requestFocus()
        binding.editTextTitle.post {
            val imm = activity?.getSystemService(InputMethodManager::class.java)
            imm?.showSoftInput(binding.editTextTitle, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.buttonDate.setOnClickListener { showDatePicker() }

        binding.buttonSave.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            if (title.isEmpty() || selectedDateMillis == 0L) {
                Toast.makeText(requireContext(), getString(R.string.fill_field), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val event = Event(id = eventId, title = title, dateMillis = selectedDateMillis)
            if (eventId == 0) eventViewModel.addEvent(event) else eventViewModel.updateEvent(event)
            dismiss()
        }

        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDateMillis != 0L) calendar.timeInMillis = selectedDateMillis
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val now = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(requireContext(), { _, y, m, d ->
            val cal = Calendar.getInstance()
            cal.set(y, m, d, 0, 0)
            selectedDateMillis = cal.timeInMillis
            binding.buttonDate.text = formatDate(selectedDateMillis)
        }, year, month, day)
        datePickerDialog.datePicker.minDate = now.timeInMillis // Prevent selecting past dates
        datePickerDialog.show()
    }

    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    }

    private fun isPastDate(dateMillis: Long): Boolean {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        return dateMillis < today.timeInMillis
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(event: Event? = null): AddEditEventBottomSheet {
            val fragment = AddEditEventBottomSheet()
            val args = Bundle()
            if (event != null) {
                args.putInt("event_id", event.id)
                args.putString("event_title", event.title)
                args.putLong("event_date", event.dateMillis)
            }
            fragment.arguments = args
            return fragment
        }
    }
}