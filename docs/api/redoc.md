---
title: API Reference
---

# API Reference

<div id="redoc-container"></div>

<script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
<script>
  window.addEventListener('load', function() {
    Redoc.init(
      '/openapi/policy-api.yaml',
      {
        hideHostname: true,
        expandResponses: '200,201',
        nativeScrollbars: true,
        suppressWarnings: true,
        theme: {
          colors: {
            primary: { main: '#3f51b5' }
          }
        }
      },
      document.getElementById('redoc-container')
    )
  })
</script>