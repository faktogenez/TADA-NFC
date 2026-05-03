#!/bin/bash
echo "--- Committing Notification Feature and UI Polish ---"

git add .

COMMIT_MESSAGE="Feature: Persistent Notification with Card Balance and Number

- Implemented BalanceNotificationService for ongoing status updates.
- Created custom notification layout with masked card number and bold balance.
- Refined TADA logo with bolder 'T' and fixed blue background for branding.
- Enforced vertical centering for notification elements on the Y-axis.
- Centralized all notification visual constants in CardConfig.kt.
- Removed opaque background for seamless integration with system UI."

git commit -m "$COMMIT_MESSAGE"
git push

echo "--- Commit Complete! ---"
