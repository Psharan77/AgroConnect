import React, { useContext, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';
import { CartContext } from '../../contexts/CartContext';

const ProductCard = ({ product }) => {
    const { user } = useContext(AuthContext);
    const { addToCart } = useContext(CartContext);
    const navigate = useNavigate();
    const [adding, setAdding] = useState(false);

    return (
        <div className="card h-100 shadow-sm">
            <img 
                src={product.imageUrl || 'https://via.placeholder.com/150'} 
                className="card-img-top" 
                alt={product.name}
                style={{ height: '200px', objectFit: 'cover' }}
            />
            <div className="card-body d-flex flex-column">
                <h5 className="card-title">{product.name}</h5>
                <h6 className="card-subtitle mb-2 text-muted">
                    {product.categoryName || `Category ${product.categoryId}`} {product.organic && <span className="badge bg-success ms-1">Organic</span>}
                </h6>
                <p className="card-text text-truncate">{product.description}</p>
                <div className="mt-auto">
                    <div className="d-flex justify-content-between align-items-center mb-2">
                        <span className="fs-5 fw-bold">${product.price.toFixed(2)}</span>
                        <span className="text-muted small">/{product.unit}</span>
                    </div>
                    <div className="d-flex justify-content-between align-items-center">
                        <span className={`small fw-bold ${product.quantity > 0 ? 'text-success' : 'text-danger'}`}>
                            {product.quantity > 0 ? `${product.quantity} in stock` : 'Out of Stock'}
                        </span>
                        <div>
                            <Link to={`/products/${product.id}`} className="btn btn-outline-primary btn-sm me-1">
                                View
                            </Link>
                            <button 
                                className="btn btn-success btn-sm"
                                disabled={product.quantity <= 0 || adding || (user && user.role !== 'CUSTOMER')}
                                onClick={async () => {
                                    if (!user) { navigate('/login'); return; }
                                    setAdding(true);
                                    try { await addToCart(product.id, 1); } 
                                    catch (e) { alert(e.response?.data?.message || 'Failed to add'); }
                                    finally { setAdding(false); }
                                }}
                            >
                                {adding ? '...' : '+ Cart'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProductCard;
