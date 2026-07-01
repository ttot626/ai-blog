#!/bin/bash
# 备份 ai_blog 数据库到当前目录
set -e
BACKUP_FILE="ai_blog_backup_$(date +%Y%m%d_%H%M%S).sql"
docker exec aiblog-mysql mysqldump -uroot -p"${DB_PASSWORD:-abc123456}" ai_blog > "$BACKUP_FILE"
echo "备份完成: $BACKUP_FILE"
