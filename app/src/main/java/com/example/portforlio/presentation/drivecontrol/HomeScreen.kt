package com.example.portforlio.presentation.drivecontrol

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.portforlio.Constants
import com.example.portforlio.R
import com.example.portforlio.presentation.MQTTConnectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.eclipse.paho.mqttv5.client.IMqttToken
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


@Composable
fun ControllerResult(
    modifier: Modifier,
    textDirection: String,
    imageView: Int,
    progress: Float,
    client: MQTTConnectionService
) {
    val velocity = progress * 255
    if (textDirection.isNotEmpty() && velocity > 0) {
        client.publish(
            Constants.topic,
            "$textDirection:${velocity.roundToInt()}",
            0,
            false
        )
    }
    Column(modifier = modifier) {
        if (imageView != 0 && progress != 0f) {
            Image(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(imageView),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.Blue)
            )
            Text(
                text = "$textDirection:$velocity",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color.Black,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}


@Composable
fun HomeScreen(modifier: Modifier, navController: NavController) {
    var imageDirection by remember { mutableIntStateOf(0) }
    var textDirection by remember { mutableStateOf("") }
    val progress = remember { mutableFloatStateOf(0.5f) }
    var showDialog by remember {
        mutableStateOf(false)
    }
    val imageCamera by remember { mutableStateOf<ImageBitmap?>(null) }
    var connectionState by remember {
        mutableStateOf("Connection ... ")
    }
    val client: MQTTConnectionService = remember {
        MQTTConnectionService(
            serverURI = Constants.uriServer,
            clientID = "12312421414"
        )
    }
    val scope = rememberCoroutineScope()

    val okHttpClient = remember {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    var imageBitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }
    LaunchedEffect(Unit) {
        try {
            withContext(Dispatchers.IO) {
                scope.launch {
                    fetchWebcamStream(okHttpClient, Constants.localUrl) { bitmap ->
                        imageBitmap = bitmap
                    }
                }
                scope.launch {
                    client.connect(
                        username = Constants.username,
                        password = Constants.password,
                        cbClient = object : MqttCallback {
                            override fun disconnected(disconnectResponse: MqttDisconnectResponse?) {
                            }

                            override fun mqttErrorOccurred(exception: MqttException?) {
                            }

                            override fun messageArrived(topic: String?, message: MqttMessage?) {
                            }

                            override fun deliveryComplete(token: IMqttToken?) {
                            }

                            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                                showDialog = true
                                connectionState = "Connected"
                            }

                            override fun authPacketArrived(
                                reasonCode: Int,
                                properties: MqttProperties?
                            ) {
                            }
                        }
                    )
                }
            }
        } catch (e: MqttException) {
            showDialog = true
            connectionState = "Not connected"
            e.printStackTrace()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Connection") },
                text = { Text(connectionState) },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (boxController, boxResult,boxImage, boxVelocitySlider) = createRefs()
        Box(
            modifier = Modifier
                .padding(12.dp)
                .constrainAs(boxController) {
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp, 200.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable._37380_200),
                    contentDescription = "test",
                    modifier = Modifier.fillMaxSize()
                )
                Column(modifier = Modifier.fillMaxSize()) {
                    // UP
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.CenterHorizontally)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    // Detect press
                                    onLongPress = {
                                        textDirection = "UP"
                                        imageDirection =
                                            R.drawable.keyboard_double_arrow_up_24dp_e8eaed_fill0_wght400_grad0_opsz24
                                    },
                                    onPress = {
                                        tryAwaitRelease()
                                        textDirection = ""
                                        client.triggerStop()
                                        imageDirection = 0
                                    }
                                )
                            }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        // LEFT
                        Box(modifier = Modifier
                            .size(64.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    // Detect press
                                    onLongPress = {
                                        textDirection = "LEFT"
                                        imageDirection =
                                            R.drawable.keyboard_double_arrow_left_24dp_e8eaed_fill0_wght400_grad0_opsz24
                                    },
                                    onPress = {
                                        tryAwaitRelease()
                                        textDirection = ""
                                        client.triggerStop()
                                        imageDirection = 0
                                    }
                                )
                            }
                        )
                        // RIGHT
                        Box(modifier = Modifier
                            .size(64.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    // Detect press
                                    onLongPress = {
                                        textDirection = "RIGHT"
                                        imageDirection =
                                            R.drawable.keyboard_double_arrow_right_24dp_e8eaed_fill0_wght400_grad0_opsz24
                                    },
                                    onPress = {
                                        tryAwaitRelease()
                                        textDirection = ""
                                        client.triggerStop()
                                        imageDirection = 0
                                    }
                                )
                            }
                        )
                    }
                    // DOWN
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.CenterHorizontally)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    // Detect press
                                    onLongPress = {
                                        textDirection = "DOWN"
                                        imageDirection =
                                            R.drawable.keyboard_double_arrow_down_24dp_e8eaed_fill0_wght400_grad0_opsz24
                                    },
                                    onPress = {
                                        tryAwaitRelease()
                                        textDirection = ""
                                        client.triggerStop()
                                        imageDirection = 0
                                    }
                                )
                            }
                    )
                }
            }
        }
        ControllerResult(
            modifier = Modifier
                .padding(12.dp)
                .constrainAs(boxResult) {
                    top.linkTo(parent.top)
                    start.linkTo(boxController.start)
                    end.linkTo(boxController.end)
                    bottom.linkTo(boxController.top)
                },
            textDirection = textDirection,
            imageView = imageDirection,
            progress = progress.floatValue,
            client = client
        )
        Box(
            modifier = Modifier
                .constrainAs(boxImage){
                    start.linkTo(boxController.end)
                    end.linkTo(boxVelocitySlider.start)
                    bottom.linkTo(parent.bottom)
                    top.linkTo(parent.top)
                }
        ) {
            imageBitmap?.let {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = it,
                    contentDescription = "neh"
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(bottom = 150.dp)
                .constrainAs(boxVelocitySlider){
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
        ) {
            Slider(
                modifier = Modifier
                    .width(255.dp)
                    .height(3.dp)
                    .graphicsLayer {
                        rotationZ = 270f // Rotate the actual slider content
                    }
                    .wrapContentHeight(),
                value = progress.floatValue,
                colors = SliderColors(
                    thumbColor = Color.Black,
                    activeTrackColor = Color.Red,
                    inactiveTrackColor = Color.Gray,
                    disabledActiveTrackColor = Color.Gray,
                    disabledInactiveTrackColor = Color.Gray,
                    disabledThumbColor = Color.Gray,
                    disabledActiveTickColor = Color.Gray,
                    disabledInactiveTickColor = Color.Gray,
                    activeTickColor = Color.Blue,
                    inactiveTickColor = Color.Blue
                ),
                onValueChange = { newValue ->
                    progress.floatValue = newValue // Update the progress
                }
            )
        }
    }
}

fun fetchWebcamStream(client: OkHttpClient, url: String, onFrameReceived: (ImageBitmap) -> Unit) {
    val request = Request.Builder().url(url).build()
    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            val bodyStream = response.body?.byteStream() ?: return
            readStreamFrames(bodyStream, onFrameReceived)
        }
    })
}

fun readStreamFrames(inputStream: InputStream, onFrameReceived: (ImageBitmap) -> Unit) {
    val boundary = "--frame\r\nContent-Type: image/jpeg\r\n\r\n".toByteArray()
    val boundarySize = boundary.size

    val buffer = ByteArrayOutputStream()
    var boundaryIndex = 0

    while (true) {
        val byte = inputStream.read()

        if (byte.toByte() == boundary[boundaryIndex]) {
            boundaryIndex++
            if (boundaryIndex == boundarySize) {
                val frameData = buffer.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(frameData, 0, frameData.size)

                bitmap?.let {
                    onFrameReceived(it.asImageBitmap())
                }
                buffer.reset()
                boundaryIndex = 0
            }
        } else {
            if (boundaryIndex > 0) {
                buffer.write(boundary, 0, boundaryIndex)
                boundaryIndex = 0
            }
            buffer.write(byte)
        }
    }
    buffer.close()
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun HomeScreenPreview() {
    HomeScreen(modifier = Modifier, navController = NavController(LocalContext.current))
}