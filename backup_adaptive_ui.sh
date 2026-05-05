#!/bin/bash

# Скрипт для бэкапа финальных доработок адаптивного дизайна и защиты NFC

echo "🚀 Подготовка финального бэкапа..."

# Добавляем все изменения
git add .

# Формируем детальное сообщение коммита
COMMIT_MSG="feat: full adaptive UI and final NFC protection redesign

- Implemented intelligent responsive scaling for fonts and DP sizes
- Redesigned NFC Disabled dialog based on user mockup (red header, purple border)
- Added 'Exit App' functionality to the NFC dialog close button
- Optimized text sizes for different screen scales (Note 9, A22 support)
- Centralized all UI variables in CardConfig.kt with Russian comments
- Fixed 'NFC Disabled' text overflow issue
- Ensured consistent semi-transparent dialog backgrounds"

# Выполняем коммит
git commit -m "$COMMIT_MSG"

# Отправляем в репозиторий
echo "⬆️ Синхронизация с GitHub..."
git push

echo "✅ Все изменения успешно сохранены в облако!"
