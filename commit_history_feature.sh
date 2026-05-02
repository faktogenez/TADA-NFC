#!/bin/bash
echo "--- Committing History Feature and UI Polish ---"

git add .

COMMIT_MESSAGE="Feature: Transaction History and Unified Dialog Design

- Implemented real transaction history reading (up to 20 records) for T-money, Cashbee, and RailPlus.
- Redesigned History and Settings dialogs to be visually consistent (same width, icons, and transparent background).
- Added a stylized 'No History' visual indicator for cards with empty records.
- Standardized all dialog widths to follow the Scene 3 (Card) template using CardConfig.
- Refined TmoneyReader logic to filter out zero-amount transactions.
- Fully localized all transaction types and status messages across 5 languages.
- Moved all Scene 4 visual constants to CardConfig.kt for easy adjustment."

git commit -m "$COMMIT_MESSAGE"
git push

echo "--- Commit Complete! ---"
