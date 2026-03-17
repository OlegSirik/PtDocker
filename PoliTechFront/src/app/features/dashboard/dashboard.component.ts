import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTabsModule } from '@angular/material/tabs';
import {
  DashboardService,
  DashboardCard,
  DashboardChartPoint,
  DashboardBarPoint
} from '../../shared/services/api/dashboard.service';
import {
  Chart,
  LineController,
  LineElement,
  PointElement,
  BarController,
  BarElement,
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
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  private dashboardService = inject(DashboardService);

  @ViewChild('chartCanvas') chartCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('overviewProductsChartCanvas') overviewProductsChartCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('overviewClientsChartCanvas') overviewClientsChartCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('productsChartCanvas') productsChartCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('clientsChartCanvas') clientsChartCanvas?: ElementRef<HTMLCanvasElement>;

  dateFrom = '';
  dateTo = '';
  chartPoints: DashboardChartPoint[] = [];
  productsChartPoints: DashboardBarPoint[] = [];
  clientsChartPoints: DashboardBarPoint[] = [];
  cards: DashboardCard[] = [];
  loadingChart = false;
  loadingCards = false;
  loadingProductsChart = false;
  loadingClientsChart = false;
  activeTabIndex = 0;
  private chartInstance: Chart | null = null;
  private overviewProductsChartInstance: Chart | null = null;
  private overviewClientsChartInstance: Chart | null = null;
  private productsChartInstance: Chart | null = null;
  private clientsChartInstance: Chart | null = null;
  private viewReady = false;

  ngOnInit(): void {
    Chart.register(
      LineController,
      LineElement,
      PointElement,
      BarController,
      BarElement,
      LinearScale,
      CategoryScale,
      Tooltip,
      Legend
    );
    this.initDefaultDates();
    this.loadAll();
  }

  ngAfterViewInit(): void {
    this.viewReady = true;
    this.scheduleRender();
    this.scheduleRenderOverviewProducts();
    this.scheduleRenderOverviewClients();
    this.scheduleRenderProducts();
    this.scheduleRenderClients();
  }

  ngOnDestroy(): void {
    this.destroyChart();
    this.destroyOverviewProductsChart();
    this.destroyOverviewClientsChart();
    this.destroyProductsChart();
    this.destroyClientsChart();
  }

  private initDefaultDates(): void {
    const now = new Date();
    const start = new Date(now);
    start.setMonth(start.getMonth() - 1);
    this.dateFrom = this.formatDate(start);
    this.dateTo = this.formatDate(now);
  }

  private formatDate(d: Date): string {
    return d.toISOString().slice(0, 10);
  }

  /** Вычисляет период по диапазону дат: <31 дн → day, <180 дн → week, <366 дн → month, иначе → year */
  private getPeriodFromDateRange(): string {
    if (!this.dateFrom || !this.dateTo) return 'month';
    const from = new Date(this.dateFrom);
    const to = new Date(this.dateTo);
    const diffTime = to.getTime() - from.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    if (diffDays < 31) return 'day';
    if (diffDays < 180) return 'week';
    if (diffDays < 366) return 'month';
    return 'year';
  }

  onShow(): void {
    this.loadAll();
  }

  onTabChange(index: number): void {
    this.activeTabIndex = index;
    if (index === 0) {
      this.scheduleRenderOverviewProducts();
      this.scheduleRenderOverviewClients();
    } else if (index === 1) {
      this.scheduleRenderProducts();
    } else if (index === 2) {
      this.scheduleRenderClients();
    }
  }

  private loadAll(): void {
    this.loadChart();
    this.loadCards();
    this.loadProductsChart();
    this.loadClientsChart();
  }

  loadChart(): void {
    this.loadingChart = true;
    const period = this.getPeriodFromDateRange();
    this.dashboardService
      .getChart(period, this.dateFrom || undefined, this.dateTo || undefined)
      .subscribe({
        next: (response) => {
          this.chartPoints = response.points || [];
          this.loadingChart = false;
          this.scheduleRender();
        },
        error: () => {
          this.chartPoints = [];
          this.loadingChart = false;
          this.scheduleRender();
        }
      });
  }

  loadCards(): void {
    this.loadingCards = true;
    this.dashboardService.getCards(this.dateFrom || undefined, this.dateTo || undefined).subscribe({
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

  loadProductsChart(): void {
    this.loadingProductsChart = true;
    this.dashboardService
      .getChartByProducts(this.dateFrom || undefined, this.dateTo || undefined)
      .subscribe({
        next: (response) => {
          this.productsChartPoints = response.points || [];
          this.loadingProductsChart = false;
          this.scheduleRenderProducts();
        },
        error: () => {
          this.productsChartPoints = [];
          this.loadingProductsChart = false;
          this.scheduleRenderProducts();
        }
      });
  }

  loadClientsChart(): void {
    this.loadingClientsChart = true;
    this.dashboardService
      .getChartByClients(this.dateFrom || undefined, this.dateTo || undefined)
      .subscribe({
        next: (response) => {
          this.clientsChartPoints = response.points || [];
          this.loadingClientsChart = false;
          this.scheduleRenderClients();
        },
        error: () => {
          this.clientsChartPoints = [];
          this.loadingClientsChart = false;
          this.scheduleRenderClients();
        }
      });
  }

  private scheduleRender(): void {
    if (!this.viewReady) return;
    requestAnimationFrame(() => {
      requestAnimationFrame(() => this.renderChart());
    });
  }

  private scheduleRenderProducts(): void {
    if (!this.viewReady) return;
    requestAnimationFrame(() => {
      requestAnimationFrame(() => this.renderProductsChart());
    });
  }

  private scheduleRenderOverviewProducts(): void {
    if (!this.viewReady) return;
    requestAnimationFrame(() => {
      requestAnimationFrame(() => this.renderOverviewProductsChart());
    });
  }

  private scheduleRenderOverviewClients(): void {
    if (!this.viewReady) return;
    requestAnimationFrame(() => {
      requestAnimationFrame(() => this.renderOverviewClientsChart());
    });
  }

  private scheduleRenderClients(): void {
    if (!this.viewReady) return;
    requestAnimationFrame(() => {
      requestAnimationFrame(() => this.renderClientsChart());
    });
  }

  private renderChart(): void {
    if (!this.chartCanvas) return;
    if (!this.chartPoints.length) {
      this.destroyChart();
      return;
    }

    const canvas = this.chartCanvas.nativeElement;
    const parent = canvas.parentElement;
    if (!parent || parent.clientWidth === 0) return;

    this.destroyChart();

    const labels = this.chartPoints.map((p) => p.period);
    const amountData = this.chartPoints.map((p) => p.amount);
    const sumData = this.chartPoints.map((p) => Number(p.sum));

    this.chartInstance = new Chart(canvas, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Количество',
            data: amountData,
            borderColor: '#3f51b5',
            backgroundColor: 'rgba(63, 81, 181, 0.12)',
            borderWidth: 2,
            tension: 0.3,
            fill: false,
            yAxisID: 'yAmount'
          },
          {
            label: 'Сумма',
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
        interaction: { mode: 'index', intersect: false },
        scales: {
          yAmount: { position: 'left', ticks: { precision: 0 } },
          ySum: { position: 'right', grid: { drawOnChartArea: false } }
        }
      }
    });
  }

  private renderBarChart(canvas: HTMLCanvasElement, points: DashboardBarPoint[]): Chart | null {
    if (!points.length) return null;

    const parent = canvas.parentElement;
    if (!parent || parent.clientWidth === 0) return null;

    const labels = points.map((p) => p.label);
    const countData = points.map((p) => Number(p.amount));
    const sumData = points.map((p) => Number(p.sum));

    return new Chart(canvas, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            label: 'count(*)',
            data: countData,
            backgroundColor: 'rgba(63, 81, 181, 0.55)',
            borderColor: '#3f51b5',
            borderWidth: 1
          },
          {
            label: 'sum(amount)',
            data: sumData,
            backgroundColor: 'rgba(255, 152, 0, 0.55)',
            borderColor: '#ff9800',
            borderWidth: 1
          }
        ]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: true } },
        scales: {
          x: { beginAtZero: true, ticks: { precision: 0 } }
        }
      }
    });
  }

  private renderProductsChart(): void {
    if (!this.productsChartCanvas) return;
    this.destroyProductsChart();
    this.productsChartInstance = this.renderBarChart(
      this.productsChartCanvas.nativeElement,
      this.productsChartPoints
    );
  }

  private getTop5BySum(points: DashboardBarPoint[]): DashboardBarPoint[] {
    return [...points]
      .sort((a, b) => Number(b.sum) - Number(a.sum))
      .slice(0, 5);
  }

  private renderOverviewProductsChart(): void {
    if (!this.overviewProductsChartCanvas) return;
    this.destroyOverviewProductsChart();
    this.overviewProductsChartInstance = this.renderBarChart(
      this.overviewProductsChartCanvas.nativeElement,
      this.getTop5BySum(this.productsChartPoints)
    );
  }

  private renderOverviewClientsChart(): void {
    if (!this.overviewClientsChartCanvas) return;
    this.destroyOverviewClientsChart();
    this.overviewClientsChartInstance = this.renderBarChart(
      this.overviewClientsChartCanvas.nativeElement,
      this.getTop5BySum(this.clientsChartPoints)
    );
  }

  private renderClientsChart(): void {
    if (!this.clientsChartCanvas) return;
    this.destroyClientsChart();
    this.clientsChartInstance = this.renderBarChart(
      this.clientsChartCanvas.nativeElement,
      this.clientsChartPoints
    );
  }

  private destroyChart(): void {
    if (this.chartInstance) {
      this.chartInstance.destroy();
      this.chartInstance = null;
    }
  }

  private destroyProductsChart(): void {
    if (this.productsChartInstance) {
      this.productsChartInstance.destroy();
      this.productsChartInstance = null;
    }
  }

  private destroyOverviewProductsChart(): void {
    if (this.overviewProductsChartInstance) {
      this.overviewProductsChartInstance.destroy();
      this.overviewProductsChartInstance = null;
    }
  }

  private destroyOverviewClientsChart(): void {
    if (this.overviewClientsChartInstance) {
      this.overviewClientsChartInstance.destroy();
      this.overviewClientsChartInstance = null;
    }
  }

  private destroyClientsChart(): void {
    if (this.clientsChartInstance) {
      this.clientsChartInstance.destroy();
      this.clientsChartInstance = null;
    }
  }
}
