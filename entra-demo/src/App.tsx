import { useState } from 'react';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';
import { Navbar } from './components/Navbar';
import { Catalog } from './pages/Catalog';
import { Orders } from './pages/Orders';
import { tokenRequest } from './authConfig';

export type TabType = 'catalog' | 'orders';

export default function App() {
  const isAuthenticated = useIsAuthenticated();
  const { instance } = useMsal();
  const [activeTab, setActiveTab] = useState<TabType>('catalog');

  const handleLogin = () => {
    instance.loginRedirect({
      ...tokenRequest,
      prompt: 'select_account',
    }).catch((e) => {
      console.error('Error al iniciar sesión:', e);
    });
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f4f6f9', fontFamily: 'Arial, sans-serif' }}>
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

      <main style={{ padding: '20px' }}>
        {!isAuthenticated ? (
          <div
            style={{
              maxWidth: '600px',
              margin: '60px auto',
              padding: '40px',
              backgroundColor: '#ffffff',
              borderRadius: '12px',
              textAlign: 'center',
              boxShadow: '0 4px 16px rgba(0,0,0,0.1)'
            }}
          >
            <div style={{ fontSize: '3.5rem', marginBottom: '10px' }}></div>
            <h1 style={{ color: '#2a75bb', margin: '0 0 10px 0' }}>Bienvenido a PokéMarket360</h1>
            <p style={{ color: '#666', lineHeight: '1.5', marginBottom: '25px' }}>
              Plataforma e-commerce distribuida sobre AWS EC2 con autenticación centralizada mediante Microsoft Entra ID.
            </p>
            <button
              onClick={handleLogin}
              style={{
                padding: '12px 24px',
                fontSize: '1rem',
                fontWeight: 'bold',
                backgroundColor: '#ffcb05',
                color: '#2a75bb',
                border: 'none',
                borderRadius: '8px',
                cursor: 'pointer',
                boxShadow: '0 2px 6px rgba(0,0,0,0.15)'
              }}
            >
            Iniciar Sesión con Microsoft
            </button>
          </div>
        ) : (
          <div>
            {activeTab === 'catalog' && <Catalog />}
            {activeTab === 'orders' && <Orders />}
          </div>
        )}
      </main>
    </div>
  );
}