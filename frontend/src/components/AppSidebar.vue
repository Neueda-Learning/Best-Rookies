<!-- 此文件是应用侧边栏组件，用于展示与参考图一致的品牌区和主导航。 -->
<template>
  <div class="sidebar">
    <RouterLink class="sidebar__brand" :to="{ name: 'portfolio-list' }">
      <span class="sidebar__brand-mark">PM</span>

      <div>
        <strong>Portfolio</strong>
        <p>Manager</p>
      </div>
    </RouterLink>

    <section class="sidebar__section">
      <p class="sidebar__caption">{{ t('sidebarNavigate') }}</p>

      <RouterLink
        class="sidebar__nav-link"
        :class="{ 'sidebar__nav-link--active': route.name === 'portfolio-list' || route.name === 'portfolio-detail' }"
        :to="{ name: 'portfolio-list' }"
      >
        <span class="sidebar__icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3.5" y="5.5" width="17" height="14" rx="2"></rect>
            <path d="M8 5.5v14"></path>
            <path d="M3.5 10.5h17"></path>
          </svg>
        </span>

        <div class="sidebar__nav-copy">
          <strong>{{ t('sidebarPortfolioRegistryTitle') }}</strong>
        </div>
      </RouterLink>

      <div class="sidebar__nav-link sidebar__nav-link--muted">
        <span class="sidebar__icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M5 18.5V10"></path>
            <path d="M11.5 18.5V5"></path>
            <path d="M18 18.5v-8"></path>
            <path d="M4 18.5h16"></path>
          </svg>
        </span>

        <div class="sidebar__nav-copy">
          <strong>{{ t('sidebarAnalyticsTitle') }}</strong>
        </div>

        <span class="sidebar__soon">{{ t('sidebarComingSoon') }}</span>
      </div>

      <div class="sidebar__nav-link sidebar__nav-link--muted">
        <span class="sidebar__icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4.5 15.5l4-4 3 3 6-7"></path>
            <path d="M17.5 7.5H20v2.5"></path>
          </svg>
        </span>

        <div class="sidebar__nav-copy">
          <strong>{{ t('sidebarMarketTrendsTitle') }}</strong>
        </div>

        <span class="sidebar__soon">{{ t('sidebarComingSoon') }}</span>
      </div>

      <div class="sidebar__nav-link sidebar__nav-link--muted">
        <span class="sidebar__icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="3"></circle>
            <path d="M19.4 15a1.6 1.6 0 0 0 .32 1.76l.06.06a2 2 0 0 1-2.82 2.82l-.06-.06a1.6 1.6 0 0 0-1.76-.32 1.6 1.6 0 0 0-.98 1.46V21a2 2 0 0 1-4 0v-.1a1.6 1.6 0 0 0-.98-1.46 1.6 1.6 0 0 0-1.76.32l-.06.06a2 2 0 1 1-2.82-2.82l.06-.06A1.6 1.6 0 0 0 4.6 15a1.6 1.6 0 0 0-1.46-.98H3a2 2 0 0 1 0-4h.1A1.6 1.6 0 0 0 4.56 9a1.6 1.6 0 0 0-.32-1.76l-.06-.06a2 2 0 1 1 2.82-2.82l.06.06A1.6 1.6 0 0 0 8.82 4.6h.1a1.6 1.6 0 0 0 1.46-.98V3a2 2 0 0 1 4 0v.1a1.6 1.6 0 0 0 .98 1.46 1.6 1.6 0 0 0 1.76-.32l.06-.06a2 2 0 0 1 2.82 2.82l-.06.06A1.6 1.6 0 0 0 19.4 9c.4.19.83.29 1.28.28H21a2 2 0 0 1 0 4h-.1a1.6 1.6 0 0 0-1.5.98Z"></path>
          </svg>
        </span>

        <div class="sidebar__nav-copy">
          <strong>{{ t('sidebarSettingsTitle') }}</strong>
        </div>

        <span class="sidebar__soon">{{ t('sidebarComingSoon') }}</span>
      </div>
    </section>

    <button type="button" class="sidebar__collapse">
      <span aria-hidden="true">&lt;</span>
      <span>{{ t('sidebarCollapse') }}</span>
    </button>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useI18n } from '../composables/useI18n'

const route = useRoute()
const { t } = useI18n()
</script>