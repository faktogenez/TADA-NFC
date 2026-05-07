#!/bin/bash

# 1. Переходим на новую ветку для дизайна и рефакторинга
BRANCH_NAME="feature/design-refactor-slate"
git checkout -b $BRANCH_NAME

# 2. Добавляем все изменения
git add .

# 3. Создаем коммит
git commit -m "Feature: Refactored project into modules and updated UI to Adult Slate style"

# 4. Отправляем в репозиторий
git push origin $BRANCH_NAME

echo "------------------------------------------------"
echo "Бекап завершен! Ветка: $BRANCH_NAME"
echo "------------------------------------------------"
