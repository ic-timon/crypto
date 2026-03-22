package mobi.timon.android.ui.screens.kdf

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mobi.timon.android.ui.components.AlgorithmSelector
import mobi.timon.android.ui.components.HexInputField
import mobi.timon.android.ui.components.InputField
import mobi.timon.android.ui.components.ResultDisplay
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.ui.components.TestStatusBadge

@Composable
fun KdfScreen(
    viewModel: KdfViewModel = viewModel(),
    signViewModel: mobi.timon.android.ui.screens.sign.SignViewModel = viewModel()
) {
    val kdfState by viewModel.state.collectAsState()
    val signState by signViewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
            Text(
                text = "Key Derivation Functions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AlgorithmSelector(
                label = "Algorithm",
                options = KdfAlgorithm.entries,
                selectedOption = kdfState.selectedAlgorithm,
                onOptionSelected = { viewModel.selectAlgorithm(it) },
                optionLabel = { it.displayName }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InputField(
                label = "Password",
                value = kdfState.password,
                onValueChange = { viewModel.updatePassword(it) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HexInputField(
                    label = "Salt (Hex, optional)",
                    value = kdfState.saltHex,
                    onValueChange = { viewModel.updateSalt(it) },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedButton(
                    onClick = { viewModel.generateSalt() }
                ) {
                    Text("Generate")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InputField(
                    label = if (kdfState.selectedAlgorithm == KdfAlgorithm.BCRYPT) "Cost" else "Iterations",
                    value = kdfState.iterations,
                    onValueChange = { viewModel.updateIterations(it) },
                    modifier = Modifier.weight(1f)
                )
                
                InputField(
                    label = "Key Length",
                    value = kdfState.keyLength,
                    onValueChange = { viewModel.updateKeyLength(it) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.derive() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Derive Key")
            }
            
            kdfState.result?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                ResultDisplay(
                    label = "Derived Key",
                    result = result
                )
            }
            
            kdfState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Digital Signatures",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KDF & Sign Tests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.runAllTests() }
                    ) {
                        Text("KDF")
                    }
                    
                    OutlinedButton(
                        onClick = { signViewModel.runAllTests() }
                    ) {
                        Text("Sign")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val allTests = kdfState.testResults + signState.testResults
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(allTests) { test ->
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
