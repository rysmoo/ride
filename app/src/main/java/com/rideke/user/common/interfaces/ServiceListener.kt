package com.rideke.user.common.interfaces

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage interfaces
 * @category ServiceListener
 * @author SMR IT Solutions
 * 
 */

import com.rideke.user.common.datamodels.JsonResponse

/*****************************************************************
 * ServiceListener
 */
interface ServiceListener {

    fun onSuccess(jsonResp: JsonResponse, data: String)

    fun onFailure(jsonResp: JsonResponse, data: String)
}
