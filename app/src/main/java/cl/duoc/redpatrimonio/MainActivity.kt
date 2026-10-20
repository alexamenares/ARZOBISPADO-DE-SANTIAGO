package cl.duoc.redpatrimonio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.redpatrimonio.ui.theme.RedPatrimonioReligiosoTheme
import cl.duoc.redpatrimonio.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RedPatrimonioReligiosoTheme {
                AppNavigation()
            }
        }
    }
}