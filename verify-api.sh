#!/usr/bin/env bash
set -euo pipefail
BASE=http://localhost:8080

echo "=== GET all (page 0, size 3) ==="
curl -sS "$BASE/api/employees?page=0&size=3&sort=lastName,asc"
echo
echo
echo "=== GET by id 1 ==="
curl -sS -D - -o /tmp/emp1.json "$BASE/api/employees/1" | head -n 15
echo
cat /tmp/emp1.json
echo
echo
echo "=== GET missing id ==="
curl -sS -o /tmp/emp404.json -w "HTTP %{http_code}\n" "$BASE/api/employees/9999"
cat /tmp/emp404.json
echo
echo
echo "=== GET department Engineering ==="
curl -sS "$BASE/api/employees/department/Engineering"
echo
echo
echo "=== GET search name=ada ==="
curl -sS "$BASE/api/employees/search?name=ada"
echo
echo
echo "=== POST create ==="
curl -sS -D - -o /tmp/created.json -X POST "$BASE/api/employees" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Edsger","lastName":"Dijkstra","email":"edsger.dijkstra@example.com","department":"Research","salary":145000}'
echo
cat /tmp/created.json
echo
echo
echo "=== POST invalid ==="
curl -sS -o /tmp/invalid.json -w "HTTP %{http_code}\n" -X POST "$BASE/api/employees" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"","lastName":"X","email":"bad","department":"IT","salary":-1}'
cat /tmp/invalid.json
echo
echo
echo "=== POST duplicate email ==="
curl -sS -o /tmp/dup.json -w "HTTP %{http_code}\n" -X POST "$BASE/api/employees" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"ada.lovelace@example.com","department":"Engineering","salary":1}'
cat /tmp/dup.json
echo
echo
echo "=== PUT update 1 ==="
curl -sS -X PUT "$BASE/api/employees/1" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"ada.lovelace@example.com","department":"Research","salary":125000.00}'
echo
echo
echo "=== DELETE last created (id 9) ==="
curl -sS -o /dev/null -w "HTTP %{http_code}\n" -X DELETE "$BASE/api/employees/9"
echo "VERIFY_DONE"
