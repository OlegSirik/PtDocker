---
aside: false
sidebar: false
---

# API Reference

<div id="redoc-container" style="min-height: 70vh;">Loading API reference...</div>

<script setup>
import { onMounted } from 'vue'
import { withBase } from 'vitepress'
import specUrl from './openapi/policy-api.yaml?url'

const REDOC_SCRIPT_ID = 'redoc-standalone-script'
const REDOC_SCRIPT_SRC = withBase('/redoc/redoc.standalone.js')

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
</script>

<style>
._PtDocker_api_redoc {
  max-width: none !important;
}

._PtDocker_api_redoc #redoc-container {
  width: 100%;
}

.VPDoc.has-aside .content-container {
  max-width: 100% !important;
}

.VPContent.has-sidebar {
  padding-left: 0 !important;
}

.VPDoc.has-sidebar .container {
  max-width: 100% !important;
}
</style>


