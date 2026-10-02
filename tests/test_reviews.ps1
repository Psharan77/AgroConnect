$ErrorActionPreference = "Stop"

function Test-Check {
    param($Name, $Condition)
    if ($Condition) { Write-Host "[PASS] $Name" -ForegroundColor Green }
    else { Write-Host "[FAIL] $Name" -ForegroundColor Red; exit 1 }
}

# 1. Setup Data
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Farmer Rev","email":"frev@test.com","password":"password123","role":"FARMER"}' } catch {}
$tokenF = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"frev@test.com","password":"password123"}').accessToken

$prod1 = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenF"} -ContentType "application/json" -Body '{"name":"Review Product A","categoryId":1,"price":10,"quantity":100}'
$prod2 = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenF"} -ContentType "application/json" -Body '{"name":"Review Product B","categoryId":1,"price":20,"quantity":100}'

try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Cust Rev A","email":"creva@test.com","password":"password123","role":"CUSTOMER"}' } catch {}
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Cust Rev B","email":"crevb@test.com","password":"password123","role":"CUSTOMER"}' } catch {}
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Cust Rev C","email":"crevc@test.com","password":"password123","role":"CUSTOMER"}' } catch {}

$tokenA = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"creva@test.com","password":"password123"}').accessToken
$tokenB = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"crevb@test.com","password":"password123"}').accessToken
$tokenC = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"crevc@test.com","password":"password123"}').accessToken

# Create Addresses
$addrA = Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"fullName":"J Doe","phoneNumber":"1234567890","street":"1","city":"C","state":"S","zipCode":"0"}'
$addrC = Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Post -Headers @{Authorization="Bearer $tokenC"} -ContentType "application/json" -Body '{"fullName":"J Doe","phoneNumber":"1234567890","street":"1","city":"C","state":"S","zipCode":"0"}'

# Customer A buys Prod 1 (DELIVERED) and Prod 2 (PLACED)
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"productId`": $($prod1.id), `"quantity`": 1}"
$orderA1 = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"addressId`": $($addrA.id)}"
Invoke-RestMethod -Uri "http://localhost:8080/api/orders/$($orderA1.id)/status" -Method Put -Headers @{Authorization="Bearer $tokenF"} -ContentType "application/json" -Body '{"status":"DELIVERED"}'

Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"productId`": $($prod2.id), `"quantity`": 1}"
$orderA2 = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"addressId`": $($addrA.id)}"

# TEST 1: Customer A reviews Prod 1 (DELIVERED)
$revA = Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prod1.id)/reviews" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"rating":5, "comment":"Great!"}'
Test-Check "TEST 1 (Review DELIVERED item)" ($revA.rating -eq 5)

# TEST 2: Customer B attempts to review Prod 1 (Never Purchased)
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prod1.id)/reviews" -Method Post -Headers @{Authorization="Bearer $tokenB"} -ContentType "application/json" -Body '{"rating":4}'
    Test-Check "TEST 2 (Unpurchased Review)" $false
} catch {
    Test-Check "TEST 2 (Unpurchased Review BLOCKED)" $true
}

# TEST 3: Customer A attempts to review Prod 2 (PLACED)
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prod2.id)/reviews" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"rating":4}'
    Test-Check "TEST 3 (PLACED Review)" $false
} catch {
    Test-Check "TEST 3 (PLACED Review BLOCKED)" $true
}

# TEST 4: Customer A edits their review
$revA = Invoke-RestMethod -Uri "http://localhost:8080/api/reviews/$($revA.id)" -Method Put -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"rating":4, "comment":"Edited"}'
Test-Check "TEST 4 (Edit Own Review)" ($revA.rating -eq 4 -and $revA.comment -eq "Edited")

# TEST 5: Customer B attempts to edit Customer A's review
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/reviews/$($revA.id)" -Method Put -Headers @{Authorization="Bearer $tokenB"} -ContentType "application/json" -Body '{"rating":1}'
    Test-Check "TEST 5 (Edit Other Review)" $false
} catch {
    Test-Check "TEST 5 (Edit Other Review BLOCKED)" $true
}

# TEST 7: Customer B attempts to delete Customer A's review
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/reviews/$($revA.id)" -Method Delete -Headers @{Authorization="Bearer $tokenB"}
    Test-Check "TEST 7 (Delete Other Review)" $false
} catch {
    Test-Check "TEST 7 (Delete Other Review BLOCKED)" $true
}

# TEST 8 & 9: Rating Bounds Validation
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/reviews/$($revA.id)" -Method Put -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"rating":0}'
    Test-Check "TEST 8 (Rating 0)" $false
} catch { Test-Check "TEST 8 (Rating 0 BLOCKED)" $true }

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/reviews/$($revA.id)" -Method Put -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"rating":6}'
    Test-Check "TEST 9 (Rating 6)" $false
} catch { Test-Check "TEST 9 (Rating 6 BLOCKED)" $true }

# TEST 10: Unauthenticated POST
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prod1.id)/reviews" -Method Post -ContentType "application/json" -Body '{"rating":5}'
    Test-Check "TEST 10 (Unauth POST)" $false
} catch { Test-Check "TEST 10 (Unauth POST BLOCKED)" $true }

# TEST 11: Public GET Reviews
$publicRevs = Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prod1.id)/reviews" -Method Get
Test-Check "TEST 11 (Public GET Reviews)" ($publicRevs.totalReviews -gt 0)

# TEST 13: Duplicate Review
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prod1.id)/reviews" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"rating":5}'
    Test-Check "TEST 13 (Duplicate Review)" $false
} catch { Test-Check "TEST 13 (Duplicate Review BLOCKED)" $true }

# TEST 12: Average Rating and Count
# Let Customer C buy and review Prod 1
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenC"} -ContentType "application/json" -Body "{`"productId`": $($prod1.id), `"quantity`": 1}"
$orderC1 = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenC"} -ContentType "application/json" -Body "{`"addressId`": $($addrC.id)}"
Invoke-RestMethod -Uri "http://localhost:8080/api/orders/$($orderC1.id)/status" -Method Put -Headers @{Authorization="Bearer $tokenF"} -ContentType "application/json" -Body '{"status":"DELIVERED"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prod1.id)/reviews" -Method Post -Headers @{Authorization="Bearer $tokenC"} -ContentType "application/json" -Body '{"rating":5}'

# Fetch Product to check DB calculated averages
$prodStats = Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($prod1.id)" -Method Get
# Cust A left 4, Cust C left 5. Average = 4.5. Count = 2.
if ($prodStats.averageRating -eq 4.5 -and $prodStats.reviewCount -eq 2) {
    Test-Check "TEST 12 (Avg Rating/Count)" $true
} else {
    Write-Host "Expected 4.5 and 2, got $($prodStats.averageRating) and $($prodStats.reviewCount)"
    Test-Check "TEST 12 (Avg Rating/Count)" $false
}

# TEST 6: Customer A deletes their review
Invoke-RestMethod -Uri "http://localhost:8080/api/reviews/$($revA.id)" -Method Delete -Headers @{Authorization="Bearer $tokenA"}
Test-Check "TEST 6 (Delete Own Review)" $true

Write-Host "`nALL 13 TESTS PASSED SUCCESSFULLY!" -ForegroundColor Green
