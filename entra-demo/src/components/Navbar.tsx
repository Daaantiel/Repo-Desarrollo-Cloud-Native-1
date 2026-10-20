import React from 'react';
import { useMsal, useIsAuthenticated } from '@azure/msal-react';
import { tokenRequest } from '../authConfig';

interface NavbarProps {
  activeTab: 'catalog' | 'orders';
  setActiveTab: (tab: 'catalog' | 'orders') => void;
}

export const Navbar: React.FC<NavbarProps> = ({ activeTab, setActiveTab }) => {
  const { instance, accounts } = useMsal();
  const isAuthenticated = useIsAuthenticated();

  const handleLogin = () => {
    instance.loginPopup(tokenRequest).catch((e) => console.error('Error en login:', e));
  };

  const handleLogout = () => {
    instance.logoutPopup().catch((e) => console.error('Error en logout:', e));
  };

  const activeAccount = accounts[0];

  return (
    <nav style={{
      background: 'linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%)',
      color: '#ffffff',
      padding: '12px 32px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      boxShadow: '0 4px 20px rgba(0, 0, 0, 0.25)',
      borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
      position: 'sticky',
      top: 0,
      zIndex: 1000
    }}>
      {/* Brand Logo */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
        <span style={{ fontSize: '1.4rem' }}></span>
        <h1 style={{ 
          margin: 0, 
          fontSize: '1.35rem', 
          fontWeight: 800, 
          letterSpacing: '0.5px',
          background: 'linear-gradient(90deg, #fbbf24, #f59e0b)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent'
        }}>
          PokéMarket360
        </h1>
      </div>

      {/* Tabs Navegación */}
      {isAuthenticated && (
        <div style={{ 
          display: 'flex', 
          gap: '6px',
          backgroundColor: 'rgba(255, 255, 255, 0.08)',
          padding: '4px',
          borderRadius: '12px',
          border: '1px solid rgba(255, 255, 255, 0.08)'
        }}>
          <button
            onClick={() => setActiveTab('catalog')}
            style={{
              padding: '8px 20px',
              borderRadius: '8px',
              border: 'none',
              cursor: 'pointer',
              fontWeight: '700',
              fontSize: '0.9rem',
              transition: 'all 0.2s ease',
              backgroundColor: activeTab === 'catalog' ? '#f59e0b' : 'transparent',
              color: activeTab === 'catalog' ? '#0f172a' : '#cbd5e1',
              boxShadow: activeTab === 'catalog' ? '0 2px 8px rgba(245, 158, 11, 0.3)' : 'none'
            }}
          >
            Catálogo
          </button>

          <button
            onClick={() => setActiveTab('orders')}
            style={{
              padding: '8px 20px',
              borderRadius: '8px',
              border: 'none',
              cursor: 'pointer',
              fontWeight: '700',
              fontSize: '0.9rem',
              transition: 'all 0.2s ease',
              backgroundColor: activeTab === 'orders' ? '#f59e0b' : 'transparent',
              color: activeTab === 'orders' ? '#0f172a' : '#cbd5e1',
              boxShadow: activeTab === 'orders' ? '0 2px 8px rgba(245, 158, 11, 0.3)' : 'none'
            }}
          >
            Pedidos
          </button>
        </div>
      )}

      
      <div>
        {isAuthenticated ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              backgroundColor: 'rgba(255, 255, 255, 0.08)',
              padding: '6px 14px',
              borderRadius: '20px',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              fontSize: '0.85rem',
              fontWeight: '600',
              color: '#f8fafc'
            }}>
              <span style={{ 
                width: '8px', 
                height: '8px', 
                backgroundColor: '#10b981', 
                borderRadius: '50%',
                boxShadow: '0 0 8px #10b981'
              }} />
              <span>{activeAccount?.name || activeAccount?.username}</span>
            </div>

            <button
              onClick={handleLogout}
              style={{
                padding: '8px 16px',
                backgroundColor: '#ef4444',
                color: '#ffffff',
                border: 'none',
                borderRadius: '8px',
                fontWeight: '700',
                fontSize: '0.85rem',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                boxShadow: '0 2px 6px rgba(239, 68, 68, 0.3)'
              }}
            >
              Cerrar Sesión
            </button>
          </div>
        ) : (
          <button
            onClick={handleLogin}
            style={{
              padding: '10px 20px',
              backgroundColor: '#f59e0b',
              color: '#0f172a',
              border: 'none',
              borderRadius: '8px',
              fontWeight: '800',
              fontSize: '0.9rem',
              cursor: 'pointer',
              transition: 'all 0.2s ease',
              boxShadow: '0 4px 12px rgba(245, 158, 11, 0.3)'
            }}
          >
            Iniciar Sesión con Microsoft
          </button>
        )}
      </div>
    </nav>
  );
};