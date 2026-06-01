package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

@Composable
fun Shield3DIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(120.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x334B86F4), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.6f
            ),
            radius = w * 0.6f
        )

        // Draw Shield Path with 3D gradient effect
        val shieldPath = Path().apply {
            moveTo(cx, cy - h * 0.45f)
            lineTo(cx + w * 0.35f, cy - h * 0.40f)
            lineTo(cx + w * 0.35f, cy - h * 0.05f)
            quadraticTo(cx + w * 0.35f, cy + h * 0.22f, cx, cy + h * 0.45f)
            quadraticTo(cx - w * 0.35f, cy + h * 0.22f, cx - w * 0.35f, cy - h * 0.05f)
            lineTo(cx - w * 0.35f, cy - h * 0.40f)
            close()
        }

        // Draw dark 3D bevel / shadow offset
        drawPath(
            path = shieldPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF233054), Color(0xFF10162B)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        // Inner glowing shield (smaller)
        val innerShieldPath = Path().apply {
            moveTo(cx, cy - h * 0.38f)
            lineTo(cx + w * 0.28f, cy - h * 0.34f)
            lineTo(cx + w * 0.28f, cy - h * 0.05f)
            quadraticTo(cx + w * 0.28f, cy + h * 0.18f, cx, cy + h * 0.38f)
            quadraticTo(cx - w * 0.28f, cy + h * 0.18f, cx - w * 0.28f, cy - h * 0.05f)
            lineTo(cx - w * 0.28f, cy - h * 0.34f)
            close()
        }

        drawPath(
            path = innerShieldPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF4B86F4), Color(0xFF1B4EAA))
            )
        )

        // Highlight split (gives 3D glossy shield look)
        val splitPath = Path().apply {
            moveTo(cx, cy - h * 0.38f)
            lineTo(cx + w * 0.28f, cy - h * 0.34f)
            lineTo(cx + w * 0.28f, cy - h * 0.05f)
            quadraticTo(cx + w * 0.28f, cy + h * 0.18f, cx, cy + h * 0.38f)
            close()
        }

        drawPath(
            path = splitPath,
            color = Color(0x22FFFFFF)
        )

        // Draw a minimalist lock inside
        val lockSize = w * 0.18f
        val lockX = cx - lockSize / 2
        val lockY = cy - lockSize * 0.2f

        // Shackle
        drawPath(
            path = Path().apply {
                moveTo(cx - lockSize * 0.3f, lockY)
                lineTo(cx - lockSize * 0.3f, lockY - lockSize * 0.4f)
                quadraticTo(cx, lockY - lockSize * 0.8f, cx + lockSize * 0.3f, lockY - lockSize * 0.4f)
                lineTo(cx + lockSize * 0.3f, lockY)
            },
            color = Color.White,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Lock Body
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(lockX, lockY),
            size = Size(lockSize, lockSize * 0.8f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )

        // Keyhole
        drawCircle(
            color = Color(0xFF1B4EAA),
            center = Offset(cx, lockY + lockSize * 0.35f),
            radius = 3.dp.toPx()
        )
        drawLine(
            color = Color(0xFF1B4EAA),
            start = Offset(cx, lockY + lockSize * 0.35f),
            end = Offset(cx, lockY + lockSize * 0.6f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun AnimatedBarrierGate(
    barrierState: String,
    modifier: Modifier = Modifier
) {
    // Rotation Angle: Horizontal (0f) to Vertical (-80f for real upright style)
    val targetAngle = when (barrierState) {
        "OPEN" -> -80f
        "OPENING" -> -80f
        else -> 0f
    }

    // Animation lasts precisely 4 seconds (4000ms), with a linear transition mapping to the real physical movement
    val gateAngleState by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(durationMillis = 4000, easing = LinearEasing),
        label = "GateRotation"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Draw a premium studio backdrop gradient like the photo's background
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF262D3D), Color(0xFF0C101E)),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.75f
            )
        )

        // 2. Ground shadow line
        drawLine(
            color = Color(0xFF080C14),
            start = Offset(0f, h - 25.dp.toPx()),
            end = Offset(w, h - 25.dp.toPx()),
            strokeWidth = 6.dp.toPx()
        )

        // 3. Main Cabinet (Pedestal) structure (Sleek steel-grey)
        val postWidth = 32.dp.toPx()
        val postHeight = 65.dp.toPx()
        val armLength = 170.dp.toPx() // Exactly 2 times longer than previous 85dp
        val totalSpan = postWidth + armLength
        val postX = (w - totalSpan) / 2f // Centered in horizontally available space
        val postY = h - 20.dp.toPx() - postHeight

        // Left-side shadow of post
        drawRect(
            color = Color(0x33000000),
            topLeft = Offset(postX - 5.dp.toPx(), postY + 4.dp.toPx()),
            size = Size(postWidth + 10.dp.toPx(), postHeight + 4.dp.toPx())
        )

        // Metallic steel-grey beveled pedestal representation
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF8A95A5), Color(0xFF434E5E), Color(0xFF2C353F)),
                start = Offset(postX, postY),
                end = Offset(postX + postWidth, postY + postHeight)
            ),
            topLeft = Offset(postX, postY),
            size = Size(postWidth, postHeight)
        )

        // Subtle 3D highlight edge (shining left bevel)
        drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(postX, postY),
            end = Offset(postX, postY + postHeight),
            strokeWidth = 1.dp.toPx()
        )

        // Ventilation slots on the lower left of the pedestal as seen in photo
        val slotW = 6.dp.toPx()
        val slotH = 2.dp.toPx()
        val slotStartX = postX + 4.dp.toPx()
        val slotYBase = postY + postHeight - 16.dp.toPx()
        for (i in 0 until 3) {
            drawRect(
                color = Color(0xFF1E242C),
                topLeft = Offset(slotStartX, slotYBase + (i * 4.dp.toPx())),
                size = Size(slotW, slotH)
            )
        }

        // Circular 3D protrusion dial/button element near upper middle of cabinet
        val dialX = postX + postWidth / 2f
        val dialY = postY + postHeight * 0.45f
        val dialRadius = 4.5.dp.toPx()

        // Dial shadow
        drawCircle(
            color = Color(0xAA000000),
            radius = dialRadius + 1.5.dp.toPx(),
            center = Offset(dialX + 0.8f.dp.toPx(), dialY + 1.5.dp.toPx())
        )
        // Dial white/grey body
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFD1D8E0)),
                center = Offset(dialX - 0.8f.dp.toPx(), dialY - 0.8f.dp.toPx()),
                radius = dialRadius
            ),
            radius = dialRadius,
            center = Offset(dialX, dialY)
        )

        // Pivot axle coordinate
        val pivotX = postX + postWidth / 2f
        val pivotY = postY + 14.dp.toPx()

        // 4. Rotating Barrier Boom
        withTransform({
            rotate(degrees = gateAngleState, pivot = Offset(pivotX, pivotY))
        }) {
            val armHeight = 8.dp.toPx()
            val armY = pivotY - armHeight / 2f

            // --- RED GLOWING NEON STRIP PROJECTING DOWNWARDS FROM ARM ---
            // Draw overlapping translucent gradients to form an incredible high-fidelity laser/neon emission
            val glowY = armY + armHeight + 1.dp.toPx()
            val glowH = 16.dp.toPx()

            // 1st Layer: Large, very soft diffuse red aura
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x33FF1111), Color.Transparent),
                    startY = glowY,
                    endY = glowY + glowH
                ),
                topLeft = Offset(pivotX, glowY),
                size = Size(armLength, glowH)
            )

            // 2nd Layer: Mid-intensity core glow line
            drawLine(
                color = Color(0x66FF3333),
                start = Offset(pivotX, glowY + 1.dp.toPx()),
                end = Offset(pivotX + armLength, glowY + 1.dp.toPx()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 3rd Layer: Hot neon intense red/orange laser edge directly at bottom of beam
            drawLine(
                color = Color(0xEEFF5533),
                start = Offset(pivotX, glowY),
                end = Offset(pivotX + armLength, glowY),
                strokeWidth = 1.5f.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw alternating red & white segments/stripes perfectly evenly distributed across the visible arm
            val segmentCount = 9
            val segmentWidth = armLength / segmentCount
            for (i in 0 until segmentCount) {
                val sX = pivotX + (i * segmentWidth)
                drawRect(
                    color = if (i % 2 == 0) Color(0xFFEF4444) else Color(0xFFF1F5F9),
                    topLeft = Offset(sX, armY),
                    size = Size(segmentWidth + 0.5f, armHeight)
                )
            }

            // Dark counterweight block / bracket anchoring the arm to the axle
            drawRoundRect(
                color = Color(0xFF333F4E),
                topLeft = Offset(pivotX - 14.dp.toPx(), armY - 1.5f.dp.toPx()),
                size = Size(20.dp.toPx(), armHeight + 3.dp.toPx()),
                cornerRadius = CornerRadius(1.5f.dp.toPx())
            )
        }

        // Pivot cap covering the rotating axis screw on the front
        drawCircle(
            color = Color(0xFF2C3E50),
            radius = 11.dp.toPx(),
            center = Offset(pivotX, pivotY)
        )
        drawCircle(
            color = Color(0xFFBDC3C7),
            radius = 5.dp.toPx(),
            center = Offset(pivotX, pivotY)
        )
    }
}
