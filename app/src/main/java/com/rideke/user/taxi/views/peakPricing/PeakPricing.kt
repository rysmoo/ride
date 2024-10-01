package com.rideke.user.taxi.views.peakPricing

import android.os.Bundle
import android.widget.TextView

import com.rideke.user.R
import com.rideke.user.common.configs.SessionManager
import com.rideke.user.common.network.AppController
import com.rideke.user.common.utils.CommonKeys

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.rideke.user.common.views.CommonActivity

class PeakPricing : CommonActivity() {
    @BindView(R.id.tv_peak_price_percentage) lateinit var tvPeakPricePercentage: TextView
    @BindView(R.id.tv_minimum_fare) lateinit var tvMinimumFare: TextView
    @BindView(R.id.tv_per_minutes) lateinit var tvPerMinutes: TextView
    @BindView(R.id.tv_per_distance) lateinit var tvPerDistance: TextView

    @Inject
    lateinit var sessionManager: SessionManager

    @OnClick(R.id.tv_accept_higherPrice)
    fun acceptHigherPriceButtonClick() {
        setResult(CommonKeys.PEAK_PRICE_ACCEPTED)
        finish()
    }

    @OnClick(R.id.tv_tryLater_higherPrice)
    fun declinedPeakPriceButtonClick() {
        setResult(CommonKeys.PEAK_PRICE_DENIED)
        finish()
    }

    @OnClick(R.id.imgvu_close_icon)
    fun closeActivity() {
        declinedPeakPriceButtonClick()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_peek_pricing)

        ButterKnife.bind(this)
        AppController.appComponent.inject(this)

        try {
            tvPeakPricePercentage.text = intent.getStringExtra(CommonKeys.KEY_PEAK_PRICE) + "x"
            tvMinimumFare.text = sessionManager.currencySymbol + intent.getStringExtra(CommonKeys.KEY_MIN_FARE)
            tvPerMinutes.text = sessionManager.currencySymbol + intent.getStringExtra(CommonKeys.KEY_PER_MINUTES)
            tvPerDistance.text = sessionManager.currencySymbol + intent.getStringExtra(CommonKeys.KEY_PER_KM)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}