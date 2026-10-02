import React, { useState, useEffect } from 'react';
import api from '../../services/api';

const AdminDashboard = () => {
    const [stats, setStats] = useState(null);
    const [recentOrders, setRecentOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const [statsRes, ordersRes] = await Promise.all([
                    api.get('/admin/dashboard'),
                    api.get('/admin/orders')
                ]);
                
                setStats(statsRes.data);
                
                // Get 5 most recent orders based on orderDate
                const sortedOrders = ordersRes.data
                    .sort((a, b) => new Date(b.orderDate) - new Date(a.orderDate))
                    .slice(0, 5);
                setRecentOrders(sortedOrders);
                
                setLoading(false);
            } catch (err) {
                setError(err.response?.data?.message || 'Failed to load admin dashboard.');
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    if (loading) return <div className="text-center mt-5"><div className="spinner-border text-primary"></div><p className="mt-2 text-muted">Loading Admin Dashboard...</p></div>;
    if (error) return <div className="alert alert-danger mt-4 container">{error}</div>;
    if (!stats) return null;

    return (
        <div className="container mt-4 mb-5">
            <h2 className="mb-4 text-primary">Admin Dashboard</h2>

            {/* Platform Overview */}
            <h4 className="mb-3 text-secondary">Platform Overview</h4>
            <div className="row g-3 mb-5">
                <div className="col-sm-6 col-lg-3">
                    <div className="card shadow-sm h-100 border-0 border-start border-primary border-4 bg-light">
                        <div className="card-body">
                            <h6 className="text-muted mb-2 text-uppercase fw-bold">Customers</h6>
                            <h3 className="mb-0 fw-bold">{stats.totalCustomers}</h3>
                        </div>
                    </div>
                </div>
                <div className="col-sm-6 col-lg-3">
                    <div className="card shadow-sm h-100 border-0 border-start border-success border-4 bg-light">
                        <div className="card-body">
                            <h6 className="text-muted mb-2 text-uppercase fw-bold">Farmers</h6>
                            <h3 className="mb-0 fw-bold">{stats.totalFarmers}</h3>
                        </div>
                    </div>
                </div>
                <div className="col-sm-6 col-lg-3">
                    <div className="card shadow-sm h-100 border-0 border-start border-info border-4 bg-light">
                        <div className="card-body">
                            <h6 className="text-muted mb-2 text-uppercase fw-bold">Products</h6>
                            <h3 className="mb-0 fw-bold">{stats.totalProducts} <small className="fs-6 fw-normal text-muted">({stats.activeProducts} active)</small></h3>
                        </div>
                    </div>
                </div>
                <div className="col-sm-6 col-lg-3">
                    <div className="card shadow-sm h-100 border-0 border-start border-warning border-4 bg-light">
                        <div className="card-body">
                            <h6 className="text-muted mb-2 text-uppercase fw-bold">Reviews</h6>
                            <h3 className="mb-0 fw-bold">{stats.totalReviews}</h3>
                        </div>
                    </div>
                </div>
            </div>

            {/* Financial & Order Summary */}
            <h4 className="mb-3 text-secondary">Sales & Orders</h4>
            <div className="row g-3 mb-5">
                <div className="col-lg-4">
                    <div className="card shadow-sm h-100 bg-primary text-white">
                        <div className="card-body d-flex flex-column justify-content-center align-items-center">
                            <h6 className="mb-2 text-uppercase fw-bold opacity-75">Total Revenue (Delivered)</h6>
                            <h1 className="mb-0 fw-bold">${stats.totalSales ? stats.totalSales.toFixed(2) : '0.00'}</h1>
                            <div className="mt-3 opacity-75">From {stats.deliveredOrders} delivered orders</div>
                        </div>
                    </div>
                </div>
                <div className="col-lg-8">
                    <div className="card shadow-sm h-100">
                        <div className="card-body">
                            <div className="row text-center h-100 align-items-center g-2">
                                <div className="col-4 col-sm-2">
                                    <div className="fs-4 fw-bold text-primary">{stats.placedOrders}</div>
                                    <div className="small text-muted fw-bold">PLACED</div>
                                </div>
                                <div className="col-4 col-sm-2">
                                    <div className="fs-4 fw-bold text-info">{stats.confirmedOrders}</div>
                                    <div className="small text-muted fw-bold">CONFIRM</div>
                                </div>
                                <div className="col-4 col-sm-2">
                                    <div className="fs-4 fw-bold text-warning">{stats.packedOrders}</div>
                                    <div className="small text-muted fw-bold">PACKED</div>
                                </div>
                                <div className="col-4 col-sm-2">
                                    <div className="fs-4 fw-bold text-primary">{stats.shippedOrders}</div>
                                    <div className="small text-muted fw-bold">SHIPPED</div>
                                </div>
                                <div className="col-4 col-sm-2">
                                    <div className="fs-4 fw-bold text-success">{stats.deliveredOrders}</div>
                                    <div className="small text-muted fw-bold">DELIVER</div>
                                </div>
                                <div className="col-4 col-sm-2">
                                    <div className="fs-4 fw-bold text-danger">{stats.cancelledOrders}</div>
                                    <div className="small text-muted fw-bold">CANCEL</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="row g-4">
                {/* Recent Orders */}
                <div className="col-lg-8">
                    <div className="card shadow-sm h-100">
                        <div className="card-header bg-white fw-bold">
                            Recent Orders
                        </div>
                        <div className="card-body p-0">
                            {recentOrders.length > 0 ? (
                                <div className="table-responsive">
                                    <table className="table table-hover mb-0 align-middle">
                                        <thead className="table-light">
                                            <tr>
                                                <th>Order ID</th>
                                                <th>Customer ID</th>
                                                <th>Date</th>
                                                <th>Amount</th>
                                                <th>Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {recentOrders.map(order => (
                                                <tr key={order.id}>
                                                    <td>#{order.id}</td>
                                                    <td>User {order.customerId}</td>
                                                    <td>{new Date(order.orderDate).toLocaleString()}</td>
                                                    <td className="fw-bold">${order.totalAmount.toFixed(2)}</td>
                                                    <td>
                                                        <span className={`badge ${order.status === 'DELIVERED' ? 'bg-success' : order.status === 'CANCELLED' ? 'bg-danger' : 'bg-primary'}`}>
                                                            {order.status}
                                                        </span>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            ) : (
                                <div className="p-4 text-center text-muted">No orders found.</div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Inventory Alerts */}
                <div className="col-lg-4">
                    <div className="card shadow-sm h-100 border-danger">
                        <div className="card-header bg-danger text-white fw-bold">
                            Inventory Alerts
                        </div>
                        <div className="card-body text-center d-flex flex-column justify-content-center">
                            <div className="mb-4">
                                <h1 className="display-4 text-warning fw-bold mb-0">{stats.lowStockProducts}</h1>
                                <p className="text-muted fw-bold text-uppercase">Low Stock Items</p>
                            </div>
                            <div>
                                <h1 className="display-4 text-danger fw-bold mb-0">{stats.outOfStockProducts}</h1>
                                <p className="text-muted fw-bold text-uppercase">Out of Stock Items</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
