package com.example.animandpaging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun Test() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
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
                .size(200.dp, 300.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxSize(),
                shadowElevation = 6.dp,
                shape = customShape
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Title")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Description")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Distance: ")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Duration: ")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {}) {
                        Text("Закрыть")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Test()
}