#!/bin/bash

# Название новой ветки
BRANCH_NAME="design-update-2024"

echo ">>> Creating and switching to branch: $BRANCH_NAME <<<"

# Проверка, есть ли изменения в текущей ветке, чтобы не потерять их
git status

# Создание новой ветки и переключение на нее
git checkout -b $BRANCH_NAME

echo ">>> Switched to $BRANCH_NAME. Now we can safely edit the design. <<<"
