#!/bin/bash

# 1. Переходим на новую ветку для интеграции Firestore
BRANCH_NAME="feature/firebase-firestore-migration"
git checkout -b $BRANCH_NAME

# 2. Добавляем изменения
git add .

# 3. Создаем коммит
git commit -m "Migration: Switched ad source from Remote Config to Firestore (collection: ads, doc: coupang)"

# 4. Отправляем в репозиторий
git push origin $BRANCH_NAME

echo "------------------------------------------------"
echo "Бекап завершен! Ветка: $BRANCH_NAME"
echo "------------------------------------------------"
