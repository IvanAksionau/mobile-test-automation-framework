package com.ivan.automation.core.mobile.api

import com.ivan.automation.core.mobile.utils.PropertiesManager

object ServiceConstants {

    val BASE_URI: String = PropertiesManager.getParameter("ivan.base.uri")
    val DASHBOARD_URI: String = PropertiesManager.getParameter("ivan.dashboard.uri")
    const val SERVICE_TIMEOUT_MS = 60000
}
