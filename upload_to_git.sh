#!/bin/bash

# 1. Переходим на новую ветку для финального дизайна рекламы
BRANCH_NAME="feature/ad-design-final"
git checkout -b $BRANCH_NAME

# 2. Добавляем изменения
git add .

# 3. Создаем коммит
git commit -m "UI: Finalized ad carousel design with white borders, compact legal text, and Google Drive auto-sync"

# 4. Отправляем
git push origin $BRANCH_NAME

echo "------------------------------------------------"
echo "Бэкап завершен! Ветка: $BRANCH_NAME"
echo "------------------------------------------------"
