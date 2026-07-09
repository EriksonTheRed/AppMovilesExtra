package com.example.appmoviles.video.provider

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

object NewPipeInitializer {

    private var initialized = false

    fun initialize() {

        if (initialized) return

        NewPipe.init(
            AndroidDownloader(),
            Localization("es", "MX"),
            ContentCountry("MX")
        )

        initialized = true
    }
}