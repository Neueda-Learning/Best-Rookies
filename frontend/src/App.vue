<!-- 此文件是应用根组件，用于组织与参考图一致的后台式壳层和顶部工具栏。 -->
<template>
  <div class="app-shell" :class="{ 'app-shell--sidebar-collapsed': isSidebarCollapsed }">
    <aside class="app-shell__sidebar">
      <AppSidebar :collapsed="isSidebarCollapsed" @toggle-collapse="toggleSidebarCollapse" />
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

          <button
            type="button"
            class="app-topbar__icon-button app-topbar__theme-button"
            :aria-label="isDarkTheme ? t('themeSwitchToLight') : t('themeSwitchToDark')"
            :title="isDarkTheme ? t('themeSwitchToLight') : t('themeSwitchToDark')"
            :aria-pressed="isDarkTheme"
            @click="toggleTheme"
          >
            <span class="app-topbar__theme-glyph" :class="{ 'app-topbar__theme-glyph--dark': isDarkTheme }"></span>
          </button>
          <button type="button" class="app-topbar__avatar" aria-label="profile">PM</button>
        </div>
      </header>

      <main class="app-content">
        <RouterView v-slot="{ Component, route: currentRoute }">
          <Transition name="view-fade" mode="out-in" appear>
            <component :is="Component" :key="currentRoute.fullPath" />
          </Transition>
        </RouterView>
      </main>
    </div>

    <AppToastContainer />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppSidebar from './components/AppSidebar.vue'
import AppToastContainer from './components/AppToastContainer.vue'
import { useI18n } from './composables/useI18n'

const THEME_STORAGE_KEY = 'best-rookies-theme'

function detectInitialTheme() {
  if (typeof window === 'undefined') {
    return 'light'
  }

  const savedTheme = window.localStorage.getItem(THEME_STORAGE_KEY)

  if (savedTheme === 'dark' || savedTheme === 'light') {
    return savedTheme
  }

  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

const route = useRoute()
const { locale, setLocale, t } = useI18n()
const isSidebarCollapsed = ref(false)
const theme = ref(detectInitialTheme())
const isDarkTheme = computed(() => theme.value === 'dark')

if (typeof document !== 'undefined') {
  watch(
    theme,
    (value) => {
      document.documentElement.dataset.theme = value
    },
    { immediate: true }
  )
}

function toggleSidebarCollapse() {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

function setTheme(nextTheme) {
  if (nextTheme !== 'dark' && nextTheme !== 'light') {
    return
  }

  theme.value = nextTheme

  if (typeof window !== 'undefined') {
    window.localStorage.setItem(THEME_STORAGE_KEY, nextTheme)
  }
}

function toggleTheme() {
  setTheme(isDarkTheme.value ? 'light' : 'dark')
}

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

