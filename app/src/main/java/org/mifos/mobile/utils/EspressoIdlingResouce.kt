package org.mifos.mobile.utils

import androidx.test.espresso.idling.CountingIdlingResource

/**
 * Used for handling async operations in the app for UI testing using Espresso
 * https://developer.android.com/training/testing/espresso/idling-resource
 *
 * @author Ashwin Ramakrishnan
 * @since 21/08/2020
 */

object EspressoIdlingResouce {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}