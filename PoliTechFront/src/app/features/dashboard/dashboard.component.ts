import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { DashboardService, DashboardCard, DashboardChartPoint } from '../../shared/services/api/dashboard.service';
import {
  Chart,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Tooltip,
  Legend
} from 'chart.js';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatOptionModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  private dashboardService = inject(DashboardService);

  @ViewChild('chartCanvas') chartCanvas?: ElementRef<HTMLCanvasElement>;

  periodOptions = [
    { value: 'day', label: 'Day' },
    { value: 'week', label: 'Week' },
    { value: 'month', label: 'Month' }
  ];

  selectedPeriod = 'month';
  chartPoints: DashboardChartPoint[] = [];
  cards: DashboardCard[] = [];
  loadingChart = false;
  loadingCards = false;
  private chartInstance: Chart | null = null;
  private viewReady = false;

  ngOnInit(): void {
    Chart.register(LineController, LineElement, PointElement, LinearScale, CategoryScale, Tooltip, Legend);
    this.loadChart();
    this.loadCards();
  }

  ngAfterViewInit(): void {
    this.viewReady = true;
    this.scheduleRender();
  }

  ngOnDestroy(): void {
    this.destroyChart();
  }

  onPeriodChange(): void {
    this.loadChart();
  }

  loadChart(): void {
    this.loadingChart = true;
    this.dashboardService.getChart(this.selectedPeriod).subscribe({
      next: (response) => {
        this.chartPoints = response.points || [];
        this.loadingChart = false;
        this.scheduleRender();
      },
      error: (error) => {
        this.chartPoints = [];
        this.loadingChart = false;
        this.scheduleRender();
      }
    });
  }

  loadCards(): void {
    this.loadingCards = true;
    this.dashboardService.getCards().subscribe({
      next: (response) => {
        this.cards = response.cards || [];
        this.loadingCards = false;
      },
      error: () => {
        this.cards = [];
        this.loadingCards = false;
      }
    });
  }

  private scheduleRender(): void {
    if (!this.viewReady) {
      return;
    }
    requestAnimationFrame(() => {
      requestAnimationFrame(() => this.renderChart());
    });
  }

  private renderChart(): void {
    if (!this.chartCanvas) {
      return;
    }
    if (!this.chartPoints.length) {
      this.destroyChart();
      return;
    }

    const canvas = this.chartCanvas.nativeElement;
    const parent = canvas.parentElement;
    if (!parent || parent.clientWidth === 0) {
      return;
    }

    this.destroyChart();

    const labels = this.chartPoints.map(point => point.period);
    const amountData = this.chartPoints.map(point => point.amount);
    const sumData = this.chartPoints.map(point => point.sum);

    this.chartInstance = new Chart(canvas, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Amount',
            data: amountData,
            borderColor: '#3f51b5',
            backgroundColor: 'rgba(63, 81, 181, 0.12)',
            borderWidth: 2,
            tension: 0.3,
            fill: false,
            yAxisID: 'yAmount'
          },
          {
            label: 'Sum',
            data: sumData,
            borderColor: '#ff9800',
            backgroundColor: 'rgba(255, 152, 0, 0.12)',
            borderWidth: 2,
            tension: 0.3,
            fill: false,
            yAxisID: 'ySum'
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: 'index',
          intersect: false
        },
        scales: {
          yAmount: {
            position: 'left',
            ticks: {
              precision: 0
            }
          },
          ySum: {
            position: 'right',
            grid: {
              drawOnChartArea: false
            }
          }
        }
      }
    });
  }

  private destroyChart(): void {
    if (this.chartInstance) {
      this.chartInstance.destroy();
      this.chartInstance = null;
    }
  }
}