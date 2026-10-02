import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { CartProvider } from './contexts/CartContext';
import 'bootstrap/dist/css/bootstrap.min.css';

import Navbar from './components/common/Navbar';
import Login from './pages/public/Login';
import Register from './pages/public/Register';
import ProtectedRoute from './components/common/ProtectedRoute';
import ProductList from './pages/public/ProductList';
import ProductDetails from './pages/public/ProductDetails';
import FarmerDashboard from './pages/farmer/FarmerDashboard';
import FarmerProducts from './pages/farmer/FarmerProducts';
import Cart from './pages/customer/Cart';
import Checkout from './pages/customer/Checkout';
import CustomerOrders from './pages/customer/CustomerOrders';
import OrderDetails from './pages/customer/OrderDetails';
import AdminDashboard from './pages/admin/AdminDashboard';

// Placeholder dashboards to demonstrate routing success
const CustomerDashboard = () => <div className="text-center mt-5"><h2>Customer Dashboard</h2><p>Welcome to the marketplace!</p></div>;
const Unauthorized = () => <div className="text-center mt-5"><h2 className="text-danger">403 - Unauthorized</h2><p>You don't have permission to view this page.</p></div>;

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Router>
          <Navbar />
          <div className="container mt-4">
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<Navigate to="/products" />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/products" element={<ProductList />} />
            <Route path="/products/:id" element={<ProductDetails />} />
            <Route path="/unauthorized" element={<Unauthorized />} />
            
            {/* Protected Routes: CUSTOMER ONLY */}
            <Route element={<ProtectedRoute allowedRoles={['CUSTOMER']} />}>
                <Route path="/customer/dashboard" element={<CustomerDashboard />} />
                <Route path="/cart" element={<Cart />} />
                <Route path="/checkout" element={<Checkout />} />
                <Route path="/customer/orders" element={<CustomerOrders />} />
                <Route path="/customer/orders/:id" element={<OrderDetails />} />
            </Route>

            {/* Protected Routes: FARMER ONLY */}
            <Route element={<ProtectedRoute allowedRoles={['FARMER']} />}>
                <Route path="/farmer/dashboard" element={<FarmerDashboard />} />
                <Route path="/farmer/products" element={<FarmerProducts />} />
            </Route>

            {/* Protected Routes: ADMIN ONLY */}
            <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                <Route path="/admin/dashboard" element={<AdminDashboard />} />
            </Route>
            
          </Routes>
        </div>
      </Router>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;
