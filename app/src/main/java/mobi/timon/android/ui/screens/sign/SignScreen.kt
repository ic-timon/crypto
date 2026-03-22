package mobi.timon.android.ui.screens.sign

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
import mobi.timon.android.ui.components.InputField
import mobi.timon.android.ui.components.ResultDisplay
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.ui.components.TestStatusBadge

@Composable
fun SignScreen(
    viewModel: SignViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
            Text(
                text = stringResource(R.string.sign_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AlgorithmSelector(
                label = stringResource(R.string.sign_algorithm),
                options = SignAlgorithm.entries,
                selectedOption = state.selectedAlgorithm,
                onOptionSelected = { viewModel.selectAlgorithm(it) },
                optionLabel = { it.displayName }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InputField(
                label = stringResource(R.string.sign_message),
                value = state.message,
                onValueChange = { viewModel.updateMessage(it) },
                minLines = 2,
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { viewModel.generateKeyPair() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sign_generate_keypair))
            }
            
            if (state.publicKeyHex.isNotEmpty() || state.privateKeyHex.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                InputField(
                    label = stringResource(R.string.sign_private_key),
                    value = state.privateKeyHex,
                    onValueChange = { viewModel.updatePrivateKey(it) },
                    minLines = 2,
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                InputField(
                    label = stringResource(R.string.sign_public_key),
                    value = state.publicKeyHex,
                    onValueChange = { viewModel.updatePublicKey(it) },
                    minLines = 2,
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.sign() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.sign_sign))
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.verify() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.sign_verify))
                    }
                }
                
                if (state.selectedAlgorithm == SignAlgorithm.RSA_2048) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.encrypt() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.cipher_encrypt))
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.decrypt() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.cipher_decrypt))
                        }
                    }
                }
            }
            
            state.signatureHex.let { sig ->
                if (sig.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultDisplay(
                        label = stringResource(R.string.sign_signature),
                        result = sig
                    )
                }
            }
            
            state.ciphertextHex.let { ct ->
                if (ct.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultDisplay(
                        label = stringResource(R.string.cipher_ciphertext),
                        result = ct
                    )
                }
            }
            
            state.result?.let { result ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            state.verifyResult?.let { verified ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (verified)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = if (verified) 
                            stringResource(R.string.sign_verified) 
                        else 
                            stringResource(R.string.sign_verification_failed),
                        modifier = Modifier.padding(12.dp),
                        color = if (verified)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
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
