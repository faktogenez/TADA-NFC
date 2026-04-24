#!/bin/bash

# Переходим в директорию проекта
cd "C:/Users/LEO/OneDrive/Desktop/AndroidStudio/TADA_NFC"

echo "--- Добавление файлов ---"
git add .

echo "--- Создание коммита ---"
# Если коммитить нечего, скрипт не упадет
git commit -m "Final design updates, localization and icon fixes" || echo "Нет изменений для коммита"

echo "--- Отправка на GitHub ---"
git push origin master

echo "-------------------------------------------------------"
echo "Готово! Проверьте: https://github.com/faktogenez/TADA-NFC"
