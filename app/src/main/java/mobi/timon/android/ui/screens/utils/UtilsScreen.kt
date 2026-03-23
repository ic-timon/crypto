package mobi.timon.android.ui.screens.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mobi.timon.android.R
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
            .verticalScroll(rememberScrollState())
    ) {
            Text(
                text = stringResource(R.string.utils_title),
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
                        UtilsMode.RANDOM_BYTES -> stringResource(R.string.utils_random)
                        UtilsMode.HEX_ENCODE -> stringResource(R.string.utils_hex_enc)
                        UtilsMode.HEX_DECODE -> stringResource(R.string.utils_hex_dec)
                        UtilsMode.BASE64_ENCODE -> stringResource(R.string.utils_b64_enc)
                        UtilsMode.BASE64_DECODE -> stringResource(R.string.utils_b64_dec)
                    })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (state.selectedMode) {
                UtilsMode.RANDOM_BYTES -> {
                    InputField(
                        label = stringResource(R.string.utils_length_bytes),
                        value = state.randomLength,
                        onValueChange = { viewModel.updateRandomLength(it) }
                    )
                }
                else -> {
                    InputField(
                        label = when (state.selectedMode) {
                            UtilsMode.HEX_ENCODE -> stringResource(R.string.utils_text_to_encode)
                            UtilsMode.HEX_DECODE -> stringResource(R.string.utils_hex_to_decode)
                            UtilsMode.BASE64_ENCODE -> stringResource(R.string.utils_text_to_encode)
                            UtilsMode.BASE64_DECODE -> stringResource(R.string.utils_base64_to_decode)
                            else -> stringResource(R.string.utils_input)
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
                    UtilsMode.RANDOM_BYTES -> stringResource(R.string.utils_generate)
                    UtilsMode.HEX_ENCODE -> stringResource(R.string.utils_encode_hex)
                    UtilsMode.HEX_DECODE -> stringResource(R.string.utils_decode_hex)
                    UtilsMode.BASE64_ENCODE -> stringResource(R.string.utils_encode_base64)
                    UtilsMode.BASE64_DECODE -> stringResource(R.string.utils_decode_base64)
                })
            }
            
            state.output?.let { output ->
                Spacer(modifier = Modifier.height(16.dp))
                ResultDisplay(
                    label = stringResource(R.string.result),
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
                    text = stringResource(R.string.auto_tests),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedButton(
                    onClick = { viewModel.runAllTests() },
                    enabled = !state.isRunning
                ) {
                    Text(stringResource(R.string.run_tests))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                state.testResults.forEach { test ->
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
