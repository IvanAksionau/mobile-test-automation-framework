package com.ivan.automation.core.mobile.api.models.request

data class CreateDeviceProfileRequest(
    val enableAllProfiles: Boolean,
    val profiles: List<ProfileEntry>
) {
    data class ProfileEntry(
        val enableAllStores: Boolean,
        val profile: String,
        val stores: List<String>
    )
}