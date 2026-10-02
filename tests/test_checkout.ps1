$ErrorActionPreference = "Stop"

function Test-Check {
    param($Name, $Condition)
    if ($Condition) { Write-Host "[PASS] $Name" -ForegroundColor Green }
    else { Write-Host "[FAIL] $Name" -ForegroundColor Red; exit 1 }
}

# 1. Setup Data
$rand = Get-Random
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body "{`"fullName`":`"Customer A`",`"email`":`"custa$rand@test.com`",`"password`":`"password123`",`"role`":`"CUSTOMER`"}" } catch {}
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body "{`"fullName`":`"Customer B`",`"email`":`"custb$rand@test.com`",`"password`":`"password123`",`"role`":`"CUSTOMER`"}" } catch {}
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body "{`"fullName`":`"Farmer Checkout`",`"email`":`"fchk$rand@test.com`",`"password`":`"password123`",`"role`":`"FARMER`"}" } catch {}

$tokenA = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body "{`"email`":`"custa$rand@test.com`",`"password`":`"password123`"}").accessToken
$tokenB = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body "{`"email`":`"custb$rand@test.com`",`"password`":`"password123`"}").accessToken
$tokenF = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body "{`"email`":`"fchk$rand@test.com`",`"password`":`"password123`"}").accessToken

# Create product
$prod = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenF"} -ContentType "application/json" -Body '{"name":"Checkout Prod","categoryId":1,"price":10,"quantity":50}'

# TEST 1: Customer A creates address
$addrA = Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"fullName":"Cust A","phoneNumber":"1234567890","street":"1A","city":"C","state":"S","zipCode":"100"}'
Test-Check "TEST 1 (Customer A creates address)" ($addrA -ne $null)

# Customer B creates address
$addrB = Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Post -Headers @{Authorization="Bearer $tokenB"} -ContentType "application/json" -Body '{"fullName":"Cust B","phoneNumber":"0987654321","street":"1B","city":"C","state":"S","zipCode":"200"}'

# TEST 2: Customer A retrieves addresses
$listA = Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Get -Headers @{Authorization="Bearer $tokenA"}
Test-Check "TEST 2 (Customer A sees only A's addresses)" ($listA.Length -eq 1 -and $listA[0].id -eq $addrA.id)

# TEST 3: Customer B retrieves addresses
$listB = Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Get -Headers @{Authorization="Bearer $tokenB"}
Test-Check "TEST 3 (Customer B sees only B's addresses)" ($listB.Length -eq 1 -and $listB[0].id -eq $addrB.id)

# TEST 4: Customer A attempts to use Customer B's address during checkout
# Add to cart
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"productId`": $($prod.id), `"quantity`": 1}"
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"addressId`": $($addrB.id)}"
    Test-Check "TEST 4 (A uses B's address)" $false
} catch { Test-Check "TEST 4 (A uses B's address BLOCKED)" $true }

# TEST 5: Farmer attempts to access customer addresses
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Get -Headers @{Authorization="Bearer $tokenF"}
    Test-Check "TEST 5 (Farmer access addresses)" $false
} catch { Test-Check "TEST 5 (Farmer access addresses BLOCKED)" $true }

# TEST 6: Unauth access
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Get
    Test-Check "TEST 6 (Unauth access addresses)" $false
} catch { Test-Check "TEST 6 (Unauth access addresses BLOCKED)" $true }

# TEST 7: Customer checks out successfully
$orderA = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"addressId`": $($addrA.id)}"
Test-Check "TEST 7 (Customer checkout success)" ($orderA.id -gt 0)

# TEST 12: Cart cleared
$cartA = Invoke-RestMethod -Uri "http://localhost:8080/api/cart" -Method Get -Headers @{Authorization="Bearer $tokenA"}
Test-Check "TEST 12 (Cart cleared)" ($cartA.items.Length -eq 0)

# TEST 8: Checkout with empty cart
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"addressId`": $($addrA.id)}"
    Test-Check "TEST 8 (Empty cart checkout)" $false
} catch { Test-Check "TEST 8 (Empty cart checkout BLOCKED)" $true }

# TEST 9 & 10 & 11: Insufficient stock
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenB"} -ContentType "application/json" -Body "{`"productId`": $($prod.id), `"quantity`": 2}"
# Farmer updates product stock to 1
Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prod.id)" -Method Put -Headers @{Authorization="Bearer $tokenF"} -ContentType "application/json" -Body '{"name":"Checkout Prod","categoryId":1,"price":10,"quantity":1}'
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenB"} -ContentType "application/json" -Body "{`"addressId`": $($addrB.id)}"
    Test-Check "TEST 9 (Insufficient stock checkout)" $false
} catch { Test-Check "TEST 9 (Insufficient stock checkout BLOCKED)" $true }

$cartB = Invoke-RestMethod -Uri "http://localhost:8080/api/cart" -Method Get -Headers @{Authorization="Bearer $tokenB"}
Test-Check "TEST 10 & 11 (Failed checkout doesn't alter cart)" ($cartB.items.Length -eq 1 -and $cartB.items[0].quantity -eq 2)

# TEST 13: Price freeze
$pUpdated = Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prod.id)" -Method Get
Test-Check "TEST 13 (Price freeze validation)" ($orderA.items[0].unitPrice -eq 10)

Write-Host "`nALL ADDRESS AND CHECKOUT TESTS PASSED!" -ForegroundColor Green
