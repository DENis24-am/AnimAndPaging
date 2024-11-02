package com.example.animandpaging

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

@Composable
fun OSM(modifier: Modifier = Modifier, content: @Composable Context.(MutableList<Overlay>) -> Unit) {
    val overlays = remember { mutableListOf<Overlay>() }

    var localContext = LocalContext.current

    AndroidView(
        factory = { context ->
            localContext = context
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(48.8588443, 2.2943506))
                setMultiTouchControls(true)

                overlays.forEach { overlay ->
                    this.overlays.add(overlay)
                }
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            mapView.overlays.addAll(overlays)
            mapView.invalidate()
        },
        modifier = modifier.fillMaxSize()
    )

    localContext.content(overlays)
}