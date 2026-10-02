import React, { useState, useEffect } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import api from '../../services/api';

const OrderDetails = () => {
    const { id } = useParams();
    const location = useLocation();
    const justPlaced = location.state?.justPlaced || false;
    
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        api.get(`/orders/${id}`)
            .then(res => {
                setOrder(res.data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.response?.data?.message || 'Failed to load order details.');
                setLoading(false);
            });
    }, [id]);

    if (loading) return <div className="text-center mt-5"><div className="spinner-border text-success"></div></div>;

    if (error) return <div className="alert alert-danger mt-4 container">{error}</div>;
    
    if (!order) return null;

    return (
        <div className="container mt-4 mb-5">
            {justPlaced && (
                <div className="alert alert-success d-flex align-items-center mb-4" role="alert">
                    <i className="bi bi-check-circle-fill fs-4 me-3"></i>
                    <div>
                        <h4 className="alert-heading mb-1">Order successfully placed!</h4>
                        <p className="mb-0">Thank you for your purchase. Your order ID is #{order.id}.</p>
                    </div>
                </div>
            )}
            
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2>Order #{order.id} Details</h2>
                <Link to="/customer/orders" className="btn btn-outline-secondary">Back to My Orders</Link>
            </div>
            
            <div className="row">
                <div className="col-lg-8">
                    <div className="card shadow-sm mb-4">
                        <div className="card-header bg-white d-flex justify-content-between align-items-center">
                            <span className="fw-bold">Items Purchased</span>
                            <span className={`badge ${order.status === 'PLACED' ? 'bg-primary' : order.status === 'CANCELLED' ? 'bg-danger' : 'bg-success'}`}>
                                STATUS: {order.status}
                            </span>
                        </div>
                        <div className="card-body p-0">
                            <div className="table-responsive">
                                <table className="table table-borderless mb-0">
                                    <thead className="border-bottom">
                                        <tr>
                                            <th>Product</th>
                                            <th className="text-center">Price</th>
                                            <th className="text-center">Qty</th>
                                            <th className="text-end">Subtotal</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {order.items.map(item => (
                                            <tr key={item.id} className="border-bottom">
                                                <td>
                                                    <Link to={`/products/${item.productId}`} className="text-decoration-none fw-bold">
                                                        {item.productName}
                                                    </Link>
                                                </td>
                                                <td className="text-center">${item.unitPrice.toFixed(2)}</td>
                                                <td className="text-center">{item.quantity}</td>
                                                <td className="text-end fw-bold">${(item.unitPrice * item.quantity).toFixed(2)}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div className="col-lg-4">
                    <div className="card shadow-sm mb-4">
                        <div className="card-header bg-white fw-bold">Order Summary</div>
                        <div className="card-body">
                            <div className="d-flex justify-content-between mb-2">
                                <span className="text-muted">Order Date</span>
                                <span>{new Date(order.orderDate).toLocaleDateString()}</span>
                            </div>
                            <div className="d-flex justify-content-between mb-2">
                                <span className="text-muted">Payment Method</span>
                                <span>Cash on Delivery</span>
                            </div>
                            <hr />
                            <div className="d-flex justify-content-between">
                                <strong>Total Amount</strong>
                                <strong className="text-success fs-5">${order.totalAmount.toFixed(2)}</strong>
                            </div>
                        </div>
                    </div>
                    
                    {order.status === 'PLACED' && (
                        <button className="btn btn-outline-danger w-100" onClick={() => {
                            if (window.confirm('Are you sure you want to cancel this order?')) {
                                api.put(`/orders/${order.id}/cancel`).then(() => window.location.reload());
                            }
                        }}>
                            Cancel Order
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default OrderDetails;
