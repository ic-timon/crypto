package mobi.timon.android.ui.navigation

import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mobi.timon.android.ui.screens.cipher.CipherScreen
import mobi.timon.android.ui.screens.hash.HashScreen
import mobi.timon.android.ui.screens.kdf.KdfScreen
import mobi.timon.android.ui.screens.sign.SignScreen
import mobi.timon.android.ui.screens.utils.UtilsScreen
import mobi.timon.android.ui.screens.dashboard.DashboardScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Dashboard.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToHash = { navController.navigate(Screen.Hash.route) },
                onNavigateToCipher = { navController.navigate(Screen.Cipher.route) },
                onNavigateToKdf = { navController.navigate(Screen.Kdf.route) },
                onNavigateToSign = { navController.navigate(Screen.Sign.route) },
                onNavigateToUtils = { navController.navigate(Screen.Utils.route) }
            )
        }
        
        composable(Screen.Hash.route) {
            HashScreen()
        }
        
        composable(Screen.Cipher.route) {
            CipherScreen()
        }
        
        composable(Screen.Kdf.route) {
            KdfScreen()
        }

        composable(Screen.Sign.route) {
            SignScreen()
        }

        composable(Screen.Utils.route) {
            UtilsScreen()
        }
    }
}
