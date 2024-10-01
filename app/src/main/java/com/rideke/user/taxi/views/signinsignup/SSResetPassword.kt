package com.rideke.user.taxi.views.signinsignup


/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage signin_signup
 * @category SSResetPassword
 * @author SMR IT Solutions
 *
 */

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.rideke.user.R
import com.rideke.user.common.configs.SessionManager
import com.rideke.user.common.datamodels.JsonResponse
import com.rideke.user.common.interfaces.ApiService
import com.rideke.user.common.interfaces.ServiceListener
import com.rideke.user.common.network.AppController
import com.rideke.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.rideke.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY
import com.rideke.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.rideke.user.common.utils.CommonMethods
import com.rideke.user.common.utils.RequestCallback
import com.rideke.user.common.views.CommonActivity
import com.rideke.user.taxi.datamodels.signinsignup.SigninResult
import com.rideke.user.taxi.views.customize.CustomDialog
import com.rideke.user.taxi.views.main.MainActivity
import kotlinx.android.synthetic.main.activity_ss_resetpassword.*
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

/* ************************************************************
   Rider can reset password when forgot the password
   ************************************************************ */

class SSResetPassword : CommonActivity(), ServiceListener {

    lateinit var dialog: AlertDialog

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    @BindView(R.id.input_password)
    lateinit var input_password: EditText

    @BindView(R.id.input_confirmpassword)
    lateinit var input_confirmpassword: EditText

    @BindView(R.id.input_layout_password)
    lateinit var input_layout_password: TextInputLayout

    @BindView(R.id.next)
    lateinit var nextButton: RelativeLayout
    lateinit var signinResult: SigninResult
    protected var isInternetAvailable: Boolean = false
    private var facebookVerifiedMobileNumberCountryCode = ""
    private var facebookVerifiedMobileNumberCountryNameCode = ""
    private var facebookKitVerifiedMobileNumber = ""

    /**
     * Reset Password next button clicked
     */
    @OnClick(R.id.next)
    operator fun next() {
        onNext()
    }

    @OnClick(R.id.back)
    fun back() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ss_resetpassword)
        ButterKnife.bind(this)
        AppController.appComponent.inject(this)
        commonMethods.setheaderText(resources.getString(R.string.resetpasswords), common_header)
        getMobileNumerAndCountryCodeFromIntent()
        dialog = commonMethods.getAlertDialog(this)
    }

    private fun getMobileNumerAndCountryCodeFromIntent() {
        if (intent != null) {
            facebookKitVerifiedMobileNumber = intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY).toString()
            facebookVerifiedMobileNumberCountryCode = intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY).toString()
            facebookVerifiedMobileNumberCountryNameCode = intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY).toString()

        }
    }


    fun onNext() {
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        if (!isInternetAvailable) {
            commonMethods.showMessage(this, dialog, getString(R.string.no_connection))
        } else {
            if (!validateFirst()) {
                return
            } else if (!validateConfirmPassword()) {
                return
            } else {
                nextButton.background = ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_yellow)
                val input_password_str = input_password.text.toString()
                val input_password_confirmstr = input_confirmpassword.text.toString()
                if (input_password_str.length > 5 && input_password_confirmstr.length > 5 && input_password_confirmstr == input_password_str) {

                    sessionManager.password = input_password_str
                    forgotPassword(input_password_str)

                } else {
                    if (input_password_confirmstr != input_password_str) {
                        commonMethods.showMessage(this, dialog, resources.getString(R.string.Passwordmismatch))
                    } else {
                        commonMethods.showMessage(this, dialog, resources.getString(R.string.InvalidPassword))
                    }
                }
            }

        }

        hideSoftKeyboard()
    }


    /**
     * Validate first name
     */
    private fun validateFirst(): Boolean {
        if (input_password.text.toString().trim { it <= ' ' }.isEmpty()) {
            //input_layout_password.error = getString(R.string.Enteryourpassword)
            error_password.visibility = View.VISIBLE
            requestFocus(input_password)
            return false
        } else {
            //input_layout_password.isErrorEnabled = false
            error_password.visibility = View.GONE
        }

        return true
    }

    private fun validateConfirmPassword(): Boolean {
        if (input_confirmpassword.text.toString().trim { it <= ' ' }.isEmpty()) {
            error_confirm_password.visibility = View.VISIBLE
            requestFocus(input_password)
            return false
        } else {
            //input_layout_password.isErrorEnabled = false
            error_confirm_password.visibility = View.GONE
        }

        return true
    }

    /**
     * Edit text request focus
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    /**
     * Back button pressed
     */
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, SigninSignupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
        this.finish()
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            commonMethods.hideProgressDialog()
            onSuccessPwd(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    /**
     * Forgot password API called
     */
    fun forgotPassword(pwd: String) {
        commonMethods.showProgressDialog(this)
        apiService.forgotpassword(facebookKitVerifiedMobileNumber, facebookVerifiedMobileNumberCountryNameCode, sessionManager.type.toString(), pwd, sessionManager.deviceType!!, sessionManager.deviceId!!).enqueue(RequestCallback(this))
    }

    fun onSuccessPwd(jsonResp: JsonResponse) {
        signinResult = gson.fromJson(jsonResp.strResponse, SigninResult::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sessionManager.currencySymbol = Html.fromHtml(signinResult.currencySymbol, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            sessionManager.currencySymbol = Html.fromHtml(signinResult.currencySymbol).toString()
        }
        sessionManager.walletAmount = signinResult.walletAmount
        sessionManager.isrequest = false
        sessionManager.accessToken = signinResult.token
        sessionManager.userId = signinResult.userId

        try {
            val response = JSONObject(jsonResp.strResponse)
            if (response.has("promo_details")) {
                val promocount = response.getJSONArray("promo_details").length()
                sessionManager.promoDetail = response.getString("promo_details")
                sessionManager.promoCount = promocount
            }
        } catch (j: JSONException) {
            j.printStackTrace()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }

    /**
     * Hide keyboard
     */
    private fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}
