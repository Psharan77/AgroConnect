import React, { useContext, useState } from 'react';
import { Link } from 'react-router-dom';
import { CartContext } from '../../contexts/CartContext';

const Cart = () => {
    const { cart, cartLoading, updateQuantity, removeItem, clearCart } = useContext(CartContext);
    const [actionLoading, setActionLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    if (cartLoading) {
        return <div className="text-center mt-5"><div className="spinner-border text-success"></div></div>;
    }

    if (!cart || !cart.items || cart.items.length === 0) {
        return (
            <div className="container mt-5 text-center">
                <h2>Your Cart is Empty</h2>
                <p className="text-muted">Looks like you haven't added anything to your cart yet.</p>
                <Link to="/products" className="btn btn-success mt-3">Browse Marketplace</Link>
            </div>
        );
    }

    const handleUpdateQty = async (itemId, currentQty, change) => {
        const newQty = currentQty + change;
        if (newQty < 1) return;
        
        setActionLoading(true);
        setErrorMsg('');
        try {
            await updateQuantity(itemId, newQty);
        } catch (err) {
            setErrorMsg(err.response?.data?.message || 'Failed to update quantity.');
        } finally {
            setActionLoading(false);
        }
    };

    const handleRemove = async (itemId) => {
        setActionLoading(true);
        setErrorMsg('');
        try {
            await removeItem(itemId);
        } catch (err) {
            setErrorMsg('Failed to remove item.');
        } finally {
            setActionLoading(false);
        }
    };

    const handleClear = async () => {
        if (window.confirm('Are you sure you want to clear your entire cart?')) {
            setActionLoading(true);
            try {
                await clearCart();
            } catch (err) {
                setErrorMsg('Failed to clear cart.');
            } finally {
                setActionLoading(false);
            }
        }
    };

    return (
        <div className="container mt-4">
            <h2 className="mb-4">Shopping Cart</h2>
            
            {errorMsg && <div className="alert alert-danger">{errorMsg}</div>}
            
            <div className="row">
                <div className="col-lg-8">
                    <div className="card shadow-sm mb-4">
                        <div className="card-body p-0">
                            <ul className="list-group list-group-flush">
                                {cart.items.map(item => (
                                    <li key={item.id} className="list-group-item p-3 d-flex justify-content-between align-items-center">
                                        <div className="d-flex align-items-center">
                                            <div>
                                                <h5 className="mb-1">
                                                    <Link to={`/products/${item.productId}`} className="text-decoration-none text-dark">{item.productName}</Link>
                                                </h5>
                                                <p className="text-muted mb-0">${item.productPrice.toFixed(2)} each</p>
                                            </div>
                                        </div>
                                        
                                        <div className="d-flex align-items-center">
                                            <div className="input-group input-group-sm me-3" style={{ width: '100px' }}>
                                                <button className="btn btn-outline-secondary" onClick={() => handleUpdateQty(item.id, item.quantity, -1)} disabled={item.quantity <= 1 || actionLoading}>-</button>
                                                <input type="text" className="form-control text-center" value={item.quantity} readOnly />
                                                <button className="btn btn-outline-secondary" onClick={() => handleUpdateQty(item.id, item.quantity, 1)} disabled={actionLoading}>+</button>
                                            </div>
                                            
                                            <div className="text-end me-4" style={{ width: '80px' }}>
                                                <strong className="d-block">${item.subTotal.toFixed(2)}</strong>
                                            </div>
                                            
                                            <button className="btn btn-sm btn-outline-danger" onClick={() => handleRemove(item.id)} disabled={actionLoading}>
                                                Remove
                                            </button>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </div>
                    <button className="btn btn-outline-danger btn-sm mb-4" onClick={handleClear} disabled={actionLoading}>
                        Clear Cart
                    </button>
                </div>
                
                <div className="col-lg-4">
                    <div className="card shadow-sm border-success">
                        <div className="card-header bg-success text-white">
                            <h5 className="mb-0">Order Summary</h5>
                        </div>
                        <div className="card-body">
                            <div className="d-flex justify-content-between mb-3">
                                <span>Subtotal</span>
                                <span>${cart.totalPrice.toFixed(2)}</span>
                            </div>
                            <div className="d-flex justify-content-between mb-3">
                                <span>Delivery Fee</span>
                                <span className="text-muted">Calculated at checkout</span>
                            </div>
                            <hr />
                            <div className="d-flex justify-content-between mb-4">
                                <strong className="fs-5">Total</strong>
                                <strong className="fs-5 text-success">${cart.totalPrice.toFixed(2)}</strong>
                            </div>
                            <Link to="/checkout" className="btn btn-success w-100">
                                Proceed to Checkout
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Cart;
