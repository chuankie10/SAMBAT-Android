package id.sambat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Night = Color(0xFF171323)
private val Violet = Color(0xFF8B5CF6)
private val Lavender = Color(0xFFE9D5FF)
private val SoftWhite = Color(0xFFF8F5FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SambatApp()
            }
        }
    }
}

@Composable
private fun SambatApp() {
    var started by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Night) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF30204A), Night, Color(0xFF171323))
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF3D2A5C), CircleShape)
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Headphones,
                        contentDescription = "SAMBAT",
                        tint = Lavender,
                        modifier = Modifier.height(76.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF91C8),
                        modifier = Modifier
                            .height(30.dp)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "SAMBAT",
                    color = SoftWhite,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Di sini, kamu boleh bercerita.",
                    color = Lavender,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ruang aman untuk berbagi cerita melalui suara, saling mendengarkan, dan menyebarkan kebaikan.",
                    color = Color(0xFFC9C0D8),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { started = !started },
                    colors = ButtonDefaults.buttonColors(containerColor = Violet),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(
                        imageVector = if (started) Icons.Filled.Spa else Icons.Filled.Mic,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = if (started) "Senang kamu di sini" else "Mulai Sambat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (started) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Kamu tidak harus menghadapi semuanya sendirian.\nFitur berbagi suara sedang disiapkan.",
                        color = Lavender,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
