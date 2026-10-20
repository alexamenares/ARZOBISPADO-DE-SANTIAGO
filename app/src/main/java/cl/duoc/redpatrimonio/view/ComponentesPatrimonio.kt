package cl.duoc.redpatrimonio.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.redpatrimonio.model.Recinto
import cl.duoc.redpatrimonio.ui.theme.*

@Composable
internal fun Pagina(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
}

@Composable
internal fun Seccion(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PatrimonioBlanco),
        border = BorderStroke(1.dp, PatrimonioBorde.copy(alpha = .6f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (titulo.isNotBlank()) Text(titulo, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
internal fun Aviso(texto: String, error: Boolean = false) {
    Text(texto, style = MaterialTheme.typography.bodyMedium,
        color = if (error) MaterialTheme.colorScheme.error else PatrimonioSecundario)
}

@Composable
internal fun Campo(valor: String, etiqueta: String, onChange: (String) -> Unit,
    multilinea: Boolean = false, numerico: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(value = valor, onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            singleLine = !multilinea, minLines = if (multilinea) 3 else 1,
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = PatrimonioBlanco,
                focusedContainerColor = PatrimonioBlanco, unfocusedBorderColor = PatrimonioBorde),
            keyboardOptions = KeyboardOptions(keyboardType = if (numerico) KeyboardType.Number else KeyboardType.Text))
    }
}

@Composable
internal fun Accion(texto: String, enabled: Boolean = true, dorado: Boolean = false, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(
            containerColor = if (dorado) PatrimonioDorado else PatrimonioTerracota,
            contentColor = if (dorado) PatrimonioTexto else PatrimonioBlanco)) { Text(texto) }
}

@Composable
internal fun Encabezado(titulo: String, subtitulo: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        subtitulo?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = PatrimonioSecundario) }
    }
}

@Composable
internal fun TituloSeccion(titulo: String, enlace: String = "Ver todos", onClick: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (onClick != null) TextButton(onClick = onClick) { Text(enlace, style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable
internal fun Etiqueta(texto: String, color: Color = PatrimonioTerracota) {
    Surface(color = color.copy(alpha = .10f), contentColor = color, shape = RoundedCornerShape(8.dp)) {
        Text(texto, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
internal fun RecintoCard(recinto: Recinto, favorito: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PatrimonioBlanco),
        border = BorderStroke(1.dp, PatrimonioBorde.copy(alpha = .5f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IlustracionRecinto(Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)), recinto.id)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(recinto.nombre, style = MaterialTheme.typography.titleSmall)
                Text("${recinto.tipo} · ${recinto.comuna}", style = MaterialTheme.typography.bodySmall, color = PatrimonioSecundario)
                Text(recinto.estado.etiqueta, color = PatrimonioTerracota, style = MaterialTheme.typography.labelMedium)
            }
            if (favorito) Icono(Simbolo.FAVORITO, "Guardado en favoritos", color = PatrimonioTerracota)
            else Icono(Simbolo.FLECHA, color = PatrimonioSecundario, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
internal fun FilaAcceso(titulo: String, subtitulo: String, icono: Simbolo, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PatrimonioBlanco), shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PatrimonioBorde.copy(alpha = .6f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).background(PatrimonioCrema, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icono(icono, color = PatrimonioTerracota)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = PatrimonioSecundario)
            }
            Icono(Simbolo.FLECHA, color = PatrimonioSecundario, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
internal fun DatoFila(icono: Simbolo, etiqueta: String, valor: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icono(icono, color = PatrimonioTerracota, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = PatrimonioSecundario)
            Text(valor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun Pestanas(opciones: List<String>, seleccion: Int, cambiar: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(opciones) { i, texto ->
            FilterChip(selected = seleccion == i, onClick = { cambiar(i) }, label = { Text(texto) },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PatrimonioTerracota, selectedLabelColor = PatrimonioBlanco))
        }
    }
}

@Composable
internal fun Pasos(actual: Int, etiquetas: List<String>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        etiquetas.forEachIndexed { indice, nombre ->
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.fillMaxWidth().height(4.dp).background(
                    if (indice <= actual) PatrimonioDorado else PatrimonioBorde, RoundedCornerShape(2.dp)))
                Text("${indice + 1}. $nombre", style = MaterialTheme.typography.labelSmall,
                    color = if (indice == actual) PatrimonioTerracota else PatrimonioSecundario)
            }
        }
    }
}

@Composable
internal fun Confirmar(titulo: String, texto: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(titulo) }, text = { Text(texto) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Volver") } })
}
