package ar.com.tikaservi.app.ui.role

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoleSelectionScreen(
    onElegirPasajero: () -> Unit,
    onElegirChofer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tikaservi",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Viajes Compartidos - Tartagal / Santa Victoria Este",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
        )

        Button(
            onClick = onElegirPasajero,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Soy pasajero")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onElegirChofer,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Soy chofer")
        }
    }
}
