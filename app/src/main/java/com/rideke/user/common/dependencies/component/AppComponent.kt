package com.rideke.user.common.dependencies.component

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage dependencies.component
 * @category AppComponent
 * @author SMR IT Solutions
 *
 */


import com.rideke.user.taxi.ScheduleRideDetailActivity
import com.rideke.user.taxi.adapters.CarDetailsListAdapter

import com.rideke.user.taxi.adapters.PastTripsPaginationAdapter
import com.rideke.user.taxi.adapters.PriceRecycleAdapter
import com.rideke.user.taxi.adapters.UpcomingAdapter
import com.rideke.user.taxi.adapters.UpcomingTripsPaginationAdapter
import com.rideke.user.common.backgroundtask.ImageCompressAsyncTask
import com.rideke.user.common.configs.RunTimePermission
import com.rideke.user.common.configs.SessionManager
import com.rideke.user.taxi.database.AddFirebaseDatabase
import com.rideke.user.common.dependencies.module.AppContainerModule
import com.rideke.user.common.dependencies.module.ApplicationModule
import com.rideke.user.common.dependencies.module.NetworkModule
import com.rideke.user.common.helper.CommonDialog
import com.rideke.user.common.drawpolyline.DownloadTask
import com.rideke.user.common.pushnotification.MyFirebaseInstanceIDService
import com.rideke.user.common.pushnotification.MyFirebaseMessagingService
import com.rideke.user.taxi.sendrequest.CancelYourTripActivity
import com.rideke.user.taxi.sendrequest.DriverNotAcceptActivity
import com.rideke.user.taxi.sendrequest.DriverRatingActivity
import com.rideke.user.taxi.sendrequest.PaymentAmountPage
import com.rideke.user.taxi.sendrequest.SendingRequestActivity
import com.rideke.user.taxi.sidebar.AddHome
import com.rideke.user.taxi.sidebar.DriverContactActivity
import com.rideke.user.taxi.sidebar.EnRoute
import com.rideke.user.taxi.sidebar.FareBreakdown
import com.rideke.user.taxi.sidebar.Profile
import com.rideke.user.taxi.sidebar.Setting
import com.rideke.user.taxi.sidebar.payment.*
import com.rideke.user.taxi.sidebar.referral.ShowReferralOptions
import com.rideke.user.taxi.sidebar.trips.Past
import com.rideke.user.taxi.sidebar.trips.Receipt
import com.rideke.user.taxi.sidebar.trips.TripDetails
import com.rideke.user.taxi.sidebar.trips.Upcoming
import com.rideke.user.taxi.sidebar.trips.YourTrips
import com.rideke.user.common.utils.CommonMethods
import com.rideke.user.common.utils.RequestCallback
import com.rideke.user.common.utils.userchoice.UserChoice
import com.rideke.user.common.views.*
import com.rideke.user.taxi.views.addCardDetails.AddCardActivity
import com.rideke.user.taxi.views.facebookAccountKit.FacebookAccountKitActivity
import com.rideke.user.taxi.views.emergency.EmergencyContact
import com.rideke.user.taxi.views.emergency.SosActivity
import com.rideke.user.taxi.views.firebaseChat.ActivityChat
import com.rideke.user.taxi.views.firebaseChat.AdapterFirebaseRecylcerview
import com.rideke.user.taxi.views.firebaseChat.FirebaseChatHandler
import com.rideke.user.taxi.views.main.MainActivity
import com.rideke.user.taxi.views.main.filter.FeaturesInVehicleAdapter
import com.rideke.user.taxi.views.peakPricing.PeakPricing
import com.rideke.user.taxi.views.search.PlaceSearchActivity
import com.rideke.user.taxi.views.signinsignup.*
import com.rideke.user.taxi.views.splash.SplashActivity

import javax.inject.Singleton

import dagger.Component


/*****************************************************************
 * App Component
 */
@Singleton
@Component(modules = [NetworkModule::class, ApplicationModule::class, AppContainerModule::class])
interface AppComponent {

    fun inject(splashActivity: SplashActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(scheduleRideDetailActivity: ScheduleRideDetailActivity)

    fun inject(sendingRequestActivity: SendingRequestActivity)

    fun inject(driverNotAcceptActivity: DriverNotAcceptActivity)

    fun inject(mainActivity: PlaceSearchActivity)

    fun inject(signin_signup_activity: SigninSignupActivity)

    fun inject(ssResetPassword: SSResetPassword)

    fun inject(ssSocialDetailsActivity: SSRegisterActivity)

    fun inject(driverContactActivity: DriverContactActivity)

    fun inject(addHome: AddHome)

    fun inject(paymentPage: PaymentPage)

    fun inject(paymentAmountPage: PaymentAmountPage)

    fun inject(fareBreakdown: FareBreakdown)

    fun inject(addWalletActivity: AddWalletActivity)

    fun inject(promoAmountActivity: PromoAmountActivity)

    fun inject(yourTrips: YourTrips)

    fun inject(tripDetails: TripDetails)

    fun inject(enRoute: EnRoute)

    fun inject(sos_activity: SosActivity)

    fun inject(driverRatingActivity: DriverRatingActivity)

    fun inject(cancelYourTripActivity: CancelYourTripActivity)

    fun inject(commonDialog: CommonDialog)

    fun inject(setting: Setting)

    fun inject(profile: Profile)


    fun inject(emergencyContact: EmergencyContact)

    fun inject(activityChat: ActivityChat)

    fun inject(facebookAccountKitActivity: FacebookAccountKitActivity)

    fun inject(loginActivity: SSLoginActivity)

    fun inject(peakPricing: PeakPricing)

    fun inject(showReferralOptions: ShowReferralOptions)


    // Fragments
    fun inject(past: Past)

    fun inject(upcoming: Upcoming)

    fun inject(receipt: Receipt)

    // Utilities
    fun inject(runTimePermission: RunTimePermission)

    fun inject(sessionManager: SessionManager)

    fun inject(commonMethods: CommonMethods)

    fun inject(requestCallback: RequestCallback)

    // Adapters

    fun inject(upcomingAdapter: UpcomingAdapter)


    fun inject(promoAmountAdapter: PromoAmountAdapter)

    fun inject(carDetailsListAdapter: CarDetailsListAdapter)

    fun inject(myFirebaseMessagingService: MyFirebaseMessagingService)

    fun inject(myFirebaseInstanceIDService: MyFirebaseInstanceIDService)

    fun inject(firebaseChatHandler: FirebaseChatHandler)

    fun inject(adapterFirebaseRecylcerview: AdapterFirebaseRecylcerview)


    // AsyncTask
    fun inject(imageCompressAsyncTask: ImageCompressAsyncTask)

    fun inject(downloadTask: DownloadTask)

    fun inject(firebaseDatabase: AddFirebaseDatabase)


    fun inject(priceRecycleAdapter: PriceRecycleAdapter)

    fun inject(pastTripsPaginationAdapter: PastTripsPaginationAdapter)

    fun inject(upcomingTripsPaginationAdapter: UpcomingTripsPaginationAdapter)
     fun inject(addCardActivity: AddCardActivity)
    fun inject(paymentMethodAdapter: PaymentMethodAdapter)
    fun inject(featuresInVehicleAdapter: FeaturesInVehicleAdapter)

    fun inject(supportActivityCommon: SupportActivityCommon)

    fun inject(supportAdapter: SupportAdapter)

    fun inject(paymentWebViewActivity: PaymentWebViewActivity)

    fun inject(commonActivity: CommonActivity)

    fun inject(userChoice: UserChoice)

}
