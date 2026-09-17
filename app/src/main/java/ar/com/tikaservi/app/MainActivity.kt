package ar.com.tikaservi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ar.com.tikaservi.app.ui.nav.TikaserviNavHost
import ar.com.tikaservi.app.ui.theme.TikaserviTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TikaserviTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TikaserviNavHost()
                }
            }
        }
    }
}
