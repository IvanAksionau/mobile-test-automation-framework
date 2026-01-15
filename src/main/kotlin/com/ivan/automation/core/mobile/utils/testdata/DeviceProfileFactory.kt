package com.ivan.automation.core.mobile.utils.testdata

import com.ivan.automation.core.mobile.api.models.request.CreateDeviceProfileRequest
import com.ivan.automation.core.mobile.api.models.request.CreateDeviceProfileRequest.ProfileEntry

/**
 * Factory for creating device profile configurations.
 */
object DeviceProfileFactory {

    /**
     * Creates a device profile request configured for mobile devices only.
     *
     * @return [CreateDeviceProfileRequest] with mobile profile enabled and all others disabled.
     */
    fun createMobileOnlyProfile(): CreateDeviceProfileRequest {
        return CreateDeviceProfileRequest(
            enableAllProfiles = false,
            profiles = listOf(
                ProfileEntry(enableAllStores = true, profile = "MOBILE", stores = emptyList()),
                ProfileEntry(enableAllStores = false, profile = "TV", stores = emptyList()),
                ProfileEntry(enableAllStores = false, profile = "ANDROID_TV", stores = emptyList()),
                ProfileEntry(enableAllStores = false, profile = "FIRE_TV", stores = emptyList()),
                ProfileEntry(enableAllStores = false, profile = "WEB", stores = emptyList()),
                ProfileEntry(enableAllStores = false, profile = "CHROMECAST", stores = emptyList()),
                ProfileEntry(enableAllStores = false, profile = "ROKU", stores = emptyList())
            )
        )
    }
}
