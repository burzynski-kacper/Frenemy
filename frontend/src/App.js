import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import AuthPage from './pages/AuthPage';
import GamePage from './pages/GamePage';
import './index.css';

function App() {
  return (
    <Router>
      <div className="app-background">
        <Routes>
          <Route path="/auth" element={<AuthPage />} />
          <Route path="/game" element={<GamePage />} />
          <Route path="*" element={<Navigate to="/auth" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
