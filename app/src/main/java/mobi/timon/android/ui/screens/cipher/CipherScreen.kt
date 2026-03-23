package mobi.timon.android.ui.screens.cipher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
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
import mobi.timon.android.ui.components.AlgorithmSelector
import mobi.timon.android.ui.components.HexInputField
import mobi.timon.android.ui.components.InputField
import mobi.timon.android.ui.components.ResultDisplay
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.ui.components.TestStatusBadge

@Composable
fun CipherScreen(
    viewModel: CipherViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.cipher_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            AlgorithmSelector(
                label = stringResource(R.string.cipher_algorithm),
                options = CipherAlgorithm.entries,
                selectedOption = state.selectedAlgorithm,
                onOptionSelected = { viewModel.selectAlgorithm(it) },
                optionLabel = { it.displayName }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = state.mode == CipherMode.ENCRYPT,
                    onClick = { viewModel.setMode(CipherMode.ENCRYPT) },
                    shape = SegmentedButtonDefaults.itemShape(0, CipherMode.entries.size)
                ) {
                    Text(stringResource(R.string.cipher_encrypt))
                }
                SegmentedButton(
                    selected = state.mode == CipherMode.DECRYPT,
                    onClick = { viewModel.setMode(CipherMode.DECRYPT) },
                    shape = SegmentedButtonDefaults.itemShape(1, CipherMode.entries.size)
                ) {
                    Text(stringResource(R.string.cipher_decrypt))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.cipher_hex_input))
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = state.useHexInput,
                    onCheckedChange = { viewModel.toggleHexInput(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.useHexInput) {
                HexInputField(
                    label = stringResource(R.string.cipher_input_hex),
                    value = state.inputHex,
                    onValueChange = { viewModel.updateInputHex(it) }
                )
            } else {
                InputField(
                    label = stringResource(R.string.cipher_input_text),
                    value = state.input,
                    onValueChange = { viewModel.updateInput(it) },
                    minLines = 2,
                    maxLines = 4
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HexInputField(
                    label = stringResource(R.string.cipher_key, state.selectedAlgorithm.keySize * 8),
                    value = state.keyHex,
                    onValueChange = { viewModel.updateKey(it) },
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = { viewModel.generateKey() }
                ) {
                    Text(stringResource(R.string.cipher_generate))
                }
            }

            if (state.selectedAlgorithm == CipherAlgorithm.AES_XTS) {
                Spacer(modifier = Modifier.height(12.dp))

                InputField(
                    label = stringResource(R.string.cipher_sector_number),
                    value = state.sectorNum,
                    onValueChange = { viewModel.updateSectorNum(it) },
                    placeholder = "0"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.execute() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.mode == CipherMode.ENCRYPT) 
                        stringResource(R.string.cipher_encrypt) 
                    else 
                        stringResource(R.string.cipher_decrypt)
                )
            }

            state.result?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                ResultDisplay(
                    label = if (state.mode == CipherMode.ENCRYPT) 
                        stringResource(R.string.cipher_ciphertext) 
                    else 
                        stringResource(R.string.cipher_plaintext),
                    result = result
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
}
