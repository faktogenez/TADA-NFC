#!/bin/bash

# Переходим в директорию проекта
cd "C:/Users/LEO/OneDrive/Desktop/AndroidStudio/TADA_NFC"

# Инициализируем локальный репозиторий
git init

# Настраиваем удаленный репозиторий
# Удаляем старую привязку, если она есть, и добавляем актуальную
git remote remove origin 2>/dev/null
git remote add origin https://github.com/faktogenez/TADA-NFC.git

# Добавляем все файлы проекта (включая код, ресурсы и настройки gradle)
git add .

# Создаем коммит
git commit -m "Initial upload: Full TADA-NFC project with new design"

# Отправляем файлы в ветку master
# Флаг -f (force) нужен, чтобы перезаписать пустой репозиторий, если там были файлы (например, README)
git push -u origin master -f

echo "-------------------------------------------------------"
echo "Скрипт завершен. Проверьте ваш GitHub: https://github.com/faktogenez/TADA-NFC"
