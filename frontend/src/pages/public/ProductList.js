import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import ProductCard from '../../components/products/ProductCard';

const ProductList = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchProducts = async () => {
            try {
                setLoading(true);
                const response = await api.get('/products');
                setProducts(response.data);
                setError(null);
            } catch (err) {
                console.error("Error fetching products:", err);
                if (!err.response) {
                    setError("Network error: Backend is unavailable.");
                } else if (err.response.status >= 500) {
                    setError("Server error: Please try again later.");
                } else {
                    setError(`Failed to load products (${err.response.status}).`);
                }
            } finally {
                setLoading(false);
            }
        };

        fetchProducts();
    }, []);

    if (loading) {
        return (
            <div className="text-center mt-5">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
                <p className="mt-2">Loading fresh harvest...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="alert alert-danger mt-4" role="alert">
                <h4 className="alert-heading">Oops!</h4>
                <p>{error}</p>
                <hr />
                <button className="btn btn-outline-danger" onClick={() => window.location.reload()}>Try Again</button>
            </div>
        );
    }

    return (
        <div className="container mt-4">
            <h2 className="mb-4">Fresh from the Farm</h2>
            
            {products.length === 0 ? (
                <div className="alert alert-info text-center">
                    No products are currently available in the marketplace.
                </div>
            ) : (
                <div className="row row-cols-1 row-cols-md-3 row-cols-lg-4 g-4">
                    {products.map(product => (
                        <div className="col" key={product.id}>
                            <ProductCard product={product} />
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default ProductList;
