package com.dolphin.jetpack.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun LineChart(modifier: Modifier = Modifier, data: List<Pair<Float, Float>>) {
    AndroidView(modifier = modifier, factory = {
        context ->
        LineChart(context).apply {
            val entries = data.map { Entry(it.first, it.second) }
            val dataSet = LineDataSet(entries, "Quiz Performance")
            this.data = LineData(dataSet)
            invalidate()
        }
    })
}
