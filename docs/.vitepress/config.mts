// docs/.vitepress/config.mts
import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'PoliTech',
  description: 'Документация платформы Direct Insurance',
  lang: 'ru-RU',
  
  // Базовый путь для GitHub Pages
  base: '/PoliTech/',
  
  // Настройки темы
  themeConfig: {
    // Логотип (опционально)
    // logo: '/logo.png',
    
    // Навигационная панель сверху
    nav: [
      { text: 'Главная', link: '/' },
      { text: 'Руководства', link: '/user-docs/' },
      { text: 'API', link: '/api/' },
      { text: 'GitHub', link: 'https://github.com/olegsirik/PoliTech' }
    ],
    
    // Боковое меню
    sidebar: {
      // Главная секция
      '/': [
        {
          text: 'Начало работы',
          items: [
            { text: 'Введение', link: '/' },
            { text: 'Установка', link: '/installation' }
          ]
        }
      ],
      
      // Руководство пользователя
      '/user-docs/': [
        {
          text: 'Руководство пользователя',
          items: [
            { text: 'Обзор', link: '/user-docs/' },
            { text: 'Личный кабинет', link: '/user-docs/personal-cabinet' },
            { text: 'Оформление договора', link: '/user-docs/contract-issuance' }
          ]
        }
      ],
      
      // Руководство администратора
      '/doc_admin/': [
        {
          text: 'Руководство администратора',
          items: [
            { text: 'Управление тенантами', link: '/doc_admin/tenant' },
            { text: 'Управление пользователями', link: '/doc_admin/users' },
            { text: 'Роли и права', link: '/doc_admin/roles' }
          ]
        }
      ],
      
      // Разработчику
      '/development/': [
        {
          text: 'Разработчику',
          items: [
            { text: 'Аутентификация', link: '/authN/' },
            { text: 'Авторизация', link: '/authZ/' },
            { text: 'Шаблонизатор', link: '/development/Text-Template' },
            { text: 'Калькулятор', link: '/development/Calculator' }
          ]
        }
      ],
      
      // API
      '/api/': [
        {
          text: 'API Reference',
          items: [
            { text: 'Обзор API', link: '/api/' },
            { text: 'Аутентификация', link: '/api/auth' },
            { text: 'Продукты', link: '/api/products' },
            { text: 'Договоры', link: '/api/contracts' }
          ]
        }
      ]
    },
    
    // Настройки поиска
    search: {
      provider: 'local'
    },
    
    // Социальные ссылки
    socialLinks: [
      { icon: 'github', link: 'https://github.com/olegsirik/PoliTech' }
    ],
    
    // Футер
    footer: {
      message: 'PoliTech — Direct Insurance Platform',
      copyright: 'Copyright © 2024'
    },
    
    // Редактировать на GitHub
    editLink: {
      pattern: 'https://github.com/olegsirik/PoliTech/edit/main/docs/:path',
      text: 'Редактировать эту страницу на GitHub'
    },
    
    // Последнее обновление
    lastUpdated: {
      text: 'Обновлено',
      formatOptions: {
        dateStyle: 'full',
        timeStyle: 'medium'
      }
    }
  },
  
  // Markdown настройки
  markdown: {
    theme: 'github-dark',
    lineNumbers: true
  }
})