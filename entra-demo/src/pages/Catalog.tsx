import React, { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';
import { PokemonCard, type PokemonProduct } from '../components/PokemonCard';
import { tokenRequest } from '../authConfig';

export const Catalog: React.FC = () => {
  const { instance, accounts } = useMsal();
  const [products, setProducts] = useState<PokemonProduct[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [orderingId, setOrderingId] = useState<number | null>(null);

  const BFF_URL = import.meta.env.VITE_BFF_BASE_URL || 'http://3.83.25.148:8081';

  // 1. Obtención del token JWT de Entra ID
  const getAccessToken = async (): Promise<string | null> => {
    if (accounts.length === 0) return null;
    try {
      const response = await instance.acquireTokenSilent({
        ...tokenRequest,
        account: accounts[0]
      });
      return response.accessToken;
    } catch (e) {
      console.error('Error al obtener token:', e);
      return null;
    }
  };

  // 2. Consulta de productos al BFF
  const loadProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAccessToken();
      const response = await fetch(`${BFF_URL}/api/catalog/products`, {
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      setProducts(data);
    } catch (err: any) {
      setError(err.message || 'Error al conectar con el servidor BFF');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, [accounts]);

  // 3. Crear pedido al hacer clic en "Pedir Carta"
  const handleCreateOrder = async (product: PokemonProduct) => {
    setOrderingId(product.id);
    try {
      const token = await getAccessToken();
      const response = await fetch(`${BFF_URL}/api/orders`, {
        method: 'POST',
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          productoId: product.id,
          cantidad: 1,
          precioUnitario: product.precio
        })
      });

      if (!response.ok) {
        throw new Error(`No se pudo procesar el pedido (Estado ${response.status})`);
      }

      alert(`¡Pedido registrado con éxito para la carta "${product.nombre}"!`);

      // Descontar stock localmente para actualización visual inmediata
      setProducts(prev =>
        prev.map(p => (p.id === product.id ? { ...p, stock: p.stock - 1 } : p))
      );
    } catch (err: any) {
      alert(`Error al generar pedido: ${err.message}`);
    } finally {
      setOrderingId(null);
    }
  };

  // Vista de Carga Estilizada
  if (loading) {
    return (
      <div style={{
        minHeight: 'calc(100vh - 70px)',
        background: 'radial-gradient(circle at 50% -20%, #1e1b4b 0%, #0f172a 60%, #020617 100%)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px',
        color: '#f8fafc'
      }}>
        <div style={{ fontSize: '3rem', marginBottom: '12px' }}></div>
        <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 800, color: '#fbbf24' }}>
          Cargando Pokédex desde AWS...
        </h3>
        <p style={{ color: '#94a3b8', fontSize: '0.85rem', marginTop: '6px' }}>
          Obteniendo cartas disponibles desde el BFF
        </p>
      </div>
    );
  }

  // Vista de Error Estilizada
  if (error) {
    return (
      <div style={{
        minHeight: 'calc(100vh - 70px)',
        background: 'radial-gradient(circle at 50% -20%, #1e1b4b 0%, #0f172a 60%, #020617 100%)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px'
      }}>
        <div style={{
          backgroundColor: 'rgba(239, 68, 68, 0.08)',
          border: '1px solid rgba(239, 68, 68, 0.25)',
          padding: '32px',
          borderRadius: '20px',
          textAlign: 'center',
          maxWidth: '450px',
          backdropFilter: 'blur(10px)'
        }}>
          <span style={{ fontSize: '2.5rem' }}></span>
          <h3 style={{ color: '#f87171', margin: '12px 0 8px 0', fontWeight: 800 }}>
            Error al consultar el catálogo
          </h3>
          <p style={{ color: '#fca5a5', fontSize: '0.88rem', margin: '0 0 20px 0' }}>
            {error}
          </p>
          <button
            onClick={loadProducts}
            style={{
              padding: '10px 20px',
              backgroundColor: '#ef4444',
              color: '#ffffff',
              border: 'none',
              borderRadius: '10px',
              cursor: 'pointer',
              fontWeight: '800',
              fontSize: '0.9rem',
              boxShadow: '0 4px 12px rgba(239, 68, 68, 0.3)'
            }}
          >
            Reintentar Carga
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={{
      minHeight: 'calc(100vh - 70px)',
      background: 'radial-gradient(circle at 50% -20%, #1e1b4b 0%, #0f172a 60%, #020617 100%)',
      padding: '32px 20px 60px 20px',
      boxSizing: 'border-box'
    }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        
        {/* Header del Catálogo */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '32px',
          flexWrap: 'wrap',
          gap: '16px'
        }}>
          <div>
            <h2 style={{
              margin: 0,
              fontSize: '1.8rem',
              fontWeight: 900,
              color: '#f8fafc',
              display: 'flex',
              alignItems: 'center',
              gap: '10px'
            }}>
            <span></span> Catálogo de Cartas Pokémon
            </h2>
            <p style={{ margin: '4px 0 0 0', color: '#94a3b8', fontSize: '0.88rem' }}>
              Colección oficial disponible para pedidos en tiempo real
            </p>
          </div>

          <button
            onClick={loadProducts}
            style={{
              padding: '10px 18px',
              backgroundColor: 'rgba(255, 255, 255, 0.06)',
              border: '1px solid rgba(255, 255, 255, 0.15)',
              color: '#fbbf24',
              borderRadius: '12px',
              cursor: 'pointer',
              fontWeight: '700',
              fontSize: '0.88rem',
              backdropFilter: 'blur(10px)',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              transition: 'all 0.2s ease'
            }}
          >
            Actualizar
          </button>
        </div>

        {/* Rejilla de Cartas */}
        {products.length === 0 ? (
          <div style={{
            textAlign: 'center',
            padding: '60px 20px',
            backgroundColor: 'rgba(255, 255, 255, 0.03)',
            borderRadius: '20px',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            color: '#94a3b8'
          }}>
            <p style={{ margin: 0, fontSize: '1rem', fontWeight: 600 }}>
              No hay cartas Pokémon registradas en la base de datos.
            </p>
          </div>
        ) : (
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(230px, 1fr))',
              gap: '24px',
              justifyItems: 'center'
            }}
          >
            {products.map((product) => (
              <PokemonCard
                key={product.id}
                product={product}
                onOrder={handleCreateOrder}
                isOrdering={orderingId === product.id}
              />
            ))}
          </div>
        )}

      </div>
    </div>
  );
};