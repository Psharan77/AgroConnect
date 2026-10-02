import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';

const FarmerDashboard = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        api.get('/farmer/dashboard')
            .then(res => {
                setStats(res.data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.response?.data?.message || 'Failed to load dashboard data.');
                setLoading(false);
            });
    }, []);

    if (loading) return <div className="text-center mt-5"><div className="spinner-border text-success"></div><p className="mt-2 text-muted">Loading your dashboard...</p></div>;
    
    if (error) return <div className="alert alert-danger mt-4 container">{error}</div>;

    if (!stats) return null;

    return (
        <div className="container mt-4 mb-5">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="text-success m-0">Farmer Dashboard</h2>
                <div className="d-flex gap-2">
                    <Link to="/farmer/products" className="btn btn-outline-success">
                        <i className="bi bi-box-seam me-1"></i> Manage Products
                    </Link>
                </div>
            </div>

            {/* Dashboard Summary Cards */}
            <div className="row g-3 mb-4">
                <div className="col-sm-6 col-lg-3">
                    <div className="card shadow-sm h-100 border-0 border-start border-primary border-4">
                        <div className="card-body">
                            <h6 className="text-muted mb-2">Total Products</h6>
                            <h3 className="mb-0 fw-bold">{stats.totalProducts}</h3>
                        </div>
                    </div>
                </div>
                <div className="col-sm-6 col-lg-3">
                    <div className="card shadow-sm h-100 border-0 border-start border-info border-4">
                        <div className="card-body">
                            <h6 className="text-muted mb-2">Total Orders</h6>
                            <h3 className="mb-0 fw-bold">{stats.totalOrders}</h3>
                        </div>
                    </div>
                </div>
                <div className="col-sm-6 col-lg-3">
                    <div className="card shadow-sm h-100 border-0 border-start border-success border-4">
                        <div className="card-body">
                            <h6 className="text-muted mb-2">Total Sales</h6>
                            <h3 className="mb-0 fw-bold text-success">${stats.totalSales ? stats.totalSales.toFixed(2) : '0.00'}</h3>
                        </div>
                    </div>
                </div>
                <div className="col-sm-6 col-lg-3">
                    <div className="card shadow-sm h-100 border-0 border-start border-danger border-4">
                        <div className="card-body">
                            <h6 className="text-muted mb-2">Low Stock Products</h6>
                            <h3 className="mb-0 fw-bold text-danger">{stats.lowStockProductsCount}</h3>
                        </div>
                    </div>
                </div>
            </div>

            <div className="row g-4 mb-4">
                {/* Order Status Breakdown */}
                <div className="col-lg-12">
                    <div className="card shadow-sm">
                        <div className="card-header bg-white fw-bold">Order Status Breakdown</div>
                        <div className="card-body">
                            <div className="row text-center g-3">
                                <div className="col-4 col-md-2">
                                    <div className="p-3 border rounded bg-light">
                                        <div className="fs-3 fw-bold text-primary">{stats.pendingOrders}</div>
                                        <div className="small text-muted">PLACED</div>
                                    </div>
                                </div>
                                <div className="col-4 col-md-2">
                                    <div className="p-3 border rounded bg-light">
                                        <div className="fs-3 fw-bold text-info">{stats.confirmedOrders}</div>
                                        <div className="small text-muted">CONFIRMED</div>
                                    </div>
                                </div>
                                <div className="col-4 col-md-2">
                                    <div className="p-3 border rounded bg-light">
                                        <div className="fs-3 fw-bold text-warning">{stats.packedOrders}</div>
                                        <div className="small text-muted">PACKED</div>
                                    </div>
                                </div>
                                <div className="col-4 col-md-2">
                                    <div className="p-3 border rounded bg-light">
                                        <div className="fs-3 fw-bold text-primary">{stats.shippedOrders}</div>
                                        <div className="small text-muted">SHIPPED</div>
                                    </div>
                                </div>
                                <div className="col-4 col-md-2">
                                    <div className="p-3 border rounded bg-light">
                                        <div className="fs-3 fw-bold text-success">{stats.deliveredOrders}</div>
                                        <div className="small text-muted">DELIVERED</div>
                                    </div>
                                </div>
                                <div className="col-4 col-md-2">
                                    <div className="p-3 border rounded bg-light">
                                        <div className="fs-3 fw-bold text-danger">{stats.cancelledOrders}</div>
                                        <div className="small text-muted">CANCELLED</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="row g-4">
                {/* Recent Orders */}
                <div className="col-lg-7">
                    <div className="card shadow-sm h-100">
                        <div className="card-header bg-white fw-bold d-flex justify-content-between align-items-center">
                            Recent Orders
                        </div>
                        <div className="card-body p-0">
                            {stats.recentOrders && stats.recentOrders.length > 0 ? (
                                <div className="table-responsive">
                                    <table className="table table-hover mb-0 align-middle">
                                        <thead className="table-light">
                                            <tr>
                                                <th>Order ID</th>
                                                <th>Date</th>
                                                <th>Amount</th>
                                                <th>Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {stats.recentOrders.map(order => (
                                                <tr key={order.id}>
                                                    <td>#{order.id}</td>
                                                    <td>{new Date(order.orderDate).toLocaleDateString()}</td>
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
                                <div className="p-4 text-center text-muted">
                                    <i className="bi bi-inbox fs-1 d-block mb-2"></i>
                                    No orders received yet.
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Low Stock Alerts */}
                <div className="col-lg-5">
                    <div className="card shadow-sm h-100 border-danger">
                        <div className="card-header bg-danger text-white fw-bold d-flex justify-content-between align-items-center">
                            Low Stock Alerts
                            <span className="badge bg-light text-danger">{stats.lowStockProductsCount}</span>
                        </div>
                        <div className="card-body p-0">
                            {stats.lowStockProducts && stats.lowStockProducts.length > 0 ? (
                                <ul className="list-group list-group-flush">
                                    {stats.lowStockProducts.map(product => (
                                        <li key={product.id} className="list-group-item d-flex justify-content-between align-items-center py-3">
                                            <div>
                                                <h6 className="mb-1">{product.name}</h6>
                                                <small className="text-muted">Price: ${product.price.toFixed(2)}</small>
                                            </div>
                                            <div className="text-end">
                                                <div className="fw-bold text-danger mb-1">{product.quantity} left</div>
                                                <Link to="/farmer/products" className="btn btn-sm btn-outline-danger">Update</Link>
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <div className="p-4 text-center text-success">
                                    <i className="bi bi-check-circle fs-1 d-block mb-2"></i>
                                    All products have healthy stock levels!
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FarmerDashboard;
