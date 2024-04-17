package com.mistershorr.loginandregistration

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mistershorr.loginandregistration.databinding.ActivitySleepDetailBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class SleepDetailActivity : AppCompatActivity() {

    companion object {
        val TAG = "SleepDetailActivity"
        val EXTRA_SLEEP: String? = null
    }

    private lateinit var binding: ActivitySleepDetailBinding
    lateinit var bedTime: LocalDateTime
    lateinit var wakeTime: LocalDateTime
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE MMM dd, yyyy")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(intent.getParcelableExtra<Sleep>(EXTRA_SLEEP)!= null){
            setExistingValues()
        }
        else{
            setDefaultValues()
        }

        binding.buttonSleepDetailBedTime.setOnClickListener {
            setTime(bedTime, timeFormatter, binding.buttonSleepDetailBedTime)
        }

        binding.buttonSleepDetailWakeTime.setOnClickListener {
            setTime(wakeTime, timeFormatter, binding.buttonSleepDetailWakeTime)
        }

        binding.buttonSleepDetailDate.setOnClickListener {
            val selection = bedTime.toEpochSecond(ZoneOffset.UTC)
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(selection*1000) // requires milliseconds
                .setTitleText("Select a Date")
                .build()

            Log.d(TAG, "onCreate: after build: ${LocalDateTime.ofEpochSecond(datePicker.selection?: 0L, 0, ZoneOffset.UTC)}")
            datePicker.addOnPositiveButtonClickListener { millis ->
                val selectedLocalDate = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDateTime()
                Toast.makeText(this, "Date is: ${dateFormatter.format(selectedLocalDate)}", Toast.LENGTH_SHORT).show()

                // make sure that waking up the next day if waketime < bedtime is preserved
                var wakeDate = selectedLocalDate

                if(wakeTime.dayOfMonth != bedTime.dayOfMonth) {
                    wakeDate = wakeDate.plusDays(1)
                }

                bedTime = LocalDateTime.of(
                    selectedLocalDate.year,
                    selectedLocalDate.month,
                    selectedLocalDate.dayOfMonth,
                    bedTime.hour,
                    bedTime.minute
                )

                wakeTime = LocalDateTime.of(
                    wakeDate.year,
                    wakeDate.month,
                    wakeDate.dayOfMonth,
                    wakeTime.hour,
                    wakeTime.minute
                )
                binding.buttonSleepDetailDate.text = dateFormatter.format(bedTime)
            }
            datePicker.show(supportFragmentManager, "datepicker")
        }

        binding.buttonSleepDetailSave.setOnClickListener{
            if(intent.getParcelableExtra<Sleep>(EXTRA_SLEEP)!=null){ updateSleep() }
            else{saveSleep() }
        }

        binding.buttonSleepDetailCancel.setOnClickListener{
            finish()
        }

    }

    fun updateSleep(){
        var sleep = intent.getParcelableExtra<Sleep>(EXTRA_SLEEP)
        var wakeString = binding.buttonSleepDetailWakeTime.text.toString()
        var bedString = binding.buttonSleepDetailBedTime.text.toString()
        var dateString = binding.buttonSleepDetailDate.text.toString()
        var wakeMillis = wakeMillisConversion(wakeString,dateString)
        var bedMillis = bedMillisConversion(bedString,dateString)
        var dateMillis = dateConversion(dateString)
        Backendless.Data.of(Sleep::class.java).save(sleep, object : AsyncCallback<Sleep> {
            override fun handleResponse(savedSleep: Sleep) {
                savedSleep.wakeMillis = wakeMillis
                savedSleep.bedMillis = bedMillis
                savedSleep.sleepDateMillis=dateMillis
                savedSleep.quality=binding.ratingBarSleepDetailQuality.numStars
                savedSleep.notes=binding.editTextTextMultiLineSleepDetailNotes.text.toString()
                Backendless.Data.of(Sleep::class.java)
                    .save(savedSleep, object : AsyncCallback<Sleep?> {
                        override fun handleResponse(response: Sleep?) {
                            Log.d(SleepListActivity.TAG, "handleResponse: successful update")
                            finish()
                        }

                        override fun handleFault(fault: BackendlessFault) {
                            Log.d(SleepListActivity.TAG, "handleFault: ${fault.code}")
                        }
                    })
            }
            override fun handleFault(fault: BackendlessFault) {
                Log.d(TAG, "handleFault: ${fault.message}")
            }
        })
    }



    fun wakeMillisConversion(wakeTime:String, dateTime:String):Long{
        var newTime = dateTime + ' ' + wakeTime
        var timeConverter = DateTimeFormatter.ofPattern("EEEE MMM dd, yyyy HH:mm a")
        var date = LocalDateTime.parse(newTime, timeConverter)
        val zonedDate = ZonedDateTime.of(date, ZoneId.systemDefault())
        var millis= zonedDate.toInstant().toEpochMilli()
        if(dateTime.substring(6).equals("AM")){
            millis+=86400000
        }
        Log.d(TAG, "wakeMillisConversion: $millis")
        return millis
    }

    fun bedMillisConversion(bedTime:String, dateTime:String):Long{
        var newTime = dateTime + ' ' + bedTime
        var timeConverter = DateTimeFormatter.ofPattern("EEEE MMM dd, yyyy HH:mm a")
        var date = LocalDateTime.parse(newTime, timeConverter)
        val zonedDate = ZonedDateTime.of(date, ZoneId.systemDefault())
        var millis= zonedDate.toInstant().toEpochMilli()
        if(dateTime.substring(6).equals("AM")){
            millis+=86400000
        }
        Log.d(TAG, "bedMillisConversion: $millis")
        return millis
    }

    fun dateConversion(dateTime:String):Long{
        var dateTime2 = dateTime + " 00:00 AM"
        var timeConverter = DateTimeFormatter.ofPattern("EEEE MMM dd, yyyy HH:mm a")
        var date = LocalDateTime.parse(dateTime2,timeConverter)
        val zonedDate = ZonedDateTime.of(date, ZoneId.systemDefault())
        var millis= zonedDate.toInstant().toEpochMilli()
        Log.d(TAG, "dateConversion: $millis")
        return millis
    }
    @SuppressLint("SuspiciousIndentation")
    fun saveSleep(){
        Log.d(TAG, "saveSleep: tryna save")
        var wakeString = binding.buttonSleepDetailWakeTime.text.toString()
        var bedString = binding.buttonSleepDetailBedTime.text.toString()
        var dateString = binding.buttonSleepDetailDate.text.toString()
        var wakeMillis = wakeMillisConversion(wakeString,dateString)
        var bedMillis = bedMillisConversion(bedString,dateString)
        var dateMillis = dateConversion(dateString)

        Log.d(TAG, "saveSleep: successful conversion ")

        val sleep = Sleep(
            wakeMillis,
            bedMillis,
            dateMillis,
            binding.ratingBarSleepDetailQuality.numStars,
            binding.editTextTextMultiLineSleepDetailNotes.text.toString())
        Log.d(TAG, "saveSleep: ${sleep}")

        sleep.ownerId = Backendless.UserService.CurrentUser().userId
        Backendless.Data.of(Sleep::class.java).save(sleep, object : AsyncCallback<Sleep?> { override fun handleResponse(response: Sleep?) {
                Log.d(SleepDetailActivity.TAG, "handleResponse: successful save")
                finish()
            }
            override fun handleFault(fault: BackendlessFault) {
                Log.d(SleepDetailActivity.TAG, "handleFault: ${fault.message}")
            }
        })
    }
    fun setExistingValues(){
        Log.d(TAG, "setExistingValues: ")
        val sleep:Sleep? = intent.getParcelableExtra<Sleep>(EXTRA_SLEEP)
        bedTime = LocalDateTime.ofEpochSecond((sleep?.bedMillis!!/1000), 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        wakeTime = LocalDateTime.ofEpochSecond(sleep?.wakeMillis!!/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val date = LocalDateTime.ofEpochSecond(sleep?.sleepDateMillis!!/1000,0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        binding.buttonSleepDetailBedTime.text = timeFormatter.format(bedTime)
        binding.buttonSleepDetailWakeTime.text = timeFormatter.format(wakeTime)
        binding.buttonSleepDetailDate.text = dateFormatter.format(date)


    }
    fun setDefaultValues(){
        Log.d(TAG, "setDefaultValues: ")
        bedTime = LocalDateTime.now()
        binding.buttonSleepDetailBedTime.text = timeFormatter.format(bedTime)
        wakeTime = bedTime.plusHours(8)
        binding.buttonSleepDetailWakeTime.text = timeFormatter.format(wakeTime)
        binding.buttonSleepDetailDate.text = dateFormatter.format(bedTime)
    }
    fun setTime(time: LocalDateTime, timeFormatter: DateTimeFormatter, button: Button) {
        val timePickerDialog = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(time.hour)
            .setMinute(time.minute)
            .build()

        timePickerDialog.show(supportFragmentManager, "bedtime")
        timePickerDialog.addOnPositiveButtonClickListener {
            var selectedTime = LocalDateTime.of(time.year, time.month, time.dayOfMonth, timePickerDialog.hour, timePickerDialog.minute)
            button.text = timeFormatter.format(selectedTime)
            when(button.id) {
                binding.buttonSleepDetailBedTime.id -> {
                    bedTime = selectedTime
                    if(wakeTime.toEpochSecond(UTC) < selectedTime.toEpochSecond(UTC)) {
                        wakeTime = wakeTime.plusDays(1)
                    }
                }
                binding.buttonSleepDetailWakeTime.id -> {
                    if(selectedTime.toEpochSecond(UTC) < bedTime.toEpochSecond(UTC)) {
                        selectedTime = selectedTime.plusDays(1)
                    }
                    wakeTime = selectedTime
                }
            }
        }
    }
}