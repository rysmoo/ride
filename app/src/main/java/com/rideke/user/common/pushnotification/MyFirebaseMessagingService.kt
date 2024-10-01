package com.rideke.user.common.pushnotification

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage pushnotification
 * @category FirebaseMessagingService
 * @author SMR IT Solutions
 * 
 */


import android.content.res.Configuration
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rideke.user.common.configs.SessionManager
import com.rideke.user.common.network.AppController
import com.rideke.user.common.utils.CommonMethods
import com.rideke.user.common.utils.CommonMethods.Companion.DebuggableLogE
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

/* ************************************************************
   Firebase Messaging Service to base push notification message to activity
   *************************************************************** */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var sessionManager: SessionManager

    private var title = ""
    private var body = ""
    private var infoPopUp: String? = ""

    override fun onNewToken(s: String) {
        super.onNewToken(s)


        //CabmeSinchService.getManagedPush(s).registerPushToken(this);
    }

    override fun onCreate() {
        super.onCreate()
        AppController.appComponent.inject(this)
        setLocale()

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        DebuggableLogE(TAG, "From tri: " + remoteMessage.from!!)
        DebuggableLogE(TAG, "From tri: $remoteMessage")
        DebuggableLogE(TAG, "Data Payload: " + remoteMessage.data.toString())

        Log.i(TAG, "onMessageReceived: data=${remoteMessage.data}")
        Log.i(TAG, "onMessageReceived: data=${remoteMessage.notification}")
        Log.i(TAG, "onMessageReceived: data=${remoteMessage.notification?.body}")
        Log.i(TAG, "onMessageReceived: data=${remoteMessage.notification?.title}")

       /* if (SinchHelpers.isSinchPushPayload(remoteMessage.data)) {

        }

        if (SinchHelpers.isSinchPushPayload(remoteMessage.data)) {

            DebuggableLogE("test push noti", "Sinch message")
            initSinchService()

        } else {

            DebuggableLogE("test push noti", "ours")

            // Check if message contains a data payload.
            if (remoteMessage.data.size > 0) {
                DebuggableLogE(TAG, "Data Payload: " + remoteMessage.data.toString())

                try {
                    val json = JSONObject(remoteMessage.data.toString())
                     commonMethods.handleDataMessage(json,this)
                    if (remoteMessage.notification != null) {
                        DebuggableLogE(TAG, "Notification Body: " + remoteMessage.notification!!.body!!)

                    }

                } catch (e: Exception) {
                    DebuggableLogE(TAG, "Exception: " + e.message)
                }

            }
        }*/

        DebuggableLogE("test push noti", "ours")

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            DebuggableLogE(TAG, "Data Payload: " + remoteMessage.data.toString())

            try {
                val json = JSONObject(remoteMessage.data.toString())
                commonMethods.handleDataMessage(json,this)
                if (remoteMessage.notification != null) {
                    DebuggableLogE(TAG, "Notification Body: " + remoteMessage.notification!!.body!!)

                }

            } catch (e: Exception) {
                DebuggableLogE(TAG, "Exception: " + e.message)
            }

        }
    }

   /* private fun showNotification(remoteMessage: RemoteMessage) {
        try {
            if (remoteMessage.notification != null) {
                title = remoteMessage.notification!!.title.toString()
                body = remoteMessage.notification!!.body.toString()
            } else {
                title = remoteMessage.data["title"].toString()
                body = remoteMessage.data["message"].toString()
                infoPopUp = remoteMessage.data["info_popup"]
                Log.i(TAG, "showNotification: infoPopUp=$infoPopUp")
            }
            if (infoPopUp != null) this.showPopInfoNotification(
                CHANNEL_ID,
                title,
                body,
                infoPopUp!!,
                2121
            ) else this.showAlertNotification(
                CHANNEL_ID, title, body
            )
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(applicationContext, notification)
                r.play()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } catch (e: java.lang.Exception) {
            appLogs(TAG, "showNotification: Err0r=" + e.localizedMessage)
        }
    }*/


    /**
     * set language
     */
    private fun setLocale() {
        val lang = sessionManager.language

        if (lang != "") {
            val langC = sessionManager.languageCode
            val locale = Locale(langC)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
           resources.updateConfiguration(config, this.resources.displayMetrics)
        } else {
            sessionManager.language = "English"
            sessionManager.languageCode = "en"
        }


    }


    companion object {

        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }


}
