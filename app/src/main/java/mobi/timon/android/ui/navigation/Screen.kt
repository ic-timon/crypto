package mobi.timon.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Key
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Home
    )
    
    data object Hash : Screen(
        route = "hash",
        title = "Hash",
        icon = Icons.Default.Tag
    )
    
    data object Cipher : Screen(
        route = "cipher",
        title = "Cipher",
        icon = Icons.Default.EnhancedEncryption
    )
    
    data object Kdf : Screen(
        route = "kdf",
        title = "KDF/Sign",
        icon = Icons.Default.Key
    )
    
    data object Utils : Screen(
        route = "utils",
        title = "Utils",
        icon = Icons.Default.Build
    )
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.Hash,
    Screen.Cipher,
    Screen.Kdf,
    Screen.Utils
)
