package com.mistershorr.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.mistershorr.loginandregistration.databinding.ActivitySleepListBinding
import java.util.Date


class SleepListActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySleepListBinding
    companion object{
        var TAG = "bloop"

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AdvancedObjectRetrieval()
        Log.d(TAG, "onCreate: successful start of sleeplist")
        binding.floatingActionButtonSleepListNew.setOnClickListener(){
            val intent = Intent(this,
                SleepDetailActivity::class.java)
            startActivity(intent)
        }
    }


    fun AdvancedObjectRetrieval(){
        Log.d(TAG, "AdvancedObjectRetrieval: ")
        val userId = Backendless.UserService.CurrentUser().userId
        val whereClause = "ownerId = '$userId'"
        val queryBuilder = DataQueryBuilder.create()
        Backendless.Data.of(Sleep::class.java).find(queryBuilder,
            object : AsyncCallback<MutableList<Sleep?>?> {
                override fun handleResponse(foundSleeps: MutableList<Sleep?>?) {
                    // the "foundContact" collection now contains instances of the Contact class.
                    // each instance represents an object stored on the server.
                    if (foundSleeps !=null){
                        Log.d(TAG, "handleResponse!!!: ${foundSleeps!!}")
                        setRecyclerView(foundSleeps!!)
                    }
                }
                override fun handleFault(fault: BackendlessFault) {
                    Log.d(TAG, "handleFault: ${fault.code}")
                }
            })


    }

    fun setRecyclerView(sleepList:MutableList<Sleep?>) {
        var adapter = SleepAdapter(sleepList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }



}