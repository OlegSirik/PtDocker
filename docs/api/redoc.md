# API Reference

<div id="redoc-container"></div>

<script setup>
import { onMounted } from 'vue'
import specUrl from './openapi/policy-api.yaml?url'

const REDOC_SCRIPT_ID = 'redoc-standalone-script'
const REDOC_SCRIPT_SRC = 'https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js'

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

  window.Redoc.init(
    specUrl,
    {
      hideHostname: true,
      expandResponses: '200,201',
      nativeScrollbars: true
    },
    container
  )
})
</script>


