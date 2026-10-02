# Create Farmer A
$regA = try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Farmer A2","email":"farmerA2@test.com","password":"password123","role":"FARMER"}' } catch { }
$loginA = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"farmerA2@test.com","password":"password123"}'
$tokenA = $loginA.accessToken

# Create Farmer B
$regB = try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Farmer B2","email":"farmerB2@test.com","password":"password123","role":"FARMER"}' } catch { }
$loginB = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"farmerB2@test.com","password":"password123"}'
$tokenB = $loginB.accessToken

# Create Customer
$regC = try { Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"fullName":"Customer C2","email":"custC2@test.com","password":"password123","role":"CUSTOMER"}' } catch { }
$loginC = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"custC2@test.com","password":"password123"}'
$tokenC = $loginC.accessToken

# Test 1: Farmer A creates a product
$newProd = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"name":"Apples","categoryId":1,"price":10,"quantity":100}'
echo "Test 1: Created product $($newProd.id)"

# Test 2: Farmer A gets my-products
$myProds = Invoke-RestMethod -Uri "http://localhost:8080/api/products/my-products" -Method Get -Headers @{Authorization="Bearer $tokenA"}
echo "Test 2: Farmer A got $($myProds.Length) products"

# Test 3: Farmer A updates own product
$updatedProd = Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($newProd.id)" -Method Put -Headers @{Authorization="Bearer $tokenA"} -ContentType "application/json" -Body '{"name":"Green Apples","categoryId":1,"price":12,"quantity":90}'
echo "Test 3: Updated product $($updatedProd.name)"

# Test 5: Customer tries to create product
try { Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Headers @{Authorization="Bearer $tokenC"} -ContentType "application/json" -Body '{"name":"Bad Apples","categoryId":1,"price":10,"quantity":100}' } catch { echo "Test 5: Customer POST caught: $_" }

# Test 6: Farmer B attempts to update Farmer A's product
try { Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($newProd.id)" -Method Put -Headers @{Authorization="Bearer $tokenB"} -ContentType "application/json" -Body '{"name":"Stolen Apples","categoryId":1,"price":1,"quantity":1}' } catch { echo "Test 6: Farmer B PUT caught: $_" }

# Test 7: Farmer B attempts to delete Farmer A's product
try { Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($newProd.id)" -Method Delete -Headers @{Authorization="Bearer $tokenB"} } catch { echo "Test 7: Farmer B DELETE caught: $_" }

# Test 8: Unauthenticated access
try { Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($newProd.id)" -Method Put -ContentType "application/json" -Body '{"name":"Hacked Apples"}' } catch { echo "Test 8: Unauth PUT caught: $_" }

# Test 9: Farmer B my-products should not have Farmer A's product
$myProdsB = Invoke-RestMethod -Uri "http://localhost:8080/api/products/my-products" -Method Get -Headers @{Authorization="Bearer $tokenB"}
echo "Test 9: Farmer B has $($myProdsB.Length) products (Expected 0)"

# Test 10: Public GET products
$allProds = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Get
echo "Test 10: Public GET returned $($allProds.Length) products"

# Test 4: Farmer A deletes own product
Invoke-RestMethod -Uri "http://localhost:8080/api/products/$($newProd.id)" -Method Delete -Headers @{Authorization="Bearer $tokenA"}
echo "Test 4: Deleted product successfully"
