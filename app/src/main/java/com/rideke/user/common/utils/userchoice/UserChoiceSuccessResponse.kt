package com.rideke.user.common.utils.userchoice

interface UserChoiceSuccessResponse {
    fun onSuccessUserSelected(type: String?, userChoiceData: String?, userChoiceCode: String?)
}