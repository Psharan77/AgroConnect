import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { AuthContext } from '../../contexts/AuthContext';
import { CartContext } from '../../contexts/CartContext';
import ProductReviews from '../../components/products/ProductReviews';

const ProductDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = React.useContext(AuthContext);
    const { addToCart } = React.useContext(CartContext);
    const [product, setProduct] = useState(null);
    const [adding, setAdding] = useState(false);
    const [addError, setAddError] = useState('');
    const [addSuccess, setAddSuccess] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchProductDetails = async () => {
            try {
                setLoading(true);
                const response = await api.get(`/products/${id}`);
                setProduct(response.data);
                setError(null);
            } catch (err) {
                console.error("Error fetching product details:", err);
                if (!err.response) {
                    setError("Network error: Backend is unavailable.");
                } else if (err.response.status === 404) {
                    setError("Product not found.");
                } else {
                    setError(`Failed to load details (${err.response.status}).`);
                }
            } finally {
                setLoading(false);
            }
        };

        fetchProductDetails();
    }, [id]);

    if (loading) return (
        <div className="text-center mt-5">
            <div className="spinner-border text-primary" role="status"></div>
            <p className="mt-2">Loading details...</p>
        </div>
    );
    
    if (error) return <div className="alert alert-danger mt-4">{error}</div>;
    if (!product) return null;

    return (
        <div className="container mt-5">
            <nav aria-label="breadcrumb">
                <ol className="breadcrumb">
                    <li className="breadcrumb-item"><Link to="/products">Marketplace</Link></li>
                    <li className="breadcrumb-item active" aria-current="page">{product.name}</li>
                </ol>
            </nav>
            
            <div className="row">
                <div className="col-md-6 mb-4">
                    <img 
                        src={product.imageUrl || 'https://via.placeholder.com/600x400'} 
                        alt={product.name} 
                        className="img-fluid rounded shadow-sm w-100"
                        style={{ objectFit: 'cover' }}
                    />
                </div>
                <div className="col-md-6">
                    <h1 className="display-5 fw-bold">{product.name}</h1>
                    
                    <div className="mb-3 d-flex align-items-center">
                        <span className="text-warning fs-5 me-2">
                            {'⭐'.repeat(Math.round(product.averageRating || 0))}
                        </span>
                        <span className="fw-bold">{product.averageRating?.toFixed(1) || '0.0'} / 5</span>
                        <span className="text-muted ms-2">Based on {product.reviewCount || 0} reviews</span>
                    </div>

                    <div className="mb-3">
                        {product.organic && <span className="badge bg-success me-2 fs-6">Certified Organic</span>}
                        {product.categoryId && <span className="badge bg-secondary fs-6">Category ID: {product.categoryId}</span>}
                    </div>
                    
                    <h2 className="text-primary mb-3">${product.price.toFixed(2)} <small className="text-muted fs-5">/ {product.unit}</small></h2>
                    
                    <p className="lead">{product.description}</p>
                    
                    <hr />
                    
                    <div className="row mb-3">
                        <div className="col-6">
                            <strong>Location:</strong> <br/>{product.location || 'Not specified'}
                        </div>
                        <div className="col-6">
                            <strong>Availability:</strong> <br/>
                            <span className={product.quantity > 0 ? 'text-success fw-bold' : 'text-danger fw-bold'}>
                                {product.quantity > 0 ? `${product.quantity} ${product.unit}s in stock` : 'Out of Stock'}
                            </span>
                        </div>
                    </div>
                    
                    {product.farmerId && (
                        <div className="card bg-light mb-4">
                            <div className="card-body">
                                <h5>Farmer Information</h5>
                                <p className="mb-0">Sold by Farmer #{product.farmerId}</p>
                            </div>
                        </div>
                    )}
                    
                    {addError && <div className="alert alert-danger">{addError}</div>}
                    {addSuccess && <div className="alert alert-success">{addSuccess}</div>}

                    <div className="d-grid gap-2">
                        <button 
                            className="btn btn-success btn-lg" 
                            disabled={product.quantity === 0 || adding}
                            onClick={async () => {
                                if (!user) {
                                    navigate('/login');
                                    return;
                                }
                                if (user.role !== 'CUSTOMER') {
                                    setAddError('Only customers can add items to cart.');
                                    return;
                                }
                                setAdding(true);
                                setAddError('');
                                setAddSuccess('');
                                try {
                                    await addToCart(product.id, 1);
                                    setAddSuccess('Added to cart!');
                                    setTimeout(() => setAddSuccess(''), 3000);
                                } catch (err) {
                                    setAddError(err.response?.data?.message || 'Failed to add to cart');
                                } finally {
                                    setAdding(false);
                                }
                            }}
                        >
                            {adding ? <span className="spinner-border spinner-border-sm me-2"></span> : <i className="bi bi-cart-plus"></i>} 
                            Add to Cart
                        </button>
                    </div>
                </div>
            </div>
            
            <hr className="my-5" />
            
            <ProductReviews productId={product.id} />
        </div>
    );
};

export default ProductDetails;
