import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    
    // We grab the login function from our global AuthContext
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const data = await login(email, password);
            
            // Redirect based on the user's role
            if (data.role === 'CUSTOMER') navigate('/customer/dashboard');
            else if (data.role === 'FARMER') navigate('/farmer/dashboard');
            else if (data.role === 'ADMIN') navigate('/admin/dashboard');
            else navigate('/');
            
        } catch (err) {
            setError('Invalid email or password. Please try again.');
        }
    };

    return (
        <div className="row justify-content-center">
            <div className="col-md-6 col-lg-4">
                <div className="card shadow-sm mt-5 border-0">
                    <div className="card-body p-5">
                        <h2 className="text-center mb-4" style={{ color: 'var(--agro-green)' }}>Welcome Back</h2>
                        
                        {error && <div className="alert alert-danger">{error}</div>}
                        
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label className="form-label text-muted">Email address</label>
                                <input 
                                    type="email" 
                                    className="form-control" 
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required 
                                />
                            </div>
                            <div className="mb-4">
                                <label className="form-label text-muted">Password</label>
                                <input 
                                    type="password" 
                                    className="form-control" 
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required 
                                />
                            </div>
                            <button type="submit" className="btn btn-success w-100 py-2" style={{ backgroundColor: 'var(--agro-green)' }}>
                                Login
                            </button>
                        </form>
                        
                        <div className="text-center mt-4">
                            <span className="text-muted">New to AgroConnect? </span>
                            <Link to="/register" style={{ color: 'var(--agro-green)', textDecoration: 'none' }}>Register here</Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
