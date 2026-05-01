#!/bin/bash

# Скрипт для фиксации изменений и отправки в GitHub

echo "--- Starting Git Update ---"

# Добавляем все измененные файлы
git add .

# Формируем сообщение коммита
COMMIT_MESSAGE="Fix: XML resource error, remove white flash, and optimize TadaCard UI

- Fixed XML declaration order in ic_launcher.xml
- Removed white screen on startup by disabling window preview and making background transparent
- Optimized TadaCard: merged User Type and Age info into one row with refined styling
- Made TadaCard height dynamic to fit long descriptions
- Fixed duplicate imports and compilation errors"

# Коммитим
git commit -m "$COMMIT_MESSAGE"

# Отправляем в текущую ветку
git push

echo "--- Update Complete! ---"
