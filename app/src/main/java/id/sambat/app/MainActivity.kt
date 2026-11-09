package id.sambat.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import id.sambat.app.domain.VoiceClipPolicy

private val Night = Color(0xFF171323)
private val Violet = Color(0xFF8B5CF6)
private val Lavender = Color(0xFFE9D5FF)
private val SoftWhite = Color(0xFFF8F5FF)
private const val MAX_STORY_MILLIS = VoiceClipPolicy.MAX_STORY_DURATION_SECONDS * 1_000L
private const val MAX_REPLY_MILLIS = VoiceClipPolicy.MAX_REPLY_DURATION_SECONDS * 1_000L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { SambatApp() } }
    }
}

@Composable
private fun SambatApp() {
    val context = LocalContext.current
    val recorder = remember(context) { VoiceRecorder(context) }
    var showWelcome by remember { mutableStateOf(true) }
    var recording by remember { mutableStateOf(false) }
    var recordingReply by remember { mutableStateOf(false) }
    var replyFile by remember { mutableStateOf(recorder.latestReply(recorder.latestClip())) }
    var playing by remember { mutableStateOf(false) }
    var elapsed by remember { mutableStateOf(0L) }
    var savedClip by remember { mutableStateOf(recorder.latestClip()) }
    var savedClips by remember { mutableStateOf(recorder.listStories()) }
    var message by remember { mutableStateOf("Rekam cerita suara pertamamu. Ceritamu tersimpan sementara di perangkat ini.") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                recorder.start()
                elapsed = 0L
                recording = true
                message = "Sedang merekam. Tekan Selesai jika sudah cukup."
            } catch (error: Exception) {
                message = "Rekaman gagal dimulai: ${error.localizedMessage ?: "kesalahan perangkat"}"
            }
        } else {
            message = "Izin mikrofon diperlukan agar SAMBAT dapat merekam suara."
        }
    }

    DisposableEffect(recorder) {
        onDispose { recorder.release() }
    }

    LaunchedEffect(recording, recordingReply) {
        if (recording) {
            val startedAt = SystemClock.elapsedRealtime()
            while (recording) {
                delay(if (recordingReply) 50 else 250)
                val limit = if (recordingReply) MAX_REPLY_MILLIS else MAX_STORY_MILLIS
                elapsed = (SystemClock.elapsedRealtime() - startedAt).coerceAtMost(limit)
                if (elapsed >= limit) {
                    val file = recorder.stop()
                    if (recordingReply) {
                        replyFile = file
                        recording = false
                        recordingReply = false
                        message = if (file != null) "Balasan suara 5 detik tersimpan lokal." else "Balasan suara gagal disimpan."
                    } else {
                        savedClip = file
                        replyFile = recorder.latestReply(file)
                        savedClips = recorder.listStories()
                        recording = false
                        message = if (file != null) "Batas 120 detik tercapai. Cerita tersimpan di perangkat." else "Rekaman tidak dapat disimpan."
                    }
                }
            }
        }
    }

    if (showWelcome) {
        WelcomeScreen(onStart = { showWelcome = false })
    } else {
    Surface(modifier = Modifier.fillMaxSize(), color = Night) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Brush.verticalGradient(listOf(Color(0xFF30204A), Night)))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.background(Color(0xFF3D2A5C), CircleShape).padding(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Headphones, contentDescription = "SAMBAT", tint = Lavender, modifier = Modifier.height(64.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text("SAMBAT", color = SoftWhite, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
            Spacer(Modifier.height(8.dp))
            Text("Di sini, kamu boleh bercerita.", color = Lavender, fontSize = 18.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF282137))
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (recording) "SEDANG MEREKAM" else "RUANG CERITAMU", color = Lavender, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(formatDuration(elapsed), color = SoftWhite, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                    Text(if (recordingReply) "Balasan maksimal 00:05 detik" else "Cerita maksimal 02:00 menit", color = Color(0xFFC9C0D8), fontSize = 13.sp)
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            if (recording) {
                                val file = recorder.stop()
                                if (recordingReply) {
                                    replyFile = file
                                    recording = false
                                    recordingReply = false
                                    message = if (file != null) "Balasan suara tersimpan lokal." else "Balasan suara gagal disimpan."
                                } else {
                                    savedClip = file
                                    replyFile = recorder.latestReply(file)
                                    savedClips = recorder.listStories()
                                    recording = false
                                    message = if (file != null) "Cerita tersimpan di perangkat." else "Rekaman gagal disimpan. Coba lagi."
                                }
                            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                try {
                                    recorder.start()
                                    recordingReply = false
                                    elapsed = 0L
                                    recording = true
                                    message = "Sedang merekam. Tekan Selesai jika sudah cukup."
                                } catch (error: Exception) {
                                    message = "Rekaman gagal dimulai: ${error.localizedMessage ?: "kesalahan perangkat"}"
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (recording) Color(0xFFB93855) else Violet)
                    ) {
                        Icon(if (recording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text(if (recording) "Selesai Merekam" else "Mulai Merekam", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val clip = savedClip
                            if (clip == null) {
                                message = "Buat cerita terlebih dahulu sebelum merekam balasan."
                            } else {
                                try {
                                    recorder.startReply(clip)
                                    recordingReply = true
                                    elapsed = 0L
                                    recording = true
                                    message = "Sedang merekam balasan. Durasi maksimal 5 detik."
                                } catch (error: Exception) {
                                    message = "Balasan gagal dimulai: " + (error.localizedMessage ?: "kesalahan perangkat")
                                }
                            }
                        },
                        enabled = !recording && savedClip?.exists() == true,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text("Rekam Balasan Suara (5 detik)")
                    }
                    OutlinedButton(
                        onClick = {
                            val reply = replyFile
                            if (reply != null && reply.exists()) {
                                try {
                                    recorder.play(reply) { playing = false }
                                    playing = true
                                    message = "Memutar balasan suara."
                                } catch (error: Exception) {
                                    message = "Balasan gagal diputar: " + (error.localizedMessage ?: "kesalahan audio")
                                }
                            }
                        },
                        enabled = !recording && replyFile?.exists() == true,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text("Dengarkan Balasan")
                    }
                    OutlinedButton(
                        onClick = {
                            val clip = savedClip
                            if (clip == null || !clip.exists()) {
                                message = "Belum ada rekaman untuk diputar."
                            } else {
                                try {
                                    recorder.play(clip) { playing = false }
                                    playing = true
                                    message = "Memutar rekaman terakhir."
                                } catch (error: Exception) {
                                    playing = false
                                    message = "Pemutaran gagal: ${error.localizedMessage ?: "kesalahan audio"}"
                                }
                            }
                        },
                        enabled = !recording && savedClip?.exists() == true,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text(if (playing) "Putar Ulang Rekaman" else "Dengarkan Rekaman")
                    }
                }
            }
            if (savedClips.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Text("CERITA TERSIMPAN", color = Lavender, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                savedClips.take(3).forEachIndexed { index, clip ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF282137)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cerita " + (savedClips.size - index), color = SoftWhite, fontWeight = FontWeight.SemiBold)
                                Text(
                                    java.text.SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(java.util.Date(clip.lastModified())),
                                    color = Color(0xFFAAA1BA),
                                    fontSize = 11.sp
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    try {
                                        recorder.play(clip) { playing = false }
                                        playing = true
                                        message = "Memutar cerita yang dipilih."
                                    } catch (error: Exception) {
                                        playing = false
                                        message = "Pemutaran gagal: " + (error.localizedMessage ?: "kesalahan audio")
                                    }
                                },
                                enabled = !recording,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Putar cerita")
                                Text("Putar")
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(message, color = Lavender, textAlign = TextAlign.Center, lineHeight = 21.sp, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            Text("Cerita disimpan lokal untuk tahap uji coba. Belum dibagikan ke pengguna lain.", color = Color(0xFFAAA1BA), textAlign = TextAlign.Center, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
    }

}


@Composable
private fun WelcomeScreen(onStart: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Night
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF30204A), Night)))
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF3D2A5C), RoundedCornerShape(64.dp))
                    .padding(horizontal = 34.dp, vertical = 42.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Headphones, contentDescription = "SAMBAT", tint = Lavender, modifier = Modifier.height(42.dp))
                    Text("♥", color = Color(0xFFF472B6), fontSize = 30.sp)
                }
            }
            Spacer(Modifier.height(28.dp))
            Text("SAMBAT", color = SoftWhite, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
            Spacer(Modifier.height(10.dp))
            Text("Di sini, kamu boleh bercerita.", color = Lavender, fontSize = 19.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Text(
                "Ruang aman untuk berbagi cerita melalui suara, saling mendengarkan, dan menyebarkan kebaikan.",
                color = Color(0xFFD0C7DC), fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 24.sp
            )
            Spacer(Modifier.height(38.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Violet)
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Mulai Sambat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Kamu tidak harus menghadapi semuanya sendirian.",
                color = Lavender, fontSize = 17.sp, textAlign = TextAlign.Center, lineHeight = 24.sp
            )
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60)
}

private class VoiceRecorder(context: Context) {
    private val appContext = context.applicationContext
    private val storiesDir = File(appContext.filesDir, "voice-stories")
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var output: File? = null

    init {
        storiesDir.mkdirs()
        removeExpiredStories()
    }

    fun listStories(): List<File> {
        removeExpiredStories()
        return storiesDir.listFiles { file ->
            file.isFile && file.name.startsWith("sambat-") && file.name.endsWith(".m4a")
        }?.sortedByDescending { it.name } ?: emptyList()
    }

    fun latestClip(): File? = listStories().firstOrNull()

    fun latestReply(story: File?): File? {
        if (story == null) return null
        val repliesDir = File(appContext.filesDir, "voice-replies")
        val storyId = story.nameWithoutExtension.removePrefix("sambat-")
        return repliesDir.listFiles { file ->
            file.isFile && file.name.startsWith("reply-${storyId}-") && file.name.endsWith(".m4a")
        }?.maxByOrNull { it.name }
    }

    private fun removeExpiredStories() {
        val now = System.currentTimeMillis()
        storiesDir.listFiles()?.forEach { file ->
            val createdAt = file.name.removePrefix("sambat-").removeSuffix(".m4a").toLongOrNull()
            if (createdAt != null && VoiceClipPolicy.isExpired(createdAt, now)) {
                file.delete()
                val repliesDir = File(appContext.filesDir, "voice-replies")
                val storyId = file.nameWithoutExtension.removePrefix("sambat-")
                repliesDir.listFiles()?.filter { it.name.startsWith("reply-${storyId}-") }?.forEach { it.delete() }
            }
        }
    }

    fun start() {
        startToFile(File(storiesDir, "sambat-${System.currentTimeMillis()}.m4a"))
    }

    fun startReply(story: File) {
        val repliesDir = File(appContext.filesDir, "voice-replies")
        repliesDir.mkdirs()
        val storyId = story.nameWithoutExtension.removePrefix("sambat-")
        val reply = File(repliesDir, "reply-${storyId}-${System.currentTimeMillis()}.m4a")
        startToFile(reply)
    }

    private fun startToFile(file: File) {
        stopPlayer()
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(appContext)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setAudioEncodingBitRate(96_000)
            mediaRecorder.setAudioSamplingRate(44_100)
            mediaRecorder.setOutputFile(file.absolutePath)
            mediaRecorder.prepare()
            mediaRecorder.start()
            recorder = mediaRecorder
            output = file
        } catch (error: Exception) {
            mediaRecorder.release()
            file.delete()
            throw error
        }
    }

    fun stop(): File? {
        val active = recorder ?: return null
        recorder = null
        return try {
            active.stop()
            active.release()
            output?.takeIf { it.exists() && it.length() > 0L }
        } catch (_: RuntimeException) {
            active.release()
            output?.delete()
            null
        }
    }

    fun play(file: File, onComplete: () -> Unit) {
        stopPlayer()
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(file.absolutePath)
        mediaPlayer.setOnCompletionListener {
            stopPlayer()
            onComplete()
        }
        mediaPlayer.prepare()
        mediaPlayer.start()
        player = mediaPlayer
    }

    private fun stopPlayer() {
        player?.runCatching { stop() }
        player?.release()
        player = null
    }

    fun release() {
        stop()
        stopPlayer()
    }
}
