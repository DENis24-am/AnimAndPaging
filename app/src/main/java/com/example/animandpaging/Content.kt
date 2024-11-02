package com.example.animandpaging

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var showPopup by remember { mutableStateOf(false) }
    var markerInfo by remember { mutableStateOf(MarkerInfo()) }
    var popupOffset by remember { mutableStateOf(Offset.Zero) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val markerRef = remember { mutableStateOf<Marker?>(null) }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }

    val customIcon = context.getDrawable(R.drawable.location)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                fetchCurrentLocation(fusedLocationClient) { location ->
                    currentLocation = GeoPoint(location.latitude, location.longitude)
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                MapView(context).apply {
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    mapViewRef.value = this
                }
            },
            update = { mapView ->
                mapView.overlays.removeIf { it is Marker }

                currentLocation?.let { location ->
                    val userMarker = Marker(mapView).apply {
                        position = location
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Your location"
                    }
                    mapView.overlays.add(userMarker)
                }

                val shopMarker1 = Marker(mapView).apply {
                    icon = customIcon
                    position = GeoPoint(48.506904010387935, 32.26054038756296)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Shop1"
                    setOnMarkerClickListener { clickedMarker, _ ->
                        markerInfo = MarkerInfo(
                            title = clickedMarker.title ?: "ShopMarker",
                            description = "Marker description: ${clickedMarker.title}.",
                            position = clickedMarker.position
                        )
                        popupOffset = updatePopupPosition(mapView, clickedMarker)
                        showPopup = true

                        markerRef.value = clickedMarker

                        currentLocation?.let { userLocation ->
                            fetchAndDrawRoute(
                                mapView,
                                userLocation,
                                clickedMarker.position
                            ) { polyline, distance, duration ->
                                routePolyline = polyline
                                markerInfo = markerInfo.copy(
                                    distance = distance,
                                    duration = duration
                                )
                            }
                        }

                        true
                    }
                }
                mapView.overlays.add(shopMarker1)


                val shopMarker2 = Marker(mapView).apply {
                    icon = customIcon
                    position = GeoPoint(48.47708156721042, 32.19018804741356)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Shop2"
                    setOnMarkerClickListener { clickedMarker, _ ->
                        markerInfo = MarkerInfo(
                            title = clickedMarker.title ?: "ShopMarker",
                            description = "Marker description: ${clickedMarker.title}.",
                            position = clickedMarker.position
                        )
                        popupOffset = updatePopupPosition(mapView, clickedMarker)
                        showPopup = true

                        markerRef.value = clickedMarker

                        currentLocation?.let { userLocation ->
                            fetchAndDrawRoute(
                                mapView,
                                userLocation,
                                clickedMarker.position
                            ) { polyline, distance, duration ->
                                routePolyline = polyline
                                markerInfo = markerInfo.copy(
                                    distance = distance,
                                    duration = duration
                                )
                            }
                        }

                        true
                    }
                }
                mapView.overlays.add(shopMarker2)

                routePolyline?.let { mapView.overlays.add(it) }

                mapView.addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        if (showPopup) {
                            markerRef.value?.let { marker ->
                                popupOffset = updatePopupPosition(mapView, marker)
                            }
                        }
                        return true
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        if (showPopup) {
                            markerRef.value?.let { marker ->
                                popupOffset = updatePopupPosition(mapView, marker)
                            }
                        }
                        return true
                    }
                })
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showPopup) {
            MarkerPopup(
                markerInfo = markerInfo,
                offset = popupOffset,
                onDismiss = { showPopup = false }
            )
        }
    }
}

fun fetchAndDrawRoute(
    mapView: MapView,
    startPoint: GeoPoint,
    endPoint: GeoPoint,
    onRouteReady: (Polyline, String, String) -> Unit
) {
    val client = OkHttpClient()
    val apiKey = "5b3ce3597851110001cf6248fe1c587920d442c8a8106bc356d68b30"
    val url =
        "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$apiKey&start=${startPoint.longitude},${startPoint.latitude}&end=${endPoint.longitude},${endPoint.latitude}"

    Log.e("LOG_TAG", "$url")

    val request = Request.Builder().url(url).build()
    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val coordinates = json.getJSONArray("features")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates")

                    val summary = json.getJSONArray("features")
                        .getJSONObject(0)
                        .getJSONObject("properties")
                        .getJSONObject("summary")

                    val distance = summary.getDouble("distance")
                    val duration = summary.getDouble("duration") / 60

                    val path = Polyline()
                    path.color = android.graphics.Color.BLUE
                    path.width = 5f

                    for (i in 0 until coordinates.length()) {
                        val point = coordinates.getJSONArray(i)
                        val lon = point.getDouble(0)
                        val lat = point.getDouble(1)
                        path.addPoint(GeoPoint(lat, lon))
                    }

                    mapView.post {
                        val distanceStr = String.format("%.2f m", distance)
                        val durationStr = String.format("%.1f mins", duration)
                        onRouteReady(path, distanceStr, durationStr)

                        mapView.overlays.add(path)
                        mapView.invalidate()
                    }
                }
            }
        }
    })
}


@SuppressLint("MissingPermission")
fun fetchCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        location?.let {
            onLocationReceived(it)
        }
    }
}

fun updatePopupPosition(mapView: MapView, marker: Marker): Offset {
    val screenPoint = mapView.projection.toPixels(marker.position, null)
    return Offset(screenPoint.x.toFloat(), screenPoint.y.toFloat())
}

@Composable
fun MarkerPopup(
    markerInfo: MarkerInfo,
    offset: Offset,
    onDismiss: () -> Unit
) {
    val customShape = GenericShape { size, _ ->
        val pyramidWidth = size.width / 3f
        val pyramidHeight = size.height / 8f
        val pyramidCenterX = size.width / 2f
        val cornerRadius = 48.dp.value

        addRoundRect(
            RoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height - pyramidHeight,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        )
        moveTo(pyramidCenterX, size.height)
        lineTo(pyramidCenterX - pyramidWidth / 2, size.height - pyramidHeight)
        lineTo(pyramidCenterX + pyramidWidth / 2, size.height - pyramidHeight)
        close()
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    offset.x.toInt() - 100.dp.roundToPx(),
                    offset.y.toInt() - 320.dp.roundToPx()
                )
            }
            .size(200.dp, 300.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxSize(),
            shadowElevation = 6.dp,
            shape = customShape
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = markerInfo.title)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = markerInfo.description)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Distance: ${markerInfo.distance}")
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Duration: ${markerInfo.duration}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss) {
                    Text("Закрыть")
                }
            }
        }
    }
}


data class MarkerInfo(
    val title: String = "",
    val description: String = "",
    val position: GeoPoint = GeoPoint(0.0, 0.0),
    val distance: String = "",
    val duration: String = ""
)

