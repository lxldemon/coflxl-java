<template>
  <v-chart class="chart" :option="option" autoresize />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const props = defineProps<{
  data: any[]
  config: any
}>()

const option = computed(() => {
  if (!props.data || props.data.length === 0) return {}

  const { chartType = 'bar', xAxis, yAxis = [], seriesNames = [] } = props.config

  if (chartType === 'pie') {
    return {
      tooltip: { trigger: 'item' },
      legend: { top: '5%', left: 'center' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          data: props.data.map(item => ({
            name: item[xAxis],
            value: item[yAxis[0]]
          }))
        }
      ]
    }
  }

  return {
    tooltip: { trigger: 'axis' },
    legend: { data: yAxis.map((y: string, i: number) => seriesNames[i] || y) },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: props.data.map(item => item[xAxis])
    },
    yAxis: { type: 'value' },
    series: yAxis.map((y: string, i: number) => ({
      name: seriesNames[i] || y,
      type: chartType,
      data: props.data.map(item => item[y]),
      smooth: chartType === 'line'
    }))
  }
})
</script>

<style scoped>
.chart {
  width: 100%;
  height: 100%;
  min-height: 300px;
}
</style>
