import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';
import { CartContext } from '../../contexts/CartContext';

const Navbar = () => {
    const { user, logout } = useContext(AuthContext);
    const { cart } = useContext(CartContext);
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="navbar navbar-expand-lg navbar-dark" style={{ backgroundColor: 'var(--agro-green)' }}>
            <div className="container">
                <Link className="navbar-brand fw-bold" to="/">AgroConnect</Link>
                <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                    <span className="navbar-toggler-icon"></span>
                </button>
                <div className="collapse navbar-collapse" id="navbarNav">
                    <ul className="navbar-nav me-auto">
                        <li className="nav-item">
                            <Link className="nav-link" to="/products">Marketplace</Link>
                        </li>
                        
                        {/* Dynamic Navigation based on Role */}
                        {user?.role === 'CUSTOMER' && (
                            <>
                                <li className="nav-item"><Link className="nav-link" to="/customer/dashboard">My Dashboard</Link></li>
                                <li className="nav-item"><Link className="nav-link" to="/customer/orders">My Orders</Link></li>
                                <li className="nav-item">
                                    <Link className="nav-link" to="/cart">
                                        Cart {cart?.items?.length > 0 && <span className="badge bg-warning text-dark ms-1">{cart.items.length}</span>}
                                    </Link>
                                </li>
                            </>
                        )}
                        {user?.role === 'FARMER' && (
                            <>
                                <li className="nav-item"><Link className="nav-link" to="/farmer/dashboard">Farmer Dashboard</Link></li>
                                <li className="nav-item"><Link className="nav-link" to="/farmer/products">My Products</Link></li>
                            </>
                        )}
                        {user?.role === 'ADMIN' && (
                            <>
                                <li className="nav-item"><Link className="nav-link" to="/admin/dashboard">Admin Panel</Link></li>
                            </>
                        )}
                    </ul>
                    
                    <ul className="navbar-nav">
                        {!user ? (
                            <>
                                <li className="nav-item"><Link className="nav-link" to="/login">Login</Link></li>
                                <li className="nav-item"><Link className="nav-link" to="/register">Register</Link></li>
                            </>
                        ) : (
                            <li className="nav-item">
                                <button className="btn btn-outline-light ms-2" onClick={handleLogout}>Logout</button>
                            </li>
                        )}
                    </ul>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
