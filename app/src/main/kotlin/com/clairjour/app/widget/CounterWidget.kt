package com.clairjour.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.clairjour.app.ClairjourApplication
import com.clairjour.app.R
import com.clairjour.app.domain.Streak
import kotlinx.coroutines.flow.first

class CounterWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as? ClairjourApplication ?: return
        val primary = app.container.addictionRepository.observePrimary().first()
        val days = primary?.let { Streak.daysSince(it.startDate) } ?: 0
        val label = primary?.name.orEmpty()

        provideContent {
            WidgetContent(days = days, label = label)
        }
    }

    @Composable
    private fun WidgetContent(days: Int, label: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(androidx.compose.ui.graphics.Color(0xFF1E3A5F)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = days.toString(),
                style = TextStyle(
                    color = ColorProvider(androidx.compose.ui.graphics.Color(0xFFF5F1E8)),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = label.ifBlank { "Clairjour" },
                style = TextStyle(
                    color = ColorProvider(androidx.compose.ui.graphics.Color(0xFFD4A857)),
                    fontSize = 12.sp
                )
            )
        }
    }
}
