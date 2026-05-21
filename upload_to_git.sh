#!/bin/bash

# 1. Переходим на новую ветку для синхронизированного пакета
BRANCH_NAME="feature/full-sync-com-bodayan-tada"
git checkout -b $BRANCH_NAME

# 2. Добавляем все изменения
git add .

# 3. Создаем коммит
git commit -m "Milestone: Full package sync to com.bodayan.tada and fixed Coupang ad clicks"

# 4. Отправляем в репозиторий
git push origin $BRANCH_NAME

echo "------------------------------------------------"
echo "Бекап завершен! Ветка: $BRANCH_NAME"
echo "------------------------------------------------"
