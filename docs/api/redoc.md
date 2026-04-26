# API Reference

<div id="redoc-container"></div>

<script setup>
import { onMounted } from 'vue'
import { withBase } from 'vitepress'

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
  await loadRedocScript()

  const container = document.getElementById('redoc-container')
  if (!container || !window.Redoc) return

  window.Redoc.init(
    withBase('/api/openapi/policy-api.yaml'),
    {
      hideHostname: true,
      expandResponses: '200,201',
      nativeScrollbars: true
    },
    container
  )
})
</script>


