package mobi.timon.android.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mobi.timon.android.R
import androidx.lifecycle.viewmodel.compose.viewModel
import mobi.timon.android.ui.components.ApiCard
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.ui.components.TestStatusBadge

@Composable
fun DashboardScreen(
    onNavigateToHash: () -> Unit,
    onNavigateToCipher: () -> Unit,
    onNavigateToKdf: () -> Unit,
    onNavigateToUtils: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_overall_status),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (state.isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (state.totalTests > 0) {
                    LinearProgressIndicator(
                        progress = { state.successRate },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(
                            R.string.dashboard_tests_passed,
                            state.passedTests,
                            state.totalTests,
                            (state.successRate * 100).toInt()
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (state.lastRunTime > 0) {
                        Text(
                            text = stringResource(R.string.dashboard_last_run, state.lastRunTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.dashboard_no_tests),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.runAllTests() },
            enabled = !state.isRunning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (state.isRunning) 
                    stringResource(R.string.dashboard_running) 
                else 
                    stringResource(R.string.dashboard_run_all)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.dashboard_modules),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                ApiCard(
                    info = state.hashApiInfo,
                    onClick = onNavigateToHash
                )
            }
            
            item {
                ApiCard(
                    info = state.cipherApiInfo,
                    onClick = onNavigateToCipher
                )
            }
            
            item {
                ApiCard(
                    info = state.kdfApiInfo,
                    onClick = onNavigateToKdf
                )
            }
            
            item {
                ApiCard(
                    info = state.utilsApiInfo,
                    onClick = onNavigateToUtils
                )
            }
        }
        
        if (state.totalTests > 0 && !state.isRunning) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.dashboard_failed_tests),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val allTests = state.hashTests + state.cipherTests + state.kdfTests + state.signTests + state.utilsTests
            val failedTests = allTests.filter { it.status == TestStatus.FAILURE }
            
            if (failedTests.isEmpty()) {
                Text(
                    text = stringResource(R.string.dashboard_all_passed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(failedTests) { test ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = test.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                TestStatusBadge(status = test.status)
                            }
                        }
                    }
                }
            }
        }
    }
}
