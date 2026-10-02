$ErrorActionPreference = "Stop"

function Test-Check {
    param($Name, $Condition)
    if ($Condition) { Write-Host "[PASS] $Name" -ForegroundColor Green }
    else { Write-Host "[FAIL] $Name" -ForegroundColor Red; exit 1 }
}

# 1. Setup Data
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Admin User","email":"admin@test.com","password":"password123","role":"ADMIN"}' } catch {}
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Farmer Z","email":"farmerz@test.com","password":"password123","role":"FARMER"}' } catch {}
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Customer Z","email":"customerz@test.com","password":"password123","role":"CUSTOMER"}' } catch {}

$tokenAdmin = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"admin@test.com","password":"password123"}').accessToken
$tokenFarmer = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"farmerz@test.com","password":"password123"}').accessToken
$tokenCustomer = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"customerz@test.com","password":"password123"}').accessToken

# Create product as farmer
$prod = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenFarmer"} -ContentType "application/json" -Body '{"name":"Admin Test Prod","categoryId":1,"price":10,"quantity":0}'

# TEST 1: Admin access
$stats = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/dashboard" -Method Get -Headers @{Authorization="Bearer $tokenAdmin"}
Test-Check "TEST 1 (Admin Access Dashboard)" ($stats -ne $null)

# TEST 2: Customer access blocked
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/admin/dashboard" -Method Get -Headers @{Authorization="Bearer $tokenCustomer"}
    Test-Check "TEST 2 (Customer -> Admin Blocked)" $false
} catch { Test-Check "TEST 2 (Customer -> Admin Blocked)" $true }

# TEST 3: Farmer access blocked
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/admin/dashboard" -Method Get -Headers @{Authorization="Bearer $tokenFarmer"}
    Test-Check "TEST 3 (Farmer -> Admin Blocked)" $false
} catch { Test-Check "TEST 3 (Farmer -> Admin Blocked)" $true }

# TEST 4: Unauth access blocked
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/admin/dashboard" -Method Get
    Test-Check "TEST 4 (Unauth -> Admin Blocked)" $false
} catch { Test-Check "TEST 4 (Unauth -> Admin Blocked)" $true }

# TEST 5: Stats verified
Test-Check "TEST 5 (Total Customers > 0)" ($stats.totalCustomers -gt 0)
Test-Check "TEST 6 (Total Farmers > 0)" ($stats.totalFarmers -gt 0)
Test-Check "TEST 7 (Total Products > 0)" ($stats.totalProducts -gt 0)
Test-Check "TEST 8 (Out of stock tracked)" ($stats.outOfStockProducts -ge 1)

Write-Host "`nALL ADMIN SECURITY TESTS PASSED SUCCESSFULLY!" -ForegroundColor Green
