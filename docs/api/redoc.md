---
aside: false
sidebar: false
nav: false
footer: false
---

# API Reference

<div id="redoc-container" style="min-height: 70vh;">Loading API reference...</div>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { withBase } from 'vitepress'
import specUrl from './openapi/policy-api.yaml?url'

const REDOC_SCRIPT_ID = 'redoc-standalone-script'
const REDOC_SCRIPT_SRC = withBase('/redoc/redoc.standalone.js')
const REDOC_PAGE_CLASS = 'redoc-page'

function loadRedocScript() {
  return new Promise((resolve, reject) => {
    if (window.Redoc) {
      resolve()
      return
    }

    const existingScript = document.getElementById(REDOC_SCRIPT_ID)
    if (existingScript) {
      existingScript.addEventListener('load', () => resolve(), { once: true })
      existingScript.addEventListener('error', reject, { once: true })
      return
    }

    const script = document.createElement('script')
    script.id = REDOC_SCRIPT_ID
    script.src = REDOC_SCRIPT_SRC
    script.async = true
    script.onload = () => resolve()
    script.onerror = reject
    document.head.appendChild(script)
  })
}

onMounted(async () => {
  document.body.classList.add(REDOC_PAGE_CLASS)

  const container = document.getElementById('redoc-container')
  if (!container) return

  try {
    await loadRedocScript()
  } catch {
    container.innerHTML = '<p>Failed to load ReDoc script.</p>'
    return
  }

  if (!window.Redoc) {
    container.innerHTML = '<p>ReDoc is not available in browser context.</p>'
    return
  }

  try {
    window.Redoc.init(
      specUrl,
      {
        hideHostname: true,
        hideRightPanel: true,
        expandResponses: '200,201',
        nativeScrollbars: true
      },
      container,
      (error) => {
        if (!error) return
        container.textContent = `ReDoc render error: ${String(error)}`
      }
    )
  } catch (error) {
    container.textContent = `ReDoc init failed: ${String(error)}`
  }
})

onUnmounted(() => {
  document.body.classList.remove(REDOC_PAGE_CLASS)
})
</script>

<style>
._PtDocker_api_redoc {
  max-width: none !important;
}

._PtDocker_api_redoc #redoc-container {
  width: 100%;
}

.vp-doc._PtDocker_api_redoc {
  max-width: none !important;
  padding: 0 !important;
}

body.redoc-page .VPDoc:not(.has-sidebar) .content {
  max-width: 1600px !important;
}

body.redoc-page .VPDoc .container {
  max-width: 1600px !important;
}

#redoc-container .redoc-wrap {
  max-width: none !important;
  margin: 0 !important;
}

#redoc-container .menu-content {
  width: 280px !important;
}
</style>


