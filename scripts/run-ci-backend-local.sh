#!/usr/bin/env bash
# run-ci-backend-local.sh
# Simula localmente el pipeline CI del backend MiniJira.
# Honesto: si algo falla, lo reporta. No simula éxito.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

PASS=0
FAIL=0

echo "======================================"
echo "  CI LOCAL — MiniJira Backend"
echo "  Directorio: $PROJECT_ROOT"
echo "======================================"
echo ""

# ─── PASO 1: Tests unitarios ─────────────────────────────────────
echo "[1/3] Tests unitarios (mvn clean test)..."
if mvn clean test --no-transfer-progress; then
  echo "      OK — tests unitarios pasaron."
  PASS=$((PASS + 1))
else
  echo "      FALLO — uno o más tests fallaron."
  echo "             Revisar salida de Maven y corregir antes de push."
  FAIL=$((FAIL + 1))
fi
echo ""

# ─── PASO 2: Verify ──────────────────────────────────────────────
echo "[2/3] Verificación integrada (mvn verify)..."
if mvn verify --no-transfer-progress; then
  echo "      OK — verify completado sin errores."
  PASS=$((PASS + 1))
else
  echo "      FALLO — mvn verify falló."
  echo "             Puede indicar fallos en integration tests o packaging."
  FAIL=$((FAIL + 1))
fi
echo ""

# ─── PASO 3: OWASP Dependency Check ─────────────────────────────
echo "[3/3] OWASP Dependency Check (CVSS >= 7 falla)..."
if mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7 --no-transfer-progress; then
  echo "      OK — sin vulnerabilidades con CVSS >= 7."
  PASS=$((PASS + 1))
else
  echo "      FALLO — vulnerabilidades con CVSS >= 7 detectadas."
  echo "             Revisar target/dependency-check-report.html para detalle."
  FAIL=$((FAIL + 1))
fi
echo ""

# ─── RESUMEN ─────────────────────────────────────────────────────
echo "======================================"
echo "  RESUMEN CI LOCAL BACKEND"
echo "======================================"
echo "  Pasaron : $PASS"
echo "  Fallaron: $FAIL"
echo "======================================"

if [ "$FAIL" -gt "0" ]; then
  echo "  Estado: FALLO — corregir antes de push."
  exit 1
else
  echo "  Estado: OK — seguro para push."
  exit 0
fi
