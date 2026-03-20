import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const AuthPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async () => {
    if (!username || !password) {
      setError('Enter both your name and secret rune!');
      return;
    }
    
    setLoading(true);
    setError('');
    
    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        username,
        password
      });
      console.log('Login successful:', response.data);
      // Save basic info to localStorage
      localStorage.setItem('user', JSON.stringify(response.data));
      // Go to game
      navigate('/game');
    } catch (err) {
      const errorMsg = err.response?.data || 'Connection to the realm failed.';
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = () => {
    alert("Registration requires selecting a Class and Race. This will be implemented soon!");
  }

  return (
    <div className="auth-container glass-panel">
      <div className="game-title">FRENEMY</div>
      <h1>Valhalla Awaits</h1>
      <p>Enter the saga. Claim your glory.</p>
      
      {error && <div style={{color: 'var(--primary-hover)', marginBottom: '1.5rem', fontWeight: 'bold'}}>{error}</div>}
      
      <div className="form-group">
        <input 
          type="text" 
          placeholder="Warrior Name" 
          className="input-field" 
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
        />
        <input 
          type="password" 
          placeholder="Secret Rune (Password)" 
          className="input-field" 
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
        />
      </div>
      <div className="action-buttons">
        <button className="btn btn-primary" onClick={handleLogin} disabled={loading}>
          {loading ? 'Entering...' : 'Enter the Fray (Login)'}
        </button>
        <button className="btn btn-secondary" onClick={handleRegister}>Forge a Legend (Register)</button>
      </div>
    </div>
  );
};

export default AuthPage;
