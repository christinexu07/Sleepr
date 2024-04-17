package com.mistershorr.loginandregistration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
@Parcelize
data class Sleep(var wakeMillis:Long = System.currentTimeMillis(),
                 var bedMillis: Long = System.currentTimeMillis(),
                 var sleepDateMillis: Long = System.currentTimeMillis(),
                 var quality: Int=5,
                 var notes: String?="",
                 var ownerId: String? = null,
                 var objectId:String?=null): Parcelable
