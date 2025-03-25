package com.xperiencelabs.astronaut.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.LightEstimationMode
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode


@Composable
fun ARView(animation: String) {
    val nodes = remember(animation) {
        mutableStateListOf<ArNode>()
    }
    val zoomed = 1f
    val modelNode = remember {
        mutableStateOf<ArModelNode?>(null)
    }
    val placeModel = remember {
        mutableStateOf(true)
    }
    var previousAnimation by remember {
        mutableStateOf("idle")
    }

    // Dynamically scale based on zoomed Float value
    val modelScale by animateFloatAsState(
        targetValue = zoomed, // Directly use zoomed as a scale factor
        animationSpec = tween(durationMillis = 500)
    )

    // Adjust position dynamically based on the zoomed state
    val modelTranslationY by animateFloatAsState(
        targetValue = if (zoomed > 1f) -0.5f else 0f, // Move closer when zoomed
        animationSpec = tween(durationMillis = 500)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            nodes = nodes,
            planeRenderer = true,
            onCreate = { arSceneView ->
                arSceneView.lightEstimationMode = LightEstimationMode.DISABLED
                modelNode.value = ArModelNode(PlacementMode.INSTANT).apply {
                    loadModelGlbAsync(
                        "models/[Rerun] A person is talking with open and exp_variant1.glb",
                        scaleToUnits = 1f
                    ) {
                        playAnimation(animation)
                    }

                    onAnchorChanged = {
                        placeModel.value = !isAnchored
                    }
                }

                nodes.add(modelNode.value!!)
            }
        )

        if (placeModel.value) {
            Button(
                onClick = {
                    modelNode.value?.anchor()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 250.dp), // Adjust the bottom padding
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),  // Change to your preferred color
                    contentColor = Color.White  // Change text color to white
                )
            ) {
                Text(text = "Place it")
            }
        }

        // Dynamically update model scale and translation when zoomed
        LaunchedEffect(key1 = zoomed) {
            modelNode.value?.scale =
                Float3(modelScale, modelScale, modelScale) // Corrected: Use Float3
            modelNode.value?.position = modelNode.value?.position?.copy(
                y = modelTranslationY
            ) ?: Float3(0f, modelTranslationY, 0f)  // Safely modify position
        }

        // Handle animation changes
        LaunchedEffect(key1 = animation) {
            modelNode.value?.stopAnimation(previousAnimation)
            modelNode.value?.playAnimation(animation)
            previousAnimation = animation
        }
    }
}