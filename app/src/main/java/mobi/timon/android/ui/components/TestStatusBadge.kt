package mobi.timon.android.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

enum class TestStatus {
    SUCCESS,
    FAILURE,
    PENDING,
    RUNNING
}

@Composable
fun TestStatusBadge(
    status: TestStatus,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val (text, color) = when (status) {
        TestStatus.SUCCESS -> "PASS" to Color(0xFF4CAF50)
        TestStatus.FAILURE -> "FAIL" to Color(0xFFF44336)
        TestStatus.PENDING -> "WAIT" to Color(0xFF9E9E9E)
        TestStatus.RUNNING -> "..." to Color(0xFF2196F3)
    }
    
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
