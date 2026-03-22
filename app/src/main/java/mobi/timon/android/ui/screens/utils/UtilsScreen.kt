package mobi.timon.android.ui.screens.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mobi.timon.android.ui.components.InputField
import mobi.timon.android.ui.components.ResultDisplay
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.ui.components.TestStatusBadge

@Composable
fun UtilsScreen(
    viewModel: UtilsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
            Text(
                text = "Utilities",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                UtilsMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = state.selectedMode == mode,
                        onClick = { viewModel.selectMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, UtilsMode.entries.size)
                    ) {
                        Text(when (mode) {
                            UtilsMode.RANDOM_BYTES -> "Random"
                            UtilsMode.HEX_ENCODE -> "Hex Enc"
                            UtilsMode.HEX_DECODE -> "Hex Dec"
                            UtilsMode.BASE64_ENCODE -> "B64 Enc"
                            UtilsMode.BASE64_DECODE -> "B64 Dec"
                        })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (state.selectedMode) {
                UtilsMode.RANDOM_BYTES -> {
                    InputField(
                        label = "Length (bytes)",
                        value = state.randomLength,
                        onValueChange = { viewModel.updateRandomLength(it) }
                    )
                }
                else -> {
                    InputField(
                        label = when (state.selectedMode) {
                            UtilsMode.HEX_ENCODE -> "Text to encode"
                            UtilsMode.HEX_DECODE -> "Hex to decode"
                            UtilsMode.BASE64_ENCODE -> "Text to encode"
                            UtilsMode.BASE64_DECODE -> "Base64 to decode"
                            else -> "Input"
                        },
                        value = state.input,
                        onValueChange = { viewModel.updateInput(it) },
                        minLines = 2,
                        maxLines = 6
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.execute() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(when (state.selectedMode) {
                    UtilsMode.RANDOM_BYTES -> "Generate"
                    UtilsMode.HEX_ENCODE -> "Encode to Hex"
                    UtilsMode.HEX_DECODE -> "Decode from Hex"
                    UtilsMode.BASE64_ENCODE -> "Encode to Base64"
                    UtilsMode.BASE64_DECODE -> "Decode from Base64"
                })
            }
            
            state.output?.let { output ->
                Spacer(modifier = Modifier.height(16.dp))
                ResultDisplay(
                    label = "Result",
                    result = output
                )
            }
            
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Auto Tests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedButton(
                    onClick = { viewModel.runAllTests() },
                    enabled = !state.isRunning
                ) {
                    Text("Run Tests")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.testResults) { test ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (test.status == TestStatus.SUCCESS)
                                MaterialTheme.colorScheme.surfaceVariant
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = test.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                test.output?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                test.error?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            TestStatusBadge(status = test.status)
                        }
                    }
                }
            }
        }
}
