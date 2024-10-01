package com.rideke.user.common.views

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.*
import com.rideke.user.R
import com.rideke.user.common.configs.SessionManager
import com.rideke.user.common.helper.Constants
import com.rideke.user.common.network.AppController
import com.rideke.user.common.utils.CommonKeys
import com.rideke.user.common.utils.CommonMethods
import com.rideke.user.taxi.views.customize.CustomDialog
import kotlinx.android.synthetic.main.activity_payment_web_view.*
import kotlinx.android.synthetic.main.app_common_header.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder
import javax.inject.Inject

class PaymentWebViewActivity : CommonActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    var type: String? = null

    lateinit var progressDialog: ProgressDialog

    var payFor: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_web_view)
        AppController.appComponent.inject(this)
        val payableWalletAmount = intent.getStringExtra("payableWalletAmount")
        payFor = intent.getStringExtra(Constants.INTENT_PAY_FOR)
        type = intent.getStringExtra(Constants.PAY_FOR_TYPE)
        commonMethods.setheaderText(resources.getString(R.string.payment), common_header)

        back.setOnClickListener {
            onBackPressed()
        }
        setProgress()

        if (!progressDialog.isShowing) {
            //progressDialog.show()
        }
        commonMethods.showProgressDialog(this)

        payment_wv.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (!progressDialog.isShowing) {
                    //progressDialog.show()
                }
                Log.i("PAYMENT_URL", "onPageStarted: url=$url")
                commonMethods.showProgressDialog(this@PaymentWebViewActivity)
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
                commonMethods.hideProgressDialog()
                Log.i("PAYMENT_URL", "onPageFinished: url=$url")
                payment_wv.loadUrl("javascript:android.showHTML(document.getElementById('data').innerHTML);")
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
                commonMethods.hideProgressDialog()
            }
        }

        payment_wv.settings.javaScriptEnabled = true
        payment_wv.addJavascriptInterface(MyJavaScriptInterface(this), "android")

        val url = resources.getString(R.string.base_url) + CommonKeys.PAYMENT_WEBVIEW_KEY
        val postData = "amount=" + URLEncoder.encode(payableWalletAmount, "UTF-8") + "&pay_for=" + URLEncoder.encode(payFor, "UTF-8") + "&payment_type=" + URLEncoder.encode(sessionManager.paymentMethod?.toLowerCase(), "UTF-8") + "&trip_id=" + URLEncoder.encode(sessionManager.tripId, "UTF-8")+ "&mode=" + URLEncoder.encode("light", "UTF-8") + "&token=" + URLEncoder.encode(sessionManager.accessToken, "UTF-8")
        payment_wv.postUrl(url, postData.toByteArray())
        Log.i("PAYMENT_TAG", "url="+url)
        Log.i("PAYMENT_TAG", "paymentMethod="+sessionManager.paymentMethod?.toLowerCase())
        Log.i("PAYMENT_TAG", "postData="+postData)
    }

    private fun setProgress() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(resources.getString(R.string.loading))
        progressDialog.setCancelable(false)
    }

    // Open previous opened link from history on webview when back button pressed
    // Detect when the back button is pressed
    override fun onBackPressed() {
        finish()
    }

    inner class MyJavaScriptInterface(private var ctx: Context) {
        @JavascriptInterface
        fun showHTML(html: String) {
            println("HTML$html")
            var response: JSONObject? = null
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
            try {
                response = JSONObject(html)
                Log.i("PAYMENT_TAG", "response="+response)
                if (response != null) {
                    val statusCode = response.getString("status_code")
                    val statusMessage = response.getString("status_message")

                    Log.i("PAYMENT_TAG", statusCode)
                    Log.i("PAYMENT_TAG", statusMessage)
                    redirect(response.toString())
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (t: Throwable) {
                Log.e("PAYMENT_TAG", "${t.message} \"$response\"")
            }
        }
    }

    private fun redirect(htmlResponse: String) {
        val intent = Intent()
        try {
            var walletAmount = ""
            val response = JSONObject(htmlResponse)
            if (response != null) {
                val statusCode = response.getString("status_code")
                val statusMessage = response.getString("status_message")
                intent.putExtra("status_message", statusMessage)
                if (statusCode.equals("1", true)) {
                    if (!payFor.isNullOrEmpty() && payFor.equals(CommonKeys.PAY_FOR_WALLET)) {
                        walletAmount = response.getString("wallet_amount")
                    }
                    if (walletAmount.isNotEmpty())
                        intent.putExtra("walletAmount", walletAmount)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}