package org.jb.tom.tc.localization

import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.SimplePageExtension

class LocalizationPageExtension(
    pagePlaces: PagePlaces,
    pluginDescriptor: PluginDescriptor,
) : SimplePageExtension(pagePlaces) {
    init {
        pluginName = "teamcity-localization"
        placeId = PlaceId.ALL_PAGES_HEADER
        includeUrl = pluginDescriptor.getPluginResourcesPath("localization.jsp")
        register()
    }
}
