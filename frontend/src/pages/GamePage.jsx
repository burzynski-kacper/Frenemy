import React from 'react';
import { useNavigate, Navigate } from 'react-router-dom';

const GamePage = () => {
  const navigate = useNavigate();
  const userString = localStorage.getItem('user');
  const user = userString ? JSON.parse(userString) : null;

  if (!user) {
    return <Navigate to="/auth" replace />;
  }

  return (
    <div className="game-layout">
      {/* HEADER */}
      <header className="game-header glass-panel">
        <div className="game-title" style={{ fontSize: '1.8rem', marginBottom: 0, letterSpacing: '8px' }}>FRENEMY</div>
        <div className="header-stats">
          <span className="gold-text">💰 10 Gold</span>
          <span>Level 1</span>
        </div>
        <button 
          className="btn btn-secondary" style={{ padding: '0.6rem 2rem' }}
          onClick={() => { localStorage.removeItem('user'); navigate('/auth'); }}
        >
          Camp (Logout)
        </button>
      </header>

      {/* LEFT SIDEBAR - CHARACTER */}
      <aside className="sidebar-left glass-panel">
        <h3 style={{ fontFamily: 'Cinzel', color: 'var(--accent)', marginTop: 0, borderBottom: '1px solid var(--panel-border)', paddingBottom: '0.5rem' }}>{user.characterName}</h3>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>Class: Unknown</p>
        
        <div className="stats-container">
          <h4 style={{ textTransform: 'uppercase', letterSpacing: '2px', color: 'var(--text-secondary)' }}>Attributes</h4>
          <div className="stat-row"><span>HP:</span> <span style={{ color: '#d4af37', fontWeight: 'bold' }}>100/100</span></div>
          <div className="stat-row"><span>Strength:</span> <span>15</span></div>
          <div className="stat-row"><span>Dexterity:</span> <span>12</span></div>
          <div className="stat-row"><span>Constitution:</span> <span>14</span></div>
          <div className="stat-row"><span>Intelligence:</span> <span>10</span></div>
          <div className="stat-row"><span>Luck:</span> <span>5</span></div>
        </div>
      </aside>

      {/* MAIN CONTENT AREA */}
      <main className="main-content glass-panel">
        <h2 style={{ fontFamily: 'Cinzel', color: 'var(--text-primary)', textAlign: 'center', fontSize: '2.5rem', marginBottom: '1rem' }}>The Wilds</h2>
        <p style={{ color: 'var(--text-secondary)', textAlign: 'center', fontSize: '1.1rem', fontStyle: 'italic' }}>
          You are standing in the middle of a muddy crossroad. The air is thick with mist. To the north lies the haunted forest.
        </p>
        
        <div className="action-buttons-grid">
          <button className="btn btn-primary" style={{ gridColumn: '1 / -1', padding: '1.5rem' }}>⚔️ Find Enemy (Combat)</button>
          <button className="btn btn-secondary">🗺️ Quest Board</button>
          <button className="btn btn-secondary">🍺 Visit Tavern</button>
        </div>
      </main>

      {/* RIGHT SIDEBAR - INVENTORY / LOGS */}
      <aside className="sidebar-right glass-panel">
        <h3 style={{ fontFamily: 'Cinzel', color: 'var(--accent)', marginTop: 0, borderBottom: '1px solid var(--panel-border)', paddingBottom: '0.5rem' }}>Inventory</h3>
        <ul className="inventory-list">
          <li>🗡️ Rusty Viking Sword</li>
          <li>🛡️ Worn Leather Armor</li>
          <li>🩸 Healing Potion x2</li>
        </ul>
      </aside>
    </div>
  );
};

export default GamePage;
