#!/bin/bash

xdg-all-apps() {
     LOCAL="${XDG_DATA_HOME:-$HOME/.local/share}/applications/mimeinfo.cache"
     GLOBAL="/usr/share/applications/mimeinfo.cache"
 
     MATCHING="$(grep -h "$1" "$LOCAL" "$GLOBAL")"
     if [ -z "$MATCHING" ]; then
         echo "There are no application associated with $1"
         return
     fi
     echo "$MATCHING" |cut -d = -f 2 |\
         sed -z -e 's:\n::;s:;:\n:g' |\
         sort |uniq
}

xdg-all-apps image/svg+xml
