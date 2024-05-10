package com.example.locationapp

import android.content.Context
import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LocationViewModel = viewModel()
            LocationAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    MyApp(viewModel)

                }

            }
        }
    }
}

@Composable
fun MyApp(viewModel: LocationViewModel) {
    val context = LocalContext.current
    val locationUtils = LocationUtils(context)

    LocationDisplay(locationUtils = locationUtils, viewModel, context = context)
}


@Composable
fun LocationDisplay(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    context: Context
) {
    val location = viewModel.location.value

    val address = location?.let {
        locationUtils.reverseGeocodeLocation(location)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {
                //I have access to location

                locationUtils.requestLocationUpdates(viewModel = viewModel)

            } else {
                // Ask for permission
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION

                )

                if (rationaleRequired) {
                    Toast.makeText(
                        context,
                        "Location permission is required for this feature to work.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Location permission is required. Please enable it in Android settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

        }
    )




    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (location != null) {
            Text(
                "Latitude is: ${location.latitude} Longitude is: ${location.longitude}\n Current address: $address ",
                modifier = Modifier.padding(8.dp),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold

            )
        } else {
            Text(text = "Location not available.")

        }



        Button(onClick = {
            if (locationUtils.hasLocationPermission(context)) {
                //Permission already granted update the location
                locationUtils.requestLocationUpdates(viewModel)


            } else {
                //Request location permission
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )


            }
        }) {
            Text(text = "Get Location")
        }

    }
}