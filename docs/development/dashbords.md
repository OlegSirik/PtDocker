Откуда беруться данные 

Из бэкенд API через DashboardService в dashboard.component.ts.

Конкретно:

Карточки (cards)

DashboardService.getCards()
endpoint: ${baseApiUrl}/dashboard/cards
График по периодам (верхний line chart)

DashboardService.getChart(period, from, to)
endpoint: ${baseApiUrl}/dashboard/chart
График по продуктам (и на вкладке, и top-5 на Overview)

DashboardService.getChartByProducts(from, to)
endpoint: ${baseApiUrl}/dashboard/chart-by-products
приходит массив points: DashboardBarPoint[] с label, amount, sum
на Overview просто берется top-5 из этого же массива (getTop5BySum)
График по клиентам (и на вкладке, и top-5 на Overview)

DashboardService.getChartByClients(from, to)
endpoint: ${baseApiUrl}/dashboard/chart-by-clients
аналогично: полные данные в clientsChartPoints, top-5 — это срез для 1-й вкладки
baseApiUrl берется из AuthService (тенант-специфичный базовый путь API).