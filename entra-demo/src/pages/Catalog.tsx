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

  // 1. Obtención silenciosa del token JWT de Entra ID
  const getAccessToken = async (): Promise<string | null> => {
    if (accounts.length === 0) return null;
    try {
      const response = await instance.acquireTokenSilent({
        ...tokenRequest,
        account: accounts[0]
      });
      return response.accessToken;
    } catch (e) {
      console.error('Error al obtener token silencioso:', e);
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

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '40px' }}>
        <h3>Cargando catálogo Pokédex desde AWS...</h3>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '40px', color: '#c62828' }}>
        <h3>Error al consultar el catálogo</h3>
        <p style={{ background: '#ffebee', padding: '10px', borderRadius: '6px', display: 'inline-block' }}>
          {error}
        </p>
        <br />
        <button
          onClick={loadProducts}
          style={{
            marginTop: '15px',
            padding: '8px 16px',
            backgroundColor: '#2a75bb',
            color: '#fff',
            border: 'none',
            borderRadius: '6px',
            cursor: 'pointer',
            fontWeight: 'bold'
          }}
        >
          Reintentar Carga
        </button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2 style={{ color: '#2a75bb', margin: 0 }}>🛒 Catálogo de Cartas Pokémon</h2>
        <button
          onClick={loadProducts}
          style={{
            padding: '8px 14px',
            backgroundColor: '#ffffff',
            border: '1px solid #2a75bb',
            color: '#2a75bb',
            borderRadius: '6px',
            cursor: 'pointer',
            fontWeight: 'bold'
          }}
        >
          🔄 Actualizar
        </button>
      </div>

      {products.length === 0 ? (
        <p>No hay cartas Pokémon registradas en la base de datos.</p>
      ) : (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(230px, 1fr))',
            gap: '20px',
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
  );
};