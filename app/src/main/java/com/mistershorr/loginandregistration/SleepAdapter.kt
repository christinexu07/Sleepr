package com.mistershorr.loginandregistration

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SleepAdapter(var dataSet: MutableList<Sleep?>) : RecyclerView.Adapter<SleepAdapter.ViewHolder>() {

    companion object {

        val TAG = "SleepAdapter"
        val ADDED_SLEEP = "added sleep"

    }




    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewDate : TextView
        val textViewHours: TextView
        val textViewDuration: TextView
        val layout : ConstraintLayout
        val ratingBarQuality : RatingBar
        init {
            textViewDate = view.findViewById(R.id.textView_itemSleep_date)
            textViewDuration = view.findViewById(R.id.textView_itemSleep_duration)
            textViewHours = view.findViewById(R.id.textView_itemSleep_hours)
            layout = view.findViewById(R.id.layout_itemSleep)
            ratingBarQuality = view.findViewById(R.id.ratingBar_itemSleep_sleepQuality)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sleep, parent, false)
        val holder = ViewHolder(view)
        return holder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: SleepAdapter.ViewHolder, position: Int) {
        val sleep = dataSet[position]
        val context = holder.layout.context


        val formatter = DateTimeFormatter.ofPattern("yyy-MM-dd")
        val sleepDate = LocalDateTime.ofEpochSecond(
            sleep!!.sleepDateMillis/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        holder.textViewDate.text = formatter.format(sleepDate)


        holder.textViewDuration.text = findDuration(sleep)


        // sets the actual hours slept textview
        val bedTime = LocalDateTime.ofEpochSecond(sleep.bedMillis/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val wakeTime = LocalDateTime.ofEpochSecond(sleep.wakeMillis/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        holder.textViewHours.text = "${timeFormatter.format(bedTime)} - ${timeFormatter.format(wakeTime)}"


        if (sleep != null) {
            holder.ratingBarQuality.rating = sleep.quality.toFloat()
        }

        //DELETE function
        holder.layout.isLongClickable = true
        holder.layout.setOnLongClickListener {
            // the holder.textViewBorrower is the textView that the PopMenu will be anchored to
            val popMenu = PopupMenu(context, holder.textViewDuration)
            popMenu.inflate(R.menu.menu_sleep_list_context)
            popMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.menu_sleeplist_delete -> {
                        deleteFromBackendless(position)
                        true
                    }
                    else -> true
                }
            }
            popMenu.show()
            true
        }


        //UPDATE function
        holder.layout.setOnClickListener {
            val intent = Intent(context, SleepDetailActivity::class.java).apply {
                putExtra(SleepDetailActivity.EXTRA_SLEEP, sleep)
            }
            context.startActivity(intent)

        }
    }

    fun findDuration(sleep:Sleep):String{
        var diffMillis:Long = (sleep.wakeMillis - sleep.bedMillis)
        var hours = diffMillis/3600000
        var minutes = diffMillis%3600000/60000
        var strHours = String.format("%02d",hours)
        var strMin = String.format("%02d", minutes)
        var time = strHours+":"+strMin
        return time
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    private fun deleteFromBackendless(position: Int) {
        // put in the code to delete the item using the callback from Backendless
        // in the handleResponse, we'll need to also delete the item from the sleepList
        // and make sure that the recyclerview is updated

                Backendless.Data.of(Sleep::class.java).remove(dataSet[position],
                    object: AsyncCallback<Long>
                    {
                        override fun handleResponse(response:Long)
                        {
                            Log.d(SleepListActivity.TAG, "handleResponse: successful deletion")
                            dataSet.remove(dataSet[position])
                            notifyDataSetChanged()
                        }
                        override fun handleFault(fault: BackendlessFault)
                        {
                            Log.d(TAG, "handleFault: unsuccessful deletion")
                        }
                    });
            }

    public fun addItem(sleep:Sleep){
        dataSet.add(sleep)
        notifyDataSetChanged()
    }


    }


