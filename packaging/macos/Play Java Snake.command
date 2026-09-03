#!/bin/zsh
set -e

package_directory="$(cd -- "$(dirname -- "$0")" && pwd)"
clear
exec "$package_directory/Java Snake.app/Contents/MacOS/Java Snake"
