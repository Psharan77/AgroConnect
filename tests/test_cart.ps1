$ErrorActionPreference = "Stop"

# Create a fresh product (let's use Farmer A)
$loginF = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"farmerA2@test.com","password":"password123"}'
$tokenF = $loginF.accessToken
$prod = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenF"} -ContentType "application/json" -Body '{"name":"Fresh Tomatoes","categoryId":1,"price":10,"quantity":10}'
$prodId = $prod.id
echo "Created product $prodId with stock 10"

# Register Customer A and Customer B
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Cart Cust A","email":"carta@test.com","password":"password123","role":"CUSTOMER"}' } catch { }
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Cart Cust B","email":"cartb@test.com","password":"password123","role":"CUSTOMER"}' } catch { }

$loginA = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"carta@test.com","password":"password123"}'
$tokenA = $loginA.accessToken

$loginB = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"cartb@test.com","password":"password123"}'
$tokenB = $loginB.accessToken

# Clear cart for A to be sure
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/clear" -Method Delete -Headers @{Authorization="Bearer $tokenA"}

# TEST 1
$cart1 = Invoke-RestMethod -Uri "http://localhost:8080/api/cart" -Method Get -Headers @{Authorization="Bearer $tokenA"}
echo "Test 1 (Initial Cart Size): $($cart1.items.Length)"

# TEST 2
$cart2 = Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"productId`": $prodId, `"quantity`": 2}"
echo "Test 2 (Add Product): Success, Cart Size = $($cart2.items.Length), Qty = $($cart2.items[0].quantity)"

# TEST 3
$cart3 = Invoke-RestMethod -Uri "http://localhost:8080/api/cart" -Method Get -Headers @{Authorization="Bearer $tokenA"}
echo "Test 3 (GET Cart): Size = $($cart3.items.Length), Qty = $($cart3.items[0].quantity), Name = $($cart3.items[0].productName)"

# TEST 4
$itemId = $cart3.items[0].id
$cart4 = Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items/$itemId" -Method Put -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"quantity`": 3}"
echo "Test 4 (Increase Qty): Success, Qty = $($cart4.items[0].quantity)"

# TEST 5
$cart5 = Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items/$itemId" -Method Put -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"quantity`": 1}"
echo "Test 5 (Decrease Qty): Success, Qty = $($cart5.items[0].quantity)"

# TEST 6
$cart6 = Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items/$itemId" -Method Delete -Headers @{Authorization="Bearer $tokenA"}
echo "Test 6 (Remove Product): Success, Size = $($cart6.items.Length)"

# TEST 7
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"productId`": $prodId, `"quantity`": 15}"
    echo "Test 7: FAILED, allowed > stock"
} catch {
    echo "Test 7 (Add > Stock): Caught error as expected"
}

# Re-add item for test 8
$cartReadd = Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"productId`": $prodId, `"quantity`": 1}"
$itemIdB = $cartReadd.items[0].id

# TEST 8: Customer B trying to modify Customer A's item
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items/$itemIdB" -Method Put -Headers @{Authorization="Bearer $tokenB"} -ContentType "application/json" -Body "{`"quantity`": 2}"
    echo "Test 8: FAILED, allowed Customer B to access A's cart"
} catch {
    echo "Test 8 (Access other cart): Caught error as expected"
}

# TEST 9
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/cart" -Method Get
    echo "Test 9: FAILED, allowed unauthenticated"
} catch {
    echo "Test 9 (Unauthenticated): Caught error as expected"
}

# TEST 10
$publicProds = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Get
echo "Test 10 (Public Listing): Returning $($publicProds.Length) products"
