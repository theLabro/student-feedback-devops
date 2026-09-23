#!/bin/bash
set -euo pipefail
test "$(id -u)" = 0 || { echo 'Run this installer with sudo.' >&2; exit 1; }
script_dir=$(cd -- "$(dirname -- "$0")" && pwd)
install -d -o root -g root -m 0755 /usr/local/lib/student-feedback
install -o root -g root -m 0755 "$script_dir/verify-health.py" /usr/local/lib/student-feedback/verify-health.py
install -o root -g root -m 0755 "$script_dir/student-feedback-release" /usr/local/sbin/student-feedback-release
install -d -o tomcat -g tomcat -m 0750 /var/lib/tomcat10/student-feedback-releases
sudoers_file=$(mktemp)
trap 'rm -f "$sudoers_file"' EXIT
cat > "$sudoers_file" <<'RULE'
jenkins ALL=(tomcat) NOPASSWD: /usr/local/sbin/student-feedback-release deploy, /usr/local/sbin/student-feedback-release accept, /usr/local/sbin/student-feedback-release rollback
RULE
visudo -cf "$sudoers_file"
install -o root -g root -m 0440 "$sudoers_file" /etc/sudoers.d/student-feedback-deploy
echo 'Installed app-specific deployment helper; Jenkins has no unrestricted sudo access.'
