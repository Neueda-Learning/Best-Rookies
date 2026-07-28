<!-- 此文件是应用根组件，用于组织与参考图一致的后台式壳层和顶部工具栏。 -->
<template>
  <div class="app-shell">
    <aside class="app-shell__sidebar">
      <AppSidebar />
    </aside>

    <div class="app-shell__workspace">
      <header class="app-topbar">
        <div class="app-topbar__title-group">
          <p class="app-topbar__section">{{ currentView.section }}</p>
          <h1 class="app-topbar__title">{{ currentView.title }}</h1>
        </div>

        <div class="app-topbar__actions">
          <div class="locale-switch" role="group" :aria-label="t('appBrand')">
            <button
              type="button"
              class="locale-switch__button"
              :class="{ 'locale-switch__button--active': locale === 'en' }"
              @click="setLocale('en')"
            >
              EN
            </button>
            <button
              type="button"
              class="locale-switch__button"
              :class="{ 'locale-switch__button--active': locale === 'zh' }"
              @click="setLocale('zh')"
            >
              中文
            </button>
          </div>

          <button type="button" class="app-topbar__icon-button" aria-label="notifications">
            <span class="app-topbar__icon-ring"></span>
          </button>
          <button type="button" class="app-topbar__avatar" aria-label="profile">PM</button>
        </div>
      </header>

      <main class="app-content">
        <RouterView />
      </main>
    </div>

    <AppToastContainer />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppSidebar from './components/AppSidebar.vue'
import AppToastContainer from './components/AppToastContainer.vue'
import { useI18n } from './composables/useI18n'

const route = useRoute()
const { locale, setLocale, t } = useI18n()

// 顶部栏只关心当前视图所属区段，避免把业务标题重复塞进根组件。
const currentView = computed(() => {
  if (route.name === 'portfolio-detail') {
    return {
      section: t('topbarSectionDetail'),
      title: t('currentViewDetailBadge')
    }
  }

  return {
    section: t('topbarSectionList'),
    title: t('currentViewListBadge')
  }
})
</script>

