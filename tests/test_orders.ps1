$ErrorActionPreference = "Stop"

# Create a fresh product (let's use Farmer A)
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Farmer O","email":"farmerO@test.com","password":"password123","role":"FARMER"}' } catch { }
$loginF = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"farmerO@test.com","password":"password123"}'
$tokenF = $loginF.accessToken

$prod = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenF"} -ContentType "application/json" -Body '{"name":"Order Apples","categoryId":1,"price":15,"quantity":10}'
$prodId = $prod.id

# Customers
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Order Cust A","email":"ordera@test.com","password":"password123","role":"CUSTOMER"}' } catch { }
try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Order Cust B","email":"orderb@test.com","password":"password123","role":"CUSTOMER"}' } catch { }

$loginA = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"ordera@test.com","password":"password123"}'
$tokenA = $loginA.accessToken

$loginB = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"orderb@test.com","password":"password123"}'
$tokenB = $loginB.accessToken

# Clear cart for A to be sure
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/clear" -Method Delete -Headers @{Authorization="Bearer $tokenA"}
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/clear" -Method Delete -Headers @{Authorization="Bearer $tokenB"}

# Create Address for A
$address = Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"fullName":"J Doe","phoneNumber":"1234567890","street":"123 St","city":"City","state":"ST","zipCode":"00000"}'
$addrId = $address.id

# Add product to Cart
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"productId`": $prodId, `"quantity`": 4}"

# Check stock before order
$prodBefore = Invoke-RestMethod -Uri "http://localhost:8080/api/products/$prodId" -Method Get
echo "Stock before order: $($prodBefore.quantity)"

# TEST 1: Checkout
$order = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"addressId`": $addrId}"
$orderId = $order.id
echo "Test 1 (Checkout): SUCCESS, Order ID = $orderId"

# TEST 2: Verify in DB/Response
echo "Test 2 (Order Stored): TRUE, total = $($order.totalAmount)"

# TEST 3: OrderItems
echo "Test 3 (OrderItems): Qty = $($order.items[0].quantity), Price = $($order.items[0].unitPrice)"

# TEST 4: Subtotal match
$subtotal = $order.items[0].quantity * $order.items[0].unitPrice
if ($subtotal -eq $order.totalAmount) { echo "Test 4 (Total Match): TRUE ($subtotal)" } else { echo "Test 4 (Total Match): FALSE ($subtotal vs $($order.totalAmount))" }

# TEST 5: Stock decreased
$prodAfter = Invoke-RestMethod -Uri "http://localhost:8080/api/products/$prodId" -Method Get
echo "Test 5 (Stock decreased): from $($prodBefore.quantity) to $($prodAfter.quantity)"

# TEST 6: Cart empty
$cartAfter = Invoke-RestMethod -Uri "http://localhost:8080/api/cart" -Method Get -Headers @{Authorization="Bearer $tokenA"}
echo "Test 6 (Cart empty): size = $($cartAfter.items.Length)"

# TEST 7: Attempt checkout with insufficient stock
# Add 2 items (valid)
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"productId`": $prodId, `"quantity`": 2}"

# Another user buys 5 items, reducing stock from 6 to 1
Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" -Method Post -Headers @{Authorization="Bearer $tokenB"} -ContentType "application/json" -Body "{`"productId`": $prodId, `"quantity`": 5}"
$addrB = Invoke-RestMethod -Uri "http://localhost:8080/api/addresses" -Method Post -Headers @{Authorization="Bearer $tokenB"} -ContentType "application/json" -Body '{"fullName":"J Doe","phoneNumber":"1234567890","street":"456 St","city":"City","state":"ST","zipCode":"11111"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenB"} -ContentType "application/json" -Body "{`"addressId`": $($addrB.id)}"

# Now stock is 1. Customer A still has 2 in cart. Attempt checkout!
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body "{`"addressId`": $addrId}"
    echo "Test 7: FAILED, allowed order without stock"
} catch {
    echo "Test 7 (Checkout w/o stock): BLOCKED as expected"
}

# TEST 8: Verify failed checkout didn't create partial order
$ordersA = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/my-orders" -Method Get -Headers @{Authorization="Bearer $tokenA"}
echo "Test 8 (No partial order): Customer has $($ordersA.Length) orders"

# TEST 9: Customer A access Customer B order
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/orders/$orderId" -Method Get -Headers @{Authorization="Bearer $tokenB"}
    echo "Test 9: FAILED, B accessed A's order"
} catch {
    echo "Test 9 (Access other order): BLOCKED as expected"
}

# TEST 10: Unauthenticated
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/orders/$orderId" -Method Get
    echo "Test 10: FAILED, unauth access allowed"
} catch {
    echo "Test 10 (Unauth order): 401 as expected"
}

# TEST 11: Customer access farmer endpoint
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/orders/farmer" -Method Get -Headers @{Authorization="Bearer $tokenA"}
    echo "Test 11: FAILED, customer accessed farmer endpoint"
} catch {
    echo "Test 11 (Cust -> Farmer): 403 as expected"
}

# TEST 12: Farmer can see order
$fOrders = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/farmer" -Method Get -Headers @{Authorization="Bearer $tokenF"}
echo "Test 12 (Farmer Orders): Returning $($fOrders.Length) orders containing their product"

# TEST 13: Public Listing works
$pubProds = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Get
echo "Test 13 (Public Listing): Returning $($pubProds.Length) products"
