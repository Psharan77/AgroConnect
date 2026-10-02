import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';

const CustomerOrders = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        api.get('/orders/my-orders')
            .then(res => {
                setOrders(res.data);
                setLoading(false);
            })
            .catch(err => {
                setError('Failed to fetch orders.');
                setLoading(false);
            });
    }, []);

    if (loading) return <div className="text-center mt-5"><div className="spinner-border text-success"></div></div>;

    if (error) return <div className="alert alert-danger mt-4 container">{error}</div>;

    return (
        <div className="container mt-4">
            <h2 className="mb-4">My Orders</h2>
            
            {orders.length === 0 ? (
                <div className="text-center mt-5">
                    <p className="text-muted">You have no previous orders.</p>
                    <Link to="/products" className="btn btn-primary">Start Shopping</Link>
                </div>
            ) : (
                <div className="table-responsive">
                    <table className="table table-hover align-middle">
                        <thead className="table-light">
                            <tr>
                                <th>Order ID</th>
                                <th>Date</th>
                                <th>Total</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {orders.sort((a, b) => b.id - a.id).map(order => (
                                <tr key={order.id}>
                                    <td>#{order.id}</td>
                                    <td>{new Date(order.orderDate).toLocaleDateString()}</td>
                                    <td>${order.totalAmount.toFixed(2)}</td>
                                    <td>
                                        <span className={`badge ${order.status === 'PLACED' ? 'bg-primary' : order.status === 'CANCELLED' ? 'bg-danger' : 'bg-success'}`}>
                                            {order.status}
                                        </span>
                                    </td>
                                    <td>
                                        <Link to={`/customer/orders/${order.id}`} className="btn btn-sm btn-outline-success">View Details</Link>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default CustomerOrders;
