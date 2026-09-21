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
      backgroundColor: '#2a75bb',
      color: '#ffffff',
      padding: '12px 24px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      boxShadow: '0 2px 8px rgba(0,0,0,0.2)'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        <span style={{ fontSize: '1.5rem' }}></span>
        <h1 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 'bold' }}>
          PokéMarket360
        </h1>
      </div>

      {isAuthenticated && (
        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            onClick={() => setActiveTab('catalog')}
            style={{
              padding: '8px 16px',
              borderRadius: '6px',
              border: 'none',
              cursor: 'pointer',
              fontWeight: 'bold',
              backgroundColor: activeTab === 'catalog' ? '#ffcb05' : 'transparent',
              color: activeTab === 'catalog' ? '#2a75bb' : '#ffffff'
            }}
          >
            Catálogo
          </button>

          <button
            onClick={() => setActiveTab('orders')}
            style={{
              padding: '8px 16px',
              borderRadius: '6px',
              border: 'none',
              cursor: 'pointer',
              fontWeight: 'bold',
              backgroundColor: activeTab === 'orders' ? '#ffcb05' : 'transparent',
              color: activeTab === 'orders' ? '#2a75bb' : '#ffffff'
            }}
          >
            Pedidos
          </button>
        </div>
      )}

      <div>
        {isAuthenticated ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <span style={{ fontSize: '0.9rem', color: '#ffcb05' }}>
              👤 {activeAccount?.name || activeAccount?.username}
            </span>
            <button
              onClick={handleLogout}
              style={{
                padding: '6px 12px',
                backgroundColor: '#ef5350',
                color: '#fff',
                border: 'none',
                borderRadius: '4px',
                fontWeight: 'bold',
                cursor: 'pointer'
              }}
            >
              Cerrar Sesión
            </button>
          </div>
        ) : (
          <button
            onClick={handleLogin}
            style={{
              padding: '8px 16px',
              backgroundColor: '#ffcb05',
              color: '#2a75bb',
              border: 'none',
              borderRadius: '6px',
              fontWeight: 'bold',
              cursor: 'pointer'
            }}
          >
            Iniciar Sesión con Microsoft
          </button>
        )}
      </div>
    </nav>
  );
};