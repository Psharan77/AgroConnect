import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';

const Register = () => {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: '',
        role: 'CUSTOMER'
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({...formData, [e.target.name]: e.target.value});
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            // We bypass the context here and hit the API directly since they aren't logging in yet
            await api.post('/auth/register', formData);
            setSuccess('Registration successful! Redirecting to login...');
            setTimeout(() => navigate('/login'), 2000);
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed. Email might already exist.');
        }
    };

    return (
        <div className="row justify-content-center">
            <div className="col-md-6 col-lg-5">
                <div className="card shadow-sm mt-5 border-0">
                    <div className="card-body p-5">
                        <h2 className="text-center mb-4" style={{ color: 'var(--agro-green)' }}>Join AgroConnect</h2>
                        
                        {error && <div className="alert alert-danger">{error}</div>}
                        {success && <div className="alert alert-success">{success}</div>}
                        
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label className="form-label text-muted">Full Name</label>
                                <input type="text" className="form-control" name="fullName" onChange={handleChange} required />
                            </div>
                            <div className="mb-3">
                                <label className="form-label text-muted">Email address</label>
                                <input type="email" className="form-control" name="email" onChange={handleChange} required />
                            </div>
                            <div className="mb-3">
                                <label className="form-label text-muted">Password</label>
                                <input type="password" className="form-control" name="password" onChange={handleChange} required />
                            </div>
                            <div className="mb-4">
                                <label className="form-label text-muted">I want to register as a:</label>
                                <select className="form-select" name="role" onChange={handleChange}>
                                    <option value="CUSTOMER">Customer (Buy fresh produce)</option>
                                    <option value="FARMER">Farmer (Sell my harvest)</option>
                                </select>
                            </div>
                            <button type="submit" className="btn btn-success w-100 py-2" style={{ backgroundColor: 'var(--agro-green)' }}>
                                Create Account
                            </button>
                        </form>
                        
                        <div className="text-center mt-4">
                            <span className="text-muted">Already have an account? </span>
                            <Link to="/login" style={{ color: 'var(--agro-green)', textDecoration: 'none' }}>Login here</Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Register;
