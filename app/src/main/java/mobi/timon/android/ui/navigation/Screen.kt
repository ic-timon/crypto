package mobi.timon.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Key
import androidx.compose.ui.graphics.vector.ImageVector
import mobi.timon.android.R

sealed class Screen(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    data object Dashboard : Screen(
        route = "dashboard",
        titleResId = R.string.nav_dashboard,
        icon = Icons.Default.Home
    )
    
    data object Hash : Screen(
        route = "hash",
        titleResId = R.string.nav_hash,
        icon = Icons.Default.Tag
    )
    
    data object Cipher : Screen(
        route = "cipher",
        titleResId = R.string.nav_cipher,
        icon = Icons.Default.EnhancedEncryption
    )
    
    data object Kdf : Screen(
        route = "kdf",
        titleResId = R.string.nav_kdf,
        icon = Icons.Default.Key
    )

    data object Sign : Screen(
        route = "sign",
        titleResId = R.string.nav_sign,
        icon = Icons.Default.EnhancedEncryption
    )

    data object Utils : Screen(
        route = "utils",
        titleResId = R.string.nav_utils,
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
