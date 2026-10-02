import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import ProductForm from './ProductForm';

const FarmerProducts = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [successMsg, setSuccessMsg] = useState('');
    
    const [showForm, setShowForm] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);

    const fetchMyProducts = async () => {
        try {
            setLoading(true);
            const res = await api.get('/products/my-products');
            setProducts(res.data);
            setError('');
        } catch (err) {
            setError('Failed to load your products. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMyProducts();
    }, []);

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this product?')) return;
        
        try {
            await api.delete(`/products/${id}`);
            setSuccessMsg('Product deleted successfully.');
            fetchMyProducts(); // Refresh list
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to delete product.');
        }
    };

    const handleFormSubmitSuccess = (msg) => {
        setSuccessMsg(msg);
        setShowForm(false);
        setEditingProduct(null);
        fetchMyProducts();
    };

    if (loading) return <div className="text-center mt-5"><div className="spinner-border text-success"></div></div>;

    if (showForm) {
        return (
            <div className="container mt-4">
                <button className="btn btn-secondary mb-3" onClick={() => { setShowForm(false); setEditingProduct(null); }}>
                    &larr; Back to Products
                </button>
                <ProductForm 
                    initialData={editingProduct} 
                    onSuccess={handleFormSubmitSuccess} 
                />
            </div>
        );
    }

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="text-success">My Inventory</h2>
                <button className="btn btn-success" onClick={() => setShowForm(true)}>
                    + Add New Product
                </button>
            </div>

            {error && <div className="alert alert-danger">{error}</div>}
            {successMsg && <div className="alert alert-success">{successMsg}</div>}

            {products.length === 0 ? (
                <div className="alert alert-info text-center">
                    You haven't listed any products yet. Click 'Add New Product' to start selling!
                </div>
            ) : (
                <div className="table-responsive shadow-sm rounded">
                    <table className="table table-hover align-middle mb-0">
                        <thead className="table-success">
                            <tr>
                                <th>Image</th>
                                <th>Name</th>
                                <th>Category ID</th>
                                <th>Price</th>
                                <th>Stock</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {products.map(p => (
                                <tr key={p.id}>
                                    <td>
                                        <img src={p.imageUrl || 'https://via.placeholder.com/50'} alt={p.name} style={{ width: '50px', height: '50px', objectFit: 'cover' }} className="rounded" />
                                    </td>
                                    <td className="fw-bold">{p.name}</td>
                                    <td>{p.categoryId}</td>
                                    <td>${p.price.toFixed(2)} / {p.unit}</td>
                                    <td>
                                        <span className={`badge ${p.quantity > 0 ? 'bg-primary' : 'bg-danger'}`}>
                                            {p.quantity} {p.unit}
                                        </span>
                                    </td>
                                    <td>
                                        <button className="btn btn-sm btn-outline-primary me-2" onClick={() => { setEditingProduct(p); setShowForm(true); }}>Edit</button>
                                        <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(p.id)}>Delete</button>
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

export default FarmerProducts;
