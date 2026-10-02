import React, { useContext, useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { CartContext } from '../../contexts/CartContext';
import api from '../../services/api';

const Checkout = () => {
    const { cart, cartLoading, fetchCart } = useContext(CartContext);
    const navigate = useNavigate();
    
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [addresses, setAddresses] = useState([]);
    const [selectedAddressId, setSelectedAddressId] = useState('');
    
    const [showAddressForm, setShowAddressForm] = useState(false);
    const [newAddress, setNewAddress] = useState({
        fullName: '',
        phoneNumber: '',
        street: '',
        city: '',
        state: '',
        zipCode: ''
    });
    const [addressError, setAddressError] = useState('');

    useEffect(() => {
        if (!cartLoading && (!cart || !cart.items || cart.items.length === 0)) {
            navigate('/cart');
        }
        
        const fetchAddresses = async () => {
            try {
                const res = await api.get('/addresses');
                setAddresses(res.data);
                if (res.data.length > 0) {
                    setSelectedAddressId(res.data[0].id);
                } else {
                    setShowAddressForm(true);
                }
            } catch (err) {
                console.error("Failed to load addresses", err);
                setError("Failed to load your addresses.");
            }
        };
        fetchAddresses();
    }, [cart, cartLoading, navigate]);

    const handleAddAddress = async (e) => {
        e.preventDefault();
        setAddressError('');
        
        // Basic frontend validation
        if (!newAddress.fullName || !newAddress.phoneNumber || !newAddress.street || !newAddress.city || !newAddress.state || !newAddress.zipCode) {
            setAddressError('All fields are required.');
            return;
        }
        if (!/^[0-9]{10}$/.test(newAddress.phoneNumber)) {
            setAddressError('Phone number must be exactly 10 digits.');
            return;
        }

        try {
            const res = await api.post('/addresses', newAddress);
            setAddresses([...addresses, res.data]);
            setSelectedAddressId(res.data.id);
            setShowAddressForm(false);
            setNewAddress({ fullName: '', phoneNumber: '', street: '', city: '', state: '', zipCode: '' });
        } catch (err) {
            if (err.response?.data?.errors) {
                const msgs = Object.values(err.response.data.errors).join(", ");
                setAddressError(msgs);
            } else {
                setAddressError(err.response?.data?.message || 'Failed to save address.');
            }
        }
    };

    const handleCheckout = async () => {
        if (!selectedAddressId) {
            setError('Please select a delivery address.');
            return;
        }
        
        setLoading(true);
        setError('');
        try {
            const res = await api.post('/orders', { addressId: selectedAddressId });
            await fetchCart(); // Cart should be empty now
            navigate(`/customer/orders/${res.data.id}`, { state: { justPlaced: true } });
        } catch (err) {
            setError(err.response?.data?.message || 'Checkout failed. Please try again.');
            setLoading(false);
        }
    };

    if (cartLoading || !cart) return <div className="text-center mt-5"><div className="spinner-border text-success"></div><p className="mt-2 text-muted">Loading Checkout...</p></div>;

    if (!cart.items || cart.items.length === 0) {
        return (
            <div className="container mt-5 text-center">
                <h3>Your cart is empty</h3>
                <Link to="/products" className="btn btn-success mt-3">Continue Shopping</Link>
            </div>
        );
    }

    return (
        <div className="container mt-4 mb-5">
            <h2 className="mb-4">Checkout</h2>
            
            {error && <div className="alert alert-danger">{error}</div>}
            
            <div className="row g-4">
                <div className="col-lg-8">
                    {/* Delivery Address Section */}
                    <div className="card shadow-sm mb-4 border-success">
                        <div className="card-header bg-success text-white fw-bold">Delivery Address</div>
                        <div className="card-body">
                            {addresses.length > 0 && !showAddressForm && (
                                <div className="mb-4">
                                    <h6 className="mb-3 text-muted fw-bold">Select Delivery Address</h6>
                                    <div className="row g-3">
                                        {addresses.map(addr => (
                                            <div className="col-md-6" key={addr.id}>
                                                <div 
                                                    className={`card h-100 ${selectedAddressId === addr.id ? 'border-success bg-light' : ''}`} 
                                                    style={{ cursor: 'pointer', borderWidth: selectedAddressId === addr.id ? '2px' : '1px' }}
                                                    onClick={() => setSelectedAddressId(addr.id)}
                                                >
                                                    <div className="card-body">
                                                        <div className="form-check">
                                                            <input 
                                                                className="form-check-input" 
                                                                type="radio" 
                                                                name="address" 
                                                                checked={selectedAddressId === addr.id}
                                                                onChange={() => setSelectedAddressId(addr.id)}
                                                            />
                                                            <label className="form-check-label fw-bold d-block mb-1">
                                                                {addr.fullName}
                                                            </label>
                                                        </div>
                                                        <div className="ms-4 text-muted small">
                                                            <div>{addr.street}</div>
                                                            <div>{addr.city}, {addr.state} - {addr.zipCode}</div>
                                                            <div className="mt-1"><i className="bi bi-telephone-fill me-1"></i> {addr.phoneNumber}</div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                    <button 
                                        className="btn btn-outline-success mt-3" 
                                        onClick={() => setShowAddressForm(true)}
                                    >
                                        + Add New Address
                                    </button>
                                </div>
                            )}

                            {(showAddressForm || addresses.length === 0) && (
                                <div className={addresses.length > 0 ? "border-top pt-4" : ""}>
                                    <div className="d-flex justify-content-between align-items-center mb-3">
                                        <h6 className="fw-bold m-0">{addresses.length === 0 ? "Add Delivery Address" : "New Address"}</h6>
                                        {addresses.length > 0 && (
                                            <button className="btn btn-sm btn-close" onClick={() => setShowAddressForm(false)}></button>
                                        )}
                                    </div>
                                    
                                    {addressError && <div className="alert alert-danger py-2">{addressError}</div>}
                                    
                                    <form onSubmit={handleAddAddress}>
                                        <div className="row g-3">
                                            <div className="col-md-6">
                                                <label className="form-label text-muted small mb-1 fw-bold">Full Name</label>
                                                <input type="text" className="form-control" value={newAddress.fullName} onChange={e => setNewAddress({...newAddress, fullName: e.target.value})} required />
                                            </div>
                                            <div className="col-md-6">
                                                <label className="form-label text-muted small mb-1 fw-bold">Phone Number (10 digits)</label>
                                                <input type="text" className="form-control" value={newAddress.phoneNumber} onChange={e => setNewAddress({...newAddress, phoneNumber: e.target.value})} required pattern="[0-9]{10}" />
                                            </div>
                                            <div className="col-12">
                                                <label className="form-label text-muted small mb-1 fw-bold">Address Line (Street, House No, Area)</label>
                                                <input type="text" className="form-control" value={newAddress.street} onChange={e => setNewAddress({...newAddress, street: e.target.value})} required />
                                            </div>
                                            <div className="col-md-4">
                                                <label className="form-label text-muted small mb-1 fw-bold">City</label>
                                                <input type="text" className="form-control" value={newAddress.city} onChange={e => setNewAddress({...newAddress, city: e.target.value})} required />
                                            </div>
                                            <div className="col-md-4">
                                                <label className="form-label text-muted small mb-1 fw-bold">State</label>
                                                <input type="text" className="form-control" value={newAddress.state} onChange={e => setNewAddress({...newAddress, state: e.target.value})} required />
                                            </div>
                                            <div className="col-md-4">
                                                <label className="form-label text-muted small mb-1 fw-bold">Pincode</label>
                                                <input type="text" className="form-control" value={newAddress.zipCode} onChange={e => setNewAddress({...newAddress, zipCode: e.target.value})} required />
                                            </div>
                                            <div className="col-12 mt-4">
                                                <button type="submit" className="btn btn-success w-100">Save & Use This Address</button>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            )}
                        </div>
                    </div>
                    
                    {/* Order Items Section */}
                    <div className="card shadow-sm">
                        <div className="card-header bg-white fw-bold">Order Items Review</div>
                        <div className="card-body p-0">
                            <ul className="list-group list-group-flush">
                                {cart.items.map(item => (
                                    <li key={item.id} className="list-group-item d-flex justify-content-between align-items-center py-3">
                                        <div className="d-flex align-items-center">
                                            <div className="bg-light rounded p-2 me-3 text-center" style={{ width: '50px', height: '50px' }}>
                                                <i className="bi bi-box-seam text-success fs-4"></i>
                                            </div>
                                            <div>
                                                <h6 className="mb-0 fw-bold">{item.productName}</h6>
                                                <small className="text-muted">Quantity: {item.quantity} × ${item.productPrice.toFixed(2)}</small>
                                            </div>
                                        </div>
                                        <span className="fw-bold">${item.subTotal.toFixed(2)}</span>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </div>
                </div>
                
                <div className="col-lg-4">
                    {/* Order Summary Panel */}
                    <div className="card shadow-sm border-0 bg-light">
                        <div className="card-body p-4">
                            <h5 className="card-title mb-4 fw-bold text-uppercase border-bottom pb-2">Order Summary</h5>
                            
                            <div className="d-flex justify-content-between mb-3 text-muted">
                                <span>Items ({cart.items.length})</span>
                                <span>${cart.totalPrice.toFixed(2)}</span>
                            </div>
                            <div className="d-flex justify-content-between mb-3 text-muted">
                                <span>Delivery Fee</span>
                                <span className="text-success fw-bold">FREE</span>
                            </div>
                            
                            <hr className="my-4" />
                            
                            <div className="d-flex justify-content-between mb-4">
                                <strong className="fs-4">Total</strong>
                                <strong className="fs-4 text-success">${cart.totalPrice.toFixed(2)}</strong>
                            </div>
                            
                            <button 
                                className="btn btn-success w-100 btn-lg shadow-sm" 
                                onClick={handleCheckout} 
                                disabled={loading || !selectedAddressId || showAddressForm}
                            >
                                {loading ? (
                                    <><span className="spinner-border spinner-border-sm me-2"></span> Processing...</>
                                ) : (
                                    "Place Order"
                                )}
                            </button>
                            
                            <div className="text-center mt-3">
                                <Link to="/cart" className="text-decoration-none text-muted small">
                                    <i className="bi bi-arrow-left me-1"></i> Return to Cart
                                </Link>
                            </div>
                            
                            <div className="mt-4 pt-3 border-top text-center text-muted small">
                                <i className="bi bi-shield-check text-success fs-4 d-block mb-1"></i>
                                Secure checkout by AgroConnect
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Checkout;
