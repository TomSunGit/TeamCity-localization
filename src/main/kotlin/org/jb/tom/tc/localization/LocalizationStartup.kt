package org.jb.tom.tc.localization

import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.serverSide.BuildServerAdapter
import jetbrains.buildServer.serverSide.BuildServerListener
import jetbrains.buildServer.util.EventDispatcher

class LocalizationStartup(events: EventDispatcher<BuildServerListener>) {
    init {
        events.addListener(object : BuildServerAdapter() {
            override fun serverStartup() {
                Loggers.SERVER.info("=== TeamCity Localization plugin loaded ===")
            }
        })
    }
}
