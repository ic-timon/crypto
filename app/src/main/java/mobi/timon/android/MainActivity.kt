package mobi.timon.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mobi.timon.enc.Aead
import mobi.timon.enc.Codec
import mobi.timon.enc.Hash
import mobi.timon.enc.Random
import mobi.timon.android.ui.theme.ImTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        val data = "Hello, World!".toByteArray()
                        val key = Random.bytes(32)
                        
                        Text("SHA1: ${Codec.toHex(Hash.sha1(data))}")
                        Text("SHA256: ${Codec.toHex(Hash.sha256(data))}")
                        
                        val encrypted = Aead.aesGcmEncrypt(data, key)
                        val decrypted = Aead.aesGcmDecrypt(encrypted, key)
                        Text("AES-GCM: ${String(decrypted)}")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ImTheme {
        Greeting("Android")
    }
}
