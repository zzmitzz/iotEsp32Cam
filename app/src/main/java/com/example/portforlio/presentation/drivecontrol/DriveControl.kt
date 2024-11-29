package com.example.portforlio.presentation.drivecontrol

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp


fun decodeBase64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}

@Composable
fun Base64Image(modifier: Modifier, base64String: String) {
    val bitmap = decodeBase64ToBitmap(base64String)
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(), contentDescription = "Decoded Image", modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .graphicsLayer(rotationZ = 90f)
        )
    }
}