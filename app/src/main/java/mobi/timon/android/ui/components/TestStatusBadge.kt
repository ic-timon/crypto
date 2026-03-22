package mobi.timon.android.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import mobi.timon.android.R

enum class TestStatus {
    SUCCESS,
    FAILURE,
    PENDING,
    RUNNING
}

@Composable
fun TestStatusBadge(
    status: TestStatus,
    modifier: Modifier = Modifier
) {
    val (textResId, color) = when (status) {
        TestStatus.SUCCESS -> R.string.status_pass to Color(0xFF4CAF50)
        TestStatus.FAILURE -> R.string.status_fail to Color(0xFFF44336)
        TestStatus.PENDING -> R.string.status_wait to Color(0xFF9E9E9E)
        TestStatus.RUNNING -> R.string.status_running to Color(0xFF2196F3)
    }
    
    Text(
        text = stringResource(textResId),
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
