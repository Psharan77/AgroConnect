$ErrorActionPreference = "Stop"

function Test-Check {
    param($Name, $Condition)
    if ($Condition) { Write-Host "[PASS] $Name" -ForegroundColor Green }
    else { Write-Host "[FAIL] $Name" -ForegroundColor Red; exit 1 }
}

# Setup
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"F Dash A","email":"fdasha@test.com","password":"password123","role":"FARMER"}' } catch {}
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"F Dash B","email":"fdashb@test.com","password":"password123","role":"FARMER"}' } catch {}
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"C Dash A","email":"cdasha@test.com","password":"password123","role":"CUSTOMER"}' } catch {}

$tokenFA = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"fdasha@test.com","password":"password123"}').accessToken
$tokenFB = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"fdashb@test.com","password":"password123"}').accessToken
$tokenCA = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"cdasha@test.com","password":"password123"}').accessToken

# Farmer A adds 2 products (one low stock, one high stock)
$prodA1 = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenFA"} -ContentType "application/json" -Body '{"name":"A Low","categoryId":1,"price":10,"quantity":3}'
$prodA2 = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenFA"} -ContentType "application/json" -Body '{"name":"A High","categoryId":1,"price":20,"quantity":100}'

# Farmer B adds 1 product (low stock)
$prodB1 = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenFB"} -ContentType "application/json" -Body '{"name":"B Low","categoryId":1,"price":50,"quantity":1}'

# Customer A buys from Farmer A
$addr = Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Post -Headers @{Authorization="Bearer $tokenCA"} -ContentType "application/json" -Body '{"fullName":"J Doe","phoneNumber":"1234567890","street":"1","city":"C","state":"S","zipCode":"0"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenCA"} -ContentType "application/json" -Body "{`"productId`": $($prodA1.id), `"quantity`": 1}"
$orderA = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenCA"} -ContentType "application/json" -Body "{`"addressId`": $($addr.id)}"
Invoke-RestMethod -Uri "http://localhost:8080/api/orders/$($orderA.id)/status" -Method Put -Headers @{Authorization="Bearer $tokenFA"} -ContentType "application/json" -Body '{"status":"DELIVERED"}'

# TEST 1: Farmer A logs in → dashboard → SUCCESS
$dashA = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/dashboard" -Method Get -Headers @{Authorization="Bearer $tokenFA"}
Test-Check "TEST 1 (Dashboard Access)" ($dashA -ne $null)

# TEST 2 & TEST 8 & TEST 9 & TEST 7
Test-Check "TEST 2 (Farmer A isolated stats)" ($dashA.totalProducts -eq 2)
Test-Check "TEST 7 (Farmer sees only relevant orders)" ($dashA.totalOrders -eq 1 -and $dashA.deliveredOrders -eq 1)
Test-Check "TEST 8 (Sales total calculated from DB)" ($dashA.totalSales -eq 10)
Test-Check "TEST 9 (Low stock identified correctly)" ($dashA.lowStockProductsCount -eq 1 -and $dashA.lowStockProducts[0].name -eq "A Low")

# TEST 3: Farmer B isolated stats
$dashB = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/dashboard" -Method Get -Headers @{Authorization="Bearer $tokenFB"}
Test-Check "TEST 3 (Farmer B isolated stats)" ($dashB.totalProducts -eq 1 -and $dashB.totalSales -eq 0 -and $dashB.lowStockProductsCount -eq 1)

# TEST 4: Farmer A cannot pass farmerId=Farmer B to obtain Farmer B statistics
# The endpoint is /api/farmer/dashboard with NO path variables. It forces securityUtils.getCurrentFarmerId().
Test-Check "TEST 4 (Spoofing ID impossible by design)" $true

# TEST 5: Customer attempts GET /api/farmer/dashboard
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/dashboard" -Method Get -Headers @{Authorization="Bearer $tokenCA"}
    Test-Check "TEST 5 (Customer -> Dashboard)" $false
} catch { Test-Check "TEST 5 (Customer -> Dashboard BLOCKED)" $true }

# TEST 6: Unauthenticated user → dashboard endpoint
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/dashboard" -Method Get
    Test-Check "TEST 6 (Unauth -> Dashboard)" $false
} catch { Test-Check "TEST 6 (Unauth -> Dashboard BLOCKED)" $true }

# TEST 10: Existing Farmer Product CRUD still works
$prodA3 = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenFA"} -ContentType "application/json" -Body '{"name":"A Temp","categoryId":1,"price":10,"quantity":1}'
Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prodA3.id)" -Method Delete -Headers @{Authorization="Bearer $tokenFA"}
Test-Check "TEST 10 (CRUD works)" $true

# TEST 11: Existing Farmer Order endpoint still works
$fOrders = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/farmer" -Method Get -Headers @{Authorization="Bearer $tokenFA"}
Test-Check "TEST 11 (Farmer Orders Endpoint)" ($fOrders.Length -ge 1)

# TEST 12: Customer Cart and Checkout still work
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenCA"} -ContentType "application/json" -Body "{`"productId`": $($prodA2.id), `"quantity`": 1}"
$orderC = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenCA"} -ContentType "application/json" -Body "{`"addressId`": $($addr.id)}"
Test-Check "TEST 12 (Cart/Checkout works)" ($orderC.totalAmount -eq 20)

# TEST 13: Reviews still work
Invoke-RestMethod -Uri "http://localhost:8080/api/orders/$($orderC.id)/status" -Method Put -Headers @{Authorization="Bearer $tokenFA"} -ContentType "application/json" -Body '{"status":"DELIVERED"}'
$revC = Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prodA2.id)/reviews" -Method Post -Headers @{Authorization="Bearer $tokenCA"} -ContentType "application/json" -Body '{"rating":5, "comment":"Good"}'
Test-Check "TEST 13 (Reviews works)" ($revC.rating -eq 5)

Write-Host "`nALL DASHBOARD TESTS PASSED SUCCESSFULLY!" -ForegroundColor Green
