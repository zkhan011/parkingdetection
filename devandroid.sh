#!/usr/bin/env bash
set -euo pipefail
exec "$(dirname "$0")/scripts/setup_compile_test_linux.sh" "${1:-https://github.com/zkhan011/parkingdetection.git}" "${2:-${PWD}/parkingdetection-linux-build}"
