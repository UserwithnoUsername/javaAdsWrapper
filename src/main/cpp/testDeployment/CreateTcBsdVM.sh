#!/usr/bin/env bash
#
#  create_tcbsd_vm.sh
#  Erstellt eine VirtualBox-VM für TwinCAT BSD unter Linux/macOS
#  (getestet mit VirtualBox ≥ 6.1)
#

set -euo pipefail

# ‑-- Standardwerte ‑--------------------------------------
TCBSD_ISO_DEFAULT="TCBSD-x64-14-203719.iso"
VBOX_PATH_DEFAULT="$(command -v VBoxManage || true)"

# ‑-- Hilfsfunktionen ‑------------------------------------
usage() {
  cat <<EOF
Verwendung: $0 -n <VM-Name> [-i <TwinCAT-ISO>] [-p <Pfad zu VBoxManage>]

  -n   Name der Virtuellen Maschine (erforderlich)
  -i   TwinCAT-BSD-Abbild (ISO/IMG); Standard: ${TCBSD_ISO_DEFAULT}
  -p   Pfad zu VBoxManage;            Standard: automatisch ermittelt

Beispiel:
  $0 -n TcBSD-VM
EOF
  exit 1
}

ask_yes_no() {
  local prompt="$1"
  read -rp "$prompt [Y/N] " answer
  [[ "${answer^^}" == "Y" ]]
}

err() { echo "Fehler: $*" >&2; exit 1; }

# ‑-- Argumente auswerten ‑------------------------------
VM_NAME=""
TCBSD_ISO="$TCBSD_ISO_DEFAULT"
VBOX_PATH="$VBOX_PATH_DEFAULT"

while getopts ":n:i:p:h" opt; do
  case "$opt" in
    n) VM_NAME="$OPTARG" ;;
    i) TCBSD_ISO="$OPTARG" ;;
    p) VBOX_PATH="$OPTARG" ;;
    h|*) usage ;;
  esac
done

[[ -z "$VM_NAME" ]] && usage
[[ -z "$VBOX_PATH" ]] && err "VBoxManage konnte nicht gefunden werden. Geben Sie den Pfad mit -p an."

WORK_DIR="$(pwd)"
VMDIR="${WORK_DIR}/${VM_NAME}"
INSTALLER_VDI="${VMDIR}/TcBSD_installer.vdi"
RUNTIME_VHD="${VMDIR}/TcBSD.vhd"

echo "Arbeitsverzeichnis:  $WORK_DIR"
echo "VM-Name:             $VM_NAME"
echo "TwinCAT-Abbild:      $TCBSD_ISO"
echo "VBoxManage:          $VBOX_PATH"
echo

# ‑-- VirtualBox prüfen / ggf. installieren ‑-------------
if [[ ! -x "$VBOX_PATH" ]]; then
  echo "VirtualBox wurde nicht gefunden: $VBOX_PATH"
  if ask_yes_no "Möchten Sie VirtualBox jetzt installieren?"; then
    if command -v apt-get &>/dev/null; then
      sudo apt-get update
      sudo apt-get install virtualbox
    elif command -v dnf &>/dev/null; then
      sudo dnf install VirtualBox
    else
      err "Automatische Installation wird auf diesem System nicht unterstützt."
    fi
    VBOX_PATH="$(command -v VBoxManage)"
  else
    err "Abbruch: Bitte VirtualBox manuell installieren."
  fi
fi

# ‑-- ISO-Datei vorhanden? ‑------------------------------
if [[ ! -f "$TCBSD_ISO" ]]; then
  echo "TwinCAT-ISO $TCBSD_ISO nicht gefunden."
  echo "Bitte von der Beckhoff-Website herunterladen und ins aktuelle Verzeichnis kopieren."
  echo "Download-Seite wird geöffnet …"
  xdg-open "https://www.beckhoff.com/de-de/produkte/ipc/software-and-tools/operating-systems/c9900-s60x-cxxxxx-0185.html" &>/dev/null || true
  exit 1
fi

# ‑-- VM anlegen ‑----------------------------------------
echo "Erstelle Virtuelle Maschine '$VM_NAME' …"
"$VBOX_PATH" createvm        --name "$VM_NAME" \
                             --basefolder "$WORK_DIR" \
                             --ostype FreeBSD_64 --register

"$VBOX_PATH" modifyvm "$VM_NAME" \
        --memory 1024 --vram 128 \
        --acpi on   --hpet on \
        --graphicscontroller vmsvga \
        --firmware efi64

# ‑-- Abbild konvertieren ‑------------------------------
echo "Konvertiere Installations-Abbild → VDI …"
mkdir -p "$VMDIR"
"$VBOX_PATH" convertfromraw --format VDI "$TCBSD_ISO" "$INSTALLER_VDI"

# ‑-- SATA-Controller & Medien einrichten ‑---------------
echo "Erstelle SATA-Controller …"
"$VBOX_PATH" storagectl "$VM_NAME" \
        --name SATA --add sata --controller IntelAhci \
        --hostiocache on --bootable on

echo "Binde Installer-VDI an Port 1 …"
"$VBOX_PATH" storageattach "$VM_NAME" \
        --storagectl SATA --port 1 --device 0 --type hdd --medium "$INSTALLER_VDI"

echo "Erstelle leere System-Festplatte (4 GiB VHD) …"
"$VBOX_PATH" createmedium --filename "$RUNTIME_VHD" --size 4096 --format VHD

echo "Binde System-Festplatte an Port 0 …"
"$VBOX_PATH" storageattach "$VM_NAME" \
        --storagectl SATA --port 0 --device 0 --type hdd --medium "$RUNTIME_VHD"

# ‑-- VM starten ‑---------------------------------------
echo
echo "Starte die VM – folgen Sie den TwinCAT-BSD-Installationsschritten."
"$VBOX_PATH" startvm "$VM_NAME" --type gui

echo "Fertig."