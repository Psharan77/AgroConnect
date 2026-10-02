import React, { useState } from 'react';
import api from '../../services/api';

const ProductForm = ({ initialData, onSuccess }) => {
    // If editing, use existing data. Otherwise, use empty defaults.
    const [formData, setFormData] = useState({
        name: initialData?.name || '',
        categoryId: initialData?.categoryId || 1, // Defaulting to 1 since we don't have a category picker yet
        description: initialData?.description || '',
        price: initialData?.price || '',
        quantity: initialData?.quantity || '',
        unit: initialData?.unit || 'kg',
        imageUrl: initialData?.imageUrl || '',
        organic: initialData?.organic || false,
        location: initialData?.location || ''
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        
        try {
            if (initialData?.id) {
                // Edit existing product
                await api.put(`/products/${initialData.id}`, formData);
                onSuccess('Product updated successfully!');
            } else {
                // Create new product
                await api.post('/products', formData);
                onSuccess('Product created successfully!');
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to save product. Check validation constraints.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="card shadow-sm border-0">
            <div className="card-header bg-success text-white py-3">
                <h4 className="mb-0">{initialData ? 'Edit Product' : 'Add New Product'}</h4>
            </div>
            <div className="card-body p-4">
                {error && <div className="alert alert-danger">{error}</div>}
                <form onSubmit={handleSubmit}>
                    <div className="row mb-3">
                        <div className="col-md-6">
                            <label className="form-label text-muted">Product Name *</label>
                            <input type="text" className="form-control" name="name" value={formData.name} onChange={handleChange} required />
                        </div>
                        <div className="col-md-3">
                            <label className="form-label text-muted">Category ID *</label>
                            <input type="number" className="form-control" name="categoryId" value={formData.categoryId} onChange={handleChange} required />
                        </div>
                        <div className="col-md-3 d-flex align-items-end pb-2">
                            <div className="form-check">
                                <input className="form-check-input" type="checkbox" name="organic" checked={formData.organic} onChange={handleChange} id="organicCheck" />
                                <label className="form-check-label text-success fw-bold" htmlFor="organicCheck">Certified Organic</label>
                            </div>
                        </div>
                    </div>

                    <div className="mb-3">
                        <label className="form-label text-muted">Description</label>
                        <textarea className="form-control" name="description" rows="3" value={formData.description} onChange={handleChange}></textarea>
                    </div>

                    <div className="row mb-3">
                        <div className="col-md-4">
                            <label className="form-label text-muted">Price ($) *</label>
                            <input type="number" step="0.01" className="form-control" name="price" value={formData.price} onChange={handleChange} required />
                        </div>
                        <div className="col-md-4">
                            <label className="form-label text-muted">Stock Quantity *</label>
                            <input type="number" className="form-control" name="quantity" value={formData.quantity} onChange={handleChange} required />
                        </div>
                        <div className="col-md-4">
                            <label className="form-label text-muted">Unit (e.g., kg, piece, bunch)</label>
                            <input type="text" className="form-control" name="unit" value={formData.unit} onChange={handleChange} />
                        </div>
                    </div>

                    <div className="row mb-4">
                        <div className="col-md-6">
                            <label className="form-label text-muted">Farm Location</label>
                            <input type="text" className="form-control" name="location" value={formData.location} onChange={handleChange} />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label text-muted">Image URL (Optional)</label>
                            <input type="url" className="form-control" name="imageUrl" value={formData.imageUrl} onChange={handleChange} placeholder="https://..." />
                        </div>
                    </div>

                    <div className="d-flex justify-content-end">
                        <button type="submit" className="btn btn-success px-4" disabled={loading}>
                            {loading ? <span className="spinner-border spinner-border-sm me-2"></span> : null}
                            {initialData ? 'Save Changes' : 'Publish Product'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ProductForm;
