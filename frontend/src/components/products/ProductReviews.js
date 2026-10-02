import React, { useState, useEffect, useContext } from 'react';
import api from '../../services/api';
import { AuthContext } from '../../contexts/AuthContext';

const ProductReviews = ({ productId }) => {
    const { user } = useContext(AuthContext);
    
    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    const [eligibility, setEligibility] = useState({ canReview: false, status: null, myReview: null });
    
    const [rating, setRating] = useState(5);
    const [comment, setComment] = useState('');
    const [submitLoading, setSubmitLoading] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const [isEditing, setIsEditing] = useState(false);
    
    const fetchReviews = async () => {
        try {
            const res = await api.get(`/products/${productId}/reviews`);
            setSummary(res.data);
            
            // If logged in as customer, check eligibility locally
            if (user && user.role === 'CUSTOMER') {
                const myRev = res.data.reviews.find(r => r.customerId === user.userId);
                
                if (myRev) {
                    setEligibility({ canReview: true, status: 'REVIEWED', myReview: myRev });
                    setRating(myRev.rating);
                    setComment(myRev.comment);
                } else {
                    // Check order history for this product
                    try {
                        const ordersRes = await api.get('/orders/my-orders');
                        let foundPurchased = false;
                        let foundDelivered = false;
                        
                        ordersRes.data.forEach(order => {
                            const hasProduct = order.items.some(i => i.productId === Number(productId));
                            if (hasProduct) {
                                foundPurchased = true;
                                if (order.status === 'DELIVERED') {
                                    foundDelivered = true;
                                }
                            }
                        });
                        
                        if (foundDelivered) {
                            setEligibility({ canReview: true, status: 'DELIVERED', myReview: null });
                        } else if (foundPurchased) {
                            setEligibility({ canReview: false, status: 'PLACED', myReview: null });
                        } else {
                            setEligibility({ canReview: false, status: 'NOT_PURCHASED', myReview: null });
                        }
                    } catch (e) {
                        console.error("Failed to fetch order history", e);
                    }
                }
            }
        } catch (err) {
            setError('Failed to load reviews.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchReviews();
        // eslint-disable-next-line
    }, [productId, user]);
    
    const handleSubmit = async (e) => {
        e.preventDefault();
        if (rating < 1 || rating > 5) {
            setSubmitError('Rating must be between 1 and 5');
            return;
        }
        
        setSubmitLoading(true);
        setSubmitError('');
        
        try {
            if (isEditing && eligibility.myReview) {
                await api.put(`/reviews/${eligibility.myReview.id}`, { rating, comment });
                setIsEditing(false);
            } else {
                await api.post(`/products/${productId}/reviews`, { rating, comment });
            }
            await fetchReviews();
        } catch (err) {
            setSubmitError(err.response?.data?.message || 'Failed to submit review');
        } finally {
            setSubmitLoading(false);
        }
    };
    
    const handleDelete = async () => {
        if (!window.confirm('Are you sure you want to delete your review?')) return;
        
        setSubmitLoading(true);
        try {
            await api.delete(`/reviews/${eligibility.myReview.id}`);
            setRating(5);
            setComment('');
            setIsEditing(false);
            await fetchReviews();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to delete');
        } finally {
            setSubmitLoading(false);
        }
    };

    const renderStars = (count) => {
        return '⭐'.repeat(count);
    };

    if (loading) return <div className="mt-4"><div className="spinner-border spinner-border-sm text-primary"></div> Loading reviews...</div>;
    if (error) return <div className="alert alert-danger mt-4">{error}</div>;

    return (
        <div className="mt-5">
            <h3>Customer Reviews</h3>
            
            <div className="d-flex align-items-center mb-4">
                <div className="display-4 me-3">{summary.averageRating.toFixed(1)}</div>
                <div>
                    <div>{renderStars(Math.round(summary.averageRating))}</div>
                    <div className="text-muted">Based on {summary.totalReviews} reviews</div>
                </div>
            </div>
            
            {user && user.role === 'CUSTOMER' && (
                <div className="card mb-4 bg-light">
                    <div className="card-body">
                        {eligibility.status === 'NOT_PURCHASED' && (
                            <p className="mb-0 text-muted">You can review this product after purchasing it.</p>
                        )}
                        {eligibility.status === 'PLACED' && (
                            <p className="mb-0 text-muted">You can review this product after your order is delivered.</p>
                        )}
                        {eligibility.status === 'REVIEWED' && !isEditing && (
                            <div>
                                <h5 className="card-title">Your Review</h5>
                                <div className="mb-2">{renderStars(eligibility.myReview.rating)}</div>
                                <p>{eligibility.myReview.comment}</p>
                                <button className="btn btn-sm btn-outline-primary me-2" onClick={() => setIsEditing(true)}>Edit Review</button>
                                <button className="btn btn-sm btn-outline-danger" onClick={handleDelete}>Delete Review</button>
                            </div>
                        )}
                        {(eligibility.status === 'DELIVERED' || isEditing) && (
                            <form onSubmit={handleSubmit}>
                                <h5 className="card-title">{isEditing ? 'Edit your review' : 'Write a Review'}</h5>
                                {submitError && <div className="alert alert-danger py-2">{submitError}</div>}
                                
                                <div className="mb-3">
                                    <label className="form-label">Rating</label>
                                    <select className="form-select" style={{ maxWidth: '150px' }} value={rating} onChange={(e) => setRating(Number(e.target.value))}>
                                        <option value="5">5 Stars</option>
                                        <option value="4">4 Stars</option>
                                        <option value="3">3 Stars</option>
                                        <option value="2">2 Stars</option>
                                        <option value="1">1 Star</option>
                                    </select>
                                </div>
                                
                                <div className="mb-3">
                                    <label className="form-label">Comment</label>
                                    <textarea 
                                        className="form-control" 
                                        rows="3" 
                                        value={comment} 
                                        onChange={(e) => setComment(e.target.value)}
                                        placeholder="What did you like or dislike?"
                                    ></textarea>
                                </div>
                                
                                <button type="submit" className="btn btn-primary" disabled={submitLoading}>
                                    {submitLoading ? 'Submitting...' : 'Submit Review'}
                                </button>
                                {isEditing && (
                                    <button type="button" className="btn btn-outline-secondary ms-2" onClick={() => setIsEditing(false)}>Cancel</button>
                                )}
                            </form>
                        )}
                    </div>
                </div>
            )}
            
            {!user && (
                <div className="alert alert-info">
                    Please <a href="/login">log in</a> to write a review.
                </div>
            )}
            
            <div className="list-group list-group-flush mt-3">
                {summary.reviews.length === 0 ? (
                    <p className="text-muted">No reviews yet. Be the first to review this product!</p>
                ) : (
                    summary.reviews.map(review => (
                        <div key={review.id} className="list-group-item py-3 px-0">
                            <div className="d-flex justify-content-between">
                                <h6 className="mb-1">{review.customerName}</h6>
                                <small className="text-muted">{new Date(review.createdAt).toLocaleDateString()}</small>
                            </div>
                            <div className="mb-2">{renderStars(review.rating)}</div>
                            <p className="mb-1">{review.comment}</p>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default ProductReviews;
