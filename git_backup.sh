#!/bin/bash

# Скрипт для бэкапа всех последних улучшений в Git

echo "🚀 Начинаю процесс бэкапа..."

# Добавляем все изменения
git add .

# Формируем сообщение коммита
COMMIT_MSG="feat: implement foolproof NFC protection and UI improvements

- Added strict NFC status check on startup and splash
- Redesigned NFC Disabled dialog with red warning theme and transparency
- Updated multi-language instructions (EN, RU, KO, JA, ZH)
- Reduced scanning time instruction to 3 seconds
- Fixed XML compilation errors in launcher icons
- Locked screen orientation to portrait for better UX
- Ensured consistent semi-transparent background for all dialogs"

# Делаем коммит
git commit -m "$COMMIT_MSG"

# Пушим в текущую ветку
echo "⬆️ Отправка данных на удаленный репозиторий..."
git push

echo "✅ Бэкап успешно завершен!"
