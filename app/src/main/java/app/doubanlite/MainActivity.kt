package app.doubanlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.doubanlite.ui.DoubanLiteApp
import app.doubanlite.ui.theme.DoubanLiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoubanLiteTheme {
                DoubanLiteApp()
            }
        }
    }
}
