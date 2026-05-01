#!/bin/bash
echo "--- Committing Improvements Phase 1 ---"
git add .
git commit -m "Improvement: NFC diagnostics, UI/UX polish, and architectural cleanup

- Added NFC status monitoring and localized diagnostic dialog
- Enhanced TadaCard UI with gradient header and refined spacing
- Improved Processing UI with informative text and centered layout
- Refactored AppState to include NfcDisabled state
- Fixed icon resolution issues in MainActivity"
git push
echo "--- Improvements Phase 1 Committed! ---"
