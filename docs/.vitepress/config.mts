// docs/.vitepress/config.mts
import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'PoliTech',
  description: 'Документация платформы Direct Insurance',
  lang: 'ru-RU',
  base: '/PtDocker/',
  
  themeConfig: {
    logo: null,  // 👈 отключаем логотип
    
    nav: [
      { text: 'Главная', link: '/about.md' },
      { text: 'Руководства', link: '/user-docs/README.md' },
      { text: 'API', link: '/api/api-overview.md' },  // ← существующий файл
      { text: 'GitHub', link: 'https://github.com/olegsirik/PtDocker' }
    ],
    
    sidebar: [
      {
        text: 'Начало работы',
        items: [
          { text: 'Что такое PoliTech', link: '/start/what-the.md' },
          { text: 'Установка', link: '/installation' }
        ]
      },
      {
        text: 'Руководство пользователя',
        items: [
          { text: 'Обзор', link: '/user-docs/README.md' },
          { text: 'Личный кабинет', link: '/user-docs/personal-cabinet' },
          { text: 'Оформление договора', link: '/user-docs/contract-issuance' }
        ]
      },
      {
        text: 'Руководство администратора',
        items: [
          { text: 'Управление тенантами', link: '/doc_admin/tenant' },
          { text: 'Управление пользователями', link: '/doc_admin/users' },
          { text: 'Роли и права', link: '/doc_admin/roles' }
        ]
      },
      {
        text: 'Разработчику',
        items: [
          { text: 'Аутентификация', link: '/authN/' },
          { text: 'Авторизация', link: '/authZ/' },
          { text: 'Шаблонизатор', link: '/development/Text-Template' },
          { text: 'Калькулятор', link: '/development/Calculator' }
        ]
      },
      {
        text: 'Типовые решения',
        items: [
            { text: 'Страхование от несчастных случаев', link: '/user-docs/product-ns' },
            { text: 'Страхование пассажиров', link: '/user-docs/product-pax' },
            { text: 'Страхование бытовой электроники', link: '/user-docs/product-gad' }
          ]
      },
      {
        text: 'API',
        items: [
          { text: 'Обзор API', link: '/api/api-overview.md' }
        ]
      }
    ],
    
    search: { provider: 'local' },
    socialLinks: [{ icon: 'github', link: 'https://github.com/olegsirik/PtDocker' }],
    footer: {
      message: 'PxP PoliTech — Direct Insurance Platform',
      copyright: 'Copyright © 2024'
    },
    editLink: {
      pattern: 'https://github.com/olegsirik/PtDocker/edit/main/docs/:path',
      text: 'Редактировать эту страницу на GitHub'
    },
    lastUpdated: {
      text: 'Обновлено',
      formatOptions: { dateStyle: 'full', timeStyle: 'medium' }
    }
  },
  
  markdown: {
    theme: 'github-dark',
    lineNumbers: true
  }
})