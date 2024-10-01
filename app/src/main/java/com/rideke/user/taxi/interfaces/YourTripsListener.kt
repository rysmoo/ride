package com.rideke.user.taxi.interfaces

/**
 * @package com.cloneappsolutions.cabmeusereatsdriver
 * @subpackage interfaces
 * @category YourTripsListener
 * @author SMR IT Solutions
 * 
 */

import android.content.res.Resources

import com.rideke.user.taxi.sidebar.trips.YourTrips


/*****************************************************************
 * YourTripsListener
 */

interface YourTripsListener {

    val res: Resources

    val instance: YourTrips
}
